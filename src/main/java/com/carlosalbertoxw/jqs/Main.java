package com.carlosalbertoxw.jqs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carlosalbertoxw.jqs.config.AppConfig;
import com.carlosalbertoxw.jqs.config.ReportsConfig;
import com.carlosalbertoxw.jqs.config.RoutineConfig;
import com.carlosalbertoxw.jqs.config.RoutinesLoader;
import com.carlosalbertoxw.jqs.config.RoutinesValidator;
import com.carlosalbertoxw.jqs.jobs.RoutineJob;
import com.carlosalbertoxw.jqs.jobs.RoutineJobFactory;
import com.carlosalbertoxw.jqs.services.DailyReportService;
import com.carlosalbertoxw.jqs.services.ExampleRoutineService;
import com.carlosalbertoxw.jqs.services.ReportCleanupService;
import com.carlosalbertoxw.jqs.services.RoutineServices;

/**
 * Punto de entrada de la aplicacion. Lee la configuracion de {@code routines.yml}, la valida
 * y programa un job y un trigger por cada rutina habilitada.
 *
 * <p>El archivo de configuracion se busca, en este orden:
 * <ol>
 *   <li>La ruta indicada como primer argumento del programa.</li>
 *   <li>{@code routines.yml} junto al jar ({@link AppDirectory}).</li>
 *   <li>El {@code routines.yml} incluido en el classpath.</li>
 * </ol>
 *
 * @author Carlos
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    static final String CONFIG_FILE = "routines.yml";

    public static void main(String[] args) {
        LOG.info("Iniciando JavaQuartzScheduler...");

        final AppConfig config;
        try {
            config = loadConfig(args);
        } catch (IOException e) {
            LOG.error("No se pudo leer la configuracion de rutinas", e);
            System.exit(1);
            return;
        }

        final RoutineServices services = registerServices(config.getReports(), Clock.systemUTC());
        final List<RoutineConfig> routines = config.getRoutines();

        // Se valida antes de crear el scheduler: un cron o zona horaria invalidos detienen
        // la aplicacion con un mensaje claro en lugar de fallar dentro de Quartz.
        final List<String> errors = validate(config, services);
        if (!errors.isEmpty()) {
            LOG.error("Configuracion de rutinas invalida:{}{}", System.lineSeparator(),
                    "  - " + String.join(System.lineSeparator() + "  - ", errors));
            System.exit(1);
            return;
        }

        try {
            final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            registerShutdownHook(scheduler);
            scheduler.setJobFactory(new RoutineJobFactory(services));

            for (RoutineConfig routine : routines) {
                if (!routine.isEnabled()) {
                    LOG.info("Rutina {} deshabilitada; no se programa.", routine.getName());
                    continue;
                }
                scheduler.scheduleJob(buildJob(routine), buildTrigger(routine));
                LOG.info("Rutina {} programada con cron '{}' ({}).", routine.getName(),
                        routine.getCronExpression(), routine.resolveTimeZone().getID());
            }

            scheduler.start();
            LOG.info("Scheduler iniciado.");
        } catch (SchedulerException e) {
            LOG.error("No se pudo iniciar el scheduler", e);
            System.exit(1);
        }
    }

    /**
     * Servicios de negocio: cada rutina de {@code routines.yml} referencia uno por su clave
     * ({@code service}). Las rutinas de reportes reciben su configuracion y un {@link Clock}.
     */
    static RoutineServices registerServices(ReportsConfig reports, Clock clock) {
        return new RoutineServices()
                .register(ExampleRoutineService.KEY, ExampleRoutineService::new)
                .register(DailyReportService.KEY, () -> new DailyReportService(reports, clock))
                .register(ReportCleanupService.KEY, () -> new ReportCleanupService(reports, clock));
    }

    /** Errores de las rutinas y de la seccion {@code reports}; vacia si todo es valido. */
    static List<String> validate(AppConfig config, RoutineServices services) {
        final List<String> errors = new ArrayList<>(new RoutinesValidator(services).validate(config.getRoutines()));
        errors.addAll(config.getReports().validate());
        return errors;
    }

    /** Carga la configuracion desde el primer origen disponible (ver la documentacion de la clase). */
    static AppConfig loadConfig(String[] args) throws IOException {
        if (args.length > 0) {
            final Path path = Path.of(args[0]);
            LOG.info("Leyendo rutinas de {}", path.toAbsolutePath());
            return RoutinesLoader.load(path);
        }

        final Path external = AppDirectory.get().resolve(CONFIG_FILE);
        if (Files.isRegularFile(external)) {
            LOG.info("Leyendo rutinas de {}", external);
            return RoutinesLoader.load(external);
        }

        try (InputStream in = Main.class.getResourceAsStream("/" + CONFIG_FILE)) {
            if (in == null) {
                throw new FileNotFoundException("No se encontro " + CONFIG_FILE);
            }
            LOG.info("Leyendo rutinas de {} incluido en el classpath", CONFIG_FILE);
            return RoutinesLoader.load(in);
        }
    }

    /** Construye el {@link JobDetail} de una rutina. */
    static JobDetail buildJob(RoutineConfig routine) {
        return JobBuilder.newJob(RoutineJob.class)
                .withIdentity(routine.getName(), routine.getGroup())
                .usingJobData(RoutineJob.SERVICE_KEY, routine.getService())
                .build();
    }

    /**
     * Construye el {@link Trigger} cron de una rutina, en su zona horaria. Si la aplicacion
     * estuvo detenida, los disparos perdidos se ignoran y se espera al siguiente.
     *
     * @param routine rutina ya validada por {@link RoutinesValidator}
     * @return el trigger configurado
     */
    static Trigger buildTrigger(RoutineConfig routine) {
        return TriggerBuilder.newTrigger()
                .withIdentity(routine.getName() + "-trigger", routine.getGroup())
                .forJob(routine.getName(), routine.getGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(routine.getCronExpression())
                        .inTimeZone(routine.resolveTimeZone())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    /**
     * Registra un hook para detener el scheduler de forma ordenada cuando la JVM recibe
     * una senal de terminacion (Ctrl+C, kill, parada del servicio, etc.): deja de disparar
     * rutinas, avisa a las que estan en curso y espera a que terminen.
     */
    private static void registerShutdownHook(Scheduler scheduler) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOG.info("Deteniendo scheduler...");
                scheduler.standby();
                for (JobExecutionContext running : scheduler.getCurrentlyExecutingJobs()) {
                    scheduler.interrupt(running.getFireInstanceId());
                }
                scheduler.shutdown(true);
                LOG.info("Scheduler detenido correctamente.");
            } catch (SchedulerException e) {
                LOG.error("Error al detener el scheduler", e);
            }
        }, "jqs-shutdown"));
    }
}
