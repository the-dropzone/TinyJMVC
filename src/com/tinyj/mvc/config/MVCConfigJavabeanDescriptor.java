package com.tinyj.mvc.config;


/**
 * a descriptor for a java bean saved in the MVCConfigurationManager
 * 
 * @author asaf.peeri
 *
 */
public class MVCConfigJavabeanDescriptor
{
	private String mName;
	private String mType;
	private String mScope;
	
	
	public MVCConfigJavabeanDescriptor()
	{
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setName(String aName)
	{
		mName = aName;
	}
	
	public String getType()
	{
		return mType;
	}
	
	public void setType(String aType)
	{
		mType = aType;
	}

	public String getScope()
	{
		return mScope;
	}

	public void setScope(String aScope)
	{
		mScope = aScope;
	}
}
