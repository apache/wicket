/*
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
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
 * <p>
 * Users can specify the MBeanServer implementation in which to register the
 * MBeans by setting the <code>wicket.mbean.server.impl</code> property to the
 * class name of the MBeanServer implementation they want. This is needed since
 * running an application server like JBoss with Java 1.5 causes more than one
 * MBeanServer to be created (the JVM one and the JBoss one). It is recognized
 * that there could possibly be more than one instance of an MBeanServer for a
 * given implementation class, but the assumption is that such a situation is
 * rare. In the event that such a situation is encountered, the first instance
 * encountered in the list returned by
 * <code>MBeanServerFactory.findMBeanServer(null)</code> is used. This
 * algorithm should handle most situations. Here is a list of known MBeanServer
 * implementation class names:
 * <ul>
 * <li>Java 1.5 JVM - com.sun.jmx.mbeanserver.JmxMBeanServer</li>
 * <li>JBoss - org.jboss.mx.server.MBeanServerImpl</li>
 * </ul>
 * </p>
 * 
 * @author eelcohillenius
 * @author David Hosier
 */
public class Initializer implements IInitializer, IDestroyer
{
	private static Logger log = LoggerFactory.getLogger(Initializer.class);

	// It's best to store a reference to the MBeanServer rather than getting it
	// over and over
	private MBeanServer mbeanServer = null;

	/**
	 * List of registered names
	 */
	private List<ObjectName> registered = new ArrayList<ObjectName>();

	/**
	 * @see wicket.IDestroyer#destroy(wicket.Application)
	 */
	public void destroy(wicket.Application application)
	{
		if (mbeanServer != null)
		{
			for (ObjectName objectName : registered)
			{
				try
				{
					mbeanServer.unregisterMBean(objectName);
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
	}

	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	@SuppressWarnings("unchecked")
	public void init(wicket.Application application)
	{
		/*
		 * This method uses the wicket.mbean.server.impl property to know which
		 * MBeanServer to get, but it could just as easily allow users to
		 * specify the MBeanServer per Application by changing the line:
		 * 
		 * String mbeanServerImplClass =
		 * System.getProperty("wicket.mbean.server.impl");
		 * 
		 * TO
		 * 
		 * String mbeanServerImplClass =
		 * System.getProperty(System.getProperty("wicket." + name +
		 * ".mbean.server.impl"), "wicket.mbean.server.impl");
		 * 
		 * That will allow users to specify a per application MBeanServer
		 * implemenation class. However, the global reference to the MBeanServer
		 * will have to be changed to maybe like a HashMap where the key is the
		 * Application name and the value is the reference to that Application's
		 * specified MBeanServer.
		 */

		try
		{
			String name = application.getName();
			ArrayList<MBeanServer> mbeanServers = (ArrayList<MBeanServer>)MBeanServerFactory
					.findMBeanServer(null);

			mbeanServer = mbeanServers.get(0); // set the MBeanServer to the
			// zero entry as a default
			String mbeanServerImplClass = System.getProperty("wicket.mbean.server.impl");
			if (mbeanServerImplClass != null)
			{
				for (MBeanServer mbs : mbeanServers)
				{
					if (mbs.getClass().getName().equals(mbeanServerImplClass))
					{
						mbeanServer = mbs;
						/*
						 * this will cause the first instance to be accepted in
						 * the case that there is more than one MBeanServer of
						 * the given implementation Class
						 */
						break;
					}
				}
			}

			// register top level application object, but first check whether
			// multiple instances of the same application (name) are running and
			// if so adjust the name
			String domain = "wicket.app." + name;
			ObjectName appBeanName = new ObjectName(domain + ":type=Application");
			String tempDomain = domain;
			int i = 0;
			while (mbeanServer.isRegistered(appBeanName))
			{
				tempDomain = name + "-" + i++;
				appBeanName = new ObjectName(tempDomain + ":type=Application");
			}
			domain = tempDomain;

			Application appBean = new Application(application);
			register(mbeanServer, appBean, appBeanName);

			register(mbeanServer, new ApplicationSettings(application), new ObjectName(domain
					+ ":type=Application,name=ApplicationSettings"));
			register(mbeanServer, new DebugSettings(application), new ObjectName(domain
					+ ":type=Application,name=DebugSettings"));
			register(mbeanServer, new MarkupSettings(application), new ObjectName(domain
					+ ":type=Application,name=MarkupSettings"));
			register(mbeanServer, new ResourceSettings(application), new ObjectName(domain
					+ ":type=Application,name=ResourceSettings"));
			register(mbeanServer, new PageSettings(application), new ObjectName(domain
					+ ":type=Application,name=PageSettings"));
			register(mbeanServer, new RequestCycleSettings(application), new ObjectName(domain
					+ ":type=Application,name=RequestCycleSettings"));
			register(mbeanServer, new SecuritySettings(application), new ObjectName(domain
					+ ":type=Application,name=SecuritySettings"));
			register(mbeanServer, new SessionSettings(application), new ObjectName(domain
					+ ":type=Application,name=SessionSettings"));
			register(mbeanServer, new CookieValuePersisterSettings(application), new ObjectName(
					domain + ":type=Application,name=CookieValuePersisterSettings"));

			RequestLogger sessionsBean = new RequestLogger(application);
			ObjectName sessionsBeanName = new ObjectName(domain + ":type=RequestLogger");
			register(mbeanServer, sessionsBean, sessionsBeanName);
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
	@Override
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
