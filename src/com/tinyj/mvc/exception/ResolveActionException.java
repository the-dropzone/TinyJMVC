package com.tinyj.mvc.exception;

public class ResolveActionException extends HandleRequestException
{

	public ResolveActionException(String msg, int errorCode)
	{
		super(msg, errorCode);
	}

}
