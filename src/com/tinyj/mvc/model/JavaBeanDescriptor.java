package com.tinyj.mvc.model;

/**
 * a descriptor structure for a JavaBean held in the JavaBeanManager
 * 
 * @author asaf.peeri
 *
 */
public class JavaBeanDescriptor
{
	private String mRegisteredName;
	private String mFQNClassName;
	private JavaBeanScope mScope;
	
	
	public JavaBeanDescriptor()
	{
	}
	
	public JavaBeanDescriptor(String aRegisteredName, String aFQNClassName, JavaBeanScope aScope)
	{
		setRegisteredName(aRegisteredName);
		setFQNClassName(aFQNClassName);
		setScope(aScope);
	}
	
	
	public String getRegisteredName()
	{
		return mRegisteredName;
	}
	public void setRegisteredName(String aRegisteredName)
	{
		mRegisteredName = aRegisteredName;
	}
	public String getFQNClassName()
	{
		return mFQNClassName;
	}
	public void setFQNClassName(String aFQNClassName)
	{
		mFQNClassName = aFQNClassName;
	}
	public JavaBeanScope getScope()
	{
		return mScope;
	}
	public void setScope(JavaBeanScope aJavaBeanScope)
	{
		mScope = aJavaBeanScope;
	}
}
