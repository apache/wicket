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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.validation.IFormValidator;
import wicket.markup.html.panel.Panel;
import wicket.model.AbstractReadOnlyModel;
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
 *                        &lt;wicket:panel&gt;
 *                         &lt;table&gt;
 *                          &lt;tr&gt;
 *                           &lt;td&gt;&lt;wicket:message key=&quot;username&quot;&gt;Username&lt;/wicket:message&gt;&lt;/td&gt;
 *                           &lt;td&gt;&lt;input type=&quot;text&quot; wicket:id=&quot;user.userName&quot; /&gt;&lt;/td&gt;
 *                          &lt;/tr&gt;
 *                          &lt;tr&gt;
 *                           &lt;td&gt;&lt;wicket:message key=&quot;email&quot;&gt;Email Adress&lt;/wicket:message&gt;&lt;/td&gt;
 *                           &lt;td&gt;&lt;input type=&quot;text&quot; wicket:id=&quot;user.email&quot; /&gt;&lt;/td&gt;
 *                          &lt;/tr&gt;
 *                         &lt;/table&gt;
 *                        &lt;/wicket:panel&gt;
 * </pre>
 * 
 * </p>
 * 
 * 
 * @author Eelco Hillenius
 */
public class WizardStep extends Panel implements IWizardStep
{
	/**
	 * Adds form validators. We don't need this in 2.0 as the hierarchy is know
	 * at construction time from then.
	 */
	private final class AddFormValidatorAction
	{
		/**
		 * Wrapper for any form validators.
		 */
		final FormValidatorWrapper formValidatorWrapper = new FormValidatorWrapper();

		void execute()
		{
			Form form = (Form)WizardStep.this.findParent(Form.class);
			form.add(formValidatorWrapper);
		}
	}

	/**
	 * Wraps form validators for this step such that they are only executed when
	 * this step is active.
	 */
	private final class FormValidatorWrapper implements IFormValidator
	{

		private static final long serialVersionUID = 1L;

		private final List validators = new ArrayList();

		/**
		 * Adds a form validator.
		 * 
		 * @param validator
		 *            The validator to add
		 */
		public final void add(IFormValidator validator)
		{
			validators.add(validator);
		}

		/**
		 * @see wicket.markup.html.form.validation.IFormValidator#getDependentFormComponents()
		 */
		public FormComponent[] getDependentFormComponents()
		{
			if (isActiveStep())
			{
				Set components = new HashSet();
				for (Iterator i = validators.iterator(); i.hasNext();)
				{
					IFormValidator v = (IFormValidator)i.next();
					FormComponent[] dependentComponents = v.getDependentFormComponents();
					if (dependentComponents != null)
					{
						int len = dependentComponents.length;
						for (int j = 0; j < len; j++)
						{
							components.add(dependentComponents[j]);
						}
					}
				}
				return (FormComponent[])components.toArray(new FormComponent[components.size()]);
			}
			return null;
		}

		/**
		 * @see wicket.markup.html.form.validation.IFormValidator#validate(wicket.markup.html.form.Form)
		 */
		public void validate(Form form)
		{
			if (isActiveStep())
			{
				for (Iterator i = validators.iterator(); i.hasNext();)
				{
					IFormValidator v = (IFormValidator)i.next();
					v.validate(form);
				}
			}
		}

		/**
		 * @return whether the step this wrapper is part of is the current step
		 */
		private final boolean isActiveStep()
		{
			return (wizardModel.getActiveStep().equals(WizardStep.this));
		}
	}

	/**
	 * Default header for wizards.
	 */
	private final class Header extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            The component id
		 * @param wizard
		 *            The containing wizard
		 */
		public Header(final String id, final IWizard wizard)
		{
			super(id);
			setModel(new CompoundPropertyModel(wizard));
			add(new Label("title", new AbstractReadOnlyModel()
			{
				private static final long serialVersionUID = 1L;

				public Object getObject(Component component)
				{
					return getTitle();
				}
			}).setEscapeModelStrings(false));
			add(new Label("summary", new AbstractReadOnlyModel()
			{
				private static final long serialVersionUID = 1L;

				public Object getObject(Component component)
				{
					return getSummary();
				}
			}).setEscapeModelStrings(false));
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Marks this step as being fully configured. Only when this is
	 * <tt>true</tt> can the wizard progress.
	 */
	private boolean complete;

	private transient AddFormValidatorAction onAttachAction;

	/**
	 * A summary of this step, or some usage advice.
	 */
	private IModel summary;

	/**
	 * The title of this step.
	 */
	private IModel title;

	/**
	 * The wizard model.
	 */
	private IWizardModel wizardModel;

	/**
	 * Construct without a title and a summary. Useful for when you provide a
	 * custom header by overiding {@link #getHeader(String, Component, Wizard)}.
	 */
	public WizardStep()
	{
		super(Wizard.VIEW_ID);
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
	public WizardStep(IModel title, IModel summary)
	{
		this(title, summary, null);
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
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public WizardStep(IModel title, IModel summary, IModel model)
	{
		super(Wizard.VIEW_ID, model);

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
		this(title, summary, null);
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
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public WizardStep(String title, String summary, IModel model)
	{
		this(new Model(title), new Model(summary), model);
	}

	/**
	 * Adds a form validator.
	 * 
	 * @param validator
	 */
	public final void add(IFormValidator validator)
	{
		if (onAttachAction == null)
		{
			onAttachAction = new AddFormValidatorAction();
		}
		onAttachAction.formValidatorWrapper.add(validator);
	}

	/**
	 * @see wicket.extensions.wizard.IWizardStep#applyState()
	 */
	public void applyState()
	{
		this.complete = true;
	}

	/**
	 * @see wicket.extensions.wizard.IWizardStep#getHeader(java.lang.String,
	 *      wicket.Component, wicket.extensions.wizard.Wizard)
	 */
	public Component getHeader(String id, Component parent, IWizard wizard)
	{
		return new Header(id, wizard);
	}

	/**
	 * Gets the summary of this step. This will be displayed in the title of the
	 * wizard while this step is active. The summary is typically an overview of
	 * the step or some usage guidelines for the user.
	 * 
	 * @return the summary of this step.
	 */
	public String getSummary()
	{
		return (summary != null) ? (String)summary.getObject(this) : (String)null;
	}

	/**
	 * Gets the title of this step.
	 * 
	 * @return the title of this step.
	 */
	public String getTitle()
	{
		return (title != null) ? (String)title.getObject(this) : (String)null;
	}

	/**
	 * @see wicket.extensions.wizard.IWizardStep#getView(java.lang.String,
	 *      wicket.Component, wicket.extensions.wizard.Wizard)
	 */
	public Component getView(String id, Component parent, IWizard wizard)
	{
		return this;
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
	public void setSummaryModel(IModel summary)
	{
		this.summary = summary;
	}

	/**
	 * Sets title.
	 * 
	 * @param title
	 *            title
	 */
	public void setTitleModel(IModel title)
	{
		this.title = title;
	}

	/**
	 * Workaround for adding the form validators; not needed in 2.0.
	 * 
	 * @see wicket.Component#onAttach()
	 */
	protected void onAttach()
	{
		if (onAttachAction != null)
		{
			onAttachAction.execute();
			onAttachAction = null;
		}
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
