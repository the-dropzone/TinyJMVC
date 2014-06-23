package com.tinyj.mvc.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinyj.mvc.config.MVCConfigurationManager;
import com.tinyj.mvc.exception.HandleRequestException;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.exception.MVCInitializationException;
import com.tinyj.mvc.model.Action;
import com.tinyj.mvc.model.ActionResolver;
import com.tinyj.mvc.model.ActionResponse;
import com.tinyj.mvc.model.AjaxActionResponse;
import com.tinyj.mvc.model.JavaBeanManager;
import com.tinyj.mvc.model.JavaBeanPopulator;
import com.tinyj.mvc.model.JavaBeanScope;

/**
 * The standard MVC controller. upon construction, this class reads from the tinyj-mvc-config.xml
 * mvc configuration file, and initializes the MVCConfigurationManager.
 * when a request is issued, the standard MVC controller calls the basic lifecycle of MVC:
 * 
 * Parse request parameters
 * |
 * Validate request parameters
 * | 
 * Populate java beans from request parameters
 * |
 * Execute an action
 * |
 * Navigate to next page
 *
 *
 * note: currently the parameter validation MVC stage is not functional 
 * 
 * @author asaf.peeri
 *
 */
public class StandardMVCController implements IMVCController
{
	private static final String MVC_CONFIG_FILE_SERVLET_INIT_PARAM_NAME = "mvc_config_file_name";
	private static final String DEFAULT_MVC_CONFIG_FILE = "/WEB-INF/tinyj-mvc-config.xml";
	
	private String mMVCConfigFile = null;  
	private ServletConfig mServletConfig;
	
	
	public StandardMVCController(ServletConfig aServletConfig)
		throws MVCInitializationException
	{
		String msg = "using default mvc config file: " + mMVCConfigFile;
		System.err.println(msg);
		
		init(aServletConfig);
	}
	
	
	public StandardMVCController(ServletConfig aServletConfig, String aMVCConfigFile)
		throws MVCInitializationException
	{
		mMVCConfigFile = aMVCConfigFile;
		
		String msg = "got mvc config file: " + mMVCConfigFile;
		System.out.println(msg);
		
		init(aServletConfig);
	}
	
	
	/**
	 * the order of priority of the mvc config file is (1 is top priority):
	 * 1. from constructor
	 * 2. from web.xml (servlet init param)
	 * 3. default
	 */
	private void decideOnMVCConfigFile()
		throws MVCInitializationException
	{
		//if we didn't get an mvc config file in the c'tor, then check the web.xml
		String msg = null;
		if (mMVCConfigFile == null)
		{
			//first check we have a servlet config
			if (mServletConfig == null)
			{
				msg = "could not find an servlet config. servlet might not been initialized correctly...";
				System.out.println(msg);
				throw new MVCInitializationException("Error: could not get ServletConfig: ", MVCExceptionCodes.MVC_SERVLET_CONFIG_OR_SERVLET_CONTEXT_ARE_NOT_INITIALIZED);
			}
			
			
			String mvcConfigFileFromServletConfig = mServletConfig.getInitParameter(MVC_CONFIG_FILE_SERVLET_INIT_PARAM_NAME);
			if (mvcConfigFileFromServletConfig != null)
			{
				msg = "got mvc config file path from web.xml. using it: " + mvcConfigFileFromServletConfig;
				System.out.println(msg);
				mMVCConfigFile = mvcConfigFileFromServletConfig;
			}
			else
			{
				//means we don't have a servlet init param in the web.xml, so use the default
				msg = "didn't find an mvc config file in web.xml. using default: " + DEFAULT_MVC_CONFIG_FILE;
				System.out.println(msg);
				mMVCConfigFile = DEFAULT_MVC_CONFIG_FILE;
			}
		}
		else
		{
			msg = "got mvc config file from c'tor. using it: " + mMVCConfigFile;
			System.out.println(msg);
		}
	}
	
	
	/**
	 * initializes the MVCConfigurationManager and the JavaBeanManager from the tinyj-mvc-config.xml
	 *  
	 * @param aServletConfig
	 */
	public void init(ServletConfig aServletConfig)
		throws MVCInitializationException
	{
		//save the ServletConfig as a member variable
		mServletConfig = aServletConfig;
		
		decideOnMVCConfigFile();
		
		//extract the mvc config xml file from it's path, and send its contents to
		//the MVCConfigurationManager in order to instantiate action-mappings and javabean-mappings
		String configFile = null;
		try
		{
			InputStream is = mServletConfig.getServletContext().getResourceAsStream(mMVCConfigFile);
			if (is == null)
			{
				System.err.println("configuration file " + mMVCConfigFile + " does not exist");
				throw new MVCInitializationException("configuration file " + mMVCConfigFile + " does not exist",MVCExceptionCodes.MVC_CONFIGURATION_FILE_CANT_BE_FOUND);
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
			throw new MVCInitializationException("Error: could not get ServletConfig or ServletContext: " + npe.toString(), MVCExceptionCodes.MVC_SERVLET_CONFIG_OR_SERVLET_CONTEXT_ARE_NOT_INITIALIZED);
		}
		catch(IOException ioe)
		{
			System.err.println("Error: could not read from " + mMVCConfigFile +  ": " + ioe.toString());
			throw new MVCInitializationException("Error: could not read from " + mMVCConfigFile +  ": " + ioe.toString(), MVCExceptionCodes.MVC_CANT_READ_FROM_MVC_CONFIG_FILE_DUE_TO_IO_ERROR);
		}
		
		//this is the first call to the parseMVCConfiguration in MVCConfigurationManager
		//this call will read the tinyj-mvc-config.xml file and store all action-mappings and 
		//javabean-mappings in the MVCConfigurationManager
		MVCConfigurationManager.getInstance().parseMVCConfiguration(configFile);
		
		//calling the JavaBeanManager for the first time will issue the initialization
		//this first time call should be done only after the MVCConfigurationManager is configured
		JavaBeanManager.getInstance();
	}
	
	
	/**
	 * recieves a request and handles it according to the MVC basic lifecycle.
	 * in the end of this method, the request is forwarded to a new jsp/html page.
	 */
	public void handleRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
		throws HandleRequestException
	{
		//creating a context out of the original request and response. during initialization, 
		//the context will analyze the request to see if it was a multipart form request. 
		//if so, it will save the uploaded files onto the temporary folder using the TempFileManager
		MVCContext mvcContext = new MVCContext(aRequest, aResponse);
		
		//attach the MVCContext to the Http request object so it will be available from response JSPs
		//note that this creates a circular reference, as the MVCContext hold itself a reference
		//to the Http request
		aRequest.setAttribute("MVCContext", mvcContext);
		
		//validate request parameters according to validation-rules.xml
		//ValidationManager.validateParamsByAction(mvcContext);
		
		//populate javaBeans that are attached to request parameters
		JavaBeanPopulator.populateJavaBeansFromParameters(mvcContext);
		
		//resolve the action that should execute
		Action action = ActionResolver.resolveMVCAction(mvcContext);
		
		//execute the action
		ActionResponse actionResponse = action.execute();
		
		
		if (actionResponse instanceof AjaxActionResponse)
		{
			//write the response string back to the caller 
			NavigationHandler.writeBackAjaxResponse((AjaxActionResponse)actionResponse, mvcContext);
		}
		else
		{
			//navigate to the next page according to the action response
			NavigationHandler.handleNavigation(actionResponse, mvcContext);
		}
	}
}
