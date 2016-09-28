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
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;


/**
 * A wizard is a dialog component that takes users through a number of steps to complete a task. It
 * has common functionality like a next, previous, finish and cancel button, and it uses a
 * {@link IWizardModel} to navigate through the steps.
 * <p>
 * Before you can use the wizard component, it needs to be initialized with a model. You do this by
 * calling {@link #init(IWizardModel)} with the wizard model you intent to use.
 * </p>
 * 
 * <p>
 * This default implementation should be useful for basic cases, if the layout is exactly what you
 * need. If you want to provide your own layout and/ or have more or less components (e.g. you want
 * to additionally provide an overview component), you can override this class and add the
 * components you want yourself using methods like {@link #newButtonBar(String)} et-cetera.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class Wizard extends Panel implements IWizardModelListener, IWizard
{
	/** Component id of the buttons panel as used by the default wizard panel. */
	public static final String BUTTONS_ID = "buttons";

	/** Component id of the feedback panel as used by the default wizard panel. */
	public static final String FEEDBACK_ID = "feedback";

	/** Component id of the header panel as used by the default wizard panel. */
	public static final String HEADER_ID = "header";

	/** Component id of the overview panel as used by the default wizard panel. */
	public static final String OVERVIEW_ID = "overview";

	/** Component id of the form as used by the default wizard panel. */
	public static final String FORM_ID = "form";

	/**
	 * Component id of the view panel (where the main wizard contents go) as used by the default
	 * wizard panel.
	 */
	public static final String VIEW_ID = "view";

	private static final long serialVersionUID = 1L;

	/**
	 * The form in which the view is nested, and on which the wizard buttons work.
	 */
	private Form<?> form;

	/** The wizard model. */
	private IWizardModel wizardModel;

	/**
	 * Construct. Adds the default style.
	 * <p>
	 * If you override this class, it makes sense to call this constructor (super(id)), then - in
	 * your constructor - construct a transition model and then call {@link #init(IWizardModel)} to
	 * initialize the wizard.
	 * </p>
	 * <p>
	 * This constructor is not meant for normal clients of this class
	 * </p>
	 * 
	 * @param id
	 *            The component model
	 */
	public Wizard(final String id)
	{
		super(id);
	}

	/**
	 * Construct with a transition model. Adds the default style.
	 * <p>
	 * For most clients, this is typically the right constructor to use.
	 * </p>
	 * 
	 * @param id
	 *            The component id
	 * @param wizardModel
	 *            The transitions model
	 */
	public Wizard(final String id, final IWizardModel wizardModel)
	{
		super(id);

		init(wizardModel);
	}

	/**
	 * Convenience method to get the active step from the model.
	 * 
	 * @return The active step
	 */
	public final IWizardStep getActiveStep()
	{
		return getWizardModel().getActiveStep();
	}

	/**
	 * Gets the form in which the view is nested, and on which the wizard buttons work.
	 * 
	 * @return The wizard form
	 */
	public Form<?> getForm()
	{
		return form;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizard#getWizardModel()
	 */
	@Override
	public final IWizardModel getWizardModel()
	{
		return wizardModel;
	}

	/**
	 * Turn versioning off for wizards. This works best when the wizard is <strong>not</strong>
	 * accessed from bookmarkable pages, so that the url doesn't change at all.
	 * 
	 * @return False
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.extensions.wizard.IWizardModelListener#onActiveStepChanged(org.apache.wicket.extensions.wizard.IWizardStep)
	 */
	@Override
	public void onActiveStepChanged(final IWizardStep newStep)
	{
		form.replace(newStep.getView(VIEW_ID, this, this));
		form.replace(newStep.getHeader(HEADER_ID, this, this));
	}

	/**
	 * Called when the wizard is canceled.
	 */
	@Override
	public void onCancel()
	{
	}

	/**
	 * Called when the wizard is finished.
	 */
	@Override
	public void onFinish()
	{
	}

	/**
	 * Initialize this wizard with a transition model.
	 * <p>
	 * If you constructed this wizard using a constructor without the transitions model argument,
	 * <strong>you must</strong> call this method prior to actually using it.
	 * </p>
	 * 
	 * @param wizardModel
	 */
	protected void init(final IWizardModel wizardModel)
	{
		if (wizardModel == null)
		{
			throw new IllegalArgumentException("argument wizardModel must be not null");
		}

		this.wizardModel = wizardModel;

		form = newForm(FORM_ID);
		addOrReplace(form);
		// dummy view to be replaced
		form.addOrReplace(new WebMarkupContainer(HEADER_ID));
		form.addOrReplace(newFeedbackPanel(FEEDBACK_ID));
		// add dummy view; will be replaced on initialization
		form.addOrReplace(new WebMarkupContainer(VIEW_ID));
		form.addOrReplace(newButtonBar(BUTTONS_ID));
		form.addOrReplace(newOverviewBar(OVERVIEW_ID));

		wizardModel.addListener(this);

		// reset model to prepare for action
		wizardModel.reset();
	}

	/**
	 * Create a new button bar. Clients can override this method to provide a custom button bar.
	 * 
	 * @param id
	 *            The id to be used to construct the component
	 * 
	 * @return A new button bar
	 */
	protected Component newButtonBar(final String id)
	{
		return new WizardButtonBar(id, this);
	}

	/**
	 * Create a new feedback panel. Clients can override this method to provide a custom feedback
	 * panel.
	 * 
	 * @param id
	 *            The id to be used to construct the component
	 * 
	 * @return A new feedback panel
	 */
	protected Component newFeedbackPanel(final String id)
	{
		return new FeedbackPanel(id, new ContainerFeedbackMessageFilter(this));
	}

	/**
	 * Create a new form. Clients can override this method to provide a custom {@link Form}.
	 * 
	 * @param <E>
	 *            The form's model object type
	 * 
	 * @param id
	 *            The id to be used to construct the component
	 * @return a new form
	 */
	protected <E> Form<E> newForm(final String id)
	{
		return new Form<>(id);
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		Component buttonBar = form.get(BUTTONS_ID);
		if (buttonBar instanceof IDefaultButtonProvider)
		{
			IFormSubmittingComponent defaultButton = ((IDefaultButtonProvider)buttonBar).getDefaultButton(wizardModel);
			form.setDefaultButton(defaultButton);
		}
	}

	/**
	 * Create a new overview bar. Clients can override this method to provide a custom bar.
	 * 
	 * @param id
	 *            The id to be used to construct the component
	 * 
	 * @return A new overview bar
	 */
	protected Component newOverviewBar(final String id)
	{
		// return a dummy component by default as we don't have an overview
		// component
		return new WebMarkupContainer(id).setVisible(false);
	}
}
