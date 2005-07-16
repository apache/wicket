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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupStream;
import wicket.markup.html.form.Form;
import wicket.model.IModel;
import wicket.util.lang.Classes;
import wicket.util.string.StringValue;
import wicket.util.value.Count;
import wicket.version.undo.Change;
import wicket.version.undo.UndoPageVersionManager;

/**
 * Abstract base class for pages. As a MarkupContainer subclass, a Page can
 * contain a component hierarchy and markup in some markup language such as
 * HTML. Users of the framework should not attempt to subclass Page directly.
 * Instead they should subclass a subclass of Page that is appropriate to the
 * markup type they are using, such as WebPage (for HTML markup).
 * <ul>
 * <li><b>Construction </b>- When a page is constructed, it is automatically
 * added to the current PageMap in the Session. When a Page is added to the
 * Session's PageMap, the PageMap assigns the Page an id. A PageMap is roughly
 * equivalent to a browser window and encapsulates a set of pages accessible
 * through that window. When a popup window is created, a new PageMap is created
 * for the popup.
 * 
 * <li><b>Identity </b>- The Session that a Page is contained in can be
 * retrieved by calling Page.getSession(). Page identifiers start at 0 for each
 * PageMap in the Session and increment as new pages are added to the map. The
 * PageMap-(and Session)-unique identifier assigned to a given Page can be
 * retrieved by calling getId(). So, the first Page added to a new user Session
 * will always be named "0".
 * 
 * <li><b>LifeCycle </b>- Subclasses of Page which are interested in lifecycle
 * events can override onBeginRequest, onEndRequest() and onModelChanged(). The
 * onBeginRequest() method is inherited from Component. A call to
 * onBeginRequest() is made for every Component on a Page before page rendering
 * begins. At the end of a request (when rendering has completed) to a Page, the
 * onEndRequest() method is called for every Component on the Page.
 * 
 * <li><b>Nested Component Hierarchy </b>- The Page class is a subclass of
 * MarkupContainer. All MarkupContainers can have "associated markup", which
 * resides alongside the Java code by default. All MarkupContainers are also
 * Component containers. Through nesting, of containers, a Page can contain any
 * arbitrary tree of Components. For more details on MarkupContainers, see
 * {@link wicket.MarkupContainer}.
 * 
 * <li><b>Bookmarkable Pages </b>- Pages can be constructed with any
 * constructor when they are being used in a Wicket session, but if you wish to
 * link to a Page using a URL that is "bookmarkable" (which implies that the URL
 * will not have any session information encoded in it), you need to implement
 * your Page with a no-arg constructor or with a constructor that accepts a
 * PageParameters argument (which wraps any query string parameters for a
 * request).
 * 
 * <li><b>Models </b>- Pages, like other Components, can have models (see
 * {@link IModel}). A Page can be assigned a model by passing one to the Page's
 * constructor, by overriding initModel() or with an explicit invocation of
 * setModel(). If the model is a {@link wicket.model.CompoundPropertyModel},
 * Components on the Page can use the Page's model implicitly via container
 * inheritance. If a Component is not assigned a model, the initModel() override
 * in Component will cause that Component to use the nearest CompoundModel in
 * the parent chain, in this case, the Page's model. For basic CompoundModels,
 * the name of the Component determines which property of the implicit page
 * model the component is bound to. If more control is desired over the binding
 * of Components to the page model (for example, if you want to specify some
 * OGNL expression other than the component's name for retrieving the model
 * object), BoundCompoundPropertyModel can be used.
 * 
 * <li><b>Back Button </b>- Pages can support the back button by enabling
 * versioning with a call to setVersioned(boolean). If a Page is versioned and
 * changes occur to it which need to be tracked, a verison manager will be
 * installed using the overridable factory method newVersionManager(). The
 * default version manager returned by the base implementation of this method is
 * an instance of UndoPageVersionManager, which manages versions of a page by
 * keeping change records that can be reversed at a later time.
 * 
 * <li><b>Security </b>- Pages can be secured by overriding checkAccess(). If
 * checkAccess() returns ACCESS_ALLOWED (true), then onRender() will render the
 * page. If it returns false (ACCESS_DENIED), then onRender() will not render
 * the page. Besides returning true or false, an implementation of checkAccess()
 * may also choose to send the user to another page with
 * Component.setResponsePage() or Page.redirectToInterceptPage(). This can be
 * used to allow a user to authenticate themselves if they were denied access.
 * 
 * <li><b>Clustering </b>- The newPageState() method provides a convenient way
 * for a Page to create a {@link PageState}record that reconstitutes the Page
 * when replicated in a cluster.
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
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public abstract class Page extends MarkupContainer
	implements IRedirectListener, IFeedbackBoundary
{
	/** Access allowed flag (value == true). */
	protected static final boolean ACCESS_ALLOWED = true;

	/** Access denied flag (value == false). */
	protected static final boolean ACCESS_DENIED = false;

	/** True if this page is dirty. */
	private static final short FLAG_IS_DIRTY = FLAG_RESERVED1;

	/** True if this page is currently rendering. */
	private static final short FLAG_IS_RENDERING = FLAG_RESERVED2;

	/** True if a new version was created for this request. */
	private static final short FLAG_NEW_VERSION = FLAG_RESERVED3;

	/** True if component changes are being tracked. */
	private static final short FLAG_TRACK_CHANGES = FLAG_RESERVED4;

	/** Log. */
	private static final Log log = LogFactory.getLog(Page.class);

	/** Used to create page-unique numbers */
	private int autoIndex;

	/** Feedback messages for this page */
	private FeedbackMessages feedbackMessages;

	/** The PageMap within the session that this page is stored in */
	private transient PageMap pageMap;

	/** Name of PageMap that this page is stored in */
	private String pageMapName;

	/** Set of components that rendered if component use checking is enabled */
	private transient Set renderedComponents;

	/** The session that this page is in. */
	private transient Session session = null;

	/** Version manager for this page */
	private IPageVersionManager versionManager;

	/**
	 * Constructor.
	 */
	protected Page()
	{
		// A Page's id is not determined until setId is called when the Page is
		// added to a PageMap in the Session.
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
		// A Page's id is not determined until setId is called when the Page is
		// added to a PageMap in the Session.
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
	public final int getAutoIndex()
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
	public final FeedbackMessages getFeedbackMessages()
	{
		if (feedbackMessages == null)
		{
			feedbackMessages = new FeedbackMessages();
		}
		return feedbackMessages;
	}

	/**
	 * @return Returns the PageMap that this Page is stored in.
	 */
	public final PageMap getPageMap()
	{
		if (pageMap == null)
		{
			setPageMap(pageMapName);
		}
		return pageMap;
	}

	/**
	 * THIS FEATURE IS CURRENTLY EXPERIMENTAL. DO NOT USE THIS METHOD.
	 * 
	 * @return The list of PageSets to which this Page belongs.
	 */
	public final Iterator getPageSets()
	{
		return getSession().getApplication().getPageSets(this);
	}

	/**
	 * Override this method to implement a custom way of producing a version of a Page
	 * when it cannot be found in the Session.
	 * @param versionNumber The version desired
	 * @return A Page object with the component/model hierarchy that was attached to this
	 *         page at the time represented by the requested version.
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
			final boolean originalTrackChanges = getFlag(FLAG_TRACK_CHANGES);
			try
			{
				// While the version manager is potentially playing around with
				// the Page, it may change the page in order to undo changes and
				// we don't want change tracking going on while its doing this.
				setFlag(FLAG_TRACK_CHANGES, false);

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
				setFlag(FLAG_TRACK_CHANGES, originalTrackChanges);
			}
		}
	}

	/**
	 * @return This page's component hierarchy as a string
	 */
	public final String hierarchyAsString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append("Page " + getId() + " (version " + getCurrentVersionNumber() + ")");
		visitChildren(new IVisitor()
		{
			public Object component(Component component)
			{
				int levels = 0;
				for (Component current = component; current != null; current = current.getParent())
				{
					levels++;
				}
				buffer.append(StringValue.repeat(levels, "	") + component.getPageRelativePath()
						+ "." + Classes.name(component.getClass()));
				return null;
			}
		});
		return buffer.toString();
	}

	/**
	 * @return True if this Page is dirty and needs to be replicated
	 */
	public final boolean isDirty()
	{
		return getFlag(FLAG_IS_DIRTY);
	}

	/**
	 * Override this method and return true if your page is used to display
	 * Wicket errors. This can help the framework prevent infinite failure
	 * loops.
	 * 
	 * @return True if this page is intended to display an error to the end
	 *         user.
	 */
	public boolean isErrorPage()
	{
		return false;
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
		final RequestCycle cycle = getRequestCycle();
	
		// Do not need to update cluster when redirecting to a page
		cycle.setUpdateCluster(false);
		
		// This method is used when redirecting to a page
		cycle.setResponsePage(this);
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page.
	 * The current request's url is saved for future use by method continueToOriginalDestination();
	 * Only use this method when you plan to continue to the current url at some later time;
	 * otherwise just use setResponsePage or - when you are in a constructor or checkAccessMethod,
	 * call redirectTo.
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	public final void doRender()
	{
		try
		{
			// we have to initialize the page's request now

			// first, give priority to IFeedback instances, as they have to collect their
			// message before components like ListViews remove any child components
			visitChildren(IFeedback.class, new IVisitor()
			{
				public Object component(Component component)
				{
					component.internalBeginRequest();
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});
			
			// now, do the initialization for the other components
			internalBeginRequest();
		

			// Handle request by rendering page
			render();

			// Check rendering if it happened fully
			checkRendering();
		}
		finally
		{
			// The request is over
			internalEndRequest();
		}
	}

	/**
	 * @param dirty
	 *            True to make this page dirty, false to make it clean.
	 */
	public final void setDirty(final boolean dirty)
	{
		setFlag(FLAG_IS_DIRTY, dirty);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param pageMapName
	 *            Sets this page into the page map with the given name. If the
	 *            page map does not yet exist, it is automatically created.
	 */
	public final void setPageMap(final String pageMapName)
	{
		// Save name for restoring transient
		this.pageMapName = pageMapName;

		// Get or create page map
		final Session session = getSession();
		this.pageMap = session.getPageMap(pageMapName);
		if (this.pageMap == null)
		{
			// TODO This could potentially enable denial of service attacks. We
			// may want to limit pagemaps created via URLs (see Session.java)
			this.pageMap = session.newPageMap(pageMapName);
		}
	}

	/**
	 * Get the string representation of this container.
	 * 
	 * @return String representation of this container
	 */
	public String toString()
	{
		return "[Page class = " + getClass().getName() + ", id = " + getId() + "]";
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Gets a url that invokes the given listener interface on the given
	 * component. The listener interface can be any interface with a single
	 * method taking no arguments which has been registered with a call to
	 * {@link RequestCycle#registerRequestListenerInterface(Class)}. The
	 * component must implement the given interface.
	 * 
	 * @param component
	 *            Component implementing listener interface
	 * @param listenerInterface
	 *            The listener interface to invoke when the URL is requested
	 * @return A URL that encodes an interface to call on a given component
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * Gets the url for the given page class using the given parameters.
	 * 
	 * @param pageMapName
	 *            Name of pagemap to use
	 * @param pageClass
	 *            Class of page
	 * @param parameters
	 *            Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public abstract String urlFor(final String pageMapName, final Class pageClass,
			final PageParameters parameters);

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

		// In case the Page markup contained a <?xml ..?> to determine the
		// markup's encoding, than forward that very same declaration to
		// the browser. The xml declaration of all components on the page
		// are swallowed. Note: this is a potential issue in cases where
		// the page's encoding (e.g. ascii) does not allow for special
		// characters used in the contained components. The user has to
		// make sure that the Page's encoding allow for all characters
		// required.

		// Note:

		final MarkupStream markupStream = findMarkupStream();
		if ((markupStream != null) && (markupStream.getXmlDeclaration() != null))
		{
			// Set content type based on markup type for page
			response.setContentType("text/" + getMarkupType() + "; charset="
					+ markupStream.getEncoding());

			response.write(markupStream.getXmlDeclaration());
		}
		else
		{
			// Set content type based on markup type for page
			response.setContentType("text/" + getMarkupType());
		}

		// Set response locale from session locale
		response.setLocale(getSession().getLocale());
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * @see wicket.Component#internalOnEndRequest()
	 */
	protected final void internalOnEndRequest()
	{
		if(log.isDebugEnabled())
		{
			log.debug("ending request for page " + this + ", request " + getRequest());
		}
		// visit all this page's children to detach the models
		visitChildren(new IVisitor()
		{
			public Object component(Component component)
			{
				try
				{
					// detach any models of the component
					component.detachModels();
				}
				catch (Exception e) // catch anything; we MUST detach all models
				{
					log.error("detaching models of component " + component + " failed:", e);
				}
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		detachModel();
		
		if (isVersioned())
		{
			// Any changes to the page after this point will be tracked by the
			// page's version manager. Since trackChanges is never set to false,
			// this effectively means that change tracking begins after the
			// first request to a page completes.
			setFlag(FLAG_TRACK_CHANGES, true);

			endVersion();
		}
	}


	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 */
	private final void endVersion()
	{
		// If a new version was created
		if (getFlag(FLAG_NEW_VERSION))
		{
			// We're done with this version
			if (versionManager != null)
			{
				versionManager.endVersion();
			}

			// Reset boolean for next request
			setFlag(FLAG_NEW_VERSION, false);
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * @see wicket.Component#internalOnModelChanged()
	 */
	protected final void internalOnModelChanged()
	{
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
	 * Renders this container to the given response object.
	 */
	protected final void onRender()
	{
		// Check access to page
		if (checkAccess())
		{
			// Set page's associated markup stream
			final MarkupStream markupStream = getAssociatedMarkupStream();
			setMarkupStream(markupStream);

			// Configure response object with locale and content type
			configureResponse();

			// Render all the page's markup
			setFlag(FLAG_IS_RENDERING, true);
			try
			{
				renderAll(markupStream);
			}
			finally
			{
				setFlag(FLAG_IS_RENDERING, false);
			}
		}
	}

	/**
	 * @param component
	 *            The component that was added
	 */
	final void componentAdded(final Component component)
	{
		setDirty(true);
		if (isVersioned(component))
		{
			versionManager.componentAdded(component);
		}
	}

	/**
	 * @param component
	 *            The component whose model is about to change
	 */
	final void componentModelChanging(final Component component)
	{
		setDirty(true);
		if (isVersioned(component))
		{
			versionManager.componentModelChanging(component);
		}
	}
	
	final void componentStateChanging(final Component component, Change change)
	{
		setDirty(true);
		if (isVersioned(component))
		{
			versionManager.componentStateChanging(change);
		}
	}

	/**
	 * @param component
	 *            The component that was removed
	 */
	final void componentRemoved(final Component component)
	{
		setDirty(true);
		if (isVersioned(component))
		{
			versionManager.componentRemoved(component);
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
	 * This method is not called getSession() because we want to ensure that
	 * getSession() is final in Component.
	 * 
	 * @return Session for this page
	 */
	final Session getSessionInternal()
	{
		if (this.session == null)
		{
			this.session = Session.get();
			if (this.session == null)
			{
				throw new IllegalStateException("Internal Error: Page not attached to session");
			}
		}
		return this.session;
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
	 * Set the id for this Page. This method is called by PageMap when a Page is
	 * added because the id, which is assigned by PageMap, is not known until
	 * this time.
	 * 
	 * @param id
	 *            The id
	 */
	final void setId(final int id)
	{
		setId(Integer.toString(id));
	}

	/**
	 * Throw an exception if not all components rendered.
	 */
	private final void checkRendering()
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		final ApplicationSettings settings = getSession().getApplication().getSettings();
		if (settings.getComponentUseCheck() && !getResponse().isRedirect())
		{
			final Count unrenderedComponents = new Count();
			final List unrenderedAutoComponents = new ArrayList();
			final StringBuffer buffer = new StringBuffer();
			visitChildren(new IVisitor()
			{
				public Object component(final Component component)
				{
					// If component never rendered
					if (renderedComponents == null || !renderedComponents.contains(component))
					{
						if (component.isAuto())
						{
							// Add to list of unrendered auto components to
							// delete below
							unrenderedAutoComponents.add(component);
						}
						else
						{
							// Increase number of unrendered components
							unrenderedComponents.increment();

							// Add to explanatory string to buffer
							buffer.append(Integer.toString(unrenderedComponents.getCount()) + ". "
									+ component + "\n");
						}
					}
					return CONTINUE_TRAVERSAL;
				}
			});

			// Remove any unrendered auto components since versioning couldn't
			// do it. We can't remove the component in the above visitChildren
			// callback because we're traversing the list at that time.
			for (int i = 0; i < unrenderedAutoComponents.size(); i++)
			{
				((Component)unrenderedAutoComponents.get(i)).remove();
			}

			// Get rid of set
			renderedComponents = null;

			// Throw exception if any errors were found
			if (unrenderedComponents.getCount() > 0)
			{
				// Throw exception
				throw new WicketRuntimeException("The component(s) below failed to render:\n\n"
						+ buffer.toString());
			}
		}
	}

	/**
	 * Initializes Page by adding it to the Session and initializing it.
	 */
	private final void init()
	{
		// All Pages are born dirty so they get clustered right away
		setDirty(true);

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
	 * @param component
	 *            The component which is affected
	 * @return True if the change is okay to report
	 */
	private final boolean isVersioned(final Component component)
	{
		// Auto components do not participate in versioning since they are
		// added during the rendering phase (which is normally illegal).
		if (component.isAuto())
		{
			return false;
		}
		else
		{
			// Throw exception if modification is attempted during rendering
			if (getFlag(FLAG_IS_RENDERING))
			{
				throw new WicketRuntimeException(
						"Cannot modify component hierarchy during render phase");
			}

			// Start new version?
			if (getFlag(FLAG_TRACK_CHANGES) && component.isVersioned())
			{
				// Start new version
				newVersion();

				// Okay to record version information
				return true;
			}

			// Not tracking changes or component not versioned
			return false;
		}
	}

	/**
	 * Starts a new version of this page
	 */
	private final void newVersion()
	{
		// If we have no version manager
		if (versionManager == null)
		{
			// then install a new version manager
			versionManager = newVersionManager();
		}

		// If a new version has not yet been started
		if (!getFlag(FLAG_NEW_VERSION))
		{
			// start a new version
			versionManager.beginVersion();
			setFlag(FLAG_NEW_VERSION, true);
		}
	}

	static
	{
		// Allow calls through the IRedirectListener interface
		RequestCycle.registerRequestListenerInterface(IRedirectListener.class);
	}
}
