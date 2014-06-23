package com.tinyj.mvc.model;


/**
 * a child class of ActionResponse which additionally holds a custom URL to go to once the action completes
 * ignoring the standard rules of action resolving by the response action string.
 * 
 * @author Asaf
 *
 */
public class CustomURLActionResponse extends ActionResponse
{
	private String mCustomURL;
	
	public CustomURLActionResponse(Action aExecutedAction, String aResponseString, String aCustomURL)
	{
		super(aExecutedAction, aResponseString);
		setCustomURL(aCustomURL);
	}

	public String getCustomURL()
	{
		return mCustomURL;
	}

	public void setCustomURL(String aCustomURL)
	{
		mCustomURL = aCustomURL;
	}
}
