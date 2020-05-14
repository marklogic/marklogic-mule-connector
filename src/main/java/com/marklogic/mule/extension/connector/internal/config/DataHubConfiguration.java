package com.marklogic.mule.extension.connector.internal.config;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class DataHubConfiguration {
    @Parameter
    @Summary("The name of the data hub's staging database.")
    @Example("data-hub-STAGING")
    private String stagingDbName;

    @Parameter
    @Summary("The app server port for staging.")
    @Example("8010")
    private int stagingPort;

    @Parameter
    @Summary("The name of the data hub's final database.")
    @Example("data-hub-FINAL")
    private String finalDbName;

    @Parameter
    @Summary("The app server port for final.")
    @Example("8011")
    private int finalPort;

    @Parameter
    @Summary("The name of the data hub's jobs database.")
    @Example("data-hub-JOBS")
    private String jobsDbName;

    @Parameter
    @Summary("The app server port for jobs.")
    @Example("8013")
    private int jobsPort;

    public DataHubConfiguration() {
        // default data hub settings
        stagingDbName = "data-hub-STAGING";
        stagingPort = 8010;
        finalDbName = "data-hub-FINAL";
        finalPort = 8011;
        jobsDbName = "data-hub-JOBS";
        jobsPort = 8013;
    }

    public String getStagingDbName() { return stagingDbName; }

    public int getStagingPort() { return stagingPort; }

    public String getFinalDbName() { return finalDbName; }

    public int getFinalPort() { return finalPort; }

    public String getJobsDbName() { return jobsDbName; }

    public int getJobsPort() { return jobsPort; }

    public void setStagingDbName() { this.stagingDbName = stagingDbName; }

    public void setStagingPort() { this.stagingPort = stagingPort; }

    public void setFinalDbName() { this.finalDbName = finalDbName; }

    public void setFinalPort() { this.finalPort = finalPort; }

    public void setJobsDbName() { this.jobsDbName = jobsDbName; }

    public void setJobsPort() { this.jobsPort = jobsPort; }
}

