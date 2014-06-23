package com.tinyj.mvc.exception;

import com.tinyj.mvc.exception.CodedException;

public class AjaxInitializationException extends CodedException
{

	public AjaxInitializationException(String aMsg, int aErrorCode)
	{
		super(aMsg, aErrorCode);
	}

}
