package com.tinyj.mvc.exception;

public class ActionExecutionException extends HandleRequestException
{

	public ActionExecutionException(String msg, int errorCode)
	{
		super(msg, errorCode);
	}

}
