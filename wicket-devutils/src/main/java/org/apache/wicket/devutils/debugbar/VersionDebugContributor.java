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
package org.apache.wicket.devutils.debugbar;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * 
 */
public class VersionDebugContributor implements IDebugBarContributor
{
	private static final long serialVersionUID = 1L;

	/** */
	public static final IDebugBarContributor DEBUG_BAR_CONTRIB = new VersionDebugContributor();

	@Override
	public Component createComponent(final String id, final DebugBar debugBar)
	{
		Label label = new Label(id, new AbstractReadOnlyModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return Application.get().getFrameworkSettings().getVersion();
			}
		});

		label.add(AttributeModifier.replace("class", "wicketversioncontrib"));
		return label;
	}
}
