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
package org.apache.wicket.cdi.util.tester;

import java.lang.reflect.Modifier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Specializes;

import org.apache.wicket.Component;
import org.apache.wicket.cdi.ComponentInjector;

/**
 * Injects components with CDI dependencies
 *
 * @author igor
 */
@ApplicationScoped
@Alternative
@Specializes
public class TestComponentInjector extends ComponentInjector
{

	@Override
	public void onInstantiation(Component component)
	{
		Class instanceClass = component.getClass();
		if (instanceClass.isAnonymousClass() ||
				(instanceClass.isMemberClass() && Modifier.isStatic(instanceClass.getModifiers()) == false))
		{
			return;
		}
		inject(component);
	}
}
