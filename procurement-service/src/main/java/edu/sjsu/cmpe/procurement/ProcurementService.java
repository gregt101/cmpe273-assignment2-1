package edu.sjsu.cmpe.procurement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import de.spinscale.dropwizard.jobs.JobsBundle;
import edu.sjsu.cmpe.procurement.api.resources.RootResource;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;
import edu.sjsu.cmpe.procurement.domain.Consumerq;
import edu.sjsu.cmpe.procurement.domain.Publishert;

public class ProcurementService extends Service<ProcurementServiceConfiguration> 
{
    //private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception 
    {
	new ProcurementService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProcurementServiceConfiguration> bootstrap)
    {
	bootstrap.setName("procurement-service");
	bootstrap.addBundle(new JobsBundle("edu.sjsu.cmpe.procurement"));
    }

    @Override
    public void run(ProcurementServiceConfiguration configuration,
	    Environment environment) throws Exception 
	{
	final Client jerseyClient = new JerseyClientBuilder()
				   .using(configuration.getJerseyClientConfiguration())
				   .using(environment)
				   .build();

	/**
	 * Root API - Without RootResource, Dropwizard will throw this
	 * exception:
	 * 
	 * ERROR [2013-10-31 23:01:24,489]
	 * com.sun.jersey.server.impl.application.RootResourceUriRules: The
	 * ResourceConfig instance does not contain any root resource classes.
	 */
	environment.addResource(new RootResource());

	Consumerq Qconsumer = new Consumerq(configuration);
    Qconsumer.initQueue();
	
    Publishert Tpublisher = new Publishert(configuration,jerseyClient);
    Tpublisher.initTopic();

    }
}
