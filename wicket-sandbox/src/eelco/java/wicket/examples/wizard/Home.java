/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import wicket.examples.wizard.framework.WizardConfiguration;
import wicket.examples.wizard.framework.WizardPanel;
import wicket.markup.html.WebPage;
import wicket.markup.html.panel.FeedbackPanel;

/**
 * Home page of the wizard example.
 * 
 * @author Eelco Hillenius
 */
public class Home extends WebPage
{
	/**
	 * Constructor.
	 */
	public Home()
	{
		WizardConfiguration prefWiz = ((WizardApplication)getApplication()).newPreferencesWizard();

		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		add(feedbackPanel);

		add(new WizardPanel("wizardPanel", prefWiz));
	}
}