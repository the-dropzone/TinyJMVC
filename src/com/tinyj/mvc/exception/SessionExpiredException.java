package com.tinyj.mvc.exception;

public class SessionExpiredException extends HandleRequestException
{

	public SessionExpiredException(String aMsg, int aErrorCode)
	{
		super(aMsg, aErrorCode);
	}

}
