package edu.sjsu.cmpe.procurement.domain;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;

@Every("5min")
public class Consumerq extends Job 
{
	private static String userName;
	private static String passWord;
	private static String hostNum;
	private static Integer portNum;
	private static String queueName;
	private static StompJmsConnectionFactory factory;
	private static Connection connection;
	private static MessageConsumer consumer;
	private static Destination dest;
	private static Session session;
	private static Client client;
	
	public Consumerq()
	{
		//no arg constructor
	}

	public Consumerq(ProcurementServiceConfiguration configuration) 
    {
    	userName = configuration.getApolloUser();
		passWord = configuration.getApolloPassword();
		hostNum = configuration.getApolloHost();
		portNum = configuration.getApolloPort();
		queueName = configuration.getStompQueueName();
		client = Client.create();
    }

	public void initQueue()
	{
		try
		{
		factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + hostNum + ":" + portNum);
	    connection = factory.createConnection(userName, passWord);
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		dest = new StompJmsDestination(queueName);
		consumer = session.createConsumer(dest);
		}
		catch(JMSException e)
		{
			e.printStackTrace();
		}
	}
	public void submitBookOrder(OrderBook book)
	{
		//Post response to Publisher
		try
		{			
			WebResource webResource = client.resource("http://54.219.156.168:9000/orders");
			String response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
										.post(String.class,book);
			System.out.println(response);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
	
	@Override
	public void doJob() 
	{
		try 
		{
			System.out.println("Waiting for messages from " + queue + "...");
			String body = "";
			Message msg;
			OrderBook book = new OrderBook();
			boolean isBookLost = false;
			while((msg = consumer.receive(1000*30)) !=null)
			{
				if(msg instanceof StompJmsMessage)
				{
					StompJmsMessage smsg = ((StompJmsMessage) msg);
					body = smsg.getFrame().contentAsString();
					System.out.println("Consumerq received message = " + body);
					String[] parts=body.split(":");
					System.out.println("ISBN Number to be sent ::"+parts[1]);
					book.setOrder_book_isbns(Integer.parseInt(parts[1]));
					isBookLost = true;
				}
			}
			if(isBookLost)
			submitBookOrder(book);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} 
	}
}
