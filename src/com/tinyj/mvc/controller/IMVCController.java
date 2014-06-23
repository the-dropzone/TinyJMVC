package com.tinyj.mvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinyj.mvc.exception.HandleRequestException;

/**
 * an interface defining how an MVCController should look
 * 
 * @author asaf.peeri
 *
 */
public interface IMVCController
{
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
		throws HandleRequestException;
}
