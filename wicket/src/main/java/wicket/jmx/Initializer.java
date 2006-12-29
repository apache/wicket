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
package wicket.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.IDestroyer;
import wicket.IInitializer;
import wicket.WicketRuntimeException;

/**
 * Registers Wicket's MBeans.
 * 
 * @author eelcohillenius
 */
public class Initializer implements IInitializer, IDestroyer
{
	private static Logger log = LoggerFactory.getLogger(Initializer.class);

	/**
	 * List of registered names
	 */
	private List<ObjectName> registered = new ArrayList<ObjectName>();

	/**
	 * @see wicket.IDestroyer#destroy(wicket.Application)
	 */
	public void destroy(wicket.Application application)
	{
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		for (ObjectName objectName : registered)
		{
			try
			{
				mbs.unregisterMBean(objectName);
			}
			catch (InstanceNotFoundException e)
			{
				log.error(e.getMessage(), e);
			}
			catch (MBeanRegistrationException e)
			{
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	public void init(wicket.Application application)
	{
		try
		{
			String name = application.getName();
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

			// register top level application object, but first check whether
			// multiple instances of the same application (name) are running and
			// if so adjust the name
			String domain = "wicket.app." + name;
			ObjectName appBeanName = new ObjectName(domain + ":type=Application");
			String tempDomain = domain;
			int i = 0;
			while (mbs.isRegistered(appBeanName))
			{
				tempDomain = name + "-" + i++;
				appBeanName = new ObjectName(tempDomain + ":type=Application");
			}
			domain = tempDomain;

			Application appBean = new Application(application);
			register(mbs, appBean, appBeanName);

			register(mbs, new ApplicationSettings(application), new ObjectName(domain
					+ ":type=Application,name=ApplicationSettings"));
			register(mbs, new DebugSettings(application), new ObjectName(domain
					+ ":type=Application,name=DebugSettings"));
			register(mbs, new MarkupSettings(application), new ObjectName(domain
					+ ":type=Application,name=MarkupSettings"));
			register(mbs, new ResourceSettings(application), new ObjectName(domain
					+ ":type=Application,name=ResourceSettings"));
			register(mbs, new PageSettings(application), new ObjectName(domain
					+ ":type=Application,name=PageSettings"));
			register(mbs, new RequestCycleSettings(application), new ObjectName(domain
					+ ":type=Application,name=RequestCycleSettings"));
			register(mbs, new SecuritySettings(application), new ObjectName(domain
					+ ":type=Application,name=SecuritySettings"));
			register(mbs, new SessionSettings(application), new ObjectName(domain
					+ ":type=Application,name=SessionSettings"));
			register(mbs, new CookieValuePersisterSettings(application), new ObjectName(domain
					+ ":type=Application,name=CookieValuePersisterSettings"));

			RequestLogger sessionsBean = new RequestLogger(application);
			ObjectName sessionsBeanName = new ObjectName(domain + ":type=RequestLogger");
			register(mbs, sessionsBean, sessionsBeanName);
		}
		catch (MalformedObjectNameException e)
		{
			throw new WicketRuntimeException(e);
		}
		catch (InstanceAlreadyExistsException e)
		{
			throw new WicketRuntimeException(e);
		}
		catch (MBeanRegistrationException e)
		{
			throw new WicketRuntimeException(e);
		}
		catch (NotCompliantMBeanException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Wicket JMX initializer";
	}

	/**
	 * Register MBean.
	 * 
	 * @param mbs
	 *            server
	 * @param o
	 *            MBean
	 * @param objectName
	 *            Object name
	 * @throws NotCompliantMBeanException
	 * @throws MBeanRegistrationException
	 * @throws InstanceAlreadyExistsException
	 */
	private void register(MBeanServer mbs, Object o, ObjectName objectName)
			throws InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException
	{
		mbs.registerMBean(o, objectName);
		registered.add(objectName);
	}
}
