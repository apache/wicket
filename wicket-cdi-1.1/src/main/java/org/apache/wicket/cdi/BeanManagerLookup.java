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

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.wicket.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines several strategies for looking up a CDI BeanManager in a portable
 * way. The following strategies are tried (in order):
 * <ul>
 * <li>JNDI under java:comp/BeanManager (default location)</li>
 * <li>JNDI under java:comp/env/BeanManager (for servlet containers like Tomcat
 * and Jetty)</li>
 * <li>CDI.current().getBeanManager() (portable lookup)</li>
 * <li>{@linkplain CdiConfiguration#getFallbackBeanManager() Fallback}</li>
 * </ul>
 * 
 * The last successful lookup strategy is saved and tried first next time.
 * 
 * @author papegaaij
 */
public final class BeanManagerLookup
{
	private static final Logger log = LoggerFactory.getLogger(BeanManagerLookup.class);

	private enum BeanManagerLookupStrategy {
		JNDI {
			@Override
			public BeanManager lookup()
			{
				try
				{
					return InitialContext.doLookup("java:comp/BeanManager");
				}
				catch (NamingException e)
				{
					return null;
				}
			}
		},
		JNDI_ENV {
			@Override
			public BeanManager lookup()
			{
				try
				{
					return InitialContext.doLookup("java:comp/env/BeanManager");
				}
				catch (NamingException e)
				{
					return null;
				}
			}
		},
		CDI_PROVIDER {
			@Override
			public BeanManager lookup()
			{
				try
				{
					return CDI.current().getBeanManager();
				}
				catch (Exception e)
				{
					log.debug(e.getMessage(), e);
					return null;
				}
			}
		},
		FALLBACK {
			@Override
			public BeanManager lookup()
			{
				return CdiConfiguration.get(Application.get()).getFallbackBeanManager();
			}
		};

		public abstract BeanManager lookup();
	}

	private static BeanManagerLookupStrategy lastSuccessful = BeanManagerLookupStrategy.JNDI;

	private BeanManagerLookup()
	{
	}

	public static BeanManager lookup()
	{
		BeanManager ret = lastSuccessful.lookup();
		if (ret != null)
			return ret;

		for (BeanManagerLookupStrategy curStrategy : BeanManagerLookupStrategy.values())
		{
			ret = curStrategy.lookup();
			if (ret != null)
			{
				lastSuccessful = curStrategy;
				return ret;
			}
		}

		throw new IllegalStateException(
				"No BeanManager found via the CDI provider and no fallback specified. Check your "
						+ "CDI setup or specify a fallback BeanManager in the CdiConfiguration.");
	}
}
