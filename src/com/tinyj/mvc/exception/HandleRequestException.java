package com.tinyj.mvc.exception;

import com.tinyj.mvc.exception.CodedException;

public class HandleRequestException extends CodedException
{

	public HandleRequestException(String msg, int errorCode)
	{
		super(msg, errorCode);
		// TODO Auto-generated constructor stub
	}

}
