package com.tinyj.mvc.helpers.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookiesHelper 
{

	public static String getCookieValue(HttpServletRequest request, String name) 
	{
	    Cookie[] cookies = request.getCookies();
	    if (cookies != null) 
	    {
	        for (Cookie cookie : cookies) 
	        {
	            if (name.equals(cookie.getName())) 
	            {
	                return cookie.getValue();
	            }
	        }
	    }
	    return null;
	}

	
	/**
	 * adds a cookie
	 * 
	 * @param response
	 * @param name
	 * @param value
	 * @param maxAge age in seconds. for ex: 2592000 is 30 days
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) 
	{
	    Cookie cookie = new Cookie(name, value);
	    cookie.setMaxAge(maxAge);
	    response.addCookie(cookie);
	}

	
	
	
	public static void removeCookie(HttpServletResponse response, String name) 
	{
	    addCookie(response, name, null, 0);
	}
}
