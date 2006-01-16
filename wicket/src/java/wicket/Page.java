/*
 * $Id$ $Revision:
 * 1.170 $ $Date$
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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.behavior.IBehaviorListener;
import wicket.feedback.FeedbackMessages;
import wicket.feedback.IFeedback;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.model.IModel;
import wicket.request.IRequestCodingStrategy;
import wicket.request.target.BookmarkablePageRequestTarget;
import wicket.request.target.ListenerInterfaceRequestTarget;
import wicket.request.target.SharedResourceRequestTarget;
import wicket.session.pagemap.IPageMapEntry;
import wicket.settings.IDebugSettings;
import wicket.settings.IPageSettings;
import wicket.util.lang.Classes;
import wicket.util.lang.Objects;
import wicket.util.string.StringValue;
import wicket.util.value.Count;
import wicket.version.IPageVersionManager;
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
 * Component.setResponsePage() or Page.redirectToInterceptPage(). This can be
 * used to allow a user to authenticate themselves if they were denied access.
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

	/**
	 * The access sequence number for this page within the session. Used to
	 * implement page eviction policies.
	 */
	private short accessSequenceNumber;

	/** Used to create page-unique numbers */
	private short autoIndex;

	/** Feedback messages for this page */
	private FeedbackMessages feedbackMessages;

	/** Numeric version of this page's id */
	private short numericId;

	/**
	 * MetaDataEntry array for efficient representation of metadata associated
	 * with child components
	 */
	private MetaDataEntry[] metaData;

	/** The PageMap within the session that this page is stored in */
	private transient PageMap pageMap;

	/** Name of PageMap that this page is stored in */
	private String pageMapName;

	/** Set of components that rendered if component use checking is enabled */
	private transient Set renderedComponents;

	/**
	 * Boolean if the page is stateless, so it doesn't have to be in the page
	 * map, will be set in urlFor
	 */
	private transient boolean stateless = true;

	/** Version manager for this page */
	private IPageVersionManager versionManager;

	/**
	 * Class used for holding meta data entries for components on this Page.
	 * Since most Components are not expected to have metadata, this saves space
	 * over having each component hold its own metadata list
	 */
	private static class MetaDataEntry
	{
		Component component;
		MetaDataKey key;
		Serializable object;
	}

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
	 * Called right after a component's listener method (the provided method
	 * argument) was called. This method may be used to clean up dependencies,
	 * do logging, etc. NOTE: this method will also be called when
	 * {@link WebPage#beforeCallComponent(Component, Method)} or the method
	 * invocation itself failed.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param method
	 *            the method of that component that is to be called
	 */
	public void afterCallComponent(final Component component, final Method method)
	{
	}

	/**
	 * Called just before a component's listener method (the provided method
	 * argument) is called. This method may be used to set up dependencies,
	 * enforce authorization, etc. NOTE: if this method fails, the method will
	 * not be excuted. Method
	 * {@link WebPage#afterCallComponent(Component, Method)} will always be
	 * called.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param method
	 *            the method of that component that is to be called
	 */
	public void beforeCallComponent(final Component component, final Method method)
	{
	}

	/**
	 * <p>
	 * Whether access is allowed to this page. If the page is not allowed you
	 * must redirect to a another page, else you will get a blank page.
	 * Redirecting to another page can be done in a few ways:
	 * <li>Use redirectToInterceptPage(Page page), You will be redirected to
	 * that page when it is done you will be returned to this one</li>
	 * <li>Use redirectTo(Page page), You will be redirected to that page when
	 * it is done you will have to specify where you will go next</li>
	 * <li>RequestCycle.setResponsePage(Page page), That page is rendered
	 * directly, no redirect will happen</li>
	 * </p>
	 * <p>
	 * NOTE: this method is not meant to be called by framework clients.
	 * </p>
	 * 
	 * @return true if access is allowed, false otherwise
	 */
	public boolean checkAccess()
	{
		return ACCESS_ALLOWED;
	}

	/**
	 * Redirects to any intercept page previously specified by a call to
	 * redirectToInterceptPage.
	 * 
	 * @return True if an original destination was redirected to
	 * @see Page#redirectToInterceptPage(Page)
	 */
	public final boolean continueToOriginalDestination()
	{
		return getPageMap().continueToOriginalDestination();
	}

	/**
	 * Detaches any attached models referenced by this page.
	 */
	public void detachModels()
	{
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
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	public final void doRender()
	{
		// Make sure it is really empty
		renderedComponents = null;

		// Reset it to stateless is false so that
		this.stateless = true;

		// Set form component values from cookies
		setFormComponentValuesFromCookies();

		try
		{
			// We have to initialize the page's request now

			// First, give priority to IFeedback instances, as they have to
			// collect their messages before components like ListViews
			// remove any child components
			visitChildren(IFeedback.class, new IVisitor()
			{
				public Object component(Component component)
				{
					((IFeedback)component).updateFeedback();
					component.internalBeginRequest();
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});

			// Now, do the initialization for the other components
			internalBeginRequest();

			// Handle request by rendering page
			render();

			// Check rendering if it happened fully
			checkRendering(this);

			// Add/touch the response page in the session (its pagemap).
			getSession().touch(this);

		}
		finally
		{
			// The request is over
			internalEndRequest();
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
	 * @see wicket.session.pagemap.IPageMapEntry#getAccessSequenceNumber()
	 */
	public int getAccessSequenceNumber()
	{
		return accessSequenceNumber;
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
	public final PageMap getPageMap()
	{
		if (pageMap == null)
		{
			setPageMap(pageMapName);
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
	public final int getSize()
	{
		this.pageMap = null;
		return Objects.sizeof(this);
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
						+ ":" + Classes.name(component.getClass()));
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
	 * Redirect to this page.
	 * 
	 * @see wicket.IRedirectListener#onRedirect()
	 */
	public final void onRedirect()
	{
	}

	/**
	 * Redirects browser to an intermediate page such as a sign-in page. The
	 * current request's url is saved for future use by method
	 * continueToOriginalDestination(); Only use this method when you plan to
	 * continue to the current url at some later time; otherwise just use
	 * setResponsePage or - when you are in a constructor or checkAccessMethod,
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
	 * @param accessSequenceNumber
	 *            New access sequence number for this page
	 */
	public void setAccessSequenceNumber(int accessSequenceNumber)
	{
		this.accessSequenceNumber = (short)accessSequenceNumber;
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
		return "[Page class = " + getClass().getName() + ", id = " + getId() + "]";
	}

	/**
	 * Returns a URL that references a given interface on a component. When the
	 * URL is requested from the server at a later time, the interface will be
	 * called. A URL returned by this method will not be stable across sessions
	 * and cannot be bookmarked by a user.
	 * 
	 * @param component
	 *            The component to reference
	 * @param listenerInterface
	 *            The listener interface on the component
	 * @return A URL that encodes a page, component and interface to call
	 */
	public final String urlFor(final Component component, final Class listenerInterface)
	{
		// The page is not stateless if it is not an IRedirectListener
		// if (!IRedirectListener.class.isAssignableFrom(listenerInterface))
		{
			stateless = false;
		}

		String interfaceName = Classes.name(listenerInterface);
		RequestCycle requestCycle = getRequestCycle();
		IRequestTarget target = new ListenerInterfaceRequestTarget(this, component, requestCycle
				.getRequestInterfaceMethod(interfaceName));
		IRequestCodingStrategy requestCodingStrategy = requestCycle.getProcessor()
				.getRequestCodingStrategy();
		return requestCodingStrategy.encode(requestCycle, target);
	}

	/**
	 * Returns a URL that references the given request target.
	 * 
	 * @param requestTarget
	 *            the request target to reference
	 * @return a URL that references the given request target
	 */
	public final String urlFor(final IRequestTarget requestTarget)
	{
		RequestCycle requestCycle = getRequestCycle();
		IRequestCodingStrategy requestCodingStrategy = requestCycle.getProcessor()
				.getRequestCodingStrategy();
		return requestCodingStrategy.encode(requestCycle, requestTarget);
	}


	/**
	 * Returns a URL that references a shared resource through the provided
	 * resource key.
	 * 
	 * @param resourceKey
	 *            The application global key of the shared resource
	 * @return The url for the shared resource
	 */
	public final String urlFor(final String resourceKey)
	{
		RequestCycle requestCycle = getRequestCycle();
		String url = requestCycle.getProcessor().getRequestCodingStrategy().encode(requestCycle,
				new SharedResourceRequestTarget(resourceKey));
		return url;
	}

	/**
	 * Returns a bookmarkable URL that references a given page class using a
	 * given set of page parameters. Since the URL which is returned contains
	 * all information necessary to instantiate and render the page, it can be
	 * stored in a user's browser as a stable bookmark.
	 * 
	 * @param pageMapName
	 *            Name of pagemap to use
	 * @param pageClass
	 *            Class of page
	 * @param parameters
	 *            Parameters to page
	 * @return Bookmarkable URL to page
	 */
	public final String urlFor(final String pageMapName, final Class pageClass,
			final PageParameters parameters)
	{
		IRequestTarget target = new BookmarkablePageRequestTarget(pageMapName, pageClass,
				parameters);
		RequestCycle requestCycle = getRequestCycle();
		IRequestCodingStrategy requestCodingStrategy = requestCycle.getProcessor()
				.getRequestCodingStrategy();
		return requestCodingStrategy.encode(requestCycle, target);
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
			response.write("<?xml version='1.0' encoding='" + encoding + "'?>");
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
		if (log.isDebugEnabled())
		{
			log.debug("ending request for page " + this + ", request " + getRequest());
		}

		detachModels();

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
		final IPageSettings settings = getSession().getApplication().getPageSettings();
		return new UndoPageVersionManager(this, settings.getMaxPageVersions());
	}

	/**
	 * Renders this container to the given response object.
	 * 
	 * @param markupStream
	 */
	protected final void onRender(final MarkupStream markupStream)
	{
		// Visit all this page's children to reset markup streams and check
		// rendering authorization, as appropriate. We set any result; positive
		// or negative as a temporary boolean in the components, and when a
		// authorization exception is thrown it will block the rendering of this
		// page
		visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
				// Find out if this component can be rendered
				final boolean renderAllowed = component.authorize(RENDER);
				if (renderAllowed)
				{
					// It could be that the markup stream has been reloaded
					// (modified) and that the markup stream positions are no
					// longer valid.
					component.resetMarkupStream();
				}

				// Authorize rendering
				component.setRenderAllowed(renderAllowed);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});

		// Set page's associated markup stream
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream();
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

		setDirty(true);
		if (mayTrackChangesFor(component))
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

		setDirty(true);
		if (mayTrackChangesFor(component))
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

		setDirty(true);
		if (mayTrackChangesFor(component))
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
	final void componentRendered(final Component component)
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
						"The markup file must not contain the same wicket:id at the same level: "
								+ component.getId());
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

		setDirty(true);
		if (mayTrackChangesFor(component))
		{
			versionManager.componentStateChanging(change);
		}
	}

	/**
	 * Gets metadata for key on the given component
	 * 
	 * @param component
	 *            The component
	 * @param key
	 *            The key
	 * @return The object
	 */
	Serializable getMetaData(final Component component, final MetaDataKey key)
	{
		if (metaData != null)
		{
			for (int i = 0; i < metaData.length; i++)
			{
				MetaDataEntry m = metaData[i];
				if (component == m.component && key.equals(m.key))
				{
					return m.object;
				}
			}
		}
		return null;
	}

	/**
	 * @return Return true from this method if you want to keep a page out of
	 *         the session.
	 */
	final boolean isStateless()
	{
		return stateless;
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
	 * Sets metadata on a given component using a given key
	 * 
	 * @param component
	 *            The component
	 * @param key
	 *            The key
	 * @param object
	 *            The object
	 * @throws IllegalArgumentException
	 *             Thrown if the object is not of the correct type for the key
	 */
	void setMetaData(final Component component, final MetaDataKey key, final Serializable object)
	{
		key.checkType(object);
		boolean set = false;
		if (metaData != null)
		{
			for (int i = 0; i < metaData.length; i++)
			{
				MetaDataEntry m = metaData[i];
				if (component == m.component && key.equals(m.key))
				{
					m.object = object;
					set = true;
				}
			}
		}
		if (!set)
		{
			MetaDataEntry m = new MetaDataEntry();
			m.component = component;
			m.key = key;
			m.object = object;
			if (metaData == null)
			{
				metaData = new MetaDataEntry[1];
				metaData[0] = m;
			}
			else
			{
				final MetaDataEntry[] newMetaData = new MetaDataEntry[metaData.length + 1];
				System.arraycopy(metaData, 0, newMetaData, 0, metaData.length);
				newMetaData[metaData.length] = m;
				metaData = newMetaData;
			}
		}
	}

	/**
	 * @param pageMapName
	 *            Sets this page into the page map with the given name. If the
	 *            page map does not yet exist, it is automatically created.
	 */
	final void setPageMap(final String pageMapName)
	{
		// Save name for restoring transient
		this.pageMapName = pageMapName;

		// Get or create page map
		final Session session = getSession();
		this.pageMap = session.getPageMap(pageMapName);
		if (this.pageMap == null)
		{
			// TODO Security: This could potentially enable denial of service
			// attacks. We may want to limit pagemaps created via URLs (see
			// Session.java)
			this.pageMap = session.newPageMap(pageMapName);
		}
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
				throw new WicketRuntimeException("The component(s) below failed to render:\n\n"
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
	 * Initializes Page by adding it to the Session and initializing it.
	 */
	private final void init()
	{
		// All Pages are born dirty so they get clustered right away
		setDirty(true);

		// Set the pagemap
		setPageMap(getRequestCycle() != null ? getRequestCycle().getRequest().getParameter(
				"pagemap") : null);

		// Set the numeric id on this page
		setNumericId(getPageMap().nextId());

		// Set versioning of page based on default
		setVersioned(Application.get().getPageSettings().getVersionPagesByDefault());
	}

	/**
	 * For the given component, whether we may record changes.
	 * 
	 * @param component
	 *            The component which is affected
	 * @return True if the change is okay to report
	 */
	private final boolean mayTrackChangesFor(final Component component)
	{
		// Auto components do not participate in versioning since they are
		// added during the rendering phase (which is normally illegal).
		if (component.isAuto() || (!component.isVersioned()))
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
						versionManager = newVersionManager();
					}

					// start a new version
					versionManager.beginVersion();
					setFlag(FLAG_NEW_VERSION, true);
				}

				// return true as we are ready for versioning
				return true;
			}

			// we are not tracking changes or the component not versioned
			return false;
		}
	}

	static
	{
		// Allow calls through the IRedirectListener interface
		RequestCycle.registerRequestListenerInterface(IRedirectListener.class);

		// Allow XmlHttpRequest calls
		RequestCycle.registerRequestListenerInterface(IBehaviorListener.class);
	}

}
