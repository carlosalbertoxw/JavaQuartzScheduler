package com.carlosalbertoxw.jqs.services;

/**
 * Contrato del trabajo de negocio que ejecuta una rutina programada. Separar la logica
 * del {@link org.quartz.Job} permite probarla de forma aislada.
 *
 * <p>Durante el apagado de la aplicacion se interrumpe el hilo que ejecuta la rutina: un
 * trabajo largo debe revisar {@link Thread#isInterrupted()} o dejar propagar la
 * {@link InterruptedException} para terminar a tiempo.
 */
@FunctionalInterface
public interface RoutineService {

    void run() throws Exception;
}
