package com.tinyj.mvc.model;

public class AjaxActionResponse extends ActionResponse 
{
	private String responseContentType;
	
	
	public AjaxActionResponse(Action aExecutedAction, String aResponseString, String aResponseContentType) 
	{
		super(aExecutedAction, aResponseString);
		responseContentType = aResponseContentType;
	}
	
	
	public String getResponseContentType()
	{
		return responseContentType;
	}

}
