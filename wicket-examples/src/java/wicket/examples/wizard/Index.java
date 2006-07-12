/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.wizard;

import wicket.MarkupContainer;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.link.Link;

/**
 * Index page for the wizard example.
 * 
 * @author Eelco Hillenius
 */
public class Index extends WicketExamplePage
{
	/**
	 * Link to the wizard. It's an internal link instead of a bookmarkable page
	 * to help us with backbutton surpression. Wizards by default do not
	 * partipcate in versioning, which has the effect that whenever a button is
	 * clicked in the wizard, it will never result in a change of the
	 * redirection url. However, though that'll work just fine when you are
	 * already in the wizard, there is still the first access to the wizard. But
	 * if you link to the page that renders it using and internal link, you'll
	 * circumvent that.
	 */
	private static final class WizardLink extends Link
	{
		private final Class wizardClass;

		/**
		 * Construct.
		 * 
		 * @param parent
		 *            The parent of this component The parent of this component.
		 * @param id
		 *            Component id
		 * @param wizardClass
		 *            Class of the wizard to instantiate
		 */
		public WizardLink(MarkupContainer parent, final String id, Class wizardClass)
		{
			super(parent, id);
			this.wizardClass = wizardClass;
		}

		/**
		 * @see wicket.markup.html.link.Link#onClick()
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
		new WizardLink(this, "staticWizardLink", StaticWizard.class);
		new WizardLink(this, "newUserWizardLink", NewUserWizard.class);
	}
}