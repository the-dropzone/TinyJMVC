package com.tinyj.mvc.config;

import java.util.Hashtable;
import java.util.Map;


/**
 * a descriptor for an action saved in the MVCConfigurationManager
 * 
 * @author asaf.peeri
 *
 */
public class MVCConfigActionDescriptor
{
	private String mPath;
	private String mType;
	private Map<String, MVCConfigForwardDescriptor> mForwards = new Hashtable<String, MVCConfigForwardDescriptor>();
	
	
	public MVCConfigActionDescriptor()
	{
	}


	public String getPath()
	{
		return mPath;
	}


	public void setPath(String aPath)
	{
		mPath = aPath;
	}


	public String getType()
	{
		return mType;
	}


	public void setType(String aType)
	{
		mType = aType;
	}


	public Map<String, MVCConfigForwardDescriptor> getForwards()
	{
		return mForwards;
	}

	
	public void addForward(String aForwardName, String aForwardPath)
	{
		MVCConfigForwardDescriptor fDescriptor = new MVCConfigForwardDescriptor();
		fDescriptor.setName(aForwardName);
		fDescriptor.setPath(aForwardPath);
		mForwards.put(aForwardName, fDescriptor);
	}
	
	
	public void addForward(MVCConfigForwardDescriptor aForwardDescriptor)
	{
		mForwards.put(aForwardDescriptor.getName(), aForwardDescriptor);
	}
	
	
	public void removeForward(String aForwardName)
	{
		mForwards.remove(aForwardName);
	}
	
	
	public MVCConfigForwardDescriptor getForward(String aForwardName)
	{
		return mForwards.get(aForwardName);
	}

	
	public void setForwards(Map<String, MVCConfigForwardDescriptor> aForwards)
	{
		mForwards = aForwards;
	}
	
	
}
