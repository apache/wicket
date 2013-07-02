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

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;

/**
 * Simple POJO bean for storing the initial configuration parameters as well as
 * the state of the dynamic variables used by {@link CdiConfiguration}.
 *
 * @author jsarman
 */
class ConfigurationParameters implements Serializable
{

	private IConversationPropagation propagation = ConversationPropagation.NONBOOKMARKABLE;
	private boolean injectComponents = true;
	private boolean injectApplication = true;
	private boolean injectSession = true;
	private boolean injectBehaviors = true;
	private boolean autoConversationManagement = false;
	private boolean configured = false;

	private Set<String> ignoredPackages = new TreeSet<>();
	private IRequestCycleListener activeRequestCycleListener;
	private IComponentOnBeforeRenderListener activeComponentOnBeforeRenderListener;

	ConfigurationParameters()
	{
	}


	IConversationPropagation getPropagation()
	{
		return propagation;
	}

	ConfigurationParameters setPropagation(IConversationPropagation propagation)
	{
		this.propagation = propagation;
		return this;
	}


	boolean isInjectComponents()
	{
		return injectComponents;
	}

	ConfigurationParameters setInjectComponents(boolean injectComponents)
	{
		this.injectComponents = injectComponents;
		return this;
	}

	boolean isInjectApplication()
	{
		return injectApplication;
	}

	ConfigurationParameters setInjectApplication(boolean injectApplication)
	{
		this.injectApplication = injectApplication;
		return this;
	}

	boolean isInjectSession()
	{
		return injectSession;
	}

	ConfigurationParameters setInjectSession(boolean injectSession)
	{
		this.injectSession = injectSession;
		return this;
	}

	boolean isInjectBehaviors()
	{
		return injectBehaviors;
	}

	ConfigurationParameters setInjectBehaviors(boolean injectBehaviors)
	{
		this.injectBehaviors = injectBehaviors;
		return this;
	}

	boolean isAutoConversationManagement()
	{
		return autoConversationManagement;
	}

	ConfigurationParameters setAutoConversationManagement(boolean autoConversationManagement)
	{
		this.autoConversationManagement = autoConversationManagement;
		return this;
	}

	Set<String> getIgnoredPackages()
	{
		return ignoredPackages;
	}

	void setIgnoredPackages(Set<String> ignoredPackages)
	{
		this.ignoredPackages = ignoredPackages;
	}

	IRequestCycleListener getActiveRequestCycleListener()
	{
		return activeRequestCycleListener;
	}

	void setActiveRequestCycleListener(IRequestCycleListener activeRequestCycleListener)
	{
		this.activeRequestCycleListener = activeRequestCycleListener;
	}

	IComponentOnBeforeRenderListener getActiveComponentOnBeforeRenderListener()
	{
		return activeComponentOnBeforeRenderListener;
	}

	void setActiveComponentOnBeforeRenderListener(IComponentOnBeforeRenderListener activeComponentOnBeforeRenderListener)
	{
		this.activeComponentOnBeforeRenderListener = activeComponentOnBeforeRenderListener;
	}

	boolean isConfigured()
	{
		return configured;
	}

	void setConfigured(boolean configured)
	{
		this.configured = configured;
	}


}
