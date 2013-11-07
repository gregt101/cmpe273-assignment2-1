package edu.sjsu.cmpe.procurement.domain;


public class Book 
{
    private long isbn;
    private String title;
    private String category;
    private String coverimage;

    public long getIsbn() 
    {
	return isbn;
    }

    public void setIsbn(long isbn) 
    {
	this.isbn = isbn;
    }

    public String getTitle() 
    {
	return title;
    }

    public void setTitle(String title) 
    {
	this.title = title;
    }
    
    public String getCategory() 
    {
	return category;
    }

    public void setCategory(String category) 
    {
	this.category = category;
    }    
    
    public String getCoverimage() 
    {
	return coverimage;
    }

    public void setCoverimage(String coverImage) 
    {
	this.coverimage = coverImage;
    }    
}
