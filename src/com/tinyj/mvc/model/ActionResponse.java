package com.tinyj.mvc.model;


/**
 * defines an action response which currently contains only a response string.
 * this response string will be mapped to a forward name (by action relation or by global
 * forward mapping), by the NavigationHandler
 * 
 * @author asaf.peeri
 *
 */
public class ActionResponse
{
	private Action mExecutedAction;
	private String mResponseString;
	
	
	public ActionResponse(Action aExecutedAction, String aResponseString)
	{
		mExecutedAction = aExecutedAction;
		mResponseString = aResponseString;
	}
	
	
	public String getResponse()
	{
		return mResponseString;
	}
	
	
	public Action getExecutedAction()
	{
		return mExecutedAction;
	}
}
