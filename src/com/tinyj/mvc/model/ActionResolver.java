package com.tinyj.mvc.model;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.tinyj.mvc.config.AjaxConfigActionDescriptor;
import com.tinyj.mvc.config.MVCConfigActionDescriptor;
import com.tinyj.mvc.config.MVCConfigurationManager;
import com.tinyj.mvc.controller.MVCContext;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.exception.ResolveActionException;


/**
 * this class is used to get the proper Action object by the action name sent in the request
 * 
 * @author asaf.peeri
 *
 */
public class ActionResolver
{
	
	/**
	 * gets an MVCContext object, extracts the action name from it, and instantiates the
	 * proper Action object, as defined in the mvc configuration file.
	 * 
	 * @param aMVCContext the mvc context of this request
	 * 
	 * @return the instantiated Action object as defined in the action mapping of the mvc configuration file
	 * 
	 * @throws ResolveActionException when any action resolving is failed
	 */
	public static Action resolveMVCAction(MVCContext aMVCContext)
		throws ResolveActionException
	{
		
		String actionPath = getActionFromRequest(aMVCContext);
		if (actionPath == null)
		{
			throw new ResolveActionException("action cannot be null, empty or slash only: " + actionPath, MVCExceptionCodes.MVC_ACTION_CANT_BE_NULL_EMPTY_OR_SLASH_ONLY);
		}
		
			
		MVCConfigActionDescriptor actionDescriptor = MVCConfigurationManager.getInstance().getMVCAction(actionPath);
		if (actionDescriptor == null)
		{
			throw new ResolveActionException("action does not exist: " + actionPath, MVCExceptionCodes.MVC_ACTION_DOES_NOT_EXIST);
		}
		
		
		try
		{
			Class actionClass = Class.forName(actionDescriptor.getType());
			Constructor actionConstructor = actionClass.getConstructor(String.class, MVCContext.class);
			Object actionObj = actionConstructor.newInstance(actionDescriptor.getPath(), aMVCContext);
			
			if (!(actionObj instanceof Action))
			{
				throw new ResolveActionException("action could not be instantiated. given action is not an instance of com.tinyj.mvc.model.Action: " + actionPath + "; " + actionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_IS_NOT_INSTANCE_OF_MVC_ACTION_CLASS);
			}
			
			Action action = (Action)actionObj;
			
			return action;
			
		}
		catch(ClassNotFoundException cnfe)
		{
			throw new ResolveActionException("action could not be instantiated. type cannot be found: " + actionPath + "; " + actionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_TYPE_CLASS_CANT_BE_FOUND);
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be accessed: " + actionPath + "; " + actionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_ACCESSED);
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be found: " + actionPath + "; " + actionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_FOUND);
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be found: " + actionPath + "; " + actionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_FOUND);
		}
		catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated: " + actionPath + "; " + actionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_CANT_BE_INSTANTIATED);
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be accessed: " + actionPath + "; " + actionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_ACCESSED);
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. exception occured: " + actionPath + "; " + actionDescriptor.getType() + ": " + e.toString() , MVCExceptionCodes.MVC_ACTION_CONSTRUCTOR_INVOCATION_EXCEPTION);
		}
	}
	
	
	/**
	 * gets an MVCContext object, extracts the ajax action name from it, and instantiates the
	 * proper Action object, as defined in the ajax configuration file.
	 * 
	 * @param aMVCContext the mvc context of this request
	 * 
	 * @return the instantiated Action object as defined in the action mapping of the ajax configuration file
	 * 
	 * @throws ResolveActionException when any action resolving is failed
	 */
	public static Action resolveAjaxAction(MVCContext aMVCContext)
		throws ResolveActionException
	{
		
		String actionPath = getAjaxActionFromRequest(aMVCContext);
		if (actionPath == null)
		{
			throw new ResolveActionException("action cannot be null, empty or slash only: " + actionPath, MVCExceptionCodes.MVC_ACTION_CANT_BE_NULL_EMPTY_OR_SLASH_ONLY);
		}
		
			
		AjaxConfigActionDescriptor ajaxActionDescriptor = MVCConfigurationManager.getInstance().getAjaxAction(actionPath);
		if (ajaxActionDescriptor == null)
		{
			throw new ResolveActionException("action does not exist: " + actionPath, MVCExceptionCodes.MVC_ACTION_DOES_NOT_EXIST);
		}
		
		
		try
		{
			Class actionClass = Class.forName(ajaxActionDescriptor.getType());
			Constructor actionConstructor = actionClass.getConstructor(String.class, MVCContext.class);
			Object actionObj = actionConstructor.newInstance(ajaxActionDescriptor.getPath(), aMVCContext);
			
			if (!(actionObj instanceof Action))
			{
				throw new ResolveActionException("action could not be instantiated. given action is not an instance of com.tinyj.mvc.model.Action: " + actionPath + "; " + ajaxActionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_IS_NOT_INSTANCE_OF_MVC_ACTION_CLASS);
			}
			
			Action action = (Action)actionObj;
			
			return action;
			
		}
		catch(ClassNotFoundException cnfe)
		{
			throw new ResolveActionException("action could not be instantiated. type cannot be found: " + actionPath + "; " + ajaxActionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_TYPE_CLASS_CANT_BE_FOUND);
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be accessed: " + actionPath + "; " + ajaxActionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_ACCESSED);
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be found: " + actionPath + "; " + ajaxActionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_FOUND);
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be found: " + actionPath + "; " + ajaxActionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_FOUND);
		}
		catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated: " + actionPath + "; " + ajaxActionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_CANT_BE_INSTANTIATED);
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. constructor cannot be accessed: " + actionPath + "; " + ajaxActionDescriptor.getType(), MVCExceptionCodes.MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_ACCESSED);
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			throw new ResolveActionException("action could not be instantiated. exception occured: " + actionPath + "; " + ajaxActionDescriptor.getType() + ": " + e.toString() , MVCExceptionCodes.MVC_ACTION_CONSTRUCTOR_INVOCATION_EXCEPTION);
		}
	}
	
	
	
	/**
	 * extracts the action name from the request. if the action includes an ".act" postfix,
	 * it removes it. if it includes a path prefix before it, it also removes it.
	 * 
	 * for ex, if the action the form sent is: 
	 * <i>/testfolder/doAction.act</i> 
	 * it is resloved into:
	 * <i>doAction</i>
	 * 
	 * @param aMVCContext the mvc context of this request 
	 * 
	 * @return the action name without any path prefix or extension postfix
	 */
	public static String getActionFromRequest(MVCContext aMVCContext) 
	{
		String actionPath = null;
//		String actionPath = aMVCContext.getOriginalRequest().getPathInfo();
//		if (actionPath == null)
//		{
//			//if the servlet is not mapped by path prefix (/demopath/*) but using a *.act
//			actionPath = aMVCContext.getOriginalRequest().getServletPath();
//		}
		actionPath = aMVCContext.getOriginalRequest().getRequestURI();
		
		//remove the .act if exist
		int lastDotActIndex = actionPath.lastIndexOf(".act");
		if (lastDotActIndex != -1)
		{
			actionPath = actionPath.substring(0, lastDotActIndex);
		}
		
		if (actionPath == null)
		{
			return null;
		}
		else
		{
			actionPath = actionPath.trim();
			int lastSlashIndex = actionPath.lastIndexOf("/");
			if (lastSlashIndex != -1 && actionPath.length() != lastSlashIndex + 1 )
			{
				actionPath = actionPath.substring(lastSlashIndex + 1);
			}
		}
		
		//if we could not parse a proper actionPath
		if ("/".equals(actionPath) || "".equals(actionPath))
		{
			return null;
		}
		
		return actionPath;
	}
	
	
	
	/**
	 * extracts the ajax action name from the request. if the action includes an ".ajx" postfix,
	 * it removes it. if it includes a path prefix before it, it also removes it.
	 * 
	 * for ex, if the action the form sent is: 
	 * <i>/testfolder/doAction.ajx</i> 
	 * it is resloved into:
	 * <i>doAction</i>
	 * 
	 * @param aMVCContext the mvc context of this request 
	 * 
	 * @return the action name without any path prefix or extension postfix
	 */
	public static String getAjaxActionFromRequest(MVCContext aMVCContext) 
	{
		String actionPath = null;
//		String actionPath = aMVCContext.getOriginalRequest().getPathInfo();
//		if (actionPath == null)
//		{
//			//if the servlet is not mapped by path prefix (/demopath/*) but using a *.act
//			actionPath = aMVCContext.getOriginalRequest().getServletPath();
//		}
		actionPath = aMVCContext.getOriginalRequest().getRequestURI();
		
		//remove the .act if exist
		int lastDotActIndex = actionPath.lastIndexOf(".ajx");
		if (lastDotActIndex != -1)
		{
			actionPath = actionPath.substring(0, lastDotActIndex);
		}
		
		if (actionPath == null)
		{
			return null;
		}
		else
		{
			actionPath = actionPath.trim();
			int lastSlashIndex = actionPath.lastIndexOf("/");
			if (lastSlashIndex != -1 && actionPath.length() != lastSlashIndex + 1 )
			{
				actionPath = actionPath.substring(lastSlashIndex + 1);
			}
		}
		
		//if we could not parse a proper actionPath
		if ("/".equals(actionPath) || "".equals(actionPath))
		{
			return null;
		}
		
		return actionPath;
	}
}
