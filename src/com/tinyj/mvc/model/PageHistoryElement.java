package com.tinyj.mvc.model;

public class PageHistoryElement
{
	private String mPageURI;
	private boolean mRedirect;
	
	public PageHistoryElement(String aPageURI, boolean aRedirect)
	{
		mPageURI = aPageURI;
		mRedirect = aRedirect;
	}
	
	
	public String getPageURI()
	{
		return mPageURI;
	}
	
	
	public boolean isRedirect()
	{
		return mRedirect;
	}
}
