/*
 * $Id$ $Revision$
 * $Date$
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
package wicket;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupStream;
import wicket.markup.html.form.Form;
import wicket.model.IModel;
import wicket.version.undo.UndoPageVersionManager;

/**
 * Abstract base class for pages. As a MarkupContainer subclass, a Page can
 * contain a component hierarchy and markup in some markup language such as
 * HTML. Users of the framework should not attempt to subclass Page directly.
 * Instead they should subclass a subcilass of Page that is appropriate to the
 * markup type they are using, such as WebPage.
 * <p>
 * When a page is constructed, it is automatically added to the user's session
 * and assigned the next page id available from the session. The session that a
 * page is contained in can be retrieved by calling getPageSession(). Page
 * identifiers start at 0 for each session and increment as new pages are added
 * to the session. The session-unique identifier assigned to a page can be
 * retrieved by calling getId(). This id serves as the Page's component name. So
 * the first page added to a new user session will always be named "0".
 * <p>
 * Pages can be constructed with any constructor when they are being used in a
 * Wicket session, but if you wish to link to a Page using a URL that is
 * bookmarkable (doesn't have session information encoded into it), you need to
 * implement your Page with a constructor that accepts a single PageParameters
 * argument.
 * 
 * @see wicket.markup.html.WebPage
 * @see MarkupContainer
 * @author Jonathan Locke
 * @author Chris Turner
 */
public abstract class Page extends MarkupContainer implements IRedirectListener
{
	/** static for access allowed flag (value == true). */
	protected static final boolean ACCESS_ALLOWED = true;

	/** static for access denied flag (value == false). */
	protected static final boolean ACCESS_DENIED = false;

	/** Log. */
	private static final Log log = LogFactory.getLog(Page.class);

	/** Used to create page-unique numbers */
	private int autoIndex;

	/** Number of changes that have occurred since beginVersion() was called */
	private int changeCount;

	/** Any feedback display for this page */
	private IFeedback feedback;

	/** Feedback messages for this page */
	private FeedbackMessages feedbackMessages;

	/** This page's identifier. */
	private int id = -1;

	/** Set of components that rendered if component use checking is enabled */
	private transient Set renderedComponents;

	/** The session that this page is in. */
	private transient Session session = null;

	/** True when changes to the Page should be tracked by versioning */
	private boolean trackChanges = false;

	/** Version manager for this page */
	private transient IPageVersionManager versionManager;

	/**
	 * Constructor.
	 */
	protected Page()
	{
		// A page's componentName is its id, which is not determined until
		// setId is called when the page is added to the session
		super(null);
		addToSession();
	}

	/**
	 * @param model
	 *            See Component
	 * @see Component#Component(String, IModel)
	 */
	protected Page(final IModel model)
	{
		super(null, model);
		addToSession();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Get a page unique number, which will be increased with each call.
	 * 
	 * @return A page unique number
	 */
	public int getAutoIndex()
	{
		return this.autoIndex++;
	}

	/**
	 * @return Returns the feedbackMessages.
	 */
	public FeedbackMessages getFeedbackMessages()
	{
		if (feedbackMessages == null)
		{
			feedbackMessages = new FeedbackMessages();
		}
		return feedbackMessages;
	}

	/**
	 * Get the identifier for this page.
	 * 
	 * @return The identifier for this page
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * Get the name of this page instance is its unique id.
	 * 
	 * @return The name of this page instance is its unique id
	 * @see wicket.Component#getName()
	 */
	public final String getName()
	{
		return Integer.toString(id);
	}

	/**
	 * Get the session for this page.
	 * 
	 * @return Returns the session for this page.
	 */
	public final Session getSession()
	{
		if (session == null)
		{
			session = Session.get();
		}
		return session;
	}

	/**
	 * @return The version of this page.
	 */
	public final int getVersion()
	{
		return getVersionManager().getVersion();
	}

	/**
	 * Override this method to implement a custom way of producing a version of
	 * a Page when it cannot be found in the Session.
	 * 
	 * @param version
	 *            The version required
	 * @return A Page object with the given component/model hierarchy that was
	 *         attached to this page at the time represented by the requested
	 *         version.
	 */
	public Page getVersion(final int version)
	{
		// If we're still the original Page
		if (versionManager == IPageVersionManager.NULL)
		{
			// return self
			return this;
		}
		
		// Get page of desired version
		final Page page = getVersionManager().getVersion(version);
		
		// If we went all the way back to the original page, remove version info
		if (page.getVersion() == -1)
		{
			page.versionManager = IPageVersionManager.NULL;
		}
		
		return page;
	}

	/**
	 * @return Gets a PageState record for this page
	 */
	public PageState newPageState()
	{
		return new PageState()
		{
			public Page getPage()
			{
				return Page.this;
			}
		};
	}

	/**
	 * Redirect to this page.
	 * 
	 * @see wicket.IRedirectListener#onRedirect()
	 */
	public final void onRedirect()
	{
		// This method is used when redirecting to a page
		getRequestCycle().setPage(this);
	}

	/**
	 * Convenience method. Search for children of type fromClass and invoke
	 * their respective removePersistedFormData() methods.
	 * 
	 * @see Form#removePersistentFormComponentValues(boolean)
	 * 
	 * @param formClass
	 *            Form to be selected. Pages may have more than one Form.
	 * @param disablePersistence
	 *            if true, disable persistence for all FormComponents on that
	 *            page. If false, it will remain unchanged.
	 */
	public final void removePersistedFormData(final Class formClass,
			final boolean disablePersistence)
	{
		// Check that formClass is an instanceof Form
		if (!Form.class.isAssignableFrom(formClass))
		{
			throw new WicketRuntimeException("Form class " + formClass.getName()
					+ " is not a subclass of Form");
		}

		// Visit all children which are an instance of formClass
		visitChildren(formClass, new IVisitor()
		{
			public Object component(final Component component)
			{
				// They must be of type Form as well
				if (component instanceof Form)
				{
					// Delete persistet FormComponent data and disable
					// persistence
					((Form)component).removePersistentFormComponentValues(disablePersistence);
				}
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Sets the feedback display for this page
	 * 
	 * @param feedback
	 *            The feedback
	 */
	public void setFeedback(final IFeedback feedback)
	{
		this.feedback = feedback;
	}

	/**
	 * Get the string representation of this container.
	 * 
	 * @return String representation of this container
	 */
	public String toString()
	{
		return "[Page class = " + getClass().getName() + ", id = " + id + "]";
	}

	/**
	 * Whether access is allowed to this page.
	 * 
	 * @return true if access is allowed, false otherwise
	 */
	protected boolean checkAccess()
	{
		return ACCESS_ALLOWED;
	}

	/**
	 * Set-up response with appropriate content type and locale.
	 */
	protected void configureResponse()
	{
		// Get response
		final Response response = getResponse();

		// Set content type based on markup type for page
		response.setContentType("text/" + getMarkupType());

		// Set response locale from session locale
		response.setLocale(getSession().getLocale());
	}

	/**
	 * @return Gets any version manager for this Page
	 */
	protected IPageVersionManager getVersionManager()
	{
		if (versionManager == null)
		{
			versionManager = IPageVersionManager.NULL;
		}
		return versionManager;
	}

	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		// A Page has no default model
		return null;
	}

	/**
	 * @return Factory method that creates a version manager for this Page
	 */
	protected IPageVersionManager newVersionManager()
	{
		return new UndoPageVersionManager(this, getSession().getApplication().getSettings()
				.getMaxPageVersions());
	}

	/**
	 * Called when a request begins working with this page
	 */
	protected void onBeginRequest()
	{
	}

	/**
	 * Called when this page is no longer being used in a request
	 */
	protected void onEndRequest()
	{
	}

	/**
	 * Renders this container to the given response object.
	 */
	protected final void onRender()
	{
		// Configure response object with locale and content type
		configureResponse();

		// Check access to page
		if (checkAccess())
		{
			// Set page's associated markup stream
			final MarkupStream markupStream = getAssociatedMarkupStream();
			setMarkupStream(markupStream);

			// Render all the page's markup
			renderAll(markupStream);
		}
	}

	/**
	 * @param component
	 *            The component that was added
	 */
	final void componentAdded(Component component)
	{
		changed();
		getVersionManager().componentAdded(component);
	}

	/**
	 * @param component
	 *            The component whose model is about to change
	 */
	final void componentModelChangeImpending(Component component)
	{
		changed();
		getVersionManager().componentModelChangeImpending(component);
	}

	/**
	 * @param component
	 *            The component that was removed
	 */
	final void componentRemoved(Component component)
	{
		changed();
		getVersionManager().componentRemoved(component);
	}

	/**
	 * Adds a component to the set of rendered components
	 * 
	 * @param component
	 *            The component that was rendered
	 */
	final void componentRendered(final Component component)
	{
		// Inform the page that this component rendered
		if (getSession().getApplication().getSettings().getComponentUseCheck())
		{
			if (renderedComponents == null)
			{
				renderedComponents = new HashSet();
			}
			renderedComponents.add(component);
		}
	}

	/**
	 * @see wicket.Component#onInternalBeginRender()
	 */
	final void onInternalBeginRender()
	{
		// Adds any feedback messages on this page to the given component
		if (feedback != null)
		{
			feedback.addFeedbackMessages(this, false);
		}
	}

	/**
	 * Called when a request begins working with this page
	 */
	final void onInternalBeginRequest()
	{
		changeCount = 0;
	}

	/**
	 * @see wicket.Component#onInternalEndRender()
	 */
	final void onInternalEndRender()
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		if (getSession().getApplication().getSettings().getComponentUseCheck()
				&& !getResponse().isRedirect())
		{
			// Visit components on page
			checkRendering();
		}

		// Clear all feedback messages
		getFeedbackMessages().clear();
	}

	/**
	 * Called when this page is no longer being used in a request
	 */
	final void onInternalEndRequest()
	{
		// Any changes to the page after this point will be versioned
		trackChanges = true;

		// If there have been changes to the page in this version
		if (changeCount > 0)
		{
			// We're done with this version
			getVersionManager().endVersion();
		}
	}

	/**
	 * @see wicket.Component#onInternalModelChanged()
	 */
	protected void onInternalModelChanged()
	{
		// Visit all the form components and validate each
		visitChildren(new Component.IVisitor()
		{
			public Object component(final Component component)
			{
				// If form component is using form model
				if (component.sameRootModel(Page.this))
				{
					component.modelChanged();
				}
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Reset this page. Called if rendering is interrupted by an exception to
	 * put the page back into a state where it can function again.
	 */
	final void resetMarkupStreams()
	{
		// When an exception is thrown while rendering a page, there may
		// be invalid markup streams set on various containers. We need
		// to reset these to null to ensure they get recreated correctly.
		visitChildren(MarkupContainer.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				final MarkupContainer container = (MarkupContainer)component;
				container.setMarkupStream(null);
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 *            The id to set.
	 */
	final void setId(final int id)
	{
		this.id = id;
	}

	/**
	 * Adds this page to the current session
	 */
	private final void addToSession()
	{
		// Add page to session. This ensures that all the nice attributes
		// of a page, such as its session and application are accessible in
		// the page constructor.
		getSession().addPage(this);
	}

	/**
	 * Install version manager if need be
	 */
	private final void changed()
	{
		// If we are creating a revision of the original Page
		if (trackChanges)
		{
			// Install a real version manager now if we don't already have one
			if (versionManager == IPageVersionManager.NULL)
			{
				versionManager = newVersionManager();
			}

			// If there have not been any changes yet
			if (changeCount == 0)
			{
				// start a new version
				getVersionManager().beginVersion();
			}

			// Increase number of changes
			changeCount++;
		}
	}

	/**
	 * Throw an exception if not all components rendered.
	 */
	private final void checkRendering()
	{
		visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
				// If component never rendered
				if (!renderedComponents.contains(component))
				{
					// Throw exception
					throw new WicketRuntimeException(component
							.exceptionMessage("Component never rendered. You probably failed to "
									+ "reference it in your markup."));
				}
				return CONTINUE_TRAVERSAL;
			}
		});

		renderedComponents = null;
	}

	static
	{
		// Allow calls through the IRedirectListener interface
		RequestCycle.registerRequestListenerInterface(IRedirectListener.class);
	}
}
