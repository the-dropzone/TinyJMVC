package com.tinyj.mvc.controller;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.tinyj.mvc.exception.MVCInitializationException;

/**
 * The MVCControllerFactory is responsible for creating an MVCController.
 * currently the MVCControllerFactory is only creating the StandardMVCController
 * 
 * @author asaf.peeri
 *
 */
public class MVCControllerFactory
{
	
	/**
	 * currently, creates and returns the StandardMVCController
	 * 
	 * @param aServletConfig the servlet config of the controller servlet
	 * 
	 * @return an MVC controller instance
	 */
	public static IMVCController createMVCController(ServletConfig aServletConfig)
		throws MVCInitializationException
	{
		return new StandardMVCController(aServletConfig);
	}
	
	
	/**
	 * currently, creates and returns the StandardMVCController
	 * 
	 * @param aServletConfig the servlet config of the controller servlet
	 * @param aMVCControllerConfigFileLocation the location of the mvc config file path. generally should be placed under /WEB-INF/
	 * 
	 * @return an MVC controller instance
	 */
	public static IMVCController createMVCController(ServletConfig aServletConfig, String aMVCControllerConfigFileLocation)
		throws MVCInitializationException
	{
		return new StandardMVCController(aServletConfig, aMVCControllerConfigFileLocation);
	}
}
