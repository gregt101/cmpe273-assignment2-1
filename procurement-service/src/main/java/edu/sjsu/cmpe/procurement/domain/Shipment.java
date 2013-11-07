package edu.sjsu.cmpe.procurement.domain;

import java.util.ArrayList;

public class Shipment 
{

	private ArrayList<Book> shipment;
	
	public Shipment()
	{
		shipment = new ArrayList<Book>();
	}
	
	public void setShipment(Book[] book)
	{
		for (int i = 0; i < book.length; i++) 
		{
			System.out.println("BOOK "+ i);
			shipment.add(book[i]);
		}
	}
	
	public ArrayList<Book> getShipment()
	{
		return shipment;
	}
}
