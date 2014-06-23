package com.tinyj.mvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinyj.mvc.exception.HandleRequestException;

public interface IAjaxController
{
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
		throws HandleRequestException;
	
}
