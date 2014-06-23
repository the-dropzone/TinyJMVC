package com.tinyj.mvc.exception;

import com.tinyj.mvc.exception.CodedException;

public class MVCInitializationException extends CodedException
{

	public MVCInitializationException(String aMsg, int aErrorCode)
	{
		super(aMsg, aErrorCode);
	}

}
