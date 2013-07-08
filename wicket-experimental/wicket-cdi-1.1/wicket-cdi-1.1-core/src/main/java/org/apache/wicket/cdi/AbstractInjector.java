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

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.wicket.cdi.AbstractCdiContainer.ContainerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for injectors
 *
 * @author igor
 */
class AbstractInjector<T>
{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractInjector.class);

	@Inject
	AbstractCdiContainer cdiContainer;

	@Inject
	INonContextualManager nonContextualManager;

	@Inject
	CdiConfiguration cdiConfiguration;

	@Inject
	@IgnoreList
	Instance<String[]> ignorePackages;

	private Map<ContainerSupport, Boolean> loggedWarnings;

	@PostConstruct
	public void init()
	{
		loggedWarnings = new TreeMap<>();
		for (ContainerSupport support : ContainerSupport.values())
		{
			loggedWarnings.put(support, Boolean.FALSE);
		}
	}

	protected void postConstruct(T instance)
	{
		if (!ignore(instance.getClass()))
		{
			nonContextualManager.postConstruct(instance);
		}
	}

	protected void inject(T instance)
	{

		if (!ignore(instance.getClass()))
		{
			nonContextualManager.inject(instance);
		}
	}


	boolean ignore(Class instanceClass)
	{
		String packageName = instanceClass.getPackage().getName();
		for (String ignore : ignorePackages.get())
		{

			if (instanceClass.getName().equals(ignore) || packageName.contains(ignore))
			{
				LOG.debug("Skipping {} which is set to ignore {}", instanceClass, packageName);
				return true;
			}
		}

		if (instanceClass.isAnonymousClass())
		{
			if (!cdiConfiguration.isContainerFeatureEnabled(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION))
			{
				if (!loggedWarnings.get(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION))
				{
					LOG.info("Anonymous inner class Injection is disabled.");
					loggedWarnings.put(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION, Boolean.TRUE);

				}
				LOG.debug("Skipping anonymous inner class '{}' ", instanceClass);
				return true;
			}
			if (!cdiContainer.isFeatureSupported(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION))
			{
				if (!loggedWarnings.get(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION))
				{
					LOG.warn("The container {} does not support injection of anonymous inner classes. Injection for inner classes is disabled.",
							cdiContainer.getContainerImplementationName());
					loggedWarnings.put(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION, Boolean.TRUE);
				}
				LOG.debug("Skipping anonymous inner class '{}' ", instanceClass);
				return true;
			}
		}
		if (instanceClass.isMemberClass() && !Modifier.isStatic(instanceClass.getModifiers()))
		{
			if (!cdiConfiguration.isContainerFeatureEnabled(ContainerSupport.NON_STATIC_INNER_CLASS_INJECTION))
			{
				if (!loggedWarnings.get(ContainerSupport.NON_STATIC_INNER_CLASS_INJECTION))
				{
					LOG.info("Non-static inner class Injection is disabled.");
					loggedWarnings.put(ContainerSupport.ANONYMOUS_INNER_CLASS_INJECTION, Boolean.TRUE);
				}
				LOG.debug("Skipping non-static inner class '{}' ", instanceClass);
				return true;
			}

			if (!cdiContainer.isFeatureSupported(ContainerSupport.NON_STATIC_INNER_CLASS_INJECTION))
			{
				if (!loggedWarnings.get(ContainerSupport.NON_STATIC_INNER_CLASS_INJECTION))
				{
					LOG.warn("The container {} does not support injection of non-static inner classes. Injection for non-static inner classes is disabled.",
							cdiContainer.getContainerImplementationName());
					loggedWarnings.put(ContainerSupport.NON_STATIC_INNER_CLASS_INJECTION, Boolean.TRUE);
				}
				LOG.debug("Skipping non-static inner class '{}' ", instanceClass);
				return true;
			}
		}
		return false;
	}

	public String[] getIgnorePackages()
	{
		return ignorePackages.get();
	}

}
