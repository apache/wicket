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
package wicket.extensions.wizard;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * default implementation of {@link IWizardStep}. It is also a panel, which is
 * used as the view component.
 * 
 * <p>
 * And example of a custom step with a panel follows.
 * 
 * Java (defined e.g. in class x.NewUserWizard):
 * 
 * <pre>
 * private final class UserNameStep extends WizardStep
 * {
 * 	public UserNameStep()
 * 	{
 * 		super(new ResourceModel(&quot;username.title&quot;), new ResourceModel(&quot;username.summary&quot;));
 * 		add(new RequiredTextField(&quot;user.userName&quot;));
 * 		add(new RequiredTextField(&quot;user.email&quot;).add(EmailAddressPatternValidator.getInstance()));
 * 	}
 * }
 * </pre>
 * 
 * HTML (defined in e.g. file x/NewUserWizard$UserNameStep.html):
 * 
 * <pre>
 *          &lt;wicket:panel&gt;
 *           &lt;table&gt;
 *            &lt;tr&gt;
 *             &lt;td&gt;&lt;wicket:message key=&quot;username&quot;&gt;Username&lt;/wicket:message&gt;&lt;/td&gt;
 *             &lt;td&gt;&lt;input type=&quot;text&quot; wicket:id=&quot;user.userName&quot; /&gt;&lt;/td&gt;
 *            &lt;/tr&gt;
 *            &lt;tr&gt;
 *             &lt;td&gt;&lt;wicket:message key=&quot;email&quot;&gt;Email Adress&lt;/wicket:message&gt;&lt;/td&gt;
 *             &lt;td&gt;&lt;input type=&quot;text&quot; wicket:id=&quot;user.email&quot; /&gt;&lt;/td&gt;
 *            &lt;/tr&gt;
 *           &lt;/table&gt;
 *          &lt;/wicket:panel&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Eelco Hillenius
 */
public abstract class WizardStep implements IWizardStep
{
	/**
	 * Default header for wizards.
	 */
	private final class Header extends Panel<IWizard>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * 
		 * @param id
		 *            The component id
		 * @param wizard
		 *            The containing wizard
		 */
		public Header(MarkupContainer parent, final String id, final IWizard wizard)
		{
			super(parent, id);
			setModel(new CompoundPropertyModel<IWizard>(wizard));
			new Label(this, "title", title).setEscapeModelStrings(false);
			new Label(this, "summary", summary).setEscapeModelStrings(false);
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Marks this step as being fully configured. Only when this is
	 * <tt>true</tt> can the wizard progress.
	 */
	private boolean complete;


	/**
	 * A summary of this step, or some usage advice.
	 */
	private IModel<String> summary;

	/**
	 * The title of this step.
	 */
	private IModel<String> title;

	/**
	 * The wizard model.
	 */
	private IWizardModel wizardModel;

	/**
	 * Construct without a title and a summary. Useful for when you provide a
	 * custom header by overiding
	 * {@link #getHeader(MarkupContainer, String, IWizard)}.
	 */
	public WizardStep()
	{
		this((IModel<String>)null, (IModel<String>)null);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and
	 * summary are displayed in the wizard title block while this step is
	 * active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public WizardStep(IModel<String> title, IModel<String> summary)
	{
		this.title = title;
		this.summary = summary;
	}

	/**
	 * Creates a new step with the specified title and summary. The title and
	 * summary are displayed in the wizard title block while this step is
	 * active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public WizardStep(String title, String summary)
	{
		this(new Model<String>(title), new Model<String>(summary));
	}

	/**
	 * @see wicket.extensions.wizard.IWizardStep#applyState()
	 */
	public void applyState()
	{
		this.complete = true;
	}

	/**
	 * @see wicket.extensions.wizard.IWizardStep#getHeader(wicket.MarkupContainer,
	 *      java.lang.String, wicket.extensions.wizard.IWizard)
	 */
	public Component getHeader(MarkupContainer parent, final String id, IWizard wizard)
	{
		return new Header(parent, id, wizard);
	}

	/**
	 * Called to initialize the step. This method will be called when the wizard
	 * is first initialising. This method sets the wizard model and then calls
	 * template method {@link #onInit(IWizardModel)}
	 * 
	 * @param wizardModel
	 *            the model to which the step belongs.
	 */
	public final void init(IWizardModel wizardModel)
	{
		this.wizardModel = wizardModel;
		onInit(wizardModel);
	}

	/**
	 * Checks if this step is compete. This method should return true if the
	 * wizard can proceed to the next step. This property is bound and changes
	 * can be made at anytime by calling {@link #setComplete(boolean)} .
	 * 
	 * @return <tt>true</tt> if the wizard can proceed from this step,
	 *         <tt>false</tt> otherwise.
	 * @see #setComplete
	 */
	public boolean isComplete()
	{
		return complete;
	}

	/**
	 * Marks this step as compete. The wizard will not be able to proceed from
	 * this step until this property is configured to <tt>true</tt>.
	 * 
	 * @param complete
	 *            <tt>true</tt> to allow the wizard to proceed, <tt>false</tt>
	 *            otherwise.
	 * @see #isComplete
	 */
	public void setComplete(boolean complete)
	{
		this.complete = complete;
	}

	/**
	 * Sets summary.
	 * 
	 * @param summary
	 *            summary
	 */
	public void setSummaryModel(IModel<String> summary)
	{
		this.summary = summary;
	}

	/**
	 * Sets title.
	 * 
	 * @param title
	 *            title
	 */
	public void setTitleModel(IModel<String> title)
	{
		this.title = title;
	}

	/**
	 * Called when the step is being initialized.
	 * 
	 * @param wizardModel
	 */
	protected void onInit(IWizardModel wizardModel)
	{
	}
}
