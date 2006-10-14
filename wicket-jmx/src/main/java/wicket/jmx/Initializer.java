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

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IInitializer;
import wicket.WicketRuntimeException;

/**
 * Registers Wicket's MBeans.
 * 
 * @author eelcohillenius
 */
public class Initializer implements IInitializer
{
	private static Log log = LogFactory.getLog(Initializer.class);

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
				tempDomain = name + "-" + i;
				appBeanName = new ObjectName(domain + ":type=Application");
			}
			name = tempDomain;

			Application appBean = new Application(application);
			mbs.registerMBean(appBean, appBeanName);

			mbs.registerMBean(new ApplicationSettings(application), new ObjectName(domain
					+ ":type=Application,name=ApplicationSettings"));
			mbs.registerMBean(new DebugSettings(application), new ObjectName(domain
					+ ":type=Application,name=DebugSettings"));
			mbs.registerMBean(new MarkupSettings(application), new ObjectName(domain
					+ ":type=Application,name=MarkupSettings"));
			mbs.registerMBean(new ResourceSettings(application), new ObjectName(domain
					+ ":type=Application,name=ResourceSettings"));
			mbs.registerMBean(new PageSettings(application), new ObjectName(domain
					+ ":type=Application,name=PageSettings"));
			mbs.registerMBean(new RequestCycleSettings(application), new ObjectName(domain
					+ ":type=Application,name=RequestCycleSettings"));
			mbs.registerMBean(new SecuritySettings(application), new ObjectName(domain
					+ ":type=Application,name=SecuritySettings"));
			mbs.registerMBean(new SessionSettings(application), new ObjectName(domain
					+ ":type=Application,name=SessionSettings"));
			mbs.registerMBean(new CookieValuePersisterSettings(application), new ObjectName(domain
					+ ":type=Application,name=CookieValuePersisterSettings"));

			RequestLogger sessionsBean = new RequestLogger(application);
			ObjectName sessionsBeanName = new ObjectName(domain + ":type=RequestLogger");
			mbs.registerMBean(sessionsBean, sessionsBeanName);
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
}
