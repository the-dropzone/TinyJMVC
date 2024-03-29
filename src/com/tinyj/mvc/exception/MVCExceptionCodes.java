package com.tinyj.mvc.exception;


/**
 * Infra exception codes are all numbers in the range 1000 -> 9999 (including)
 * All numbers under 1000 are saved
 * 
 * @author asaf.peeri
 *
 */
public class MVCExceptionCodes
{
	public static int MVC_CONFIGURATION_PARSE_ERROR = 1000;
	public static int MVC_CANT_RESOLVE_ACTION_NAME_FROM_REQUEST = 1001;
	public static int MVC_CANT_FIND_ACTION_PATH = 1002;
	public static int MVC_CANT_RESOLVE_ACTION_RESPONSE_TO_DEFINED_FORWARD_NAME = 1003;
	public static int MVC_FORWARD_PATH_FOR_ACTION_CANT_BE_NULL_OR_EMPTY = 1004;
	public static int MVC_COULD_NOT_FORWARD_REQUEST_DUE_TO_IO_ERROR = 1005;
	public static int MVC_SERVLET_CONFIG_OR_SERVLET_CONTEXT_ARE_NOT_INITIALIZED = 1006;
	public static int MVC_CANT_READ_FROM_MVC_CONFIG_FILE_DUE_TO_IO_ERROR = 1007;
	public static int MVC_ACTION_CANT_BE_NULL_EMPTY_OR_SLASH_ONLY = 1008;
	public static int MVC_ACTION_DOES_NOT_EXIST = 1009;
	public static int MVC_ACTION_IS_NOT_INSTANCE_OF_MVC_ACTION_CLASS = 1010;
	public static int MVC_ACTION_TYPE_CLASS_CANT_BE_FOUND = 1011;
	public static int MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_ACCESSED = 1012;
	public static int MVC_ACTION_PROPER_CONSTRUCTOR_CANT_BE_FOUND = 1013;
	public static int MVC_ACTION_CANT_BE_INSTANTIATED = 1014;
	public static int MVC_ACTION_CONSTRUCTOR_INVOCATION_EXCEPTION = 1015;
	public static int MVC_CONFIGURATION_FILE_CANT_BE_FOUND = 1032;
	
	public static int MVC_CANT_FIND_JAVABEAN_BY_GIVEN_NAME = 1016;
	public static int MVC_JAVABEAN_TYPE_CLASS_CANT_BE_FOUND = 1017;
	public static int MVC_JAVABEAN_CANT_BE_INSTANTIATED = 1018;
	public static int MVC_JAVABEAN_PROPER_CONSTRUCTOR_CANT_BE_ACCESSED= 1019;
	public static int MVC_JAVABEAN_IS_NOT_DEFINED = 1020;
	public static int MVC_JAVABEAN_DEFINED_TYPE_AND_ACTUAL_TYPE_DOES_NOT_MATCH = 1021;
	public static int MVC_USED_JAVABEAN_NAME_IS_NOT_WELL_FORMED = 1022;
	public static int MVC_NO_SETTER_METHOD_FOUND_FOR_ONE_OF_JAVABEAN_FIELDS = 1023;
	public static int MVC_NO_GETTER_METHOD_FOUND_FOR_ONE_OF_JAVABEAN_FIELDS = 1024;
	public static int MVC_SETTER_METHOD_FOR_ONE_OF_JAVABEAN_FIELDS_SHOULD_HAVE_ONLY_ONE_ARGUMENT = 1025;
	public static int MVC_GETTER_OR_SETTER_METHOD_FOR_ONE_OF_JAVABEAN_FIELDS_IS_NOT_ACCESSIBLE = 1026;
	public static int MVC_GETTER_OR_SETTER_METHOD_FOR_ONE_OF_JAVABEAN_FIELDS_INVOCATION_EXCEPTION = 1027;
	public static int MVC_JAVABEAN_POPULATION_CANT_CONVERT_EMPTY_STRING_TO_CHAR = 1028;
	public static int MVC_JAVABEAN_POPULATION_CANT_CONVERT_STRING_TO_NUMBER = 1029;
	
	public static int MVC_AJAX_CONFIGURATION_PARSE_ERROR = 1030;
	public static int MVC_CANT_READ_FROM_AJAX_CONFIG_FILE_DUE_TO_IO_ERROR = 1031;
	
	public static int MVC_ERROR_WHILE_ANALYZING_FORM_PARAMETERS = 1032;
	
	
	
}
