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

import org.apache.wicket.util.lang.Args;

/**
 * Base class for injectors
 * 
 * @author igor
 */
class AbstractInjector
{
	private final AbstractCdiContainer container;
	private static final String[] ignoredPackages =new String[]{
		"org.apache.wicket.markup.html",       
		"org.apache.wicket.protocol.html",
		"org.apache.wicket.behavior",    
	};

	public AbstractInjector(AbstractCdiContainer container)
	{
		Args.notNull(container, "container");
		this.container = container;
	}

	protected <T> void postConstruct(T instance)
	{
		container.getNonContextualManager().postConstruct(instance);
	}

	protected <T> void inject(T instance)
	{
		if(!ignore(instance.getClass())) {
			container.getNonContextualManager().inject(instance);
		}
	}
        
	private static boolean ignore(Class clazz)
	{
		String packageName = clazz.getName();
		for(String ignore:ignoredPackages)
		{
			if(packageName.contains(ignore))
			{
				return true;
			}
		}           
		return false;
	}
}
