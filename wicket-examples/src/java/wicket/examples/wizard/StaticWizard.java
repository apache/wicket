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
import wicket.extensions.wizard.StaticContentStep;
import wicket.extensions.wizard.Wizard;
import wicket.extensions.wizard.WizardModel;

/**
 * This is kind of the hello world example for wizards. It doesn't do anything
 * useful, except displaying some static text and following static flow.
 * <p>
 * {@link StaticContentStep static content steps} are useful when you have some
 * text to display that you don't want to define seperate panels for. E.g. when
 * the contents come from a database, this is a convenient class to use.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class StaticWizard extends Wizard<String>
{

	/**
	 * Construct.
	 * @param parent 
	 * 
	 * @param id
	 *            The component id
	 */
	public StaticWizard(MarkupContainer parent, String id)
	{
		super(parent, id);

		// create a model with the stupidest steps you can think of
		WizardModel model = new WizardModel();
		model.add(new StaticContentStep("One", "The first step",
				"The <span style=\"color:red\">first step</span> in the "
						+ "<i>wonderful world</i> of <strong>wizards</strong>", true));
		model.add(new StaticContentStep("Two", "The second step", "Aren't we having fun?", true));
		model.add(new StaticContentStep("Three", "The third and last step",
				"Owk, I'm done with this wizard", true));

		// and initialize the wizard
		init(model);
	}

	/**
	 * @see wicket.extensions.wizard.Wizard#onCancel()
	 */
	@Override
	public void onCancel()
	{
		setResponsePage(Index.class);
	}

	/**
	 * @see wicket.extensions.wizard.Wizard#onFinish()
	 */
	@Override
	public void onFinish()
	{
		setResponsePage(Index.class);
	}
}