package com.tinyj.mvc.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.tinyj.mvc.config.MVCConfigActionDescriptor;
import com.tinyj.mvc.config.MVCConfigForwardDescriptor;
import com.tinyj.mvc.config.MVCConfigurationManager;
import com.tinyj.mvc.exception.HandleNavigationException;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.model.ActionResolver;
import com.tinyj.mvc.model.ActionResponse;
import com.tinyj.mvc.model.AjaxActionResponse;
import com.tinyj.mvc.model.CustomURLActionResponse;
import com.tinyj.mvc.model.PageHistoryElement;


/**
 * The NavigationHandler is responsible of getting an ActionResponse object, and make a page
 * forward to the proper page defined in the Action's forward list, or one of the global forwards.
 * if there is a forward name existing both in the Action's forward list and in the global
 * forwards mapping, the forward in the Action's forward list takes precedence.
 * 
 * @author asaf.peeri
 *
 */
public class NavigationHandler
{
	
	/**
	 * gets the ActionResponse object and the current mvc context, resloves the forward page
	 * according to the Action's forward list or the global forwards mapping.
	 * 
	 * @param aActionResponse the current Action's response object
	 * @param aMVCContext the mvc context of the current request
	 * 
	 * @throws HandleNavigationException
	 */
	public static void handleNavigation(ActionResponse aActionResponse, MVCContext aMVCContext)
		throws HandleNavigationException
	{
		String actionPath = ActionResolver.getActionFromRequest(aMVCContext);
		if (actionPath == null)
		{
			throw new HandleNavigationException("could not resolve action name from request", MVCExceptionCodes.MVC_CANT_RESOLVE_ACTION_NAME_FROM_REQUEST);
		}
		
		MVCConfigActionDescriptor actionDescriptor = MVCConfigurationManager.getInstance().getMVCAction(actionPath);
		if (actionDescriptor == null)
		{
			throw new HandleNavigationException("could not find action path: " + actionPath, MVCExceptionCodes.MVC_CANT_FIND_ACTION_PATH);
		}
		
		MVCConfigForwardDescriptor forwardDescriptor = actionDescriptor.getForward(aActionResponse.getResponse());
		if (forwardDescriptor == null)
		{
			//if this forward wasn't found in the actions forwards, it might be a global forward
			forwardDescriptor = MVCConfigurationManager.getInstance().getGlobalForward(aActionResponse.getResponse());
			if (forwardDescriptor == null)
			{
				throw new HandleNavigationException("could not resolve action response to a defined forward name:" + aActionResponse.getResponse(), MVCExceptionCodes.MVC_CANT_RESOLVE_ACTION_RESPONSE_TO_DEFINED_FORWARD_NAME);
			}
		}
		
		//String pathToNavigateTo = aMVCContext.getBasePath() + "/" + forwardDescriptor.getPath();
		String pathToNavigateTo = null;
		String customURLToGoTo = null;
		boolean redirect = Boolean.parseBoolean(forwardDescriptor.getRedirect());
		
		//first check if this action descriptor was defined as custom URL
		//if so, we need to extract the path to navigate to from the getCustomURL() method of the CustomURLActionResponse
		//in the action itself the user is responsible to fill in this value for the navigation to take place
		if (forwardDescriptor.isCustomURL())
		{
			if (aActionResponse instanceof CustomURLActionResponse)
			{
				//navigate to the next page defined in the forward descriptor
				pathToNavigateTo = ((CustomURLActionResponse)aActionResponse).getCustomURL();
				if (pathToNavigateTo == null || "".equals(pathToNavigateTo))
				{
					throw new HandleNavigationException("CustomURLActionResponse does not contain a custom URL property (it is null or empty) for action:" + actionPath, MVCExceptionCodes.MVC_FORWARD_PATH_FOR_ACTION_CANT_BE_NULL_OR_EMPTY);
				}
				
				customURLToGoTo = pathToNavigateTo;
			}
			else
			{
				throw new HandleNavigationException("customurl property defined in action descriptor, but class is not of type CustomURLActionResponse for action:" + actionPath, MVCExceptionCodes.MVC_ACTION_IS_NOT_INSTANCE_OF_MVC_ACTION_CLASS);
			}
		}
		else
		{
			if (forwardDescriptor.isBackToCaller())
			{
				//according to the forward descriptor, it is needed to return to the caller page.
				//this is actually the page that was last inserted to the page history stack
				PageHistoryElement pageHistoryElem = aMVCContext.popPageFromHistoryStack();
				pathToNavigateTo = pageHistoryElem.getPageURI();
				redirect = pageHistoryElem.isRedirect();
			}
			else
			{
				//navigate to the next page defined in the forward descriptor
				pathToNavigateTo = forwardDescriptor.getPath();
				if (pathToNavigateTo == null || "".equals(pathToNavigateTo))
				{
					throw new HandleNavigationException("forward path cannot be null or empty for action:" + actionPath, MVCExceptionCodes.MVC_FORWARD_PATH_FOR_ACTION_CANT_BE_NULL_OR_EMPTY);
				}
			}
		}
		
		
		try
		{
//			//save the pathToNavigateTo, in the history stack, unless defined specifically not to save
//			if (forwardDescriptor.isAvoidHistorySave() == false)
//			{
//				aMVCContext.pushPageToHistoryStack(pathToNavigateTo, redirect);
//			}
//			
//			if ( redirect )
//			{
//				//redirect value is "true", so redirect the browser to the next page
//				String path = aMVCContext.getOriginalRequest().getContextPath();
//				String basePath = aMVCContext.getOriginalRequest().getScheme()+"://"+aMVCContext.getOriginalRequest().getServerName()+":"+aMVCContext.getOriginalRequest().getServerPort()+path;
//				
//				aMVCContext.getOriginalResposne().sendRedirect(basePath + pathToNavigateTo);
//			}
//			else
//			{
//				//redirect value IS NOT "true", therefore do a request forward
//				RequestDispatcher requestDispatcher = aMVCContext.getOriginalRequest().getRequestDispatcher(pathToNavigateTo);
//					
//				requestDispatcher.forward(aMVCContext.getOriginalRequest(), aMVCContext.getOriginalResposne());
//			}
			
			navigate(aMVCContext, forwardDescriptor, customURLToGoTo);
		}
		catch(IOException ioe)
		{
			throw new HandleNavigationException("could not forward request: " + ioe.toString(), MVCExceptionCodes.MVC_COULD_NOT_FORWARD_REQUEST_DUE_TO_IO_ERROR);
		}
		catch (ServletException se)
		{
			throw new HandleNavigationException("could not forward request: " + se.toString(), MVCExceptionCodes.MVC_COULD_NOT_FORWARD_REQUEST_DUE_TO_IO_ERROR);
		}
	}
	
	
	
	/**
	 * this method makes the actual navigation to the desired location using the forward descriptor
	 * 
	 * @param aMVCContext the current MVCContext to use
	 * @param aForwardDescriptor the forward descriptor describing the location to navigate to
	 * @param aCustomURLToGoTo in case the action is defined as customURL=true, this parameter holds the URL to go to. otherwise it is null.
	 * @throws IOException
	 * @throws ServletException
	 */
	public static void navigate(MVCContext aMVCContext, MVCConfigForwardDescriptor aForwardDescriptor, String aCustomURLToGoTo)
		throws IOException, ServletException
	{
		String pathToNavigateTo = aForwardDescriptor.getPath();
		if (aCustomURLToGoTo != null)
		{
			pathToNavigateTo = aCustomURLToGoTo; 
		}
		boolean redirect = Boolean.parseBoolean(aForwardDescriptor.getRedirect());
		
		//save the pathToNavigateTo, in the history stack, unless defined specifically not to save
		if (aForwardDescriptor.isAvoidHistorySave() == false)
		{
			aMVCContext.pushPageToHistoryStack(pathToNavigateTo, redirect);
		}
		
		if ( redirect )
		{
			//redirect value is "true", so redirect the browser to the next page
			String path = aMVCContext.getOriginalRequest().getContextPath();
			String basePath = aMVCContext.getOriginalRequest().getScheme()+"://"+aMVCContext.getOriginalRequest().getServerName()+":"+aMVCContext.getOriginalRequest().getServerPort()+path;
			
			aMVCContext.getOriginalResposne().sendRedirect(basePath + pathToNavigateTo);
		}
		else
		{
			//redirect value IS NOT "true", therefore do a request forward
			RequestDispatcher requestDispatcher = aMVCContext.getOriginalRequest().getRequestDispatcher(pathToNavigateTo);
				
			requestDispatcher.forward(aMVCContext.getOriginalRequest(), aMVCContext.getOriginalResposne());
		}
	}
	
	
	
	/**
	 * gets the ActionResponse object and the current mvc context, and writes the response
	 * set in the ActionResponse back to the caller.
	 * 
	 * @param aAjaxActionResponse the current Action's response object
	 * @param aMVCContext the mvc context of the current request
	 * 
	 * @throws HandleNavigationException
	 */
	public static void writeBackAjaxResponse(AjaxActionResponse aAjaxActionResponse, MVCContext aMVCContext)
		throws HandleNavigationException
	{
		HttpServletResponse response = aMVCContext.getOriginalResposne();
		OutputStream os = null;
		String str = aAjaxActionResponse.getResponse();
		response.setContentType(aAjaxActionResponse.getResponseContentType());
		try
		{
			os = response.getOutputStream();
			os.write(str.getBytes());
			os.flush();
		}
		catch (Exception e)
		{
			System.out.println("error in writing Ajax response");
		}
		finally
		{
			try
			{
				os.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*HttpServletResponse response = aMVCContext.getOriginalResposne();
		Writer resWriter = null;
		
		try
		{
			resWriter = response.getWriter();
			resWriter.write(aActionResponse.getResponse());
			resWriter.flush();
			System.out.println("wrote back: " + aActionResponse.getResponse());
		}
		catch (IOException e)
		{
			throw new HandleNavigationException("could not write back the response of action: " + aActionResponse.getExecutedAction().getActionPath() + ": " + e.toString(), 2000);
		}
		finally
		{
			try
			{
				resWriter.close();
			}
			catch (IOException e)
			{
				throw new HandleNavigationException("could not write back the response of action: " + aActionResponse.getExecutedAction().getActionPath() + ": " + e.toString(), 2000);
			}
		}*/		
	}
}
