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
import java.util.Iterator;
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
 * Instead they should subclass a subclass of Page that is appropriate to the
 * markup type they are using, such as WebPage.
 * <p>
 * When a page is constructed, it is automatically added to the user's session
 * and assigned the next page id available from the session. The session that a
 * page is contained in can be retrieved by calling getSession(). Page
 * identifiers start at 0 for each session and increment as new pages are added
 * to the session. The session-unique identifier assigned to a page can be
 * retrieved by calling getId(). So, the first page added to a new user session
 * will always be named "0".
 * <p>
 * Pages can be constructed with any constructor when they are being used in a
 * Wicket session, but if you wish to link to a Page using a URL that is
 * "bookmarkable" (which implies that the URL will not have any session
 * information encoded in it), you need to implement your Page with a no-arg
 * constructor or with a constructor that accepts a PageParameters argument.
 * <p>
 * Subclasses of Page which are interested in lifecycle events can override
 * onEndRequest(), onBeginRender(), onEndRender() and onModelChanged(), each of
 * which are called at the obvious time. Note that there is no onBeginRequest
 * for pages because there is no way to know when a request has begun working
 * with a page, since it might change the page it uses to respond at any time.
 * <p>
 * Pages, like other components, can have models. A page can be assigned a model
 * by passing one to a page's constructor, by overriding initModel() or with an
 * explicit invocation of setModel(). If the model is a CompoundPropertyModel,
 * components on the page can use the page's model implicitly. If they are not
 * assigned a model, the initModel() override in Component will cause the
 * component to use the page's model. In this case, the name of the component
 * determines which property of the implicit page model the component is bound
 * to. A similar implicit model scheme exists for Form models and FormComponent
 * children of forms. If more control is desired over binding of components to
 * the page model, BoundCompoundPropertyModel can be used.
 * <p>
 * Pages can support the back button by enabling versioning with a call to
 * setVersioned(boolean). If a Page is versioned and changes occur to it which
 * need to be tracked, a verison manager will be installed using the overridable
 * factory method newVersionManager(). The default version manager returned by
 * the base implementation of this method is an instance of
 * UndoPageVersionManager, which manages versions of a page by keeping change
 * records that can be reversed at a later time.
 * <p>
 * A page can have one or more feedback messages and it can specify a feedback
 * component implementing IFeedback for displaying these messages. An easy and
 * useful implementation of IFeedback is the FeedbackPanel component which
 * displays feedback messages in a list view.
 * <p>
 * Pages can be secured by overriding checkAccess(). If checkAccess() returns
 * ACCESS_ALLOWED (true), then onRender() will render the page. If it returns
 * false (ACCESS_DENIED), then onRender() will not render the page. Besides
 * returning true or false, an implementation of checkAccess() may also choose
 * to send the user to another page with Component.setResponsePage() or
 * Page.redirectToInterceptPage(). This can be used to allow a user to
 * authenticate themselves if they were denied access.
 * 
 * @see wicket.markup.html.WebPage
 * @see wicket.MarkupContainer
 * @see wicket.model.CompoundPropertyModel
 * @see wicket.model.BoundCompoundPropertyModel
 * @see wicket.Component
 * @see wicket.IPageVersionManager
 * @see wicket.version.undo.UndoPageVersionManager
 * 
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

	/** Number of changes that have occurred since the request began */
	private transient int changeCount;

	/** Any feedback display for this page */
	private IFeedback feedback;

	/** Feedback messages for this page */
	private FeedbackMessages feedbackMessages;

	/** This page's identifier. */
	private int id = -1;

	/** The PageMap within the session that this page is stored in */
	private transient PageMap pageMap;

	/** Set of components that rendered if component use checking is enabled */
	private transient Set renderedComponents;

	/** The session that this page is in. */
	private transient Session session = null;

	/** True when changes to the Page should be tracked by version management */
	private boolean trackChanges = false;

	/** Version manager for this page */
	private IPageVersionManager versionManager;

	/**
	 * Constructor.
	 */
	protected Page()
	{
		// A page's componentId is its id, which is not determined until
		// setId is called when the page is added to the session
		super(null);
		init();
	}

	/**
	 * @param model
	 *            See Component
	 * @see Component#Component(String, IModel)
	 */
	protected Page(final IModel model)
	{
		super(null, model);
		init();
	}

	/**
	 * Redirects to any intercept page previously specified by a call to
	 * redirectToInterceptPage.
	 * 
	 * @return True if an original destination was redirected to
	 * @see PageMap#redirectToInterceptPage(Page)
	 */
	public final boolean continueToOriginalDestination()
	{
		return getPageMap().continueToOriginalDestination();
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
	 * @return The current version number of this page. If the page has been
	 *         changed once, the return value will be 1. If the page has not yet
	 *         been revised, the version returned will be 0, indicating that the
	 *         page is still in its original state.
	 */
	public final int getCurrentVersionNumber()
	{
		return versionManager == null ? 0 : versionManager.getCurrentVersionNumber();
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
	 * @see wicket.Component#getId()
	 */
	public final String getId()
	{
		return Integer.toString(id);
	}

	/**
	 * @return The list of PageSets to which this Page belongs.
	 */
	public final Iterator getPageSets()
	{
		return getSession().getApplication().getPageSets(this);
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
	 * Override this method to implement a custom way of producing a version of
	 * a Page when it cannot be found in the Session.
	 * 
	 * @param versionNumber
	 *            The version desired
	 * @return A Page object with the component/model hierarchy that was
	 *         attached to this page at the time represented by the requested
	 *         version.
	 */
	public Page getVersion(final int versionNumber)
	{
		// If we're still the original Page and that's what's desired
		if (versionManager == null)
		{
			if (versionNumber == 0)
			{
				return this;
			}
			else
			{
				throw new IllegalStateException(
						"No version manager available to retrieve requested versionNumber "
								+ versionNumber);
			}
		}
		else
		{
			// Save original change tracking state
			final boolean originalTrackChanges = trackChanges;
			try
			{
				// While the version manager is potentially playing around with
				// the Page, it may change the page in order to undo changes and
				// we don't want change tracking going on while its doing this.
				trackChanges = false;

				// Get page of desired version
				final Page page = versionManager.getVersion(versionNumber);

				// If we went all the way back to the original page
				if (page != null && page.getCurrentVersionNumber() == 0)
				{
					// remove version info
					page.versionManager = null;
				}

				return page;
			}
			finally
			{
				// Restore change tracking state
				trackChanges = originalTrackChanges;
			}
		}
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
		getRequestCycle().setResponsePage(this);
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page.
	 * 
	 * @param page
	 *            The sign in page
	 */
	public final void redirectToInterceptPage(final Page page)
	{
		getPageMap().redirectToInterceptPage(page);
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
	 * @param pageMapName
	 *            Sets this page into the page map with the given name. If the
	 *            page map does not yet exist, it is automatically created.
	 */
	public final void setPageMap(final String pageMapName)
	{
		final Session session = getSession();
		PageMap pageMap = session.getPageMap(pageMapName);
		if (pageMap == null)
		{
			pageMap = session.newPageMap(pageMapName);
		}
		setPageMap(pageMap);
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Gets the url for the given page class using the given parameters.
	 * 
	 * @param pageClass
	 *            Class of page
	 * @param parameters
	 *            Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public abstract String urlFor(final Class pageClass, final PageParameters parameters);

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Gets the url for the given component/ listener interface.
	 * 
	 * @param component
	 *            Component that has listener interface
	 * @param listenerInterface
	 *            The listener interface
	 * @return A URL that encodes a page, component and interface to call
	 */
	public abstract String urlFor(final Component component, final Class listenerInterface);

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * 
	 * @param path
	 *            The path
	 * @return The url for the path
	 */
	public abstract String urlFor(final String path);

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
	 * @return Returns the pageMap.
	 */
	protected PageMap getPageMap()
	{
		return pageMap;
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
	 * @see wicket.Component#internalOnModelChanged()
	 */
	protected void internalOnModelChanged()
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
	 * @return Factory method that creates a version manager for this Page
	 */
	protected IPageVersionManager newVersionManager()
	{
		final ApplicationSettings settings = getSession().getApplication().getSettings();
		return new UndoPageVersionManager(this, settings.getMaxPageVersions());
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
		// Check access to page
		if (checkAccess())
		{
			// Configure response object with locale and content type
			configureResponse();

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
	final void componentAdded(final Component component)
	{
		if (trackChanges && component.isVersioned())
		{
			onChanged();
			if (versionManager != null)
			{
				versionManager.componentAdded(component);
			}
		}
	}

	/**
	 * @param component
	 *            The component whose model is about to change
	 */
	final void componentModelChangeImpending(final Component component)
	{
		if (trackChanges && component.isVersioned())
		{
			onChanged();
			if (versionManager != null)
			{
				versionManager.componentModelChangeImpending(component);
			}
		}
	}

	/**
	 * @param component
	 *            The component that was removed
	 */
	final void componentRemoved(final Component component)
	{
		if (trackChanges && component.isVersioned())
		{
			onChanged();
			if (versionManager != null)
			{
				versionManager.componentRemoved(component);
			}
		}
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
			if (log.isDebugEnabled())
			{
				log.debug("Rendered " + component);
			}
		}
	}

	/**
	 * @see wicket.Component#internalOnBeginRender()
	 */
	final void internalOnBeginRender()
	{
		// Adds any feedback messages on this page to the given component
		if (feedback != null)
		{
			feedback.addFeedbackMessages(this, false);
		}
	}

	/**
	 * @see wicket.Component#internalOnEndRender()
	 */
	final void internalOnEndRender()
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		final ApplicationSettings settings = getSession().getApplication().getSettings();
		if (settings.getComponentUseCheck() && !getResponse().isRedirect())
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
	final void internalOnEndRequest()
	{
		if (isVersioned())
		{
			// Any changes to the page after this point will be tracked by the
			// page's version manager. Since trackChanges is never set to false,
			// this effectively means that change tracking begins after the
			// first request to a page completes.
			trackChanges = true;

			// If there have been changes to the page
			if (changeCount > 0)
			{
				// We're done with this version
				if (versionManager != null)
				{
					versionManager.endVersion();
				}

				// Reset change count for next time
				changeCount = 0;
			}
		}
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
	 * @param pageMap
	 *            PageMap where this page resides
	 */
	final void setPageMap(final PageMap pageMap)
	{
		this.pageMap = pageMap;
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
				if ((renderedComponents == null) || !renderedComponents.contains(component))
				{
					// Throw exception
					throw new WicketRuntimeException(component
							.exceptionMessage("Component never rendered. You probably failed to "
									+ "reference it in your markup."));
				}
				return CONTINUE_TRAVERSAL;
			}
		});

		// Get rid of set
		renderedComponents = null;
	}

	/**
	 * Initializes Page by adding it to the Session and initializing it.
	 */
	private void init()
	{
		// Get session
		final Session session = getSession();

		// Add page to session
		session.add(this);

		// Set versioning of page based on default
		setVersioned(session.getApplication().getSettings().getVersionPagesByDefault());

		// Loop through the PageSet objects for this Page
		for (final Iterator iterator = getPageSets(); iterator.hasNext();)
		{
			// Get next PageSet
			final PageSet pageSet = (PageSet)iterator.next();

			// Let PageSet initialize the Page
			pageSet.init(this);
		}
	}

	/**
	 * Install version manager if need be
	 */
	private final void onChanged()
	{
		// If we have no version manager
		if (versionManager == null)
		{
			// then install a new version manager
			versionManager = newVersionManager();
		}

		// If there have not been any changes yet
		if (changeCount == 0)
		{
			// start a new version
			versionManager.beginVersion();
		}

		// Increase number of changes
		changeCount++;
	}

	static
	{
		// Allow calls through the IRedirectListener interface
		RequestCycle.registerRequestListenerInterface(IRedirectListener.class);
	}
}
