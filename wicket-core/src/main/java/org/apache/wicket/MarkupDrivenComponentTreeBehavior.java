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
package org.apache.wicket;

import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;

/**
 *
 */
public class MarkupDrivenComponentTreeBehavior extends Behavior
{
	@Override
	public void bind(Component component)
	{
		super.bind(component);

		if (component instanceof MarkupContainer == false)
		{
			throw new WicketRuntimeException(MarkupDrivenComponentTreeBehavior.class.getSimpleName() +
					" can be assigned only to " + MarkupContainer.class.getSimpleName());
		}
	}

	@Override
	public void onConfigure(Component component)
	{
		super.onConfigure(component);

		MarkupDrivenComponentTreeBuilder builder = new MarkupDrivenComponentTreeBuilder();
		builder.rebuild((MarkupContainer) component);
	}
	
	public static final Listener LISTENER = new Listener();

	private static class Listener implements IComponentInstantiationListener
	{
		@Override
		public void onInstantiation(Component component)
		{
			if (component instanceof MarkupContainer)
			{
				component.add(new MarkupDrivenComponentTreeBehavior());
			}
		}
	}
}
