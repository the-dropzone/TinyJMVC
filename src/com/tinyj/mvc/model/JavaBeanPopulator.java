package com.tinyj.mvc.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.tinyj.infra.colors.ColorUtils;
import com.tinyj.mvc.controller.MVCContext;
import com.tinyj.mvc.exception.JavaBeanInstantiationException;
import com.tinyj.mvc.exception.JavaBeanPopulationException;
import com.tinyj.mvc.exception.MVCExceptionCodes;
import com.tinyj.mvc.exception.ParameterTypeConversionException;
import com.tinyj.mvc.exception.SessionExpiredException;

public class JavaBeanPopulator
{
	public static final int HEX_RADIX = 16;
	public static final int DEC_RADIX = 10;
	
	/**
	 * this method gets an MVCContext, extracts all the request parameters from it, and 
	 * populate the javaBean attached parameters onto javaBean objects.
	 * the parameters that will be populated into javaBeans are only the ones starting with a
	 * <i>#</i> prefix, and contain at least one Dot (<i>.</i>) separator.
	 * for ex: a form field which looks like the following:
	 * 
	 * <input type="text" name="#person.address.street">
	 * 
	 * will be populated onto a javaBean named <i>person</i>, in its <i>address</i> internal
	 * field, and in <i>street</i> internal field of <i>address</i>.
	 * 
	 * any form fields which do not have the <i>#</i> prefix or do not contain the a Dot (<i>.</i>)
	 * will not be populated onto any javaBean.
	 * 
	 * @param aMVCContext the context of the current request
	 * 
	 * @throws JavaBeanPopulationException when any javaBean population error occurs
	 */
	public static void populateJavaBeansFromParameters(MVCContext aMVCContext)
		throws JavaBeanPopulationException, SessionExpiredException
	{
		String tempFieldName = null;
		String beanName = null;
		String fieldName = null;
		Enumeration<String> enumeration = (Enumeration<String>)aMVCContext.getParameterNames();
		
		while (enumeration.hasMoreElements())
		{
			String key = enumeration.nextElement();
			if (key == null || !key.startsWith("#") || !key.contains("."))
			{
				//if the name of the field doesn't start with # or doesn't contain a dot separator
				//then it is not intended to be populated in a JavaBean
				continue;
			}
			
			String values[] = aMVCContext.getParameterValues(key);
			Object obj = values;
			
			//get the bean field without the # prefix and trim leading and trailing spaces
			tempFieldName = key.substring(1);
			tempFieldName.trim();
			
			setJavaBeanField(aMVCContext, null, tempFieldName, values);
		}
	}
	
	
	/**
	 * sets a bean field with the given value.
	 * this method is a recursive method to be used in order to set a javaBean field with a
	 * value. the method is scanning the aRestOfName in order to decide whether this is a
	 * complex inside a complex javaBean, and recursively continues the scan until it reaches
	 * one level complexity.
	 * 
	 * for ex: if the javaBean field was: <i>person.address.street</i> then it extracts the 
	 * <i>person</i>, tries to get it as an object, and then sends the rest of the name 
	 * <i>address.street</i> recursively into the same method.
	 * if the rest of the name contains <i>address.street</i> it retrieves the <i>address</i>
	 * as an object using a getter method out of the current Object, and then invokes a setter
	 * method for the <i>street</i> field.
	 * 
	 * 
	 * @param aMVCContext this request context
	 * @param aCurrObj the current Object to work on
	 * @param aRestOfName the rest of the javaBean name
	 * @param aFieldValue the value to set on the field
	 * 
	 * @throws JavaBeanPopulationException when any population exception occurs
	 */
	protected static void setJavaBeanField(MVCContext aMVCContext, Object aCurrObj, String aRestOfName, Object aFieldValue)
		throws JavaBeanPopulationException, SessionExpiredException
	{
		String[] partsOfName = aRestOfName.split("\\.");
		String objectName = null;
		String fieldName = null;
		String restOfName = null;
		
		try
		{		
			if (partsOfName.length > 2)
			{
				int firstDotIndex = aRestOfName.indexOf(".");
				if (firstDotIndex + 1 == aRestOfName.length())
				{
					//this means the Dot is at the end of the name. this is not well formed
					//therefore, fail this population
					throw new JavaBeanPopulationException("bean name is not well defined: " + aRestOfName, MVCExceptionCodes.MVC_USED_JAVABEAN_NAME_IS_NOT_WELL_FORMED);
				}
				objectName = aRestOfName.substring(0, firstDotIndex);
				restOfName = aRestOfName.substring(firstDotIndex + 1, aRestOfName.length());
				
				//if aCurrObj is null, get the object from the beans location (first occurrence)
				Object newObj = null;
				if (aCurrObj == null)
				{
					newObj = JavaBeanManager.getInstance().getJavaBeanObjectByName(aMVCContext, objectName);
					
				}
				else
				{
					String getterMethodName = "get" + objectName.substring(0,1).toUpperCase() + objectName.substring(1);
					Method getterMethod = aCurrObj.getClass().getMethod(getterMethodName, (Class[])null);
					newObj = getterMethod.invoke(aCurrObj, (Object[])null);
					
					if (newObj == null)
					{
						//it means the field is an internal complex type and it is not instantiated
						newObj = instantiateNewObjectOnCurrentObject(objectName, getterMethod, aCurrObj);
//						Object internalComplexTypeInstance = JavaBeanManager.getInstance().instantiateClass(getterMethod.getReturnType().getName());
//						String complexTypeSetterMethodName = "set" + objectName.substring(0,1).toUpperCase() + objectName.substring(1);
//						Method complexTypeSetterMethod = aCurrObj.getClass().getMethod(complexTypeSetterMethodName, new Class[]{getterMethod.getReturnType()});
//						if (complexTypeSetterMethod == null)
//						{
//							throw new JavaBeanPopulationException("no setter method found for " + objectName, MVCExceptionCodes.MVC_NO_SETTER_METHOD_FOUND_FOR_ONE_OF_JAVABEAN_FIELDS);
//						}
//						complexTypeSetterMethod.invoke(aCurrObj, internalComplexTypeInstance);
//						newObj = internalComplexTypeInstance;
					}
				}
				
				//recursive call
				setJavaBeanField(aMVCContext, newObj, restOfName, aFieldValue);
				
			}
			else if (partsOfName.length == 2)
			{
				objectName = partsOfName[0];
				fieldName = partsOfName[1];
				
				//if aCurrObj is null, get the object from the beans location (first occurrence)
				Object newObj = null;
				if (aCurrObj == null)
				{
					newObj = JavaBeanManager.getInstance().getJavaBeanObjectByName(aMVCContext, objectName);
					
				}
				else
				{
					String getterMethodName = "get" + objectName.substring(0,1).toUpperCase() + objectName.substring(1);
					Method getterMethod = aCurrObj.getClass().getMethod(getterMethodName, (Class[])null);
					newObj = getterMethod.invoke(aCurrObj, (Object[])null);
					
					if (newObj == null)
					{
						//it means the field is an internal complex type and it is not instantiated
						newObj = instantiateNewObjectOnCurrentObject(objectName, getterMethod, aCurrObj);
//						Object internalComplexTypeInstance = JavaBeanManager.getInstance().instantiateClass(getterMethod.getReturnType().getName());
//						String complexTypeSetterMethodName = "set" + objectName.substring(0,1).toUpperCase() + objectName.substring(1);
//						Method complexTypeSetterMethod = aCurrObj.getClass().getMethod(complexTypeSetterMethodName, new Class[]{getterMethod.getReturnType()});
//						if (complexTypeSetterMethod == null)
//						{
//							throw new JavaBeanPopulationException("no setter method found for " + objectName, MVCExceptionCodes.MVC_NO_SETTER_METHOD_FOUND_FOR_ONE_OF_JAVABEAN_FIELDS);
//						}
//						complexTypeSetterMethod.invoke(aCurrObj, internalComplexTypeInstance);
//						newObj = internalComplexTypeInstance;
					}
				}
				
				String setterMethodName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
				Method setterMethod = null;
				Method[] newObjMethods = newObj.getClass().getMethods();
				for (int i=0 ; i<newObjMethods.length ; ++i)
				{
					if (newObjMethods[i].getName().equals(setterMethodName))
					{
						setterMethod = newObjMethods[i];
						break;
					}
				}
				
				if (setterMethod == null)
				{
					throw new JavaBeanPopulationException("no setter method found for " + fieldName, MVCExceptionCodes.MVC_NO_SETTER_METHOD_FOUND_FOR_ONE_OF_JAVABEAN_FIELDS);
				}
				
				Class[] parameterTypes = setterMethod.getParameterTypes();
				Object parameterizedValue = null;
				if (parameterTypes.length != 1)
				{
					throw new JavaBeanPopulationException("setter method should have exactly one argument: " + fieldName, MVCExceptionCodes.MVC_SETTER_METHOD_FOR_ONE_OF_JAVABEAN_FIELDS_SHOULD_HAVE_ONLY_ONE_ARGUMENT);
				}
				else
				{
					parameterizedValue = convertStringValueToProperParameterType(parameterTypes[0], aFieldValue);
				}
				
				setterMethod.invoke(newObj, new Object[]{parameterizedValue});
				
	
			}
			else
			{
				throw new JavaBeanPopulationException("bean name is not well defined: " + fieldName, MVCExceptionCodes.MVC_USED_JAVABEAN_NAME_IS_NOT_WELL_FORMED);
			}
		}
		catch(NoSuchMethodException nsme)
		{
			throw new JavaBeanPopulationException("no getter method found for " + fieldName, MVCExceptionCodes.MVC_NO_GETTER_METHOD_FOUND_FOR_ONE_OF_JAVABEAN_FIELDS);
		}
		catch (IllegalArgumentException iarge)
		{
			throw new JavaBeanPopulationException("getter or setter methods are not accessible for " + fieldName, MVCExceptionCodes.MVC_GETTER_OR_SETTER_METHOD_FOR_ONE_OF_JAVABEAN_FIELDS_IS_NOT_ACCESSIBLE);
		}
		catch (IllegalAccessException iacse)
		{
			throw new JavaBeanPopulationException("getter or setter methods are not accessible for " + fieldName, MVCExceptionCodes.MVC_GETTER_OR_SETTER_METHOD_FOR_ONE_OF_JAVABEAN_FIELDS_IS_NOT_ACCESSIBLE);
		}
		catch (InvocationTargetException ite)
		{
			throw new JavaBeanPopulationException("getter or setter method for " + fieldName + " threw an exception: " + ite.getCause(), MVCExceptionCodes.MVC_GETTER_OR_SETTER_METHOD_FOR_ONE_OF_JAVABEAN_FIELDS_INVOCATION_EXCEPTION);
		}
		catch (ParameterTypeConversionException ptce)
		{
			throw new ParameterTypeConversionException("could not convert the type correctly. Error is: " + ptce.getMessage() + " for " + fieldName, MVCExceptionCodes.MVC_JAVABEAN_POPULATION_CANT_CONVERT_STRING_TO_NUMBER);
		}
	}
	
	
	
	/*
	 * instantiates a new object of the given object name, onto the given current object, with the help
	 * of the given getter method of the new object from the current object 
	 */
	static protected Object instantiateNewObjectOnCurrentObject(String aObjectName, Method aGetterMethod, Object aCurrObj)
		throws JavaBeanPopulationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		Object internalComplexTypeInstance = JavaBeanManager.getInstance().instantiateClass(aGetterMethod.getReturnType().getName());
		String complexTypeSetterMethodName = "set" + aObjectName.substring(0,1).toUpperCase() + aObjectName.substring(1);
		Method complexTypeSetterMethod = aCurrObj.getClass().getMethod(complexTypeSetterMethodName, new Class[]{aGetterMethod.getReturnType()});
		if (complexTypeSetterMethod == null)
		{
			throw new JavaBeanPopulationException("no setter method found for " + aObjectName, MVCExceptionCodes.MVC_NO_SETTER_METHOD_FOUND_FOR_ONE_OF_JAVABEAN_FIELDS);
		}
		complexTypeSetterMethod.invoke(aCurrObj, internalComplexTypeInstance);
		Object newObj = internalComplexTypeInstance;
		
		return newObj;
	}
	
	
	
	
//	/**
//	 * returns a javaBean instance according to its given name.
//	 * all the javaBeans information is held by the JavaBeanManager singleton. this method
//	 * first gets the javaBean descriptor from the JavaBeanManager, and then tries to see if
//	 * it is already hung on the proper scoped object (application, session, request). if
//	 * it does not exist, it tries to instantiate a new instance of this javaBean, using the
//	 * fully qualified class name retrieved from the javaBean descriptor.
//	 * if the instantiation goes successful, it hangs the javaBean on the proper scoped object
//	 * and returns this instance to the caller.
//	 *    
//	 * @param aMVCContext the MVCContext of the current request
//	 * @param aJavaBeanName the name of the javaBean to be retrieved
//	 * 
//	 * @return the javaBean instance from the proper scoped object
//	 * 
//	 * @throws JavaBeanInstantiationException when any instantiation problem occurs
//	 */
//	protected static Object getJavaBeanObjectByName(MVCContext aMVCContext, String aJavaBeanName)
//		throws JavaBeanInstantiationException
//	{
//		
//		JavaBeanDescriptor descriptor = JavaBeanManager.getInstance().getJavaBeanDescriptor(aJavaBeanName);
//		Object javaBeanObj = null;
//		
//		if (descriptor.getScope().equals(JavaBeanScope.application))
//		{
//			javaBeanObj = aMVCContext.getOriginalRequest().getSession(true).getServletContext().getAttribute(aJavaBeanName);
//			if (javaBeanObj == null)
//			{
//				//the javaBean has to be created and hanged
//				javaBeanObj = instantiateClass(descriptor.getFQNClassName());
//				aMVCContext.getOriginalRequest().getSession(true).getServletContext().setAttribute(aJavaBeanName, javaBeanObj);
//			}
//		}
//		else if (descriptor.getScope().equals(JavaBeanScope.session))
//		{
//			javaBeanObj = aMVCContext.getOriginalRequest().getSession(true).getAttribute(aJavaBeanName);
//			if (javaBeanObj == null)
//			{
//				//the javaBean has to be created and hanged
//				javaBeanObj = instantiateClass(descriptor.getFQNClassName());
//				aMVCContext.getOriginalRequest().getSession(true).setAttribute(aJavaBeanName, javaBeanObj);
//			}
//		}
//		else if (descriptor.getScope().equals(JavaBeanScope.request))
//		{
//			javaBeanObj = aMVCContext.getOriginalRequest().getAttribute(aJavaBeanName);
//			if (javaBeanObj == null)
//			{
//				//the javaBean has to be created and hanged
//				javaBeanObj = instantiateClass(descriptor.getFQNClassName());
//				aMVCContext.getOriginalRequest().setAttribute(aJavaBeanName, javaBeanObj);
//			}
//		}
//				
//		return javaBeanObj;
//	}
	
	
//	/**
//	 * instantiates an object given its fully qualified class name
//	 * 
//	 * @param aFQNClassName the FQN Class name
//	 * 
//	 * @return the instantiated Object
//	 * 
//	 * @throws JavaBeanInstantiationException when the instantiation fails for any reason
//	 */
//	protected static Object instantiateClass(String aFQNClassName)
//		throws JavaBeanInstantiationException
//	{
//		Object newObj = null;
//		
//		try
//		{
//			Class myclass = Class.forName(aFQNClassName);
//			newObj = myclass.newInstance();
//			
//			return newObj;
//		}
//		catch(ClassNotFoundException cnfe)
//		{
//			throw new JavaBeanInstantiationException(cnfe.toString(), 7000);
//		}
//		catch (InstantiationException ie)
//		{
//			throw new JavaBeanInstantiationException(ie.toString(), 7000);
//		}
//		catch (IllegalAccessException iae)
//		{
//			throw new JavaBeanInstantiationException(iae.toString(), 7000);
//		}
//	}
	
	
	
	/**
	 * converts the given String typed value into the given type
	 * 
	 * @param aParameterType the type to convert to
	 * @param aFieldValue the String value to convert
	 * 
	 * @return the converted String (according to conversion type)
	 * 
	 * @throws ParameterTypeConversionException when the conversion fails for any reason
	 */
	protected static Object convertStringValueToProperParameterType(Class aParameterType, Object aFieldValue)
		throws ParameterTypeConversionException
	{
		Object returnedValue = aFieldValue;
		String[] valuesArr = (String[])aFieldValue;
		String firstValue = valuesArr[0];
		int radix = DEC_RADIX;
		boolean isHex = false;
		boolean isColor = false;
		
		//save the original value before making any trim to it.
		//this is useful for the case where we want to relate to this value as a plain 
		//text (where leading and trailing spaces are allowed and should not be trimmed)
		String originalFirstValue = firstValue;
		
		//trim the value if it contains spaces
		if (firstValue != null)
		{
			firstValue = firstValue.trim();
		}
		
		//check if the value is in hexadecimal format
		if (firstValue.startsWith("0x"))
		{
			isHex = true;
			radix = HEX_RADIX;
			firstValue = firstValue.substring(2, firstValue.length());
		}
		
		//TODO: this is a VERY UGLY PATCH to convert string values that represent html color, into int color
		//should remove this after there is a Color object that has both representations
		//check if the value might represent an HTML color in hexadecimal format
		if (firstValue.startsWith("#") && firstValue.length() == 7)
		{
			isColor = true;
			radix = DEC_RADIX;
			firstValue = String.valueOf(ColorUtils.fromHtmlHexColor(firstValue));
		}
		
		
		try
		{	if (!aParameterType.isArray())
			{
				if (aParameterType == String.class)
				{	
					if (!isHex && !isColor)
					{
						//this is not hex or color so use the original value (not trimmed)
						returnedValue = originalFirstValue;
					}
					else
					{
						returnedValue = firstValue;
					}
				}
				if (aParameterType == Byte.class || aParameterType == byte.class)
				{
					returnedValue = Byte.parseByte(firstValue, radix);
				}
				else if (aParameterType == Short.class || aParameterType == short.class)
				{		
					returnedValue = Short.parseShort(firstValue, radix);
				}
				else if (aParameterType == Integer.class || aParameterType == int.class)
				{
					returnedValue = Integer.parseInt(firstValue, radix);
				}
				else if (aParameterType == Long.class || aParameterType == long.class)
				{
					returnedValue = Long.parseLong(firstValue, radix);
				}
				else if (aParameterType == Float.class || aParameterType == float.class)
				{
					returnedValue = Float.parseFloat(firstValue);
				}
				else if (aParameterType == Double.class || aParameterType == double.class)
				{
					returnedValue = Double.parseDouble(firstValue);
				}
				else if (aParameterType == Boolean.class || aParameterType == boolean.class)
				{
					//checkboxes are regularly sent as "on" when they are checked.
					//the "true" ensures that the parseBoolean will convert it to boolean true
					if ("on".equals(firstValue))
					{
						//old line. not sure why it was done...
						//aFieldValue = "true";
						firstValue = "true";
						
					}
					returnedValue = Boolean.parseBoolean(firstValue);
				}
				else if (aParameterType == Character.class || aParameterType == char.class)
				{
					if (firstValue.length() == 1)
					{
						returnedValue = firstValue.charAt(0);
					}
					else
					{
						throw new ParameterTypeConversionException("cannot convert empty string to char", MVCExceptionCodes.MVC_JAVABEAN_POPULATION_CANT_CONVERT_EMPTY_STRING_TO_CHAR);
					}
				}
			}
			else
			{
				// **************** here starts the array type conversions ********************
				
				//first, trim all the values of the array
				for (int i=0 ; i<valuesArr.length ; ++i)
				{
					if (valuesArr[i] != null)
					{
						valuesArr[i].trim();
					}
				}
				
				//now convert to the correct array type
				if (aParameterType == byte[].class || aParameterType == Byte[].class)
				{
					byte[] convertedValues = new byte[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						convertedValues[i] = Byte.parseByte(valuesArr[i], radix);
					}
					returnedValue = convertedValues;
				}
				else if (aParameterType == short[].class || aParameterType == Short[].class)
				{
					short[] convertedValues = new short[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						convertedValues[i] = Short.parseShort(valuesArr[i], radix);
					}
					returnedValue = convertedValues;
				}
				else if (aParameterType == int[].class || aParameterType == Integer[].class)
				{
					int[] convertedValues = new int[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						convertedValues[i] = Integer.parseInt(valuesArr[i], radix);
					}
					returnedValue = convertedValues;
				}
				else if (aParameterType == long[].class || aParameterType == Long[].class)
				{
					long[] convertedValues = new long[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						convertedValues[i] = Long.parseLong(valuesArr[i], radix);
					}
					returnedValue = convertedValues;
				}
				else if (aParameterType == float[].class || aParameterType == Float[].class)
				{
					float[] convertedValues = new float[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						convertedValues[i] = Float.parseFloat(valuesArr[i]);
					}
					returnedValue = convertedValues;
				}
				else if (aParameterType == double[].class || aParameterType == Double[].class)
				{
					double[] convertedValues = new double[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						convertedValues[i] = Double.parseDouble(valuesArr[i]);
					}
					returnedValue = convertedValues;
				}
				else if (aParameterType == boolean[].class || aParameterType == Boolean[].class)
				{
					boolean[] convertedValues = new boolean[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						convertedValues[i] = Boolean.parseBoolean(valuesArr[i]);
					}
					returnedValue = convertedValues;
				}
				else if (aParameterType == char[].class || aParameterType == Character[].class)
				{
					char[] convertedValues = new char[valuesArr.length];
					for (int i=0 ; i<valuesArr.length ; ++i)
					{
						if (valuesArr[i].length() == 1)
						{
							convertedValues[i] = valuesArr[i].charAt(0);
						}
						else
						{
							throw new ParameterTypeConversionException("cannot convert empty string to char: " + valuesArr[i], MVCExceptionCodes.MVC_JAVABEAN_POPULATION_CANT_CONVERT_EMPTY_STRING_TO_CHAR);
						}
					}
					returnedValue = convertedValues;
				}
			}
		}
		catch(NumberFormatException nfe)
		{
			throw new ParameterTypeConversionException("cannot convert parameter value: " + firstValue + " to " + aParameterType.getSimpleName() + " : " + nfe.getMessage(), 6006);
		}
		
		return returnedValue;
	
	}
}
