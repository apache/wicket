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

import java.net.URL;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.MarkupResourceStreamLookupResult;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.resource.UrlResourceStream;
import wicket.util.string.Strings;

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
 * @param <T>
 *            The type
 * 
 * @author Eelco Hillenius
 */
public class WizardStep<T> implements IWizardStep
{
	/**
	 * Content panel.
	 */
	private final class Content extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 * @param wizard
		 */
		public Content(MarkupContainer parent, String id, IWizard wizard)
		{
			super(parent, id);
			WizardStep.this.populate(this);
		}

		/**
		 * @see wicket.MarkupContainer#newMarkupResourceStream(java.lang.Class)
		 */
		@Override
		public MarkupResourceStreamLookupResult newMarkupResourceStream(final Class containerClass)
		{
			return WizardStep.this.newMarkupResourceStream(containerClass);
		}
	}

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
			new Label(this, "title", new AbstractReadOnlyModel()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Object getObject(Component component)
				{
					return getTitle();
				}
			}).setEscapeModelStrings(false);
			new Label(this, "summary", new AbstractReadOnlyModel()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Object getObject(Component component)
				{
					return getSummary();
				}
			}).setEscapeModelStrings(false);
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Marks this step as being fully configured. Only when this is
	 * <tt>true</tt> can the wizard progress.
	 */
	private boolean complete;

	/** The cached content panel. */
	private Content content = null;

	/**
	 * Any model of the step.
	 */
	private IModel<T> model;

	/**
	 * A summary of this step, or some usage advice.
	 */
	private IModel<String> summary;

	/**
	 * The title of this step.
	 */
	private IModel<String> title;

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
	public WizardStep(IModel<String> title, IModel<String> summary, IModel<T> model)
	{
		this.title = title;
		this.summary = summary;
		this.model = model;
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
		this(new Model<String>(title), new Model<String>(summary), null);
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
	 * Gets the summary of this step. This will be displayed in the title of the
	 * wizard while this step is active. The summary is typically an overview of
	 * the step or some usage guidelines for the user.
	 * 
	 * @return the summary of this step.
	 */
	public String getSummary()
	{
		return (summary != null) ? summary.getObject(null) : (String)null;
	}

	/**
	 * Gets the title of this step.
	 * 
	 * @return the title of this step.
	 */
	public String getTitle()
	{
		return (title != null) ? title.getObject(null) : (String)null;
	}

	/**
	 * @see wicket.extensions.wizard.IWizardStep#getView(wicket.MarkupContainer,
	 *      java.lang.String, wicket.extensions.wizard.IWizard)
	 */
	public Component getView(MarkupContainer parent, final String id, IWizard wizard)
	{
		if (content == null)
		{
			content = new Content(parent, id, wizard);
		}
		return content;
	}

	/**
	 * Called to initialize the step. This method will be called when the wizard
	 * is first initialising. This implementation does nothing; override when
	 * you need to do specific work when the step initializes
	 * 
	 * @param model
	 *            the model to which the step belongs.
	 */
	public void init(IWizardModel model)
	{
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
	public final boolean isComplete()
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
	public final void setComplete(boolean complete)
	{
		this.complete = complete;
	}

	/**
	 * Sets summary.
	 * 
	 * @param summary
	 *            summary
	 */
	public final void setSummaryModel(IModel<String> summary)
	{
		this.summary = summary;
	}

	/**
	 * Sets title.
	 * 
	 * @param title
	 *            title
	 */
	public final void setTitleModel(IModel<String> title)
	{
		this.title = title;
	}

	/**
	 * Locates the markup for the current step. It tries to match the step class
	 * first, and moves higher up the inheritance hierarchy until something is
	 * found, possibly arriving at this class. WARNING: don't override unless
	 * you know what you are doing.
	 * 
	 * @param containerClass
	 *            The container the markup should be associated with
	 * @return The resource stream for this step
	 */
	protected MarkupResourceStreamLookupResult newMarkupResourceStream(final Class containerClass)
	{
		return newMarkupResourceStream(getClass(), containerClass);
	}

	/**
	 * Populate this step's content panel (e.g. add labels, textfields and
	 * such).
	 * 
	 * @param contentPanel
	 *            The wizard step's content panel
	 */
	protected void populate(Panel contentPanel)
	{
	}

	/**
	 * Tries to load the markup stream matching the provided step class.
	 * 
	 * @param stepClass
	 *            The class to match
	 * @param containerClass
	 *            The container to associate with
	 * @return The markup stream or null
	 */
	private final MarkupResourceStreamLookupResult newMarkupResourceStream(final Class stepClass,
			final Class containerClass)
	{
		if (!stepClass.equals(WizardStep.class))
		{
			String name = Strings.afterLast(stepClass.getName(), '.') + ".html";
			// try to load template with name of implementing step class;
			final URL url = stepClass.getResource(name);
			if (url != null)
			{
				return new MarkupResourceStreamLookupResult(new UrlResourceStream(url));
			}
			else
			{
				return newMarkupResourceStream(stepClass.getSuperclass(), containerClass);
			}
		}

		// not resource was not found
		return null;
	}
}
