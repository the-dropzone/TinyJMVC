package com.tinyj.mvc.model;

import com.tinyj.mvc.controller.MVCContext;
import com.tinyj.mvc.exception.ActionExecutionException;
import com.tinyj.mvc.exception.SessionExpiredException;


/**
 * A abstract class defining the base structure of an MVC action.
 * the action should always be constructed with the action name and the current request's
 * MVCContext object (which contains the original request and response).
 * 
 * users should extend this class to create a proprietary Action class, and implement the
 * <i>execute()</i> method and a call to the base contructor defined in this class. 
 *  
 * @author asaf.peeri
 *
 */
public abstract class Action
{
	private String mActionPath;
	private MVCContext mMVCContext;
	
	public Action(String aActionPath, MVCContext aMVCContext)
	{
		mActionPath = aActionPath;
		mMVCContext = aMVCContext;
	}
	
	
	public String getActionPath()
	{
		return mActionPath;
	}
	
	
	public MVCContext getMVCContext()
	{
		return mMVCContext;
	}
	
	
	public abstract ActionResponse execute() throws ActionExecutionException, SessionExpiredException;
}
