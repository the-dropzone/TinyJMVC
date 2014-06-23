package com.tinyj.mvc.exception;


public class ParameterTypeConversionException extends JavaBeanPopulationException
{

	public ParameterTypeConversionException(String msg, int errorCode)
	{
		super(msg, errorCode);
	}
}
