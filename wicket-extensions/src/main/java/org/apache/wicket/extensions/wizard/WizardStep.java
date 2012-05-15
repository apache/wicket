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
package org.apache.wicket.extensions.wizard;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.*;

/**
 * default implementation of {@link IWizardStep}. It is also a panel, which is used as the view
 * component.
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
 * 		add(new RequiredTextField(&quot;user.email&quot;).add(EmailAddressValidator.getInstance()));
 * 	}
 * }
 * </pre>
 * 
 * HTML (defined in e.g. file x/NewUserWizard$UserNameStep.html):
 * 
 * <pre>
 *  &lt;wicket:panel&gt;
 *   &lt;table&gt;
 *    &lt;tr&gt;
 *     &lt;td&gt;&lt;wicket:message key=&quot;username&quot;&gt;Username&lt;/wicket:message&gt;&lt;/td&gt;
 *     &lt;td&gt;&lt;input type=&quot;text&quot; wicket:id=&quot;user.userName&quot; /&gt;&lt;/td&gt;
 *    &lt;/tr&gt;
 *    &lt;tr&gt;
 *     &lt;td&gt;&lt;wicket:message key=&quot;email&quot;&gt;Email Address&lt;/wicket:message&gt;&lt;/td&gt;
 *     &lt;td&gt;&lt;input type=&quot;text&quot; wicket:id=&quot;user.email&quot; /&gt;&lt;/td&gt;
 *    &lt;/tr&gt;
 *   &lt;/table&gt;
 *  &lt;/wicket:panel&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class WizardStep extends Panel implements IWizardStep
{
	/**
	 * Wraps form validators for this step such that they are only executed when this step is
	 * active.
	 */
	private final class FormValidatorWrapper implements IFormValidator
	{

		private static final long serialVersionUID = 1L;

		private final List<IFormValidator> validators = new ArrayList<IFormValidator>();

		/**
		 * Adds a form validator.
		 * 
		 * @param validator
		 *            The validator to add
		 */
		public final void add(final IFormValidator validator)
		{
			validators.add(validator);
		}

		/**
		 * @see org.apache.wicket.markup.html.form.validation.IFormValidator#getDependentFormComponents()
		 */
		public FormComponent<?>[] getDependentFormComponents()
		{
			if (isActiveStep())
			{
				Set<Component> components = new HashSet<Component>();
				for (IFormValidator v : validators)
				{
					FormComponent<?>[] dependentComponents = v.getDependentFormComponents();
					if (dependentComponents != null)
					{
						int len = dependentComponents.length;
						components.addAll(Arrays.asList(dependentComponents).subList(0, len));
					}
				}
				return components.toArray(new FormComponent[components.size()]);
			}
			return null;
		}

		/**
		 * @see org.apache.wicket.markup.html.form.validation.IFormValidator#validate(org.apache.wicket.markup.html.form.Form)
		 */
		public void validate(final Form<?> form)
		{
			if (isActiveStep())
			{
				for (IFormValidator v : validators)
				{
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
            setDefaultModel(new CompoundPropertyModel<IWizard>(wizard));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            // Title
            final AbstractReadOnlyModel<String> titleModel = new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {
                    return getTitle();
                }
            };
            add(new HeaderLabel("title", titleModel, this));

            // Summary
            final AbstractReadOnlyModel<String> summaryModel = new AbstractReadOnlyModel<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {
                    return getSummary();
                }
            };
            add(new HeaderLabel("summary", summaryModel, this));
        }

    }

    /**
     * Default label for title and summary, calls {@link Header#getEscapeModelStrings()}
     * to determine wether it's model strings should be escaped or not.
     */
    private static final class HeaderLabel extends Label
    {
        private static final long serialVersionUID = 1L;
        private final Header header;

        /**
         * Construct.
         *
         * @param id
         *          The component id
         * @param model
         *          The model
         * @param header
         *          The wizard's header
         */
        private HeaderLabel(String id, IModel<?> model, Header header) {
            super(id, model);
            this.header = header;
        }

        @Override
        protected void onConfigure() {
            super.onConfigure();
            // Header decides about escaping of model strings
            setEscapeModelStrings(header.getEscapeModelStrings());
        }

    }

	private static final long serialVersionUID = 1L;

	/**
	 * Marks this step as being fully configured. Only when this is <tt>true</tt> can the wizard
	 * progress. True by default as that works best with normal forms. Clients can set this to false
	 * if some intermediate step, like a file upload, needs to be completed before the wizard may
	 * progress.
	 */
	private boolean complete = true;

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
	 * The wrapper of {@link IFormValidator}s for this step.
	 */
	private FormValidatorWrapper formValidatorWrapper = new FormValidatorWrapper();

	/**
	 * Construct without a title and a summary. Useful for when you provide a custom header by
	 * overiding {@link #getHeader(String, Component, IWizard)}.
	 */
	public WizardStep()
	{
		super(Wizard.VIEW_ID);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public WizardStep(final IModel<String> title, final IModel<String> summary)
	{
		this(title, summary, null);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public WizardStep(final IModel<String> title, final IModel<String> summary,
		final IModel<?> model)
	{
		super(Wizard.VIEW_ID, model);

		this.title = wrap(title);
		this.summary = wrap(summary);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 */
	public WizardStep(final String title, final String summary)
	{
		this(title, summary, null);
	}

	/**
	 * Creates a new step with the specified title and summary. The title and summary are displayed
	 * in the wizard title block while this step is active.
	 * 
	 * @param title
	 *            the title of this step.
	 * @param summary
	 *            a brief summary of this step or some usage guidelines.
	 * @param model
	 *            Any model which is to be used for this step
	 */
	public WizardStep(final String title, final String summary, final IModel<?> model)
	{
		this(new Model<String>(title), new Model<String>(summary), model);
	}

	/**
	 * Adds a form validator.
	 * 
	 * @param validator
	 */
	public final void add(final IFormValidator validator)
	{
		formValidatorWrapper.add(validator);
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardStep#applyState()
	 */
	public void applyState()
	{
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardStep#getHeader(java.lang.String,
	 *      org.apache.wicket.Component, org.apache.wicket.extensions.wizard.IWizard)
	 */
	public Component getHeader(final String id, final Component parent, final IWizard wizard)
	{
		return new Header(id, wizard);
	}

	/**
	 * Gets the summary of this step. This will be displayed in the title of the wizard while this
	 * step is active. The summary is typically an overview of the step or some usage guidelines for
	 * the user.
	 * 
	 * @return the summary of this step.
	 */
	public String getSummary()
	{
		return (summary != null) ? summary.getObject() : null;
	}

	/**
	 * Gets the title of this step.
	 * 
	 * @return the title of this step.
	 */
	public String getTitle()
	{
		return (title != null) ? title.getObject() : null;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardStep#getView(java.lang.String,
	 *      org.apache.wicket.Component, org.apache.wicket.extensions.wizard.IWizard)
	 */
	public Component getView(final String id, final Component parent, final IWizard wizard)
	{
		return this;
	}

	/**
	 * Called to initialize the step. When this method is called depends on the kind of wizard model
	 * that is used.
	 * 
	 * The {@link WizardModel static wizard model} knows all the steps upfront and initializes themm
	 * when starting up. This method will be called when the wizard is {@link #init(IWizardModel)
	 * initializing}.
	 * 
	 * The {@link DynamicWizardModel dynamic wizard model} initializes steps every time they are
	 * encountered.
	 * 
	 * This method sets the wizard model and then calls template method
	 * {@link #onInit(IWizardModel)}
	 * 
	 * @param wizardModel
	 *            the model to which the step belongs.
	 */
	public final void init(final IWizardModel wizardModel)
	{
		this.wizardModel = wizardModel;
		onInit(wizardModel);
	}

	/**
	 * Checks if this step is compete. This method should return true if the wizard can proceed to
	 * the next step. This property is bound and changes can be made at anytime by calling
	 * {@link #setComplete(boolean)} .
	 * 
	 * @return <tt>true</tt> if the wizard can proceed from this step, <tt>false</tt> otherwise.
	 * @see #setComplete
	 */
	public boolean isComplete()
	{
		return complete;
	}

	/**
	 * Marks this step as compete. The wizard will not be able to proceed from this step until this
	 * property is configured to <tt>true</tt>.
	 * 
	 * @param complete
	 *            <tt>true</tt> to allow the wizard to proceed, <tt>false</tt> otherwise.
	 * @see #isComplete
	 */
	public void setComplete(final boolean complete)
	{
		this.complete = complete;
	}

	/**
	 * Sets summary.
	 * 
	 * @param summary
	 *            summary
	 */
	public void setSummaryModel(final IModel<String> summary)
	{
		this.summary = wrap(summary);
	}

	/**
	 * Sets title.
	 * 
	 * @param title
	 *            title
	 */
	public void setTitleModel(final IModel<String> title)
	{
		this.title = wrap(title);
	}

	/**
	 * @see org.apache.wicket.Component#detachModel()
	 */
	@Override
	protected void detachModel()
	{
		super.detachModel();
		if (title != null)
		{
			title.detach();
		}
		if (summary != null)
		{
			summary.detach();
		}
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		Form<?> form = findParent(Form.class);
		form.add(formValidatorWrapper);
	}

	/**
	 * Template method that is called when the step is being initialized.
	 * 
	 * @param wizardModel
	 * @see #init(IWizardModel)
	 */
	protected void onInit(final IWizardModel wizardModel)
	{
	}

	/**
	 * @return wizard model
	 */
	public IWizardModel getWizardModel()
	{
		return wizardModel;
	}
}
