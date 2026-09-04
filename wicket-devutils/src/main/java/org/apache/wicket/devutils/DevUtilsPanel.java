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
package org.apache.wicket.devutils;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * All panels in the wicket-devutils package should extend this panel so that they automatically get
 * checked to make sure that the utilities are enabled in the application debug settings.
 * 
 * @author Jeremy Thomerson
 */
public class DevUtilsPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public DevUtilsPanel(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public DevUtilsPanel(final String id)
	{
		super(id);
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		DevelopmentUtilitiesNotEnabledException.check();
	}
}
