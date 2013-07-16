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
package org.apache.wicket.cdi;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.servlet.FilterConfig;

import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebApplicationFactory designed for CDI Injection.
 * <p/>
 * This class can be added to the default WicketFilter by setting the init-param
 * 'applicationFactoryClassName' with this classes fully qualified path. In this
 * case the applicationClassName init-param is required. The CdiConfiguration is
 * automatically configured and the Application class is postConstructed, unless
 * overriding with the enableApplicationInjection init-param.
 * <p/>
 * This class is also used by CdiWicketFilter. When using CdiWicketFilter you do
 * not need to use the applicationClassName init-param. CdiWicketFilter uses the
 * Injected CdiWebApplicationFactory. When createApplication is called a
 * WebApplication Instance iterator is searched to find the proper
 * WebApplication. Search works by first looking up the init-param
 * applicationName. If this is found then the WebApplication annotated with
 * {@link WicketApp}('someApp') is selected. This allows for multiple
 * WebApplications to exist in the classloader. If multiple WebApplication are
 * in the classloader then the use of {@link WicketApp} is required. If the
 * init-param applicationName is not set then the classloader is searched to
 * verify there is only one WebApplication. If there is only one WebApplication
 * then it is used. If there are no WebApplications or multiple Applications
 * without applicationName being set then an exception will be thrown. When
 * using CdiWicketFilter the Application is always Injected. When using this in
 * a EE container like glassfish the {@link WicketApp} is required to let EE
 * container manage the object.
 * 
 * @author jsarman
 */
@ApplicationScoped
public class CdiWebApplicationFactory extends ContextParamWebApplicationFactory
{

	private final static Logger log = LoggerFactory.getLogger(CdiWebApplicationFactory.class);
	static final String WICKET_APP_NAME = "applicationName";
	static final String INJECT_APP = "enableApplicationInjection";
	static final String INJECT_COMPONENT = "enableComponentInjection";
	static final String INJECT_SESSION = "enableSessionInjection";
	static final String INJECT_BEHAVIOR = "enableBehaviorInjecion";
	static final String AUTO_CONVERSATION = "enableAutoConversationManagement";
	static final String PROPAGATION = "initialConversationPropagation";
	@Inject
	@Any
	Instance<WebApplication> applications;
	private boolean overrideApplicationInjection;

	public CdiWebApplicationFactory()
	{
	}

	@Override
	public WebApplication createApplication(WicketFilter filter)
	{

		WebApplication application;
		if (applications == null)
		{
			application = super.createApplication(filter);
		}
		else
		{
			String appName = filter.getFilterConfig().getInitParameter(WICKET_APP_NAME);
			if (appName != null)
			{
				try
				{
					ApplicationQualifier qualifier = new ApplicationQualifier(appName);
					application = applications.select(qualifier).get();
					log.info("Found WicketApp('{}') annotated WebApplication: {} ", appName,
							application.getClass());
				}
				catch (IllegalArgumentException iae)
				{
					log.error(
							"The init param {} set to {} is either has no @Named parameter or duplicates.",
							WICKET_APP_NAME, appName);
					throw iae;
				}
				catch (UnsatisfiedResolutionException ure)
				{
					log.error(
							"The init param {} set to {} requires a WebApplication to be annotated with @Named(\"{}\").",
							new Object[] { WICKET_APP_NAME, appName, appName });
					throw ure;
				}
			}
			else
			{
				Iterator<WebApplication> possibleApps = applications.iterator();
				try
				{
					application = possibleApps.next();
				}
				catch (NoSuchElementException nsee)
				{
					log.error("The classLoader does not contain any WebApplications. Please create a WebApplication.");
					throw new RuntimeException("Missing WebApplication");
				}
				if (possibleApps.hasNext())
				{
					log.error(
							"Multiple WebApplications are in the classloader. This requires using @Named parameter on WebApplication"
									+ " and setting the init-param {} with the matching name in web.xml",
							WICKET_APP_NAME);
					throw new IllegalArgumentException("Missing init-param " + WICKET_APP_NAME
							+ " to match against multiple WebApplications in classLoader. ");
				}
				log.info("Found Single WebApplication: {}", application.getClass());
			}
			overrideApplicationInjection = true; // Already injected so don't
													// let CdiConfiguration
													// reinject it.
		}

		ConfigurationParameters parameters = buildParameters(filter.getFilterConfig());
		CdiConfiguration.get().configure(filter.getFilterConfig().getFilterName(), application,
				parameters);
		return application;
	}

	private ConfigurationParameters buildParameters(FilterConfig filterConfig)
	{
		ConfigurationParameters parameters = new ConfigurationParameters();
		if (!overrideApplicationInjection)
		{
			final String injectApp = filterConfig.getInitParameter(INJECT_APP);

			if (injectApp != null)
			{
				parameters.setInjectApplication(Boolean.parseBoolean(injectApp));
			}
		}
		else
		{
			parameters.setInjectApplication(false);
		}
		final String injectComponent = filterConfig.getInitParameter(INJECT_COMPONENT);
		if (injectComponent != null)
		{
			parameters.setInjectComponents(Boolean.parseBoolean(injectComponent));
		}
		final String injectSession = filterConfig.getInitParameter(INJECT_SESSION);
		if (injectSession != null)
		{
			parameters.setInjectSession(Boolean.parseBoolean(injectSession));
		}
		final String injectBehavior = filterConfig.getInitParameter(INJECT_BEHAVIOR);
		if (injectBehavior != null)
		{
			parameters.setInjectBehaviors(Boolean.parseBoolean(injectBehavior));
		}
		final String autoConversation = filterConfig.getInitParameter(AUTO_CONVERSATION);
		if (autoConversation != null)
		{
			parameters.setAutoConversationManagement(Boolean.parseBoolean(autoConversation));
		}
		final String propagation = filterConfig.getInitParameter(PROPAGATION);
		if (propagation != null)
		{
			try
			{
				parameters.setPropagation(ConversationPropagation.valueOf(propagation));
			}
			catch (IllegalArgumentException iae)
			{
				log.warn("Init Param {} = {} is not a valid propagation type. Using Default {}",
						new Object[] { PROPAGATION, propagation,
								parameters.getPropagation().toString() });
			}
		}

		return parameters;
	}
}
