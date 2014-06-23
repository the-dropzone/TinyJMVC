package com.tinyj.mvc.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinyj.mvc.config.MVCConfigurationManager;
import com.tinyj.mvc.exception.AjaxInitializationException;
import com.tinyj.mvc.exception.HandleRequestException;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.model.Action;
import com.tinyj.mvc.model.ActionResolver;
import com.tinyj.mvc.model.ActionResponse;
import com.tinyj.mvc.model.AjaxActionResponse;
import com.tinyj.mvc.model.JavaBeanManager;
import com.tinyj.mvc.model.JavaBeanPopulator;

public class StandardAjaxController implements IAjaxController
{
	private static final String AJAX_CONFIG_FILE = "/WEB-INF/tinyj-ajax-config.xml";
	private ServletConfig mServletConfig;
	
	
	public StandardAjaxController(ServletConfig aServletConfig)
		throws AjaxInitializationException
	{
		init(aServletConfig);
	}
	
	
	
	/**
	 * initializes the MVCConfigurationManager with the ajax definitions from the tinyj-ajax-config.xml
	 * @param aServletConfig
	 */
	public void init(ServletConfig aServletConfig)
		throws AjaxInitializationException
	{
		//save the ServletConfig as a member variable
		mServletConfig = aServletConfig;
		
		//extract the tinyj-ajax-config.xml file from the WEB-INF directory, and send its contents to
		//the MVCConfigurationManager in order to instantiate ajax-action-mappings
		String configFile = null;
		try
		{
			InputStream is = mServletConfig.getServletContext().getResourceAsStream(AJAX_CONFIG_FILE);
			if (is == null)
			{
				System.err.println("configuration file " + AJAX_CONFIG_FILE + " does not exist");
				throw new AjaxInitializationException("configuration file " + AJAX_CONFIG_FILE + " does not exist", MVCExceptionCodes.MVC_CONFIGURATION_FILE_CANT_BE_FOUND);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
			StringBuilder configFileStringBuilder = new StringBuilder();
			String line = null;
			while ( (line = br.readLine()) != null )
			{
				configFileStringBuilder.append(line);
			}
			
			configFile = configFileStringBuilder.toString();
		}
		catch(NullPointerException npe)
		{
			System.err.println("Error: could not get ServletConfig or ServletContext: " + npe.toString());
			throw new AjaxInitializationException("Error: could not get ServletConfig or ServletContext", MVCExceptionCodes.MVC_SERVLET_CONFIG_OR_SERVLET_CONTEXT_ARE_NOT_INITIALIZED);
		}
		catch(IOException ioe)
		{
			System.err.println("Error: could not read from " + AJAX_CONFIG_FILE +  ": " + ioe.toString());
			throw new AjaxInitializationException("Error: could not read from " + AJAX_CONFIG_FILE +  ": " + ioe.toString(), MVCExceptionCodes.MVC_CANT_READ_FROM_AJAX_CONFIG_FILE_DUE_TO_IO_ERROR);
		}
		
		//this is the first call to parseAjaxConfiguration the MVCConfigurationManager. 
		//this call will read the tinyj-ajax-config.xml file and store all action-mappings 
		//and javabean-mappings in the MVCConfigurationManager
		MVCConfigurationManager.getInstance().parseAjaxConfiguration(configFile);
	}
	
	
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
		throws HandleRequestException
	{
		//creating a context out of the original request and response. during initialization, 
		//the context will analyze the request to see if it was a multipart form request. 
		//if so, it will save the uploaded files onto the temporary folder using the TempFileManager
		MVCContext mvcContext = new MVCContext(aRequest, aResponse);
		
		//resolve the action that should execute
		Action action = ActionResolver.resolveAjaxAction(mvcContext);
		
		//execute the action
		ActionResponse actionResponse = action.execute();
				
		//return the response back to the caller according to the action response
		NavigationHandler.writeBackAjaxResponse((AjaxActionResponse)actionResponse, mvcContext);
	}

}
