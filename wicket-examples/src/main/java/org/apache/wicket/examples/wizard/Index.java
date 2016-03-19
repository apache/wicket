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

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.markup.html.link.Link;


/**
 * Index page for the wizard example.
 * 
 * @author Eelco Hillenius
 */
public class Index extends WicketExamplePage
{
	/**
	 * Link to the wizard. It's an internal link instead of a bookmarkable page to help us with
	 * backbutton surpression. Wizards by default do not partipcate in versioning, which has the
	 * effect that whenever a button is clicked in the wizard, it will never result in a change of
	 * the redirection url. However, though that'll work just fine when you are already in the
	 * wizard, there is still the first access to the wizard. But if you link to the page that
	 * renders it using and internal link, you'll circumvent that.
	 */
	private static final class WizardLink extends Link<Void>
	{
		private final Class<? extends Wizard> wizardClass;

		/**
		 * Construct.
		 * 
		 * @param <C>
		 * 
		 * @param id
		 *            Component id
		 * @param wizardClass
		 *            Class of the wizard to instantiate
		 */
		public <C extends Wizard> WizardLink(String id, Class<C> wizardClass)
		{
			super(id);
			this.wizardClass = wizardClass;
		}

		/**
		 * @see org.apache.wicket.markup.html.link.Link#onClick()
		 */
		@Override
		public void onClick()
		{
			setResponsePage(new WizardPage(wizardClass));
		}
	}

	/**
	 * Construct.
	 */
	public Index()
	{
		add(new WizardLink("staticWizardLink", StaticWizard.class));
		add(new WizardLink("staticWizardWithPanelsLink", StaticWizardWithPanels.class));
		add(new WizardLink("newUserWizardLink", NewUserWizard.class));
	}
}