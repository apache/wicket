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

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.cdi.weld.WeldContainer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * Provides access to CDI features from inside a Wicket request
 * 
 * @author igor
 * 
 */
public abstract class CdiContainer
{
	private static final MetaDataKey<WeldContainer> CONTEXT_KEY = new MetaDataKey<WeldContainer>()
	{
		private static final long serialVersionUID = 1L;
	};

	protected final BeanManager beanManager;

	/**
	 * Constructor
	 * 
	 * @param beanManager
	 *            bean manager
	 */
	public CdiContainer(BeanManager beanManager)
	{
		Args.notNull(beanManager, "beanManager");
		this.beanManager = beanManager;
	}

	public <T> void inject(T instance)
	{
		Args.notNull(instance, "instance");
		NonContextual.of(instance.getClass(), beanManager).postConstruct(instance);
	}

	/**
	 * Gets the current active conversation
	 * 
	 * @param cycle
	 * @return current conversation or {@code null} if none
	 */
	public abstract Conversation getCurrentConversation(RequestCycle cycle);

	/**
	 * Deactivates conversational context
	 * 
	 * @param cycle
	 */
	public abstract void deactivateConversationalContext(RequestCycle cycle);

	/**
	 * Activates the conversational context and starts the conversation with the specified cid
	 * 
	 * @param cycle
	 * @param cid
	 */
	public abstract void activateConversationalContext(RequestCycle cycle, String cid);


	/**
	 * Binds this container instance to the {@link Application}, making it possible to retrieve it
	 * later
	 * 
	 * @param application
	 */
	protected void bind(Application application)
	{
		application.setMetaData(CONTEXT_KEY, this);
	}

	/**
	 * Retrieves container instance stored in the application
	 * 
	 * @param application
	 * @return container instance or {@code null} if none
	 */
	public static final CdiContainer get(Application application)
	{
		WeldContainer ctx = application.getMetaData(CONTEXT_KEY);
		if (ctx == null)
		{
			throw new IllegalStateException("No CDI Context bound to application");
		}
		return ctx;
	}

	/**
	 * Retrieves container instance stored in the current thread's application
	 * 
	 * @return container instance or {@code null} if none
	 */
	public static final CdiContainer get()
	{
		return get(Application.get());
	}


}