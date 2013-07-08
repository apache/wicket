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

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.apache.wicket.request.cycle.RequestCycle;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.context.http.HttpConversationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsarman
 */
@ApplicationScoped
public class MockCdiContainer extends AbstractCdiContainer
{
	private static final Logger logger = LoggerFactory.getLogger(MockCdiContainer.class);

	@Inject
	private HttpConversationContext conversationContext;

	private String specificationTitle;
	private String specificationVersion;

	private Map<ContainerSupport, Boolean> supportedFeatures;

	@PostConstruct
	public void init()
	{
		supportedFeatures = new TreeMap<>();
		boolean isSnapshot = false;
		specificationTitle = WeldBootstrap.class.getPackage().getSpecificationTitle();
		specificationVersion = WeldBootstrap.class.getPackage().getSpecificationVersion();
		int major = 0;
		int minor = 0;
		int micro = 0;

		if (specificationTitle == null && specificationVersion == null)
		{
			isSnapshot = true;
			logger.warn("Using a weld snaphot without version info. Enabling all features.");
			specificationTitle = "Weld Snapshot";
			specificationVersion = "UNKNOWN";
		} else
		{
			String[] versionSplit = specificationVersion.split("\\.");
			if (versionSplit.length > 0)
				major = Integer.parseInt(versionSplit[0]);
			if (versionSplit.length > 1)
			{
				minor = Integer.parseInt(versionSplit[1]);
			}
			if (versionSplit.length > 2)
			{
				micro = Integer.parseInt(versionSplit[2]);
			}
		}

		if (major != 2 && !isSnapshot)
		{
			throw new RuntimeException("The Weld CDI 1.1 code requires major version 2");
		}

		for (ContainerSupport support : ContainerSupport.values())
		{

			switch (support)
			{
				case ANONYMOUS_INNER_CLASS_INJECTION:
				case NON_STATIC_INNER_CLASS_INJECTION:
					if (major == 2 && (minor > 0 || (minor == 0 && micro > 2)))
					{
						supportedFeatures.put(support, true);
					} else if (specificationVersion.equals("2.0.2.Final"))
					{
						//Support officially introduced at this release
						supportedFeatures.put(support, true);
					} else
					{
						supportedFeatures.put(support, isSnapshot);
					}
					break;
				default:
					supportedFeatures.put(support, isSnapshot);
			}
		}
	}

	/**
	 * Activates the conversational context and starts the conversation with the
	 * specified cid
	 *
	 * @param cycle
	 * @param cid
	 */
	@Override
	public void activateConversationalContext(RequestCycle cycle, String cid)
	{
		conversationContext.associate(getRequest(cycle));
		if (conversationContext.isActive())
		{
			conversationContext.invalidate();
			conversationContext.deactivate();
			conversationContext.activate(cid);
		} else
		{
			conversationContext.activate(cid);
		}
	}

	@Override
	public Conversation getCurrentConversation()
	{
		return conversationContext.getCurrentConversation();
	}

	@Override
	public String getContainerImplementationName()
	{
		return specificationTitle + " " + specificationVersion;
	}

	@Override
	public boolean isFeatureSupported(ContainerSupport support)
	{
		Boolean isSupported = supportedFeatures.get(support);
		if (isSupported == null)
		{
			return false;
		}
		return isSupported;
	}


}
