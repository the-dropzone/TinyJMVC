package com.tinyj.mvc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.tinyj.infra.file.TempFilesManager;
import com.tinyj.infra.stream.StreamUtils;
import com.tinyj.infra.structures.LimitedSizeStack;
import com.tinyj.mvc.config.MVCConfigurationManager;
import com.tinyj.mvc.exception.FormParametersInitializationException;
import com.tinyj.mvc.exception.MVCContextInstantiationException;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.exception.SessionExpiredException;
import com.tinyj.mvc.model.PageHistoryElement;


/**
 * this class serves as a context object for an http request that is processed through the
 * MVC mechanism. the MVCContext object is holding the original HttpServletRequest and
 * HttpServletResponse objects sent in the request.
 * 
 * upon construction of the MVCContext, it inquires the request, fetching all the request
 * parameters inside (whether the request is a multipart request or an application-url encoded
 * request (regular request).
 * 
 * if the request is multipart, all the parsed parameters are saved in the MVCContext and not
 * in the original request. therefore, when one needs to extract a parameter from the request,
 * he should use one of the parameter retrieval methods in the MVCContext, and not from the
 * original request.
 * 
 * also, if the request is multipart, all the uploaded files (if any) are stored by the MVCContext
 * using the TempFileManager, in a temporary location. the names of the temporary files are saved
 * in the MVCContext inside a special Map, which can be retrieved by the getUploadedFileParameterMap
 * method, or retrieve a specific temporary file name, by using the getUploadedFileName method.  
 * 
 * @author asaf.peeri
 *
 */
public class MVCContext
{
	private HttpServletRequest mRequest;
	private HttpServletResponse mResponse;
	private String mBasePath;
	private boolean mMultipartForm;
	private Map<String, ArrayList<String>> mParametersMap = new HashMap<String, ArrayList<String>>();
	private Map<String, String> mUploadedFileParametersMap = new HashMap<String, String>();

	
	/**
	 * the constructor gets the original HttpServlerRequest and saves it in a member variable.
	 * then it calls analyzeParams method, to arrange the request parameters.
	 * 
	 * @param aRequest
	 */
	public MVCContext(HttpServletRequest aRequest, HttpServletResponse aResponse)
		throws MVCContextInstantiationException
	{
		mRequest = aRequest;
		mResponse = aResponse;
		String path = aRequest.getContextPath();
		mBasePath = aRequest.getScheme()+"://"+aRequest.getServerName()+":"+aRequest.getServerPort()+path;
		analyzeParams();
	}
	
	/**
	 * returns the original HttpServletRequest instance that is saved in the MVCContext
	 *  
	 * @return the original HttpServletRequest
	 */
	public HttpServletRequest getOriginalRequest()
	{
		return mRequest;
	}
	
	
	
	/**
	 * returns the original HttpServletResponse instance that is saved in the holder
	 * @return
	 */
	public HttpServletResponse getOriginalResposne()
	{
		return mResponse;
	}
	
	
	
	/**
	 * returns the base path of this request. this is useful in order to calculate forward URLs.
	 * 
	 * @return the base path of this request
	 */
	public String getBasePath()
	{
		return mBasePath;
	}
	
	
	
	/**
	 * adds a value to the parameter's values list (usable for multiple values parameters
	 * like <select multiple>.
	 * 
	 * @param aFieldName the field name to add the value to
	 * @param aValue the value to add
	 */
	protected void addParameterValue(String aFieldName, String aValue)
	{
		ArrayList<String> valuesArrayList = mParametersMap.get(aFieldName);
		
		if (valuesArrayList == null)
		{
			valuesArrayList = new ArrayList<String>();
		}
		
		valuesArrayList.add(aValue);
		mParametersMap.put(aFieldName, valuesArrayList);		
	}

	
	/**
	 * this method is responsible of arranging the request parameters.
	 * if the request was a POST multipart form request, the parameters are gathered into one Map
	 * and the uploaded files are saved in the temp files location (Using the TempFileManager)
	 * and their temp names are saved in a different Map.
	 */
	protected void analyzeParams()
		throws FormParametersInitializationException
	{
		boolean isMultipart = ServletFileUpload.isMultipartContent(mRequest);

		if (isMultipart)
		{
			// this is a multipart form request, therefore, parameters and files
			// uploaded should be extracted from the request in a non-standard way
			mMultipartForm = true;
			try
			{
				ServletFileUpload upload = new ServletFileUpload();
				FileItemIterator iter = upload.getItemIterator(mRequest);
				
				FileItemStream item = null;
				String fieldName = null;
				String originalFileName = null;
				InputStream stream = null;
				String value = null;
				
				while (iter.hasNext())
				{
					item = iter.next();
					stream = item.openStream();
					
					if (item.isFormField())
					{
						fieldName = item.getFieldName();				
						value = Streams.asString(stream);
						addParameterValue(fieldName, value);
					}
					else
					{
						fieldName = item.getFieldName();
						originalFileName = item.getName();
						
						String shortFileName = null;
						//if a file was not uploaded, the file name will be empty. in this case skip saving the file
						if (!"".equals(originalFileName) && originalFileName != null) 
						{
							shortFileName = TempFilesManager.getInstance().getShortFileName(originalFileName);
							String fileExtension = TempFilesManager.getInstance().getFileExtension(originalFileName);
							//TODO: checkAndGetHttpSession() - there is no reason to fail here the whole operation because 
							//there is no session, just to get an id for the uploaded file. we need to replace this
							//with a generated id, without using the session...
							String fileName = checkAndGetHttpSession().getId() + "_" + System.currentTimeMillis() + ("".equals(fileExtension)?fileExtension:("." + fileExtension));
							String tempFileName = TempFilesManager.getInstance().saveTemporaryFile(fileName, stream);
							mUploadedFileParametersMap.put(fieldName, tempFileName);
							addParameterValue(fieldName, shortFileName);
						}
						else
						{						
							addParameterValue(fieldName, originalFileName);
						}
					}
				}
			}
			catch (Exception e)
			{
				System.out.println("Error occured while analyzing params for multipart form: " + e.toString());
				throw new FormParametersInitializationException("Error occured while analyzing params for multipart form: " + e.toString(), MVCExceptionCodes.MVC_ERROR_WHILE_ANALYZING_FORM_PARAMETERS);
			}

		}
		else
		{
			// this is a regular form request. no need to do anything but set
			// the multipart flag to false.
			mMultipartForm = false;
		}
	}

	
	/**
	 * returns a parameter value according to its name.
	 * if the original request was a multipart form request, the parameter value is retrieved
	 * from the context's internal parameters Map, otherwise, the value is retrieved from the
	 * original request's parameter Map.
	 * 
	 * @param aParamName the name of the parameter to retrieve
	 * 
	 * @return the value of the requested parameter
	 */
	public String getParameter(String aParamName)
	{
		if (mMultipartForm)
		{
			ArrayList<String> valuesArrayList = mParametersMap.get(aParamName);
			if (valuesArrayList == null || valuesArrayList.size() < 1)
			{
				return null;
			}
			else
			{
				//return the first parameter value (works like in regular request object)
				return valuesArrayList.get(0); 
			}
		}
		else
		{
			return mRequest.getParameter(aParamName);
		}
	}

	/**
	 * returns an enumeation of all the parameter names.
	 * if the original request was a multipart form request, the parameter names are retrieved
	 * from the context's internal parameters Map, otherwise, the names are retrieved from the
	 * original request's parameter Map.
	 * 
	 * @return the parameters Map
	 */
	public Enumeration getParameterNames()
	{
		if (mMultipartForm)
		{
			return Collections.enumeration(mParametersMap.keySet());
		}
		else
		{
			return mRequest.getParameterNames();
		}
	}

	
	/**
	 * returns a parameter values according to its name (for multi value params).
	 * if the original request was a multipart form request, the parameter values are retrieved
	 * from the context's internal parameters Map, otherwise, the values are retrieved from the
	 * original request's parameter Map.
	 * 
	 * @param aParamName the parameter to retrieve its values
	 * 
	 * @return the parameter values
	 */
	public String[] getParameterValues(String aParamName)
	{
		if (mMultipartForm)
		{
			ArrayList<String> valuesArrayList = mParametersMap.get(aParamName);
			if (valuesArrayList == null || valuesArrayList.size() < 1)
			{
				return null;
			}
			else
			{
				return valuesArrayList.toArray(new String[valuesArrayList.size()]);
			}
			
		}
		else
		{
			return mRequest.getParameterValues(aParamName);
		}
	}

	/**
	 * returns the parameters Map.
	 * if the original request was a multipart form request, the parameters Map is retrieved
	 * from the context itself, otherwise, the Map is retrieved from the
	 * original request.
	 * 
	 * @return the parameters Map
	 */
	public Map getParameterMap()
	{
		if (mMultipartForm)
		{
			return mParametersMap;
		}
		else
		{
			return mRequest.getParameterMap();
		}
	}
	
	
	/**
	 * returns the temporary saved file name that was uploaded, to be used to retrieve the file
	 * itself using the TempFilesManager.
	 * 
	 * @param aUploadedFileParameterName the parameter name of the uploaded file
	 * 
	 * @return the temporary saved uploaded file name (to be used to retrieve the actual file 
	 * 			with the TempFileManager)
	 */
	public String getUploadedFileName(String aUploadedFileParameterName)
	{
		String uploadedFileName = mUploadedFileParametersMap.get(aUploadedFileParameterName);
		return uploadedFileName;
	}
	
	
	/**
	 * returns the uploaded file names Map.
	 * 
	 * @return the uploaded file names Map
	 */
	public Map<String, String> getUploadedFileParameterMap()
	{
		return mUploadedFileParametersMap;
	}

	
	/**
	 * pushes the given page onto the page history stack which is available as an HttpSession attribute.
	 * 
	 * @param aPageURI the page URI to push onto the history stack
	 */
	public void pushPageToHistoryStack(String aPageURI, boolean aRedirect)
	{
		HttpSession httpSession = this.getOriginalRequest().getSession(true);
		LimitedSizeStack<PageHistoryElement> historyStack = (LimitedSizeStack<PageHistoryElement>)httpSession.getAttribute("MVCHistoryStack");
		if (historyStack == null)
		{
			int historyStackSize = MVCConfigurationManager.getInstance().getPageHistoryStackSize();
			historyStack = new LimitedSizeStack<PageHistoryElement>(historyStackSize);
		}
		
		historyStack.push(new PageHistoryElement(aPageURI, aRedirect));
		httpSession.setAttribute("MVCHistoryStack", historyStack);
	}
	
	
	
	public PageHistoryElement popPageFromHistoryStack()
	{
		HttpSession httpSession = this.getOriginalRequest().getSession(true);
		LimitedSizeStack<PageHistoryElement> historyStack = (LimitedSizeStack<PageHistoryElement>)httpSession.getAttribute("MVCHistoryStack");
		if (historyStack == null)
		{
			return null;
		}
		
		PageHistoryElement pageHistoryElem = historyStack.pop();
		return pageHistoryElem;
	}
	
	
	
	/**
	 * this method is intended for getting the http session, but only if it is not null.
	 * if the http session is null, then we assume that the session is expired and therefore
	 * a SessionExpiredException is thrown
	 * 
	 * if needed to get the http session, whether it is null or not, use the getHttpSession() method
	 * 
	 * @return the http session if it is not null (expired)
	 * 
	 * @throws SessionExpiredException when the http session is null
	 */
	public HttpSession checkAndGetHttpSession()
		throws SessionExpiredException
	{
		HttpSession httpSession = this.getOriginalRequest().getSession(false);
		
		if (httpSession == null)
		{
			//the session probably expired, so throw a proper exception
			throw new SessionExpiredException("Session was expired. need to relogin", MVCExceptionCodes.MVC_ERROR_WHILE_ANALYZING_FORM_PARAMETERS);
		}
		else
		{
			return httpSession;
		}
	}
	
	
	
	/**
	 * this method is intended to be used whenever needing the http session.
	 * if the session does not exist (is null), then it is created and then
	 * returned
	 * 
	 * @return the http session related to this MVContext
	 */
	public HttpSession getHttpSession()
	{
		HttpSession httpSession = this.getOriginalRequest().getSession(true);
		
		return httpSession;
	}
	
	
	
	/**
	 * sets the given error message onto a session attribute called "lastError"
	 * 
	 * @param aErrorMsg the error message to set
	 */
	public void setLastErrorOnSession(String aErrorMsg)
	{
		HttpSession httpSession = getHttpSession();
		httpSession.setAttribute("lastError", aErrorMsg);
	}
	
	
	/**
	 * appends the given error message onto a session attribute called "lastError".
	 * the delimiter between error messages is '\n'
	 * 
	 * @param aErrorMsg the error message to append
	 */
	public void appendLastErrorOnSession(String aErrorMsg)
	{
		HttpSession httpSession = getHttpSession();
		String currentLastError = (String)httpSession.getAttribute("lastError");
		if (currentLastError == null)
		{
			currentLastError = "";
		}
		
		currentLastError += ("".equals(currentLastError)?"":"\n") + aErrorMsg;
		
		setLastErrorOnSession(currentLastError);
	}
	
	/**
	 * gets the last error message value(s) that is set onto the session attribute "lastError"
	 * 
	 * @return the last message value(s)
	 */
	public String getLastErrorFromSession()
	{
		HttpSession httpSession = getHttpSession();
		String currentLastError = (String)httpSession.getAttribute("lastError");
		
		return currentLastError;
	}
	
	
	/**
	 * gets the last error message value(s) that is set onto the session attribute "lastError"
	 * with a '<BR>' rather than '\n' delimiter between error messages that were appended.
	 * this is useful to present the list of errors in a HTML page
	 * 
	 * @return the last message value(s) delimited by '<BR>'
	 */
	public String getLastErrorFromSessionAsHTML()
	{
		HttpSession httpSession = getHttpSession();
		String currentLastError = (String)httpSession.getAttribute("lastError");
		currentLastError.replace("\n", "<br>");
		
		return currentLastError;
	}
	
	
	/**
	 * removes the session attribute "lastError"
	 */
	public void clearLastErrorFromSession()
	{
		HttpSession httpSession = getHttpSession();
		httpSession.removeAttribute("lastError");
	}
	
	
	/**
	 * retrieves the Http request input stream
	 * 
	 * @return the Http request input stream
	 * 
	 * @throws IOException when any error occurs trying to get the input stream from the request
	 */
	public InputStream getInputStreamFromRequest()
		throws IOException
	{
		InputStream is = mRequest.getInputStream();
		return is;
	}
	
	
	/**
	 * returns the input stream of the current request with the given encoding.
	 * if the sent encoding is null, it defaults to UTF-8
	 * 
	 * @param aCharsetEncoding the char set to convert the input stream by
	 * 
	 * @return request input stream as string
	 * 
	 * @throws IOException IOException when any error occurs trying to get the input stream from the request
	 */
	public String getInputStreamFromRequestAsString(String aCharsetEncoding)
		throws IOException
	{
		if (aCharsetEncoding == null)
		{
			aCharsetEncoding = "UTF-8";
		}
		InputStream is = getInputStreamFromRequest();
		String inputStreamAsString = StreamUtils.inputStreamToString(is, aCharsetEncoding);
		return inputStreamAsString;
	}
	
	
	/**
	 * returns the input stream of the current request in UTF-8 encoding
	 * 
	 * @return request input stream as string
	 * 
	 * @throws IOException IOException when any error occurs trying to get the input stream from the request
	 */
	public String getInputStreamFromRequestAsString()
		throws IOException
	{
		String inputStreamAsString = getInputStreamFromRequestAsString("UTF-8");
		return inputStreamAsString;
	}
}
