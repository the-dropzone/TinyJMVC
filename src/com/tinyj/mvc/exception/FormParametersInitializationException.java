package com.tinyj.mvc.exception;

public class FormParametersInitializationException extends MVCContextInstantiationException
{

	public FormParametersInitializationException(String msg, int errorCode)
	{
		super(msg, errorCode);
	}

}
