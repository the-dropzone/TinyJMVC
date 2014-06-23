package com.tinyj.mvc.config;


/**
 * a descriptor for a forward saved in the MVCConfigurationManager
 * 
 * @author asaf.peeri
 *
 */
public class MVCConfigForwardDescriptor
{
	private String mName;
	private String mPath;
	private String mRedirect;
	private boolean mBackToCaller;
	private boolean mAvoidHistorySave;
	private boolean mCustomURL;
	
	
	public MVCConfigForwardDescriptor()
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
	
	public String getPath()
	{
		return mPath;
	}
	
	public void setPath(String aPath)
	{
		mPath = aPath;
	}

	public String getRedirect()
	{
		return mRedirect;
	}

	public void setRedirect(String aRedirect)
	{
		mRedirect = aRedirect;
	}

	public boolean isBackToCaller()
	{
		return mBackToCaller;
	}

	public void setBackToCaller(boolean aBackToCaller)
	{
		this.mBackToCaller = aBackToCaller;
	}

	public boolean isAvoidHistorySave()
	{
		return mAvoidHistorySave;
	}

	public void setAvoidHistorySave(boolean aAvoidHistorySave)
	{
		this.mAvoidHistorySave = aAvoidHistorySave;
	}

	public boolean isCustomURL()
	{
		return mCustomURL;
	}

	public void setCustomURL(boolean aCustomURL)
	{
		mCustomURL = aCustomURL;
	}
}
