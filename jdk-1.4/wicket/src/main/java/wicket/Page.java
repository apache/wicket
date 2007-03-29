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
package wicket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.authorization.UnauthorizedActionException;
import wicket.feedback.FeedbackMessages;
import wicket.feedback.IFeedback;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.model.IModel;
import wicket.request.RequestParameters;
import wicket.session.pagemap.IPageMapEntry;
import wicket.settings.IDebugSettings;
import wicket.util.concurrent.ConcurrentHashMap;
import wicket.util.lang.Classes;
import wicket.util.lang.Objects;
import wicket.util.string.StringValue;
import wicket.util.value.Count;
import wicket.version.IPageVersionManager;
import wicket.version.undo.Change;

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
 * will not have any session information encoded in it, and that you can call
 * this page directly without having a session first directly from your
 * browser), you need to implement your Page with a no-arg constructor or with a
 * constructor that accepts a PageParameters argument (which wraps any query
 * string parameters for a request). In case the page has both constructors, the
 * constructor with PageParameters will be used.
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
 * property expression other than the component's name for retrieving the model
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
 * Component.setResponsePage() or Component.redirectToInterceptPage(). This can
 * be used to allow a user to authenticate themselves if they were denied
 * access.
 * 
 * @see wicket.markup.html.WebPage
 * @see wicket.MarkupContainer
 * @see wicket.model.CompoundPropertyModel
 * @see wicket.model.BoundCompoundPropertyModel
 * @see wicket.Component
 * @see wicket.version.IPageVersionManager
 * @see wicket.version.undo.UndoPageVersionManager
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public abstract class Page extends MarkupContainer implements IRedirectListener, IPageMapEntry
{
	private static final long serialVersionUID = 1L;

	/**
	 * {@link #isBookmarkable()} is expensive, we cache the result here
	 */
	private static final ConcurrentHashMap pageClassToBookmarkableCache = new ConcurrentHashMap();

	/**
	 * When passed to {@link Page#getVersion(int)} the latest page version is
	 * returned.
	 */
	public static final int LATEST_VERSION = -1;

	/** True if this page is currently rendering. */
	private static final short FLAG_IS_RENDERING = FLAG_RESERVED2;

	/** True if a new version was created for this request. */
	private static final short FLAG_NEW_VERSION = FLAG_RESERVED3;

	/** True if component changes are being tracked. */
	private static final short FLAG_TRACK_CHANGES = FLAG_RESERVED4;

	/** True if the page should try to be stateless */
	private static final int FLAG_STATELESS_HINT = FLAG_RESERVED5;

	/** Log. */
	private static final Log log = LogFactory.getLog(Page.class);

	/** Used to create page-unique numbers */
	private short autoIndex;

	/** Feedback messages for this page */
	private FeedbackMessages feedbackMessages;

	/** Numeric version of this page's id */
	private short numericId;

	/** The PageMap within the session that this page is stored in */
	private transient IPageMap pageMap;

	/** Name of PageMap that this page is stored in */
	private String pageMapName;

	/** Set of components that rendered if component use checking is enabled */
	private transient Set renderedComponents;

	/**
	 * Boolean if the page is stateless, so it doesn't have to be in the page
	 * map, will be set in urlFor
	 */
	private transient Boolean stateless = null;

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
	 * Constructor.
	 * 
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
	 * Constructor.
	 * 
	 * @param pageMap
	 *            The page map to put this page in
	 */
	protected Page(final IPageMap pageMap)
	{
		// A Page's id is not determined until setId is called when the Page is
		// added to a PageMap in the Session.
		super(null);
		init(pageMap);
	}

	/**
	 * Constructor.
	 * 
	 * @param pageMap
	 *            the name of the page map to put this page in
	 * @param model
	 *            See Component
	 * @see Component#Component(String, IModel)
	 */
	protected Page(final IPageMap pageMap, final IModel model)
	{
		// A Page's id is not determined until setId is called when the Page is
		// added to a PageMap in the Session.
		super(null, model);
		init(pageMap);
	}


	/**
	 * Called right after a component's listener method (the provided method
	 * argument) was called. This method may be used to clean up dependencies,
	 * do logging, etc. NOTE: this method will also be called when
	 * {@link WebPage#beforeCallComponent(Component, RequestListenerInterface)}
	 * or the method invocation itself failed.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param listener
	 *            the listener of that component that is to be called
	 */
	public void afterCallComponent(final Component component,
			final RequestListenerInterface listener)
	{
	}

	/**
	 * Called just before a component's listener method (the provided method
	 * argument) is called. This method may be used to set up dependencies,
	 * enforce authorization, etc. NOTE: if this method fails, the method will
	 * not be excuted. Method
	 * {@link WebPage#afterCallComponent(Component, RequestListenerInterface)}
	 * will always be called.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param listener
	 *            the listener of that component that is to be called
	 */
	public void beforeCallComponent(final Component component,
			final RequestListenerInterface listener)
	{
	}


	/**
	 * Detaches any attached models referenced by this page.
	 */
	public void detachModels()
	{
//		// visit all this page's children to detach the models
//		visitChildren(new IVisitor()
//		{
//			public Object component(Component component)
//			{
//				try
//				{
//					// detach any models of the component
//					component.detachModels();
//				}
//				catch (Exception e) // catch anything; we MUST detach all models
//				{
//					log.error("detaching models of component " + component + " failed:", e);
//				}
//				return IVisitor.CONTINUE_TRAVERSAL;
//			}
//		});

		super.detachModels();
	}

	/**
	 * Mark this page as dirty in the session
	 */
	public final void dirty()
	{
		Session.get().dirtyPage(this);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	public final void renderPage()
	{
		// first try to check if the page can be rendered:
		if (!isActionAuthorized(RENDER))
		{
			if (log.isDebugEnabled())
			{
				log.debug("Page not allowed to render: " + this);
			}
			throw new UnauthorizedActionException(this, Component.RENDER);
		}

		// Make sure it is really empty
		renderedComponents = null;

		// Reset it to stateless so that it can be tested again
		this.stateless = null;

		// Set form component values from cookies
		setFormComponentValuesFromCookies();

		// First, give priority to IFeedback instances, as they have to
		// collect their messages before components like ListViews
		// remove any child components
		visitChildren(IFeedback.class, new IVisitor()
		{
			public Object component(Component component)
			{
				((IFeedback)component).updateFeedback();
				component.attach();
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		if (this instanceof IFeedback)
		{
			((IFeedback)this).updateFeedback();
		}

		// Now, do the initialization for the other components
		attach();

		// Visit all this page's children to reset markup streams and check
		// rendering authorization, as appropriate. We set any result; positive
		// or negative as a temporary boolean in the components, and when a
		// authorization exception is thrown it will block the rendering of this
		// page

		// first the page itself
		setRenderAllowed(isActionAuthorized(RENDER));
		// children of the page
		visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
				// Find out if this component can be rendered
				final boolean renderAllowed = component.isActionAuthorized(RENDER);

				// Authorize rendering
				component.setRenderAllowed(renderAllowed);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		// Handle request by rendering page
		render(null);

		// Check rendering if it happened fully
		checkRendering(this);

		if (!isPageStateless())
		{
			// trigger creation of the actual session in case it was deferred
			Session.get().getSessionStore().getSessionId(RequestCycle.get().getRequest(), true);
			// Add/touch the response page in the session (its pagemap).
			getSession().touch(this);
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * This method is called when a component was rendered standalone. If it is
	 * a markupcontainer then the rendering for that container is checked.
	 * 
	 * @param component
	 * 
	 */
	public final void endComponentRender(Component component)
	{
		if (component instanceof MarkupContainer)
		{
			checkRendering((MarkupContainer)component);
		}
		else
		{
			renderedComponents = null;
		}
	}

	/**
	 * Expire the oldest version of this page
	 */
	public final void expireOldestVersion()
	{
		if (versionManager != null)
		{
			versionManager.expireOldestVersion();
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Get a page unique number, which will be increased with each call.
	 * 
	 * @return A page unique number
	 */
	public final short getAutoIndex()
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
	 * @return The current ajax version number of this page. 
	 */
	public final int getAjaxVersionNumber()
	{
		return versionManager == null ? 0 : versionManager.getAjaxVersionNumber();
	}
	
	/**
	 * This returns a page instance that is rollbacked the number of versions
	 * that is specified compared to the current page.
	 * 
	 * This is a rollback including ajax versions. 
	 * 
	 * @param numberOfVersions to rollback
	 * @return
	 */
	public final Page rollbackPage(int numberOfVersions)
	{
		Page page =  versionManager == null? this : versionManager.rollbackPage(numberOfVersions);
		getSession().touch(page);
		return page;
	}
	/**
	 * @return Returns feedback messages from all components in this page
	 *         (including the page itself).
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
	 * @see wicket.Component#getId()
	 */
	public final String getId()
	{
		return Integer.toString(numericId);
	}

	/**
	 * @see wicket.session.pagemap.IPageMapEntry#getNumericId()
	 */
	public int getNumericId()
	{
		return numericId;
	}

	/**
	 * @see wicket.session.pagemap.IPageMapEntry#getPageClass()
	 */
	public final Class getPageClass()
	{
		return getClass();
	}

	/**
	 * @return Returns the PageMap that this Page is stored in.
	 */
	public final IPageMap getPageMap()
	{
		// If the transient needs to be restored
		if (pageMap == null)
		{
			// Look the page map up in the session
			pageMap = PageMap.forName(pageMapName);
		}
		return pageMap;
	}

	/**
	 * @return Get a page map entry for this page. By default, this is the page
	 *         itself. But if you know of some way to compress the state for the
	 *         page, you can return a custom implementation that produces the
	 *         page on-the-fly.
	 */
	public IPageMapEntry getPageMapEntry()
	{
		return this;
	}

	/**
	 * @return Size of this page in bytes
	 */
	public final long getSizeInBytes()
	{
		this.pageMap = null;
		return Objects.sizeof(this);
	}

	/**
	 * Returns whether the page should try to be stateless. To be stateless,
	 * getStatelessHint() of every component on page (and it's behavior) must
	 * return true and the page must be bookmarkable.
	 * 
	 * @see wicket.Component#getStatelessHint()
	 */
	public final boolean getStatelessHint()
	{
		return getFlag(FLAG_STATELESS_HINT);
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
			if (versionNumber == 0 || versionNumber == LATEST_VERSION)
			{
				return this;
			}
			else
			{
				log.info("No version manager available to retrieve requested versionNumber "
						+ versionNumber);
				return null;
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
				final Page page;
				if (versionNumber != LATEST_VERSION)
				{
					page = versionManager.getVersion(versionNumber);
				}
				else
				{
					page = versionManager.getVersion(getCurrentVersionNumber());
				}

				// If we went all the way back to the original page
				if (page != null && page.getCurrentVersionNumber() == 0 && page.getAjaxVersionNumber() == 0)
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
	 * @return Number of versions of this page
	 */
	public final int getVersions()
	{
		return versionManager == null ? 1 : versionManager.getVersions() + 1;
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
						+ ":" + Classes.simpleName(component.getClass()));
				return null;
			}
		});
		return buffer.toString();
	}

	/**
	 * Bookmarkable page can be instantiated using a bookmarkable URL.
	 * 
	 * @return Returns true if the page is bookmarkable.
	 */
	public boolean isBookmarkable()
	{
		Boolean bookmarkable = (Boolean)pageClassToBookmarkableCache.get(getClass());
		if (bookmarkable == null)
		{
			try
			{

				if (getClass().getConstructor(new Class[] {}) != null)
				{
					bookmarkable = Boolean.TRUE;
				}

			}
			catch (Exception ignore)
			{
				try
				{
					if (getClass().getConstructor(new Class[] { PageParameters.class }) != null)
					{
						bookmarkable = Boolean.TRUE;
					}
				}
				catch (Exception ignore2)
				{
				}
			}
			if (bookmarkable == null)
			{
				bookmarkable = Boolean.FALSE;
			}
			pageClassToBookmarkableCache.put(getClass(), bookmarkable);
		}
		return bookmarkable.booleanValue();

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
	 * Set page stateless
	 * 
	 * @param stateless
	 */
	void setPageStateless(Boolean stateless)
	{
		this.stateless = stateless;
	}

	/**
	 * Gets whether the page is stateless. Components on stateless page must not
	 * render any statefull urls, and components on statefull page must not
	 * render any stateless urls. Statefull urls are urls, which refer to a
	 * certain (current) page instance.
	 * 
	 * @return Whether to page is stateless
	 */
	public final boolean isPageStateless()
	{
		if (isBookmarkable() == false)
		{
			stateless = Boolean.FALSE;
			if (getStatelessHint())
			{
				log.warn("Page '" + this + "' is not stateless because it is not bookmarkable, "
						+ "but the stateless hint is set to true!");
			}
		}

		if (stateless == null)
		{
			final Object[] returnArray = new Object[1];
			Object returnValue = visitChildren(Component.class, new IVisitor()
			{
				public Object component(Component component)
				{
					if (!component.isStateless())
					{
						returnArray[0] = component;
						return Boolean.FALSE;
					}

					return CONTINUE_TRAVERSAL;
				}
			});
			if (returnValue == null)
			{
				stateless = Boolean.TRUE;
			}
			else if (returnValue instanceof Boolean)
			{
				stateless = (Boolean)returnValue;
			}

			if (!stateless.booleanValue() && getStatelessHint())
			{
				log.warn("Page '" + this + "' is not stateless because of '" + returnArray[0]
						+ "' but the stateless hint is set to true!");
			}
		}

		return stateless.booleanValue();
	}

	/**
	 * Redirect to this page.
	 * 
	 * @see wicket.IRedirectListener#onRedirect()
	 */
	public final void onRedirect()
	{
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * Set the id for this Page. This method is called by PageMap when a Page is
	 * added because the id, which is assigned by PageMap, is not known until
	 * this time.
	 * 
	 * @param id
	 *            The id
	 */
	public final void setNumericId(final int id)
	{
		this.numericId = (short)id;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * This method is called when a component will be rendered standalone.
	 * 
	 * @param component
	 * 
	 */
	public final void startComponentRender(Component component)
	{
		renderedComponents = null;
	}

	/**
	 * Get the string representation of this container.
	 * 
	 * @return String representation of this container
	 */
	public String toString()
	{
		if(versionManager != null)
		{
			return "[Page class = " + getClass().getName() + ", id = " + getId() + 
				", version = " + versionManager.getCurrentVersionNumber()  + ", ajax = " + 
				versionManager.getAjaxVersionNumber() + "]";	
		}
		else
		{
			return "[Page class = " + getClass().getName() + ", id = " + getId() + ", version = " + 0 + "]";
		}
	}

	/**
	 * Set-up response with appropriate content type, locale and encoding. The
	 * locale is set equal to the session's locale. The content type header
	 * contains information about the markup type (@see #getMarkupType()) and
	 * the encoding. The response (and request) encoding is determined by an
	 * application setting (@see
	 * ApplicationSettings#getResponseRequestEncoding()). In addition, if the
	 * page's markup contains a xml declaration like &lt?xml ... ?&gt; an xml
	 * declaration with proper encoding information is written to the output as
	 * well, provided it is not disabled by an applicaton setting (@see
	 * ApplicationSettings#getStripXmlDeclarationFromOutput()).
	 * <p>
	 * Note: Prior to Wicket 1.1 the output encoding was determined by the
	 * page's markup encoding. Because this caused uncertainties about the
	 * /request/ encoding, it has been changed in favour of the new, much safer,
	 * approach. Please see the Wiki for more details.
	 */
	protected void configureResponse()
	{
		// Get the response and application
		final RequestCycle cycle = getRequestCycle();
		final Application application = cycle.getApplication();
		final Response response = cycle.getResponse();

		// Determine encoding
		final String encoding = application.getRequestCycleSettings().getResponseRequestEncoding();

		// Set content type based on markup type for page
		response.setContentType("text/" + getMarkupType() + "; charset=" + encoding);

		// Write out an xml declaration if the markup stream and settings allow
		final MarkupStream markupStream = findMarkupStream();
		if ((markupStream != null) && (markupStream.getXmlDeclaration() != null)
				&& (application.getMarkupSettings().getStripXmlDeclarationFromOutput() == false))
		{
			response.write("<?xml version='1.0' encoding='");
			response.write(encoding);
			response.write("'?>");
		}

		// Set response locale from session locale
		response.setLocale(getSession().getLocale());
	}

	/**
	 * @see wicket.Component#onDetach()
	 */
	protected void onDetach()
	{
		if (log.isDebugEnabled())
		{
			log.debug("ending request for page " + this + ", request " + getRequest());
		}

		endVersion();
		
		super.onDetach();
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
	protected final IPageVersionManager newVersionManager()
	{
		return null;
	}

	/**
	 * Renders this container to the given response object.
	 * 
	 * @param markupStream
	 */
	protected void onRender(final MarkupStream markupStream)
	{
		// Set page's associated markup stream
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream(true);
		setMarkupStream(associatedMarkupStream);

		// Configure response object with locale and content type
		configureResponse();

		// Render all the page's markup
		setFlag(FLAG_IS_RENDERING, true);
		try
		{
			renderAll(associatedMarkupStream);
		}
		finally
		{
			setFlag(FLAG_IS_RENDERING, false);
		}
	}

	/**
	 * A component was added.
	 * 
	 * @param component
	 *            The component that was added
	 */
	final void componentAdded(final Component component)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, component.getParent()))
		{
			versionManager.componentAdded(component);
		}
	}

	/**
	 * A component's model changed.
	 * 
	 * @param component
	 *            The component whose model is about to change
	 */
	final void componentModelChanging(final Component component)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, null))
		{
			versionManager.componentModelChanging(component);
		}
	}

	/**
	 * A component was removed.
	 * 
	 * @param component
	 *            The component that was removed
	 */
	final void componentRemoved(final Component component)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, component.getParent()))
		{
			versionManager.componentRemoved(component);
		}
	}

	/**
	 * Adds a component to the set of rendered components.
	 * 
	 * @param component
	 *            The component that was rendered
	 */
	public final void componentRendered(final Component component)
	{
		// Inform the page that this component rendered
		if (Application.get().getDebugSettings().getComponentUseCheck())
		{
			if (renderedComponents == null)
			{
				renderedComponents = new HashSet();
			}
			if (renderedComponents.add(component) == false)
			{
				throw new MarkupException(
						"The component "
								+ component
								+ " has the same wicket:id as another component already added at the same level");
			}
			if (log.isDebugEnabled())
			{
				log.debug("Rendered " + component);
			}
		}
	}

	final void componentStateChanging(final Component component, Change change)
	{
		checkHierarchyChange(component);

		dirty();
		if (mayTrackChangesFor(component, null))
		{
			versionManager.componentStateChanging(change);
		}
	}

	/**
	 * Sets values for form components based on cookie values in the request.
	 * 
	 */
	final void setFormComponentValuesFromCookies()
	{
		// Visit all Forms contained in the page
		visitChildren(Form.class, new Component.IVisitor()
		{
			// For each FormComponent found on the Page (not Form)
			public Object component(final Component component)
			{
				((Form)component).loadPersistentFormComponentValues();
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * @param pageMap
	 *            Sets this page into the page map with the given name. If the
	 *            page map does not yet exist, it is automatically created.
	 */
	final void setPageMap(final IPageMap pageMap)
	{
		// Save transient reference to pagemap
		this.pageMap = pageMap;

		// Save name for restoring transient
		this.pageMapName = pageMap.getName();
	}

	/**
	 * Sets whether the page should try to be stateless. To be stateless,
	 * getStatelessHint() of every component on page (and it's behavior) must
	 * return true and the page must be bookmarkable.
	 * 
	 * @param value
	 *            whether the page should try to be stateless
	 */
	public final void setStatelessHint(boolean value)
	{
		if (value && !isBookmarkable())
		{
			throw new WicketRuntimeException(
					"Can't set stateless hint to true on a page when the page is not bookmarkable, page: "
							+ this);
		}
		setFlag(FLAG_STATELESS_HINT, value);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * @param map
	 */
	protected final void moveToPageMap(IPageMap map)
	{
		// TODO post 1.2 shouldn't we remove this page from the pagemap/session
		// if it would be in there?
		// This should be done if the page was not cloned first, but shouldn't
		// be done if it was cloned..
		setPageMap(map);
		numericId = (short)map.nextId();
	}


	/**
	 * Checks whether the hierarchy may be changed at all, and throws an
	 * exception if this is not the case.
	 * 
	 * @param component
	 *            the component which is about to be added or removed
	 */
	private void checkHierarchyChange(Component component)
	{
		// Throw exception if modification is attempted during rendering
		if ((!component.isAuto()) && getFlag(FLAG_IS_RENDERING))
		{
			throw new WicketRuntimeException(
					"Cannot modify component hierarchy during render phase");
		}
	}

	/**
	 * Throw an exception if not all components rendered.
	 * 
	 * @param renderedContainer
	 *            The page itself if it was a full page render or the container
	 *            that was rendered standalone
	 */
	private final void checkRendering(final MarkupContainer renderedContainer)
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		final IDebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.getComponentUseCheck() && !getResponse().isRedirect())
		{
			final Count unrenderedComponents = new Count();
			final List unrenderedAutoComponents = new ArrayList();
			final StringBuffer buffer = new StringBuffer();
			renderedContainer.visitChildren(new IVisitor()
			{
				public Object component(final Component component)
				{
					// If component never rendered
					if (renderedComponents == null || !renderedComponents.contains(component))
					{
						// If auto component ...
						if (component.isAuto())
						{
							// Add to list of unrendered auto components to
							// delete below
							unrenderedAutoComponents.add(component);
						}
						else if (component.isVisibleInHierarchy())
						{
							// Increase number of unrendered components
							unrenderedComponents.increment();

							// Add to explanatory string to buffer
							buffer.append(Integer.toString(unrenderedComponents.getCount()) + ". "
									+ component + "\n");
						}
						else
						{
							// if the component is not visible in hierarchy we
							// should not visit its children since they are also
							// not visible
							return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
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

			// Throw exception if any errors were found
			if (unrenderedComponents.getCount() > 0)
			{
				// Get rid of set
				renderedComponents = null;

				// Throw exception
				throw new WicketRuntimeException(
						"The component(s) below failed to render. A common problem is that you have added a component in code but forgot to reference it in the markup (thus the component will never be rendered).\n\n"
								+ buffer.toString());
			}
		}

		// Get rid of set
		renderedComponents = null;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 */
	private final void endVersion()
	{
		// Any changes to the page after this point will be tracked by the
		// page's version manager. Since trackChanges is never set to false,
		// this effectively means that change tracking begins after the
		// first request to a page completes.
		setFlag(FLAG_TRACK_CHANGES, true);
		
		// If a new version was created
		if (getFlag(FLAG_NEW_VERSION))
		{
			// Reset boolean for next request
			setFlag(FLAG_NEW_VERSION, false);

			// We're done with this version
			if (versionManager != null)
			{
				versionManager.endVersion(getRequest().mergeVersion());
			}

			// Evict any page version(s) as need be
			getApplication().getSessionSettings().getPageMapEvictionStrategy().evict(getPageMap());
		}
	}

	/**
	 * Initializes Page by adding it to the Session and initializing it.
	 */
	private final void init()
	{
		final RequestCycle cycle = getRequestCycle();
		String pageMapName = null;
		if (cycle != null)
		{
			RequestParameters parameters = getRequest().getRequestParameters();
			pageMapName = parameters.getPageMapName();
		}
		final IPageMap pageMap = PageMap.forName(pageMapName);
		init(pageMap);
	}

	/**
	 * Initializes Page by adding it to the Session and initializing it.
	 * 
	 * @param pageMap
	 *            The page map to put this page in.
	 */
	private final void init(final IPageMap pageMap)
	{
		// Set the page map
		if (pageMap != null)
		{
			setPageMap(pageMap);
		}
		else
		{
			throw new IllegalStateException("PageMap cannot be null");
		}

		// Set the numeric id on this page
		setNumericId(getPageMap().nextId());

		// Set versioning of page based on default
		setVersioned(Application.get().getPageSettings().getVersionPagesByDefault());

		// All Pages are born dirty so they get clustered right away
		dirty();
	}

	/**
	 * For the given component, whether we may record changes.
	 * 
	 * @param component
	 *            The component which is affected
	 * @param parent
	 * @return True if the change is okay to report
	 */
	private final boolean mayTrackChangesFor(final Component component, MarkupContainer parent)
	{
		// Auto components do not participate in versioning since they are
		// added during the rendering phase (which is normally illegal).
		if (component.isAuto() || (parent == null && !component.isVersioned())
				|| (parent != null && !parent.isVersioned()))
		{
			return false;
		}
		else
		{
			// the component is versioned... are we tracking changes at all?
			if (getFlag(FLAG_TRACK_CHANGES))
			{
				// we are tracking changes... do we need to start new version?
				if (!getFlag(FLAG_NEW_VERSION))
				{
					// if we have no version manager
					if (versionManager == null)
					{
						// then install a new version manager
						versionManager = getSession().getSessionStore().newVersionManager(this);
					}

					// start a new version
					versionManager.beginVersion(getRequest().mergeVersion());
					setFlag(FLAG_NEW_VERSION, true);
				}

				// return true as we are ready for versioning
				return true;
			}

			// we are not tracking changes or the component not versioned
			return false;
		}
	}

	/**
	 * Call this method when the current (ajax) request shouldn't merge 
	 * the changes that are happening to the page with the previous version. 
	 * 
	 * This is for example needed when you want to redirect to this 
	 * page in an ajax request and then you do want to version normally.. 
	 * 
	 * This method doesn't do anything if the getRequest().mergeVersion
	 * doesn't return true.
	 */
	public final void ignoreVersionMerge()
	{
		if (getRequest().mergeVersion())
		{
			mayTrackChangesFor(this, null);
			if (versionManager != null)
			{
				versionManager.ignoreVersionMerge();
			}
		}
	}
}
