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

import java.util.Iterator;

import wicket.Application;
import wicket.Component;
import wicket.IInitializer;
import wicket.behavior.HeaderContributor;
import wicket.feedback.ContainerFeedbackMessageFilter;
import wicket.markup.html.PackageResource;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.markup.html.panel.Panel;

/**
 * A wizard is a dialog component that takes it's users through a number of
 * predefined steps. It has common functionality like a next, previous, finish
 * and cancel button, and it uses a {@link IWizardModel} to navigate through the
 * steps.
 * 
 * <p>
 * This default implementation should be useful for basic cases, if the layout
 * is exactly what you need. If you want to provide your own layout and/ or have
 * more or less components (e.g. you want to additionally provide an overview
 * component), you can override this class and add the components you want
 * yourself.
 * </p>
 * <p>
 * If that's still not enough flexiblity for you, but you want to use the
 * {@link IWizardModel wizard model} and {@link IWizardStep wizard step}
 * functionality provided in this package, you can provde a custom wizard
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class Wizard extends Panel implements IWizardModelListener, IWizard
{
	/**
	 * Initializer for this component; binds static resources.
	 */
	public final static class WizardInitializer implements IInitializer
	{
		/**
		 * @see wicket.IInitializer#init(wicket.Application)
		 */
		public void init(Application application)
		{
			PackageResource.bind(application, Wizard.class, "Wizard.css");
		}
	}

	/** Component id of the buttons panel as used by the default wizard panel. */
	public static final String BUTTONS_ID = "buttons";

	/** Component id of the feedback panel as used by the default wizard panel. */
	public static final String FEEDBACK_ID = "feedback";

	/** Component id of the header panel as used by the default wizard panel. */
	public static final String HEADER_ID = "header";

	/** Component id of the overview panel as used by the default wizard panel. */
	public static final String OVERVIEW_ID = "overview";

	/**
	 * Component id of the view panel (where the main wizard contents go) as
	 * used by the default wizard panel.
	 */
	public static final String VIEW_ID = "view";

	private static final long serialVersionUID = 1L;

	/** The currently active step. */
	private IWizardStep activeStep;

	/**
	 * The form in which the view is nested, and on which the wizard buttons
	 * work.
	 */
	private Form form;

	/** The wizard model. */
	private IWizardModel wizardModel;

	/**
	 * Construct. Adds the default style.
	 * <p>
	 * If you override this class, it makes sense to call this constructor
	 * (super(id)), then - in your constructor - construct a transition model
	 * and then call {@link #init(IWizardModel)} to initialize the wizard.
	 * </p>
	 * <p>
	 * This constructor is not meant for normal clients of this class
	 * </p>
	 * 
	 * @param id
	 *            The component model
	 */
	public Wizard(String id)
	{
		this(id, true);
	}

	/**
	 * Construct.
	 * <p>
	 * If you override this class, it makes sense to call this constructor
	 * (super(id)), then - in your constructor - construct a transition model
	 * and then call {@link #init(IWizardModel)} to initialize the wizard.
	 * </p>
	 * <p>
	 * This constructor is not meant for normal clients of this class
	 * </p>
	 * 
	 * @param id
	 *            The component model
	 * @param addDefaultCssStyle
	 *            Whether to add the {@link #addDefaultCssStyle() default style}
	 */
	public Wizard(String id, boolean addDefaultCssStyle)
	{
		super(id);

		if (addDefaultCssStyle)
		{
			addDefaultCssStyle();
		}
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
	public Wizard(String id, IWizardModel wizardModel)
	{
		this(id, wizardModel, true);
	}

	/**
	 * Construct with a transition model.
	 * <p>
	 * For most clients, this is typically the right constructor to use.
	 * </p>
	 * 
	 * @param id
	 *            The component id
	 * @param wizardModel
	 *            The transitions model
	 * @param addDefaultCssStyle
	 *            Whether to add the {@link #addDefaultCssStyle() default style}
	 */
	public Wizard(String id, IWizardModel wizardModel, boolean addDefaultCssStyle)
	{
		super(id);

		init(wizardModel);

		if (addDefaultCssStyle)
		{
			addDefaultCssStyle();
		}
	}

	/**
	 * Will let the wizard contribute a CSS include to the page's header. It
	 * will add Wizard.css from this package. This method is typically called by
	 * the class that creates the wizard.
	 */
	public final void addDefaultCssStyle()
	{
		add(HeaderContributor.forCss(Wizard.class, "Wizard.css"));
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
	 * Gets the form in which the view is nested, and on which the wizard
	 * buttons work.
	 * 
	 * @return The wizard form
	 */
	public final Form getForm()
	{
		return form;
	}

	/**
	 * @see wicket.extensions.wizard.IWizard#getWizardModel()
	 */
	public final IWizardModel getWizardModel()
	{
		return wizardModel;
	}

	/**
	 * Turn versioning off for wizards. This works best when the wizard is
	 * <strong>not</strong> accessed from bookmarkable pages, so that the url
	 * doesn't change at all.
	 * 
	 * @return False
	 * @see wicket.Component#isVersioned()
	 */
	public boolean isVersioned()
	{
		return false;
	}

	/**
	 * @see wicket.extensions.wizard.IWizardModelListener#onActiveStepChanged(wicket.extensions.wizard.IWizardStep)
	 */
	public void onActiveStepChanged(IWizardStep newStep)
	{
		this.activeStep = newStep;
		form.replace(activeStep.getView(VIEW_ID, this, this));
		form.replace(activeStep.getHeader(HEADER_ID, this, this));
	}

	/**
	 * Called when the wizard is cancelled.
	 */
	public void onCancel()
	{
	};

	/**
	 * Called when the wizard is finished.
	 */
	public void onFinish()
	{
	}

	/**
	 * Initialize this wizard with a transition model.
	 * <p>
	 * If you constructed this wizard using a constructor without the
	 * transitions model argument, <strong>you must</strong> call this method
	 * prior to actually using it.
	 * </p>
	 * 
	 * @param wizardModel
	 */
	protected void init(IWizardModel wizardModel)
	{
		if (wizardModel == null)
		{
			throw new IllegalArgumentException("argument wizardModel must be not null");
		}

		this.wizardModel = wizardModel;

		form = new Form("form");
		add(form);
		// dummy view to be replaced
		form.add(new WebMarkupContainer(HEADER_ID));
		form.add(newFeedbackPanel(FEEDBACK_ID));
		// add dummy view; will be replaced on initialization
		form.add(new WebMarkupContainer(VIEW_ID));
		form.add(newButtonBar(BUTTONS_ID));
		form.add(newOverviewBar(OVERVIEW_ID));

		wizardModel.addListener(this);

		for (Iterator iter = wizardModel.stepIterator(); iter.hasNext();)
		{
			((IWizardStep)iter.next()).init(wizardModel);
		}

		// reset model to prepare for action
		wizardModel.reset();
	}

	/**
	 * Create a new button bar. Clients can override this method to provide a
	 * custom button bar.
	 * 
	 * @param id
	 *            The id to be used to construct the component
	 * 
	 * @return A new button bar
	 */
	protected Component newButtonBar(String id)
	{
		return new WizardButtonBar(id, this);
	}

	/**
	 * Create a new feedback panel. Clients can override this method to provide
	 * a custom feedback panel.
	 * 
	 * @param id
	 *            The id to be used to construct the component
	 * 
	 * @return A new feedback panel
	 */
	protected FeedbackPanel newFeedbackPanel(String id)
	{
		return new FeedbackPanel(id, new ContainerFeedbackMessageFilter(this));
	}

	/**
	 * Create a new overview bar. Clients can override this method to provide a
	 * custom bar.
	 * 
	 * @param id
	 *            The id to be used to construct the component
	 * 
	 * @return A new ovewview bar
	 */
	protected Component newOverviewBar(String id)
	{
		// return a dummy component by default as we don't have an overview
		// component
		return new WebMarkupContainer(id).setVisible(false);
	}
}
