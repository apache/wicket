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

import org.apache.wicket.IBehaviorInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injects components with CDI dependencies
 * 
 * @author igor
 * 
 */
class BehaviorInjector extends AbstractInjector implements IBehaviorInstantiationListener
{
        private static final Logger LOG = LoggerFactory.getLogger(ComponentInjector.class);
	/**
	 * Constructor
	 * 
	 * @param container
	 */
	public BehaviorInjector(CdiContainer container)
	{
		super(container);
	}

	@Override
	public void onInstantiation(Behavior behavior)
	{
		Class<? extends Behavior> behaviorClass = behavior.getClass();

		if (!behaviorClass.isAnnotationPresent(CdiAware.class))
		{
			LOG.debug("Skipping non cdi aware class '{}' ", behaviorClass);
		}
		else
		{
			inject(behavior);
		}
	}
}
