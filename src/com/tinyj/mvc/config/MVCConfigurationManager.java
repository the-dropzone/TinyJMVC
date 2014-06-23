package com.tinyj.mvc.config;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tinyj.mvc.exception.AjaxInitializationException;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.exception.MVCInitializationException;


/**
 * MVCConfigurationManager is responsible for reading the mvc configuration xml file and map
 * the action mappings, global-forwards and javabean mappings to local repository, to be used
 * by any other class.
 * 
 * @author asaf.peeri
 *
 */
public class MVCConfigurationManager
{
	protected static MVCConfigurationManager sInstance;
	
	//for mvc
	public Map<String, MVCConfigActionDescriptor> mActionMappings;
	public Map<String, MVCConfigForwardDescriptor> mGlobalForwards;
	public Map<String, MVCConfigJavabeanDescriptor> mJavabeanMappings;
	public int mPageHistoryStackSize = 10;
	
	//for ajax
	public Map<String, AjaxConfigActionDescriptor> mAjaxActionMappings;
	
	
	/**
	 * the c'tor
	 */
	protected MVCConfigurationManager()
	{
		mActionMappings = new Hashtable<String, MVCConfigActionDescriptor>();
		mGlobalForwards = new Hashtable<String, MVCConfigForwardDescriptor>();
		mJavabeanMappings = new Hashtable<String, MVCConfigJavabeanDescriptor>();
		mAjaxActionMappings = new Hashtable<String, AjaxConfigActionDescriptor>();
		
		//parseConfiguration will be called from outside because
		//it needs to have the configuration xml as a parameter
		//parseConfiguration(); 
	}
	
	
	/**
	 * gets the instance of the MVCConfigurationManager
	 * 
	 * @return the instance of this MVCConfigurationManager
	 */
	public static MVCConfigurationManager getInstance()
	{
		if (sInstance != null)
		{
			return sInstance;
		}
		
		synchronized (MVCConfigurationManager.class)
		{
			if (sInstance != null)
			{
				return sInstance;
			}
			else
			{
				sInstance = new MVCConfigurationManager();
				return sInstance;
			}
		}
	}
	
	
	/**
	 * parses the MVC configuration file given, into action-mappings, global-forwards and javabean-mappings
	 * 
	 * @param aMVCConfigurationXML the content of the MVC configuration file
	 */
	public void parseMVCConfiguration(String aMVCConfigurationXML)
		throws MVCInitializationException
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(aMVCConfigurationXML.getBytes());
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
			
			parseActionMappings(xpath, d);
			parseGlobalForwards(xpath, d);
			parseJavabeanMappings(xpath, d);
			parsePageHistoryStack(xpath, d);
			
		}
		catch(Throwable t)
		{
			System.out.println("could not parse MVC configuration: " + t.toString());
			t.printStackTrace();
			throw new MVCInitializationException("could not parse MVC configuration: " + t.toString(), MVCExceptionCodes.MVC_CONFIGURATION_PARSE_ERROR);
		}
	}
	
	
	/**
	 * a helper method used to parse the action-mappings in the MVC configuration file
	 * 
	 * @param aXPath the currently used XPath object
	 * @param aDocument the current document object
	 * 
	 * @throws XPathExpressionException for any expression error occurs
	 */
	protected void parseActionMappings(XPath aXPath, Document aDocument)
		throws XPathExpressionException
	{
		NodeList nodeList = (NodeList)aXPath.evaluate("//tinyj-mvc-config/action-mappings/action", aDocument, XPathConstants.NODESET);
		for (int i=0 ; i<nodeList.getLength() ; ++i)
		{
			Element actionElem = (Element)nodeList.item(i);
			MVCConfigActionDescriptor actionDescriptor = parseAction(actionElem);
			
			if (actionDescriptor != null)
			{
				//mActionMappings.put(actionDescriptor.getPath(), actionDescriptor);
				addMVCAction(actionDescriptor);
			}
		}
	}
	
	
	/**
	 * a helper method used to parse a single Action xml element in the MVC configuration file
	 * 
	 * @param aActionElem the Action xml element
	 * 
	 * @return the parsed Action as a descriptor
	 */
	protected MVCConfigActionDescriptor parseAction(Element aActionElem)
	{
		if (aActionElem == null)
		{
			return null;
		}
		
		String path = aActionElem.getAttribute("path");
		String type = aActionElem.getAttribute("type");
				
		Element forwardElem = null;
		
		MVCConfigActionDescriptor actionDescriptor = new MVCConfigActionDescriptor();
		actionDescriptor.setPath(path);
		actionDescriptor.setType(type);
				
		NodeList forwardNodeList = aActionElem.getElementsByTagName("forward");
		for (int i=0 ; i<forwardNodeList.getLength() ; ++i)
		{
			
			forwardElem = (Element)forwardNodeList.item(i);
			MVCConfigForwardDescriptor forwardDescriptor = parseForward(forwardElem);
			
			actionDescriptor.addForward(forwardDescriptor);
		}
		
		return actionDescriptor;
	}
	
	
	/**
	 * a helper method used to parse a Forward xml element in the MVC configuration file
	 * 
	 * @param aForwardElem the Forward xml element
	 * 
	 * @return the parsed Forward as a descriptor
	 */
	protected MVCConfigForwardDescriptor parseForward(Element aForwardElem)
	{
		if (aForwardElem == null)
		{
			return null;
		}
		
		String forwardName = aForwardElem.getAttribute("name");
		String forwardPath = aForwardElem.getAttribute("path");
		String forwardRedirect = aForwardElem.getAttribute("redirect");
		String backToCaller = aForwardElem.getAttribute("backtocaller");
		String avoidHistorySave = aForwardElem.getAttribute("avoidhistorysave");
		String customURL = aForwardElem.getAttribute("customurl");
		
		
		MVCConfigForwardDescriptor forwardDescriptor = new MVCConfigForwardDescriptor();
		forwardDescriptor.setName(forwardName);
		forwardDescriptor.setPath(forwardPath);
		forwardDescriptor.setRedirect(forwardRedirect);
		forwardDescriptor.setBackToCaller(Boolean.parseBoolean(backToCaller));
		forwardDescriptor.setAvoidHistorySave(Boolean.parseBoolean(avoidHistorySave));
		forwardDescriptor.setCustomURL(Boolean.parseBoolean(customURL));
		
		return forwardDescriptor;
	}
		
	
	
	/**
	 * a helper method used to parse the global forwards mapping in the MVC configuration file
	 * 
	 * @param aXPath the currently used XPath object
	 * @param aDocument the current document object
	 * 
	 * @throws XPathExpressionException for any expression error occurs
	 */
	protected void parseGlobalForwards(XPath aXPath, Document aDocument)
		throws XPathExpressionException
	{
		Element forwardElem = null; 
		
		NodeList nodeList = (NodeList)aXPath.evaluate("//tinyj-mvc-config/global-forwards/forward", aDocument, XPathConstants.NODESET);
		for (int i=0 ; i<nodeList.getLength() ; ++i)
		{
			forwardElem = (Element)nodeList.item(i);
			MVCConfigForwardDescriptor forwardDescriptor =  parseForward(forwardElem);
			
			mGlobalForwards.put(forwardDescriptor.getName(), forwardDescriptor);
		}
	}
	
	
	
	/**
	 * a helper method used to parse the java beans mapping in the MVC configuration file
	 * 
	 * @param aXPath the currently used XPath object
	 * @param aDocument the current document object
	 * 
	 * @throws XPathExpressionException for any expression error occurs
	 */
	protected void parseJavabeanMappings(XPath aXPath, Document aDocument)
		throws XPathExpressionException
	{
		Element javabeanElem = null; 
		
		NodeList nodeList = (NodeList)aXPath.evaluate("//tinyj-mvc-config/javabean-mappings/javabean", aDocument, XPathConstants.NODESET);
		for (int i=0 ; i<nodeList.getLength() ; ++i)
		{
			javabeanElem = (Element)nodeList.item(i);
			MVCConfigJavabeanDescriptor javabeanDescriptor =  parseJavabean(javabeanElem);
			
			mJavabeanMappings.put(javabeanDescriptor.getName(), javabeanDescriptor);
		}
	}
	
	
	/**
	 * a helper method used to parse a single JavaBean xml element in the MVC configuration file
	 * 
	 * @param aJavabeanElem the JavaBean xml element
	 * 
	 * @return the parsed JavaBean as a descriptor
	 */
	protected MVCConfigJavabeanDescriptor parseJavabean(Element aJavabeanElem)
	{
		if (aJavabeanElem == null)
		{
			return null;
		}
		
		String javabeanName = aJavabeanElem.getAttribute("name");
		String javabeanType = aJavabeanElem.getAttribute("type");
		String javabeanScope = aJavabeanElem.getAttribute("scope");
		
		MVCConfigJavabeanDescriptor javabeanDescriptor = new MVCConfigJavabeanDescriptor();
		javabeanDescriptor.setName(javabeanName);
		javabeanDescriptor.setType(javabeanType);
		javabeanDescriptor.setScope(javabeanScope);
		
		return javabeanDescriptor;
	}
	
	
	
	protected void parsePageHistoryStack(XPath aXPath, Document aDocument)
		throws XPathExpressionException, MVCInitializationException
	{
		Element historyStackElem = null; 
		
		NodeList nodeList = (NodeList)aXPath.evaluate("//tinyj-mvc-config/page-history-stack", aDocument, XPathConstants.NODESET);
		if (nodeList.getLength() == 1)
		{
			historyStackElem = (Element)nodeList.item(0);
			String historyStackSize = historyStackElem.getAttribute("size");
			try
			{
				mPageHistoryStackSize = Integer.parseInt(historyStackSize);
			}
			catch(NumberFormatException nfe)
			{
				String errMsg = "page-history-stack size could not be parsed into a valid Integer: " + nfe.toString();
				System.out.println(errMsg);
				nfe.printStackTrace();
				throw new MVCInitializationException(errMsg, MVCExceptionCodes.MVC_CONFIGURATION_PARSE_ERROR);
			}
			
			 
		}
	}
	
	
	
	/**
	 * parses the Ajax configuration file given, into ajax-action-mappings
	 * 
	 * @param aAjaxConfigurationXML the content of the Ajax configuration file
	 */
	public void parseAjaxConfiguration(String aAjaxConfigurationXML)
		throws AjaxInitializationException
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(aAjaxConfigurationXML.getBytes());
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
			
			parseAjaxActionMappings(xpath, d);			
		}
		catch(Exception e)
		{
			System.out.println("could not parse Ajax configuration: " + e.toString());
			e.printStackTrace();
			throw new AjaxInitializationException("could not parse Ajax configuration: " + e.toString(), MVCExceptionCodes.MVC_AJAX_CONFIGURATION_PARSE_ERROR);
		}
	}
	
	
	/**
	 * a helper method used to parse the ajax-action-mappings in the Ajax configuration file
	 * 
	 * @param aXPath the currently used XPath object
	 * @param aDocument the current document object
	 * 
	 * @throws XPathExpressionException for any expression error occurs
	 */
	protected void parseAjaxActionMappings(XPath aXPath, Document aDocument)
		throws XPathExpressionException
	{
		NodeList nodeList = (NodeList)aXPath.evaluate("//tinyj-ajax-config/ajax-action-mappings/ajax-action", aDocument, XPathConstants.NODESET);
		for (int i=0 ; i<nodeList.getLength() ; ++i)
		{
			Element actionElem = (Element)nodeList.item(i);
			AjaxConfigActionDescriptor ajaxActionDescriptor = parseAjaxAction(actionElem);
			
			if (ajaxActionDescriptor != null)
			{
				//mAjaxActionMappings.put(ajaxActionDescriptor.getPath(), ajaxActionDescriptor);
				addAjaxAction(ajaxActionDescriptor);
			}
		}
	}
	
	
	/**
	 * a helper method used to parse a single Ajax Action xml element in the Ajax configuration file
	 * 
	 * @param aAjaxActionElem the Ajax Action xml element
	 * 
	 * @return the parsed Ajax Action as a descriptor
	 */
	protected AjaxConfigActionDescriptor parseAjaxAction(Element aAjaxActionElem)
	{
		if (aAjaxActionElem == null)
		{
			return null;
		}
		
		String path = aAjaxActionElem.getAttribute("path");
		String type = aAjaxActionElem.getAttribute("type");
		
		AjaxConfigActionDescriptor ajaxActionDescriptor = new AjaxConfigActionDescriptor();
		ajaxActionDescriptor.setPath(path);
		ajaxActionDescriptor.setType(type);
				
		return ajaxActionDescriptor;
	}
	
	
	
	/**
	 * adds a new action descriptor to the configuration
	 * 
	 * @param aActionDescriptor the action descriptor to add
	 */
	public void addMVCAction(MVCConfigActionDescriptor aActionDescriptor)
	{
		if (aActionDescriptor == null || aActionDescriptor.getPath() == null)
		{
			return;
		}
		
		mActionMappings.put(aActionDescriptor.getPath(), aActionDescriptor);
	}
	
	
	/**
	 * retrieves an action descriptor according to the the given action name
	 * 
	 * @param aActionPath the name of the action to retrieve 
	 * 
	 * @return the action descriptor
	 */
	public MVCConfigActionDescriptor getMVCAction(String aActionPath)
	{
		return mActionMappings.get(aActionPath);
	}
	
	
	/**
	 * adds a new ajax action descriptor to the configuration
	 * 
	 * @param aActionDescriptor the ajax action descriptor to add
	 */
	public void addAjaxAction(AjaxConfigActionDescriptor aAjaxActionDescriptor)
	{
		if (aAjaxActionDescriptor == null || aAjaxActionDescriptor.getPath() == null)
		{
			return;
		}
		
		mAjaxActionMappings.put(aAjaxActionDescriptor.getPath(), aAjaxActionDescriptor);
	}
	
	
	/**
	 * retrieves an ajax action descriptor according to the the given ajax action name
	 * 
	 * @param aActionPath the name of the ajax action to retrieve 
	 * 
	 * @return the ajax action descriptor
	 */
	public AjaxConfigActionDescriptor getAjaxAction(String aAjaxActionPath)
	{
		return mAjaxActionMappings.get(aAjaxActionPath);
	}
	
	
	/**
	 * retrieves a global forward descriptor according to the given forward name
	 * 
	 * @param aForwardName the name of the forward to retrieve
	 * 
	 * @return the global forward descriptor
	 */
	public MVCConfigForwardDescriptor getGlobalForward(String aForwardName)
	{
		return mGlobalForwards.get(aForwardName);
	}
	
	
	/**
	 * retrieves all the actions mapping as an iterator
	 * 
	 * @return an iterator of action descriptors
	 */
	public Iterator<MVCConfigActionDescriptor> getMVCActionMappings()
	{
		return mActionMappings.values().iterator();
	}
	
	
	/**
	 * retrieves all the ajax actions mapping as an iterator
	 * 
	 * @return an iterator of ajax action descriptors
	 */
	public Iterator<AjaxConfigActionDescriptor> getAjaxActionMappings()
	{
		return mAjaxActionMappings.values().iterator();
	}


	/**
	 * retrieves all the global forwards mapping as an iterator
	 * 
	 * @return an iterator of global forward descriptors
	 */
	public Iterator<MVCConfigForwardDescriptor> getGlobalForwards()
	{
		return mGlobalForwards.values().iterator();
	}


	/**
	 * retrieves all the java beans mapping as an iterator
	 * 
	 * @return an iterator of java bean descriptors
	 */
	public Iterator<MVCConfigJavabeanDescriptor> getJavabeanMappings()
	{
		return mJavabeanMappings.values().iterator();
	}
	
	
	/**
	 * returns the page-history-stack size
	 * 
	 * @return the page-history-stack size
	 */
	public int getPageHistoryStackSize()
	{
		return mPageHistoryStackSize;
	}
	
}