package com.tinyj.mvc.model;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.tinyj.mvc.config.MVCConfigJavabeanDescriptor;
import com.tinyj.mvc.config.MVCConfigurationManager;
import com.tinyj.mvc.controller.MVCContext;
import com.tinyj.mvc.exception.JavaBeanInstantiationException;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.exception.SessionExpiredException;


/**
 * this class should be used to organize the java beans used by the web application.
 * it saves a Hashtable with all the beans information. the information can be filled in using
 * the <i>registerJavaBean(...)</i> method. the source of the beans information can be from an external
 * XML file containing all the managed beans, (or automatically "on the fly" using the 
 * <tinyjmvc:useBean/> tag from the <i>tinyjmvc-taglib.tld</i> library).
 * 
 * @author asaf.peeri
 *
 */
public class JavaBeanManager
{
	protected static JavaBeanManager sInstance;
	
	private Map<String, JavaBeanDescriptor> mJavaBeansInfoMap;
	
	
	protected JavaBeanManager()
	{
		mJavaBeansInfoMap = new Hashtable<String, JavaBeanDescriptor>();
		init();
	}
	
	
	public static JavaBeanManager getInstance()
	{
		if (sInstance != null)
		{
			return sInstance;
		}
		else
		{
			synchronized (JavaBeanManager.class)
			{
				if (sInstance != null)
				{
					return sInstance;
				}
				else
				{
					sInstance = new JavaBeanManager();
					return sInstance;
				}
			}
		}
	}
	
	
	/**
	 * initializes the java beans information from the MVCConfigurationManager
	 */
	public void init()
	{
		MVCConfigJavabeanDescriptor javabeanDescriptor = null;
		Iterator<MVCConfigJavabeanDescriptor> javabeansIter = MVCConfigurationManager.getInstance().getJavabeanMappings();
		while (javabeansIter.hasNext())
		{
			javabeanDescriptor = javabeansIter.next();
			registerJavaBean(javabeanDescriptor.getName(), javabeanDescriptor.getType(), JavaBeanScope.valueOf(javabeanDescriptor.getScope()));
		}
	}
	
	
	/**
	 * registers a new JavaBean to be used in the application
	 * 
	 * @param aRegisteredName the name that the javaBean will be registered on
	 * @param aFQNClassName the fully qualified class name of the javaBean
	 * @param aScope the scope which the bean will be created (application, session, request, page)
	 */
	public void registerJavaBean(String aRegisteredName, String aFQNClassName, JavaBeanScope aScope)
	{
		if (mJavaBeansInfoMap.get(aRegisteredName) != null)
		{
			return;
		}
		
		JavaBeanDescriptor descriptor = new JavaBeanDescriptor(aRegisteredName, aFQNClassName, aScope);
		mJavaBeansInfoMap.put(aRegisteredName, descriptor);
	}
	
	
	/**
	 * returns a JavaBeanDescriptor object, according to the given javaBean registered name
	 * 
	 * @param aRegisteredName the name of the javaBean to retrieve
	 * 
	 * @return the JavaBeanDescriptor of the requested bean. null if the given name does not
	 * exist.
	 */
	public JavaBeanDescriptor getJavaBeanDescriptor(String aRegisteredName)
	{
		JavaBeanDescriptor descriptor = mJavaBeansInfoMap.get(aRegisteredName);
		return descriptor;
	}
	
	
	/**
	 * unregisters a bean according to a given name.
	 * this method is not exposed outside of the JavaBeanManager in order to avoid problems.
	 * any child class can use this method upon necessity.
	 * 
	 * @param aRegisteredName the bean to be unregistered
	 */
	protected void unregisterJavaBean(String aRegisteredName)
	{
		mJavaBeansInfoMap.remove(aRegisteredName);
	}
	
	
	/**
	 * unregisters all javaBeans from the JavaBeanManager.
	 * this method is not exposed outside of the JavaBeanManager in order to avoid problems.
	 * any child class can use this method upon necessity.
	 */
	protected void clearJavaBeans()
	{
		mJavaBeansInfoMap.clear();
	}
	
	
	/**
	 * returns an Iterator for all the registered javaBean descriptors.
	 * 
	 * @return JavaBeanDescriptor iterator for all registered javaBeans
	 */
	public Iterator<JavaBeanDescriptor> javaBeansIterator()
	{
		return mJavaBeansInfoMap.values().iterator();
	}
	
	
	/**
	 * returns an Iterator for all the registered javaBean names.
	 * 
	 * @return Iterator for all the registered javaBean names
	 */
	public Iterator<String> javaBeanNamesIterator()
	{
		return mJavaBeansInfoMap.keySet().iterator();
	}
	
	
	
	
	/**
	 * checks whether the http session is expired (if it is null, means it is expired)
	 * if not, it is returned. if expired, a SessionExpiredException is thrown
	 * 
	 * @param aMVCContext the mvc context from which the http session is extracted
	 * @return the http session
	 * @throws SessionExpiredException if the http session is null
	 */
//	protected HttpSession checkAndGetHttpSession(MVCContext aMVCContext)
//		throws SessionExpiredException
//	{
//		HttpSession httpSession = aMVCContext.getOriginalRequest().getSession(false);
//		
//		if (httpSession == null)
//		{
//			//the session probably expired, so throw a proper exception
//			throw new SessionExpiredException("Session was expired. need to relogin", MVCExceptionCodes.MVC_ERROR_WHILE_ANALYZING_FORM_PARAMETERS);
//		}
//		else
//		{
//			return httpSession;
//		}
//	}
	
	
	
	/**
	 * returns a javaBean instance according to its given name.
	 * all the javaBeans information is held by the JavaBeanManager singleton. this method
	 * first gets the javaBean descriptor from the JavaBeanManager, and then tries to see if
	 * it is already hung on the proper scoped object (application, session, request). if
	 * it does not exist, it tries to instantiate a new instance of this javaBean, using the
	 * fully qualified class name retrieved from the javaBean descriptor.
	 * if the instantiation goes successful, it hangs the javaBean on the proper scoped object
	 * and returns this instance to the caller.
	 *    
	 * @param aMVCContext the MVCContext of the current request
	 * @param aJavaBeanName the name of the javaBean to be retrieved
	 * 
	 * @return the javaBean instance from the proper scoped object
	 * 
	 * @throws JavaBeanInstantiationException when any instantiation problem occurs
	 */
	public Object getJavaBeanObjectByName(MVCContext aMVCContext, String aJavaBeanName)
		throws JavaBeanInstantiationException, SessionExpiredException
	{
		
		JavaBeanDescriptor descriptor = JavaBeanManager.getInstance().getJavaBeanDescriptor(aJavaBeanName);
		
		if (descriptor == null)
		{
			//we didnt find the requested java bean in the JavaBeanManager
			throw new JavaBeanInstantiationException("could not find bean named: " + aJavaBeanName, MVCExceptionCodes.MVC_CANT_FIND_JAVABEAN_BY_GIVEN_NAME);
		}
		
		Object javaBeanObj = null;
		if (descriptor.getScope().equals(JavaBeanScope.application))
		{
			javaBeanObj = aMVCContext.checkAndGetHttpSession().getServletContext().getAttribute(aJavaBeanName);
			//javaBeanObj = aMVCContext.getOriginalRequest().getSession(true).getServletContext().getAttribute(aJavaBeanName);
			
			if (javaBeanObj == null)
			{
				//the javaBean has to be created and hanged
				javaBeanObj = instantiateClass(descriptor.getFQNClassName());
				
				aMVCContext.checkAndGetHttpSession().getServletContext().setAttribute(aJavaBeanName, javaBeanObj);
				//aMVCContext.getOriginalRequest().getSession(true).getServletContext().setAttribute(aJavaBeanName, javaBeanObj);
			}
		}
		else if (descriptor.getScope().equals(JavaBeanScope.session))
		{
			javaBeanObj = aMVCContext.checkAndGetHttpSession().getAttribute(aJavaBeanName);
			//javaBeanObj = aMVCContext.getOriginalRequest().getSession(true).getAttribute(aJavaBeanName);
			if (javaBeanObj == null)
			{
				//the javaBean has to be created and hanged
				javaBeanObj = instantiateClass(descriptor.getFQNClassName());
				aMVCContext.checkAndGetHttpSession().setAttribute(aJavaBeanName, javaBeanObj);
				//aMVCContext.getOriginalRequest().getSession(true).setAttribute(aJavaBeanName, javaBeanObj);
			}
		}
		else if (descriptor.getScope().equals(JavaBeanScope.request))
		{
			javaBeanObj = aMVCContext.getOriginalRequest().getAttribute(aJavaBeanName);
			if (javaBeanObj == null)
			{
				//the javaBean has to be created and hanged
				javaBeanObj = instantiateClass(descriptor.getFQNClassName());
				aMVCContext.getOriginalRequest().setAttribute(aJavaBeanName, javaBeanObj);
			}
		}
				
		return javaBeanObj;
	}
	
	
	
	/**
	 * instantiates an object given its fully qualified class name
	 * 
	 * @param aFQNClassName the FQN Class name
	 * 
	 * @return the instantiated Object
	 * 
	 * @throws JavaBeanInstantiationException when the instantiation fails for any reason
	 */
	public Object instantiateClass(String aFQNClassName)
		throws JavaBeanInstantiationException
	{
		Object newObj = null;
		
		try
		{
			Class myclass = Class.forName(aFQNClassName);
			newObj = myclass.newInstance();
			
			return newObj;
		}
		catch(ClassNotFoundException cnfe)
		{
			throw new JavaBeanInstantiationException(cnfe.toString(), MVCExceptionCodes.MVC_JAVABEAN_TYPE_CLASS_CANT_BE_FOUND);
		}
		catch (InstantiationException ie)
		{
			throw new JavaBeanInstantiationException(ie.toString(), MVCExceptionCodes.MVC_JAVABEAN_CANT_BE_INSTANTIATED);
		}
		catch (IllegalAccessException iae)
		{
			throw new JavaBeanInstantiationException(iae.toString(), MVCExceptionCodes.MVC_JAVABEAN_PROPER_CONSTRUCTOR_CANT_BE_ACCESSED);
		}
	}
	
	
	
	

	public void setJavaBeanObjectByName(MVCContext aMVCContext, String aJavaBeanName, Object aJavaBeanObject)
		throws JavaBeanInstantiationException, SessionExpiredException
	{
		JavaBeanDescriptor descriptor = JavaBeanManager.getInstance().getJavaBeanDescriptor(aJavaBeanName);
		
		if (descriptor == null)
		{
			//we didnt find the requested java bean description in the JavaBeanManager
			throw new JavaBeanInstantiationException("could not find definition for java bean named: " + aJavaBeanName, MVCExceptionCodes.MVC_JAVABEAN_IS_NOT_DEFINED);
		}
		
	
		if (!aJavaBeanObject.getClass().getName().equals(descriptor.getFQNClassName()))
		{
			//if the given java bean object and the required type according to java bean
			//definitions do not match, fail this set operation
			throw new JavaBeanInstantiationException("cannot set java bean: " + aJavaBeanName + " defined type is: " + descriptor.getFQNClassName() + " actual type is: " + aJavaBeanObject.getClass().getName(), MVCExceptionCodes.MVC_JAVABEAN_DEFINED_TYPE_AND_ACTUAL_TYPE_DOES_NOT_MATCH);
		}
		
		Object javaBeanObj = null;
		
		if (descriptor.getScope().equals(JavaBeanScope.application))
		{
			aMVCContext.checkAndGetHttpSession().getServletContext().setAttribute(aJavaBeanName, aJavaBeanObject);
			//aMVCContext.getOriginalRequest().getSession(true).getServletContext().setAttribute(aJavaBeanName, aJavaBeanObject);
		}
		else if (descriptor.getScope().equals(JavaBeanScope.session))
		{
			aMVCContext.checkAndGetHttpSession().setAttribute(aJavaBeanName, aJavaBeanObject);
			//aMVCContext.getOriginalRequest().getSession(true).setAttribute(aJavaBeanName, aJavaBeanObject);
		}
		else if (descriptor.getScope().equals(JavaBeanScope.request))
		{
			aMVCContext.getOriginalRequest().setAttribute(aJavaBeanName, aJavaBeanObject);
		}
	}
}
