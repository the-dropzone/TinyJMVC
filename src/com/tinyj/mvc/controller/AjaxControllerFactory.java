package com.tinyj.mvc.controller;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.tinyj.mvc.exception.AjaxInitializationException;

/**
 * The AjaxControllerFactory is responsible for creating an AjaxController.
 * currently the AjaxControllerFactory is only creating the StandardAjaxController
 * 
 * @author asaf.peeri
 *
 */
public class AjaxControllerFactory
{
		
	/**
	 * currently, creates and returns the StandardAjaxController
	 * @param aServletConfig
	 * @return
	 * @throws AjaxInitializationException 
	 */
	public static IAjaxController createAjaxController(ServletConfig aServletConfig) 
		throws AjaxInitializationException
	{
		return new StandardAjaxController(aServletConfig);
	}
}
