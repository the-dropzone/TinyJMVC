package com.tinyj.mvc.exception;

public class HandleNavigationException extends HandleRequestException
{
	public HandleNavigationException(String msg, int errorCode)
	{
		super(msg, errorCode);
	}

}
