	package edu.sjsu.cmpe.procurement.domain;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;

@Every("5min")
public class Publishert extends Job 
{
	
	private static String userName;
	private static String passWord;
	private static String hostNum;
	private static Integer portNum;
	private static StompJmsConnectionFactory factory;
	
	private static Connection[] connection;
	private static Destination[] dest;
	private static Session[] session;
	private static MessageProducer[] producer;
	private static String[] topicName;
	
	private static Client client;
	
	public Publishert()
	{
		//no arg constructor
	}
	
	public Publishert(ProcurementServiceConfiguration configuration,Client jerseyClient)
	{
		userName = configuration.getApolloUser();
		passWord = configuration.getApolloPassword();
		hostNum = configuration.getApolloHost();
		portNum = configuration.getApolloPort();
		
		topicName = new String[2];
		connection = new Connection[2];
		session = new Session[2];
		dest = new Destination[2];
		producer = new MessageProducer[2];
		
		topicName[0] = "/topic/26759.book.all";
		topicName[1] = "/topic/26759.book.computer";
		client = jerseyClient; 
	}
	
	public void initTopic()
	{
		try
		{	
			factory = new StompJmsConnectionFactory();
			factory.setBrokerURI("tcp://" + hostNum + ":" + portNum);
			for (int i = 0; i < topicName.length; i++) 
			{
				connection[i] = factory.createConnection(userName, passWord);	
				connection[i].start();	
				session[i] = connection[i].createSession(false, Session.AUTO_ACKNOWLEDGE);
				dest[i] = new StompJmsDestination(topicName[i]);
				producer[i] = session[i].createProducer(dest[i]);
				producer[i].setDeliveryMode(DeliveryMode.PERSISTENT);
			}		
		}
		catch(JMSException exception)
		{
			System.out.println(exception);
		}
	}
	
	@Override
	public void doJob()  
	{
		int libA = 0 ;
		int libB = 1;
		//GET Resource from Publisher
		try
		{
			WebResource webResource = client.resource("http://54.219.156.168:9000/orders/26759");
			Shipment sb  = webResource.accept(MediaType.APPLICATION_JSON).get(Shipment.class);
			
			String data = "";		
			for (int i = 0; i < sb.getShipment().size(); i++) 
			{
				
				data = sb.getShipment().get(i).getIsbn() +
				 		":\"" + sb.getShipment().get(i).getTitle() + "\"" +
				 		":\"" + sb.getShipment().get(i).getCategory() + "\"" +
				 		":\"" + sb.getShipment().get(i).getCoverimage() +"\"" ;		
				
				
				TextMessage msg = session[libA].createTextMessage(data);
				msg.setLongProperty("id", System.currentTimeMillis());
				System.out.println("Sending to " + topicName[libA] + " "+ 
											sb.getShipment().get(i).getCategory());
				producer[libA].send(msg);
				if(sb.getShipment().get(i).getCategory().compareTo("computer") == 0)
				{
					msg = session[libB].createTextMessage(data);
					msg.setLongProperty("id", System.currentTimeMillis());
					System.out.println("Sending to " + topicName[libB] + " " +
											sb.getShipment().get(i).getCategory());
					producer[libB].send(msg);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception" +e);
		}
	}
		
}


