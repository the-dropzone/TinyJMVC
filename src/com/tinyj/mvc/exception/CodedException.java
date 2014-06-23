package com.tinyj.mvc.exception;

public class CodedException extends Exception 
{
	public int mErrorCode;
	
	public CodedException(String aMsg, int aErrorCode)
	{
		super(aMsg);
		mErrorCode = aErrorCode;
	}
}
