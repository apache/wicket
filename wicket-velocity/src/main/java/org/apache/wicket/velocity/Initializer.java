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
package org.apache.wicket.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.velocity.app.Velocity;
import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.core.util.file.WebApplicationPath;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link org.apache.wicket.IInitializer} for the Velocity Runtime Singleton.
 * If Application is an instance of WebApplication, Initializer will retrieve
 * "velocityPropertiesFolder" as an initparam to point to the directory the properties file lives
 * in, and "velocity.properties" for the name of the properties file. If the params don't exist,
 * then velocity.properties next to this class will be loaded.
 * 
 */
public class Initializer implements IInitializer
{
	private static final Logger log = LoggerFactory.getLogger(Initializer.class);

	/**
	 * {@inheritDoc}
	 */
	public void init(final Application application)
	{
		Properties props = getVelocityProperties(application);

		try
		{
			if (null != props)
			{
				Velocity.init(props);
			}
			else
			{
				Velocity.init();
			}
			log.info("Initialized Velocity successfully");
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	private Properties getVelocityProperties(final Application application)
	{
		String velocityPropertiesFile = "velocity.properties";

		if (application instanceof WebApplication)
		{
			WebApplication webapp = (WebApplication)application;
			ServletContext servletContext = webapp.getServletContext();
			String propertiesFolder = servletContext.getInitParameter("velocityPropertiesFolder");
			String propsFile = servletContext.getInitParameter("velocity.properties");

			if (null != propsFile)
			{
				velocityPropertiesFile = propsFile;
			}

			if (null != propertiesFolder)
			{
				WebApplicationPath webPath = new WebApplicationPath(servletContext);
				webPath.add(propertiesFolder);
				IResourceStream stream = webPath.find(Initializer.class, velocityPropertiesFile);
				InputStream is = null;
				try
				{
					is = stream.getInputStream();
					Properties props = new Properties();
					props.load(is);
					return props;
				}
				catch (IOException e)
				{
					throw new WicketRuntimeException(e);
				}
				catch (ResourceStreamNotFoundException e)
				{
					throw new WicketRuntimeException(e);
				}
				finally
				{
					try
					{
						IOUtils.close(is);
					}
					catch (IOException e)
					{
						log.error(e.getMessage(), e);
					}
				}
			}
		}

		// if it's not a web app, load from the package
		InputStream is = Initializer.class.getResourceAsStream("velocity.properties");
		try
		{
			Properties props = new Properties();
			props.load(is);
			return props;
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}
		finally
		{
			try
			{
				IOUtils.close(is);
			}
			catch (IOException e)
			{
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy(final Application application)
	{
	}
}
