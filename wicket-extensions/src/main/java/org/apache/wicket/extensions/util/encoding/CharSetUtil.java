/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.util.encoding;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class. Initialize Locale to character encoding mapping based on defaults and optionally a
 * properties file CharSetMap.CHARSET_RESOURCE to be found in WEB-INF.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class CharSetUtil
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(CharSetUtil.class);

	/** Locale to character encoding mapping */
	private static CharSetMap charSetMap;

	/**
	 * Constructor
	 */
	public CharSetUtil()
	{
	}

	/**
	 * Initialize the mapping table based on defaults and optionally modified by entries from a
	 * properties file to be found in WEB-INF.
	 * 
	 * @param application
	 *            Wicket application object
	 */
	private synchronized static void initialize(final WebApplication application)
	{
		if (charSetMap == null)
		{
			// Get servlet context
			final ServletContext context = application.getServletContext();

			final InputStream inputStream = context.getResourceAsStream("/WEB-INF/" +
				CharSetMap.CHARSET_RESOURCE);

			if (inputStream == null)
			{
				charSetMap = new CharSetMap();
				if (log.isDebugEnabled())
				{
					log.debug("File '" + CharSetMap.CHARSET_RESOURCE + "' not found");
				}
			}
			else
			{
				try
				{
					charSetMap = new CharSetMap(inputStream);
				}
				catch (IOException ex)
				{
					throw new WicketRuntimeException("Error while reading CharSetMap", ex);
				}
			}
		}
	}

	/**
	 * Based on the Session's locale determine the associated character encoding.
	 * 
	 * @param cycle
	 * @return Char set to use for response.
	 */
	public static String getEncoding(final RequestCycle cycle)
	{
		if (charSetMap == null)
		{
			initialize((WebApplication)Application.get());
		}

		return charSetMap.getCharSet(Session.get().getLocale());
	}
}
