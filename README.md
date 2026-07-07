# JavaQuartzScheduler

Ejemplo sencillo de automatización de tareas con [Quartz Scheduler](http://www.quartz-scheduler.org/).

La aplicación programa una tarea recurrente (`Worker`) mediante una expresión cron y la
ejecuta mientras el proceso siga vivo. Está pensado como plantilla base para automatizar
trabajos periódicos en Java.

## Requisitos

- Java 17 o superior
- Maven 3.6 o superior

## Importar en Eclipse

Es un proyecto Maven estándar, así que se importa directamente con el plugin m2e
(incluido en Eclipse):

1. *File → Import… → Maven → Existing Maven Projects*.
2. Selecciona la carpeta raíz del proyecto (la que contiene `pom.xml`).
3. Finaliza. Eclipse descargará las dependencias y configurará el classpath solo.

Los archivos específicos de Eclipse (`.project`, `.classpath`, `.settings/`) se generan
localmente y están excluidos del repositorio mediante `.gitignore`.

## Compilación

```
mvn clean package
```

Se genera el jar ejecutable (con todas las dependencias incluidas) en:

```
target/JavaQuartzScheduler-2.0-shaded.jar
```

## Ejecución

```
java -jar target/JavaQuartzScheduler-2.0-shaded.jar
```

Por defecto la tarea se ejecuta **cada minuto**. Puedes pasar tu propia expresión cron
de Quartz como primer argumento:

```
java -jar target/JavaQuartzScheduler-2.0-shaded.jar "0 0/5 * 1/1 * ? *"
```

Para detener la aplicación usa `Ctrl+C`; el scheduler se apaga de forma ordenada.

## Pruebas

Las pruebas unitarias usan **JUnit 5** y **Mockito**:

```
mvn test
```

En Eclipse: clic derecho sobre el proyecto → *Run As → JUnit Test*.

## Logging

El logging usa **SLF4J + Logback** y se configura automáticamente desde el classpath
(`src/main/resources/logback.xml`)

Los logs se escriben en consola y en la carpeta `./logs` (relativa al directorio de
ejecución). Puedes cambiar el destino con la propiedad del sistema `log.dir`:

```
java -Dlog.dir=/var/log/jqs -jar target/JavaQuartzScheduler-2.0-shaded.jar
```

## Expresiones cron

Para generar expresiones cron compatibles con Quartz puedes usar, por ejemplo,
[freeformatter.com](https://www.freeformatter.com/cron-expression-generator-quartz.html).
