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

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for injectors
 * 
 * @author igor
 */
class AbstractInjector <T>
{
    
	private static final Logger LOG = LoggerFactory.getLogger(AbstractInjector.class);

	@Inject
	INonContextualManager nonContextualManager;

	@Inject	
	@IgnoreList
	Instance<String[]> ignorePackages;

	protected void postConstruct(T instance)
	{
		if(!ignore(instance.getClass()))
		{
			nonContextualManager.postConstruct(instance);
		}
	}

	protected void inject(T instance)
	{
		
		if(!ignore(instance.getClass()))
		{
			nonContextualManager.inject(instance);
		}
	}
        
        
	private boolean ignore(Class<?> instanceClass)
	{
		String packageName = instanceClass.getPackage().getName();
		for(String ignore:ignorePackages.get())
		{
			if(packageName.contains(ignore))
			{
				LOG.debug("Skipping {} which is in a package to ignore {}",instanceClass,packageName);	
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
