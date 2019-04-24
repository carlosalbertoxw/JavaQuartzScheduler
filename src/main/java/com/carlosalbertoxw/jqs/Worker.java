/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carlosalbertoxw.jqs;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Carlos
 */
public class Worker implements Job {

    private static final Logger LOG = Logger.getLogger(Worker.class);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        LOG.trace("Trace Message!");
        LOG.debug("Debug Message!");
        LOG.info("Info Message!");
        LOG.warn("Warn Message!");
        LOG.error("Error Message!");
    }

}
