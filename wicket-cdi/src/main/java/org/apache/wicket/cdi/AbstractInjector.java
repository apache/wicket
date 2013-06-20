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

import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for injectors
 * 
 * @author igor
 */
class AbstractInjector
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractInjector.class);

	private final CdiContainer container;

	public AbstractInjector(CdiContainer container)
	{
		Args.notNull(container, "container");
		this.container = container;
	}

	protected <T> void postConstruct(T instance)
	{
		// TODO: is #canProcess() needed here too ?
		// What are the rules to post construct an instance
		container.getNonContextualManager().postConstruct(instance);
	}

	protected <T> void inject(T instance)
	{
		if (canProcess(instance))
		{
			container.getNonContextualManager().inject(instance);
		}
	}

	private <T> boolean canProcess(T instance)
	{
		final boolean canProcess;
		Class<?> instanceClass = instance.getClass();

		if (instanceClass.isAnonymousClass() ||
				(instanceClass.isMemberClass() && Modifier.isStatic(instanceClass.getModifiers()) == false))
		{
			canProcess = false;
			LOG.debug("Class '{}' will not be processed for CDI injection because it is anonymous or non-static member class",
					instanceClass);
		}
		else
		{
			canProcess = true;
			LOG.debug("Going to process class '{}' for CDI injection", instanceClass);
		}

		return canProcess;
	}
}
