/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ====================================================================
 * Copyright (c) 2003, Open Edge B.V. All rights reserved. Redistribution and
 * use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of OpenEdge B.V. nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package nl.openedge.util.jetty;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

/**
 * Helper class for interpreting url locations.
 * 
 * @author Eelco Hillenius
 */
public final class URLHelper
{

	/**
	 * Hidden constructor (utility class).
	 */
	private URLHelper()
	{
		// no nada
	}

	/**
	 * Interprets some absolute URLs as external paths or from classpath.
	 * 
	 * @param path
	 *            path to translate
	 * @param caller
	 *            caller class of method
	 * @return URL the converted URL
	 * @throws MalformedURLException
	 *             when the path does not follow the URL rules
	 */
	public static URL convertToURL(String path, Class caller) throws MalformedURLException
	{
		return convertToURL(path, caller, null);
	}

	/**
	 * Interprets some absolute URLs as external paths, otherwise generates URL
	 * appropriate for loading from internal webapp or, servletContext is null,
	 * loading from the classpath.
	 * 
	 * @param pathToTranslate
	 *            path to translate
	 * @param caller
	 *            caller of method
	 * @param servletContext
	 *            servlet context of webapp
	 * @throws MalformedURLException
	 *             when the path does not follow the URL rules
	 * @return the converted URL
	 */
	public static URL convertToURL(String pathToTranslate, Class caller,
			ServletContext servletContext) throws MalformedURLException
	{
		String path = pathToTranslate;
		URL url = null;
		if (path.startsWith("file:") || path.startsWith("http:") || path.startsWith("https:")
				|| path.startsWith("ftp:") || path.startsWith("jar:"))
		{
			url = new URL(path);
		}
		else if (servletContext != null)
		{
			url = getServletContextURL(servletContext, path);
		}
		else
		{
			url = getClasspathURL(caller, path);
		}
		return url;
	}

	/**
	 * @param servletContext
	 *            the servlet context
	 * @param pathToTranslate
	 *            the path to translate
	 * @return URL the URL as a resource from the servlet context
	 * @throws MalformedURLException
	 *             when the path does not follow the URL rules
	 */
	private static URL getServletContextURL(ServletContext servletContext, String pathToTranslate)
			throws MalformedURLException
	{
		String path = pathToTranslate;
		URL url;
		// Quick sanity check
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		url = servletContext.getResource(path);
		return url;
	}

	/**
	 * @param caller
	 *            class of the caller
	 * @param path
	 *            the path to get the URL for
	 * @return the URL as a resource from the classpath
	 */
	private static URL getClasspathURL(Class caller, String path)
	{
		URL url;
		ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
		if (clsLoader == null)
		{
			if (caller != null)
			{
				url = caller.getResource(path);
			}
			else
			{
				url = ClassLoader.getSystemResource(path);
			}
		}
		else
		{
			url = clsLoader.getResource(path);
			// fallthrough
			if (url == null)
			{
				if (caller != null)
				{
					url = caller.getResource(path);
				}
				else
				{
					url = ClassLoader.getSystemResource(path);
				}
			}
		}
		return url;
	}
}