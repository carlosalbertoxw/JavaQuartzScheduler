# JavaQuartzScheduler

[![build](https://github.com/carlosalbertoxw/JavaQuartzScheduler/actions/workflows/build.yml/badge.svg)](https://github.com/carlosalbertoxw/JavaQuartzScheduler/actions/workflows/build.yml)

Ejemplo de automatización de tareas (rutinas) con [Quartz Scheduler](https://www.quartz-scheduler.org/)
sobre **Java 21**, con configuración externa en YAML, validación al arrancar y logging con SLF4J + Logback.
Está pensado como plantilla base para automatizar trabajos periódicos en Java.

Proyecto hermano en .NET: [CSharpQuartzScheduler](https://github.com/carlosalbertoxw/CSharpQuartzScheduler).
Ambos tienen la misma estructura, las mismas rutinas de ejemplo y el mismo comportamiento.

## Características

- **Varias rutinas desde configuración**: cada entrada de `routines` en `routines.yml` crea un job y un trigger.
- **Validación al arrancar**: cron, zona horaria, servicio registrado, identidad única y sección `reports`; un
  error de configuración detiene la aplicación con todos los errores juntos y código de salida `1`.
- **Job desacoplado de la lógica de negocio**: `RoutineJob` resuelve por clave el `RoutineService` a ejecutar,
  lo que permite probar cada rutina de forma aislada.
- **Tres rutinas de ejemplo**: una mínima y dos de reportes que muestran cómo una rutina recibe su propia
  configuración y dependencias.
- `@DisallowConcurrentExecution` para evitar que una ejecución de una misma rutina se solape con la siguiente.
- Los disparos perdidos (aplicación detenida) se ignoran: se espera al siguiente disparo programado.
- **Apagado ordenado**: con `Ctrl+C` o al detener el servicio deja de disparar rutinas, interrumpe las que
  están en curso y espera a que terminen.
- **Logs** en consola y en archivo con rotación diaria, en `logs/` junto al jar (también al correr como servicio).
- Se puede ejecutar como servicio de Windows (WinSW) o de Linux (systemd).
- Pruebas unitarias con JUnit 6 + Mockito y CI en GitHub Actions.

## Estructura

```
src/main/java/com/carlosalbertoxw/jqs/
├── Main.java                         # Carga y valida la configuración y programa las rutinas
├── AppDirectory.java                 # Carpeta del jar (para logs/, reports/ y routines.yml)
├── LogDirPropertyDefiner.java        # Carpeta de logs para logback.xml
├── config/
│   ├── AppConfig.java                # Raíz de routines.yml (rutinas + reports)
│   ├── RoutineConfig.java            # Configuración de una rutina
│   ├── ReportsConfig.java            # Configuración de las rutinas de reportes (sección "reports")
│   ├── RoutinesLoader.java           # Lectura de routines.yml
│   └── RoutinesValidator.java        # Validación de cron, zona horaria, servicio, etc.
├── jobs/
│   ├── RoutineJob.java               # Job de Quartz: resuelve el servicio y maneja errores/interrupción
│   └── RoutineJobFactory.java        # Crea los RoutineJob con el registro de servicios
└── services/
    ├── RoutineService.java           # Contrato de la lógica de negocio
    ├── RoutineServices.java          # Registro de servicios por clave
    ├── ExampleRoutineService.java    # Rutina mínima de ejemplo ("Example")
    ├── DailyReportService.java       # Genera un reporte de texto ("DailyReport")
    └── ReportCleanupService.java     # Borra reportes antiguos ("ReportCleanup")
src/main/resources/
├── routines.yml                      # Rutinas y configuración de reportes por defecto
├── quartz.properties                 # Configuración del scheduler
└── logback.xml                       # Configuración de logging
src/test/                             # Pruebas unitarias (JUnit 6 + Mockito) y logback-test.xml
deploy/
├── windows/JavaQuartzScheduler.xml   # Servicio de Windows con WinSW
└── linux/javaquartzscheduler.service # Unidad de systemd
.github/workflows/build.yml           # CI: compila y ejecuta las pruebas
mvnw, mvnw.cmd, .mvn/                 # Maven Wrapper
```

## Requisitos

- Java 21 o superior
- Maven no es necesario: el proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`), que descarga Maven 3.9.11
  la primera vez.

## Compilación y pruebas

```bash
./mvnw clean package
```

En Windows usa `mvnw.cmd` en lugar de `./mvnw`. Se genera el jar ejecutable (con todas las dependencias
incluidas) en `target/JavaQuartzScheduler-3.0-shaded.jar`.

Solo las pruebas:

```bash
./mvnw test
```

| Clase de prueba          | Qué cubre                                                                   |
|--------------------------|-----------------------------------------------------------------------------|
| `MainTest`               | Job y trigger de una rutina; el `routines.yml` incluido es válido.          |
| `RoutinesLoaderTest`     | Lectura del YAML, valores por defecto y claves desconocidas.                |
| `RoutinesValidatorTest`  | Cron, zona horaria, servicio, identidad duplicada y campos obligatorios.    |
| `RoutineJobTest`         | Ejecución del servicio, manejo de errores e interrupción por apagado.       |
| `RoutineServicesTest`    | Registro de servicios por clave.                                            |
| `ReportServicesTest`     | Generación de reportes y limpieza de los vencidos (con `Clock` fijo).       |

## Ejecución

```bash
java -jar target/JavaQuartzScheduler-3.0-shaded.jar
```

La configuración se lee del primer archivo que se encuentre:

1. La ruta indicada como primer argumento: `java -jar JavaQuartzScheduler-3.0-shaded.jar C:\config\routines.yml`
2. `routines.yml` junto al jar.
3. El `routines.yml` incluido en el jar (las rutinas descritas abajo).

Para detener la aplicación usa `Ctrl+C`: espera a que las rutinas en curso terminen antes de salir.

Si la configuración tiene errores, la aplicación no arranca y los muestra todos juntos:

```
ERROR c.c.j.Main - Configuracion de rutinas invalida:
  - routines[0] (A): no hay un RoutineService registrado con la clave 'NoExiste'.
  - routines[0] (A): la expresion cron 'esto-no-es-cron' no es valida.
  - routines[0] (A): la zona horaria 'Marte/Olympus' no existe.
  - routines[1] (A): ya existe otra rutina con el mismo group/name.
```

## Rutinas incluidas

Son las mismas que en CSharpQuartzScheduler:

| Rutina          | Servicio (`service`)                      | Cuándo corre                                      | Qué hace                                                         |
|-----------------|-------------------------------------------|---------------------------------------------------|------------------------------------------------------------------|
| `RoutineJob`    | `ExampleRoutineService` (`Example`)       | Cada minuto (UTC)                                 | Ejemplo mínimo: registra inicio y fin.                           |
| `DailyReport`   | `DailyReportService` (`DailyReport`)      | Lunes a viernes a las 7:00, `America/Mexico_City` | Escribe `reports/report-<fecha>.txt` con un resumen del equipo.  |
| `ReportCleanup` | `ReportCleanupService` (`ReportCleanup`)  | Todos los días a las 3:30 (UTC)                   | Borra los `report-*.txt` con más de `retentionDays` días.        |

Las dos rutinas de reportes muestran cómo un servicio recibe su propia configuración (`ReportsConfig`) y otras
dependencias (`java.time.Clock`, que permite fijar la hora en las pruebas). La fecha del nombre del reporte
está en UTC (`report-yyyyMMdd-HHmmss.txt`), y la limpieza solo borra archivos con el prefijo `report-`.

Ejemplo de reporte:

```
Reporte generado: 2026-09-22T13:00:00.0040593Z
Equipo:           MI-EQUIPO
Sistema:          Windows 10 10.0
Procesadores:     4
Memoria en uso:   5885 KB
```

Para verlas correr sin esperar, copia `src/main/resources/routines.yml` junto al jar y cambia sus crons, por
ejemplo `"0/5 * * ? * *"` para `DailyReport` y `"2/5 * * ? * *"` para `ReportCleanup`.

## Configuración

Ejemplo de `routines.yml`:

```yaml
routines:
  - name: DailyReport
    group: Reports
    service: DailyReport
    cronExpression: "0 0 7 ? * MON-FRI"
    timeZone: America/Mexico_City
    enabled: true

reports:
  directory: reports
  retentionDays: 7
```

Cada elemento de `routines` es una rutina:

| Clave            | Descripción                                                            |
|------------------|------------------------------------------------------------------------|
| `name`           | Nombre lógico del job (único dentro de su grupo). Obligatorio.         |
| `group`          | Grupo de Quartz para job y trigger. Por defecto `Routines`.            |
| `service`        | Clave del `RoutineService` registrado en `Main.registerServices()`.    |
| `cronExpression` | Cron de Quartz (6-7 campos). `0 * * ? * *` = cada minuto.              |
| `timeZone`       | Zona horaria IANA (p. ej. `America/Mexico_City`). Vacío = UTC.         |
| `enabled`        | `false` para no programar la rutina. Por defecto `true`.               |

La sección `reports` configura las rutinas de reportes:

| Clave           | Descripción                                                     |
|-----------------|-----------------------------------------------------------------|
| `directory`     | Carpeta de reportes. Relativa = junto al jar. Por defecto `reports`. |
| `retentionDays` | Días que se conservan los reportes antes de borrarlos (1-3650). Por defecto `7`. |

Una clave desconocida (por ejemplo `cron` en lugar de `cronExpression`) se reporta como error al arrancar.

> El cron de Quartz usa el formato `segundo minuto hora díaMes mes díaSemana [año]`, distinto del cron de
> Unix. Prueba expresiones en
> [freeformatter.com/cron-expression-generator-quartz](https://www.freeformatter.com/cron-expression-generator-quartz.html).

El scheduler (nombre, número de hilos y umbral de *misfire*) se configura en `src/main/resources/quartz.properties`.

## Agregar una rutina

1. Crea una clase que implemente `RoutineService` con tu lógica en `run()` y una constante `KEY`:
   ```java
   public class MiRutinaService implements RoutineService {

       public static final String KEY = "MiRutina";

       @Override
       public void run() throws Exception {
           // Tu lógica: llamadas a API, consultas a BD, etc.
       }
   }
   ```
   Si el trabajo es largo, revisa `Thread.currentThread().isInterrupted()` o deja propagar la
   `InterruptedException` para respetar el apagado (ver `ReportCleanupService`). Si lanza una excepción, se
   registra en el log y la rutina se reintenta en su siguiente disparo.
2. Regístrala con su clave en `Main.registerServices()`. Se crea una instancia nueva en cada ejecución; si
   necesita dependencias, pásalas en la lambda, como las rutinas de reportes:
   ```java
   return new RoutineServices()
           .register(ExampleRoutineService.KEY, ExampleRoutineService::new)
           .register(DailyReportService.KEY, () -> new DailyReportService(reports, clock))
           .register(ReportCleanupService.KEY, () -> new ReportCleanupService(reports, clock))
           .register(MiRutinaService.KEY, MiRutinaService::new);
   ```
3. Agrega una entrada en `routines.yml`:
   ```yaml
   - name: MiRutina
     group: Routines
     service: MiRutina
     cronExpression: "0 0 7 ? * MON-FRI"
     timeZone: America/Mexico_City
   ```
4. Agrega sus pruebas en `src/test/java` (ver `ReportServicesTest` como guía).

## Logging

El logging usa **SLF4J + Logback** (`src/main/resources/logback.xml`). Los logs se escriben en consola y en
`logs/application.log` junto al jar, con rotación diaria y por tamaño (5 MB por archivo, 14 días, 100 MB
máximo). Puedes cambiar la carpeta con la propiedad del sistema `log.dir`:

```bash
java -Dlog.dir=/var/log/jqs -jar target/JavaQuartzScheduler-3.0-shaded.jar
```

Al ejecutar desde el IDE o con Maven (sin jar), `logs/` y `reports/` se crean en el directorio de trabajo; ambos
están excluidos del repositorio en `.gitignore`.

## Instalar como servicio

La aplicación crea `logs/` y `reports/` junto al jar, así que el usuario del servicio necesita permiso de
escritura en esa carpeta. Para cambiar las rutinas sin recompilar, copia también tu `routines.yml` junto al jar.

### Windows (WinSW)

1. Copia a una carpeta (p. ej. `C:\Scheduler`) el jar, [`deploy/windows/JavaQuartzScheduler.xml`](deploy/windows/JavaQuartzScheduler.xml)
   y el ejecutable de [WinSW](https://github.com/winsw/winsw/releases) renombrado como `JavaQuartzScheduler.exe`.
2. Instala e inicia el servicio:
   ```powershell
   C:\Scheduler\JavaQuartzScheduler.exe install
   C:\Scheduler\JavaQuartzScheduler.exe start
   ```

Al detener el servicio, WinSW envía `Ctrl+C` y espera hasta 30 segundos a que terminen las rutinas en curso.

### Linux (systemd)

1. Crea el usuario `jqs` y copia el jar a `/opt/javaquartzscheduler`, con `jqs` como dueño de la carpeta.
2. Copia [`deploy/linux/javaquartzscheduler.service`](deploy/linux/javaquartzscheduler.service) a `/etc/systemd/system/`.
3. Habilita e inicia el servicio:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable --now javaquartzscheduler
   ```

## Importar en Eclipse

Es un proyecto Maven estándar, así que se importa directamente con el plugin m2e (incluido en Eclipse):

1. *File → Import… → Maven → Existing Maven Projects*.
2. Selecciona la carpeta raíz del proyecto (la que contiene `pom.xml`).
3. Finaliza. Eclipse descargará las dependencias y configurará el classpath solo.

Para ejecutar, clic derecho sobre `Main.java` → *Run As → Java Application*. Para las pruebas: clic derecho
sobre el proyecto → *Run As → JUnit Test*. Los archivos específicos de Eclipse (`.project`, `.classpath`,
`.settings/`) están excluidos del repositorio mediante `.gitignore`.

## Historial de versiones

### 3.0

- Varias rutinas configurables en `routines.yml`, con zona horaria por rutina y validación al arrancar.
- **Cambio incompatible:** el primer argumento ya no es una expresión cron sino la ruta del archivo de
  configuración.
- `Worker` se reemplaza por `RoutineJob` + `RoutineService`; nuevas rutinas de ejemplo `DailyReport` y
  `ReportCleanup`, iguales a las de CSharpQuartzScheduler.
- Sin solapamiento de ejecuciones, disparos perdidos ignorados y apagado ordenado con interrupción.
- Corregido: con una expresión cron inválida el proceso quedaba colgado en lugar de terminar.
- Logs junto al jar; ejemplos de servicio para WinSW y systemd.
- Java 21, dependencias actualizadas, Maven Wrapper y CI en GitHub Actions.

### 2.0

- Una sola tarea (`Worker`) con cron por argumento, Java 17, SLF4J + Logback y pruebas con JUnit 5.
