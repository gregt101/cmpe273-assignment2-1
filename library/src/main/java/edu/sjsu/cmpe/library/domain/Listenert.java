package edu.sjsu.cmpe.library.domain;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

public class Listenert implements Runnable
{

	private String userName; 
	private String passWord; 
	private String hostNum; 
	private Integer portNum;
	private String topicName;

	private StompJmsConnectionFactory factory;
	private Connection connection;
	private Session session;
	private Destination dest;
	private MessageConsumer consumer;
	
	private static BookRepositoryInterface bookRepository;
	
	public Listenert(LibraryServiceConfiguration configuration,BookRepositoryInterface bookRepo)
	{
		userName = configuration.getApolloUser();
		passWord = configuration.getApolloPassword();
		hostNum = configuration.getApolloHost();
		portNum = configuration.getApolloPort();
		topicName = configuration.getStompTopicName();
		bookRepository = bookRepo;
	}

	public void initTopic()
	{
		factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + hostNum + ":" + portNum);
		try
		{
		    connection = factory.createConnection(userName, passWord);
		    connection.start();
		    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);	
		    dest = new StompJmsDestination(topicName);
		    consumer = session.createConsumer(dest);
		}
		catch(JMSException e)
		{
			System.out.println("Jms exe");
		}
	}
	
	/* 1. For each lost book, set its status back to ‘available’
	 * 2. For any new book that doesn’t exist in the hashmap, then create a new entry to the map.
	 * 3. Otherwise, ignore the message.
	 */	
	public void processMessage(String body)
	{		
		ConcurrentHashMap<Long, Book> hashMap= bookRepository.getHashMap();
		//Parsing the input String 
		body.split(":");
		String[] parts= body.substring(0, body.length() - 1).split("\"?:\"?");
		if(parts[0] == null)
		{
			System.out.println("parts[0] is empty");
			return;
		}
		long isbn = Long.parseLong(parts[0]);
		if(hashMap.containsKey(isbn))
		{
			Book book = hashMap.get(isbn);
			if(book.getStatus() == Book.Status.lost)
				book.setStatus(Book.Status.available);
		}
		else
		{
			Book book = new Book();
			book.setIsbn(Long.parseLong(parts[0]));
			book.setTitle(parts[1]);
			book.setCategory(parts[2]);
			String url = parts[3]+":"+parts[4];
			try 
			{
			    book.setCoverimage(new URL(url));
			}
			catch (MalformedURLException e)
			{
			    e.printStackTrace();
			}
			hashMap.putIfAbsent(book.getIsbn(), book);
		}
	}
	
	/*  The Topic Listener is running in a background thread so 
	 *  main threads can handle REST API calls.
	 */
	@Override
	public void run() 
	{
		try
		{
			while(true) 
			{
				Thread.sleep(6000);
				System.out.println("Waiting for msg from topic :: " + topicName);
				Message msg = consumer.receive();
				StompJmsMessage smsg = ((StompJmsMessage) msg);
				String body = smsg.getFrame().contentAsString();
				System.out.println("Received message from topic :: " + topicName);
				System.out.println("Message ::" + body);
				processMessage(body);
			}
		}
		catch(Exception exception)
		{
			System.out.println("Error JMSException " + exception);
		}
	}
}
