package edu.sjsu.cmpe.library.domain;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;

public class Producerq
{
	private String queueName; 
	private String userName; 
	private String passWord; 
	private String hostNum; 
	private Integer portNum;
	private Connection connection;
	private Session session;
	private MessageProducer producer;

    public Producerq(LibraryServiceConfiguration configuration)
    {
	    queueName = configuration.getStompQueueName();
	    userName = configuration.getApolloUser();
	    passWord = configuration.getApolloPassword();
	    hostNum = configuration.getApolloHost();
	    portNum = configuration.getApolloPort();
    }	
    	
	public void initialise()
	{
		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + hostNum + ":" + portNum);
		try
		{
			connection = factory.createConnection(userName, passWord);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination dest = new StompJmsDestination(queueName);
			producer = session.createProducer(dest);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}
		catch(JMSException e)
		{
			System.out.println("Jms exeception");
		}
	}
	public void sendDataByQueue(String data)
	{
		try
		{
			TextMessage msg = session.createTextMessage(data);
			msg.setLongProperty("id", System.currentTimeMillis());
			System.out.println("Sending msg :"+ msg.getText());
			producer.send(msg);
		}
		catch(JMSException exception)
		{
			System.out.println("Error JMSException " + exception);
		}
	}

	public void close()
	{
		try
		{
			connection.close();
		}
		catch(JMSException exception)
		{
			System.out.println("Error JMSException " + exception);
		}
	}
}
