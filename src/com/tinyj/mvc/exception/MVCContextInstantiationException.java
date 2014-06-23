package com.tinyj.mvc.exception;

public class MVCContextInstantiationException extends HandleRequestException
{

	public MVCContextInstantiationException(String msg, int errorCode)
	{
		super(msg, errorCode);
	}

}
