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

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Defines several strategies for looking up a CDI BeanManager in a portable way. The following
 * strategies are tried (in order):
 * <ul>
 * <li>JNDI under java:comp/BeanManager (default location)</li>
 * <li>JNDI under java:comp/env/BeanManager (for servlet containers like Tomcat and Jetty)</li>
 * <li>CDI.current().getBeanManager() (portable lookup)</li>
 * </ul>
 *
 * This is de default strategy used in {@link CdiConfiguration} to look for a BeanManger, unless
 * one is defined in CdiConfiguration(BeanManager)
 *
 * @author papegaaij
 */
public final class BeanManagerLookup
{
	private static final Logger log = LoggerFactory.getLogger(BeanManagerLookup.class);

	private enum BeanManagerLookupStrategy
	{
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
		};

		public abstract BeanManager lookup();
	}

	private BeanManagerLookup()
	{

	}

	public static BeanManager lookup()
	{
		for (BeanManagerLookupStrategy curStrategy : BeanManagerLookupStrategy.values())
		{
			BeanManager ret = curStrategy.lookup();
			if (ret != null)
			{
				return ret;
			}
		}
		return null;
	}

}
