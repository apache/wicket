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
package org.apache.wicket.examples.wizard;

import java.lang.reflect.Constructor;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;


/**
 * Page for displaying a wizard.
 * 
 * @author Eelco Hillenius
 */
public class WizardPage extends WicketExamplePage
{
	/**
	 * Construct.
	 * 
	 * @param <C>
	 * 
	 * @param wizardClass
	 *            class of the wizard component
	 */
	public <C extends Wizard> WizardPage(Class<C> wizardClass)
	{
		if (wizardClass == null)
		{
			throw new IllegalArgumentException("argument wizardClass must be not null");
		}
		try
		{
			Constructor<? extends Wizard> ctor = wizardClass.getConstructor(String.class);
			Wizard wizard = ctor.newInstance("wizard");
			add(wizard);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(new CssResourceReference(WizardPage.class,
			"Wizard.css")));
	}
}
