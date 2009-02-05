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
package org.apache.wicket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.settings.IDebugSettings;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.version.IPageVersionManager;
import org.apache.wicket.version.undo.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract base class for pages. As a MarkupContainer subclass, a Page can contain a component
 * hierarchy and markup in some markup language such as HTML. Users of the framework should not
 * attempt to subclass Page directly. Instead they should subclass a subclass of Page that is
 * appropriate to the markup type they are using, such as WebPage (for HTML markup).
 * <ul>
 * <li><b>Construction </b>- When a page is constructed, it is automatically added to the current
 * PageMap in the Session. When a Page is added to the Session's PageMap, the PageMap assigns the
 * Page an id. A PageMap is roughly equivalent to a browser window and encapsulates a set of pages
 * accessible through that window. When a popup window is created, a new PageMap is created for the
 * popup.
 * 
 * <li><b>Identity </b>- The Session that a Page is contained in can be retrieved by calling
 * Page.getSession(). Page identifiers start at 0 for each PageMap in the Session and increment as
 * new pages are added to the map. The PageMap-(and Session)-unique identifier assigned to a given
 * Page can be retrieved by calling getId(). So, the first Page added to a new user Session will
 * always be named "0".
 * 
 * <li><b>LifeCycle </b>- Subclasses of Page which are interested in lifecycle events can override
 * onBeginRequest, onEndRequest() and onModelChanged(). The onBeginRequest() method is inherited
 * from Component. A call to onBeginRequest() is made for every Component on a Page before page
 * rendering begins. At the end of a request (when rendering has completed) to a Page, the
 * onEndRequest() method is called for every Component on the Page.
 * 
 * <li><b>Nested Component Hierarchy </b>- The Page class is a subclass of MarkupContainer. All
 * MarkupContainers can have "associated markup", which resides alongside the Java code by default.
 * All MarkupContainers are also Component containers. Through nesting, of containers, a Page can
 * contain any arbitrary tree of Components. For more details on MarkupContainers, see
 * {@link org.apache.wicket.MarkupContainer}.
 * 
 * <li><b>Bookmarkable Pages </b>- Pages can be constructed with any constructor when they are being
 * used in a Wicket session, but if you wish to link to a Page using a URL that is "bookmarkable"
 * (which implies that the URL will not have any session information encoded in it, and that you can
 * call this page directly without having a session first directly from your browser), you need to
 * implement your Page with a no-arg constructor or with a constructor that accepts a PageParameters
 * argument (which wraps any query string parameters for a request). In case the page has both
 * constructors, the constructor with PageParameters will be used.
 * 
 * <li><b>Models </b>- Pages, like other Components, can have models (see {@link IModel}). A Page
 * can be assigned a model by passing one to the Page's constructor, by overriding initModel() or
 * with an explicit invocation of setModel(). If the model is a
 * {@link org.apache.wicket.model.CompoundPropertyModel}, Components on the Page can use the Page's
 * model implicitly via container inheritance. If a Component is not assigned a model, the
 * initModel() override in Component will cause that Component to use the nearest CompoundModel in
 * the parent chain, in this case, the Page's model. For basic CompoundModels, the name of the
 * Component determines which property of the implicit page model the component is bound to. If more
 * control is desired over the binding of Components to the page model (for example, if you want to
 * specify some property expression other than the component's name for retrieving the model
 * object), BoundCompoundPropertyModel can be used.
 * 
 * <li><b>Back Button </b>- Pages can support the back button by enabling versioning with a call to
 * setVersioned(boolean). If a Page is versioned and changes occur to it which need to be tracked, a
 * version manager will be installed using the {@link ISessionStore}'s factory method
 * newVersionManager().
 * 
 * <li><b>Security </b>- See {@link IAuthorizationStrategy}, {@link SimplePageAuthorizationStrategy}
 * 
 * @see org.apache.wicket.markup.html.WebPage
 * @see org.apache.wicket.MarkupContainer
 * @see org.apache.wicket.model.CompoundPropertyModel
 * @see org.apache.wicket.model.BoundCompoundPropertyModel
 * @see org.apache.wicket.Component
 * @see org.apache.wicket.version.IPageVersionManager
 * @see org.apache.wicket.version.undo.UndoPageVersionManager
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Johan Compagner
 * 
 */
public abstract class Page extends MarkupContainer implements IRedirectListener, IPageMapEntry
{
	/**
	 * You can set implementation of the interface in the {@link Page#serializer} then that
	 * implementation will handle the serialization of this page. The serializePage method is called
	 * from the writeObject method then the implementation override the default serialization.
	 * 
	 * @author jcompagner
	 */
	public static interface IPageSerializer
	{
		/**
		 * Called when page is being deserialized
		 * 
		 * @param id
		 *            TODO
		 * @param name
		 *            TODO
		 * @param page
		 * @param stream
		 * @throws IOException
		 * @throws ClassNotFoundException
		 * 
		 */
		public void deserializePage(int id, String name, Page page, ObjectInputStream stream)
			throws IOException, ClassNotFoundException;

		/**
		 * Called from the {@link Page#writeObject(java.io.ObjectOutputStream)} method.
		 * 
		 * @param page
		 *            The page that must be serialized.
		 * @param stream
		 *            ObjectOutputStream
		 * @throws IOException
		 */

		public void serializePage(Page page, ObjectOutputStream stream) throws IOException;

		/**
		 * Returns object to be serialized instead of given page (called from writeReplace).
		 * 
		 * @param serializedPage
		 * @return object to be serialized instead of page (or the page instance itself)
		 */
		public Object getPageReplacementObject(Page serializedPage);
	}

	/**
	 * When passed to {@link Page#getVersion(int)} the latest page version is returned.
	 */
	public static final int LATEST_VERSION = -1;

	/**
	 * This is a thread local that is used for serializing page references in this page.It stores a
	 * {@link IPageSerializer} which can be set by the outside world to do the serialization of this
	 * page.
	 */
	public static final ThreadLocal<IPageSerializer> serializer = new ThreadLocal<IPageSerializer>();

	/** True if a new version was created for this request. */
	private static final short FLAG_NEW_VERSION = FLAG_RESERVED3;

	/** True if the page should try to be stateless */
	private static final int FLAG_STATELESS_HINT = FLAG_RESERVED5;

	/** True if component changes are being tracked. */
	private static final short FLAG_TRACK_CHANGES = FLAG_RESERVED4;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Page.class);

	/**
	 * {@link #isBookmarkable()} is expensive, we cache the result here
	 */
	private static final ConcurrentHashMap<String, Boolean> pageClassToBookmarkableCache = new ConcurrentHashMap<String, Boolean>();

	private static final long serialVersionUID = 1L;

	/** Used to create page-unique numbers */
	private short autoIndex;

	/** Numeric version of this page's id */
	private int numericId;

	/** The PageMap within the session that this page is stored in */
	private transient IPageMap pageMap;

	/** Name of PageMap that this page is stored in */
	private String pageMapName;

	/** Set of components that rendered if component use checking is enabled */
	private transient Set<Component> renderedComponents;

	/**
	 * Boolean if the page is stateless, so it doesn't have to be in the page map, will be set in
	 * urlFor
	 */
	private transient Boolean stateless = null;

	/** Version manager for this page */
	private IPageVersionManager versionManager;

	/** The page parameters object hat constructed this page */
	private PageParameters parameters;

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
	protected Page(final IModel<?> model)
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
	 *            the page map to put this page in
	 * @param model
	 *            See Component
	 * @see Component#Component(String, IModel)
	 */
	protected Page(final IPageMap pageMap, final IModel<?> model)
	{
		// A Page's id is not determined until setId is called when the Page is
		// added to a PageMap in the Session.
		super(null, model);
		init(pageMap);
	}

	/**
	 * The {@link PageParameters} parameter will be stored in this page and then those parameters
	 * will be used to create stateless links to this bookmarkable page.
	 * 
	 * @param parameters
	 *            externally passed parameters
	 * @see PageParameters
	 */
	protected Page(final PageParameters parameters)
	{
		super(null);
		this.parameters = parameters;
		init();
	}

	/**
	 * The {@link PageParameters} parameter will be stored in this page and then those parameters
	 * will be used to create stateless links to this bookmarkable page.
	 * 
	 * @param pageMap
	 *            the page map to put this page in
	 * @param parameters
	 *            externally passed parameters
	 * @see PageParameters
	 */
	protected Page(final IPageMap pageMap, final PageParameters parameters)
	{
		super(null);
		this.parameters = parameters;
		init(pageMap);
	}

	/**
	 * The {@link PageParameters} object that was used to construct this page. This will be used in
	 * creating stateless/bookmarkable links to this page
	 * 
	 * @return {@link PageParameters} The construction page parameter
	 */
	public PageParameters getPageParameters()
	{
		return parameters;
	}

	/**
	 * Called right after a component's listener method (the provided method argument) was called.
	 * This method may be used to clean up dependencies, do logging, etc. NOTE: this method will
	 * also be called when {@link WebPage#beforeCallComponent(Component, RequestListenerInterface)}
	 * or the method invocation itself failed.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param listener
	 *            the listener of that component that is to be called
	 */
	// TODO Post-1.3: We should create a listener on Application like
	// IComponentInstantiationListener
	// that forwards to IAuthorizationStrategy for RequestListenerInterface
	// invocations.
	public void afterCallComponent(final Component component,
		final RequestListenerInterface listener)
	{
	}

	/**
	 * Called just before a component's listener method (the provided method argument) is called.
	 * This method may be used to set up dependencies, enforce authorization, etc. NOTE: if this
	 * method fails, the method will not be executed. Method
	 * {@link WebPage#afterCallComponent(Component, RequestListenerInterface)} will always be
	 * called.
	 * 
	 * @param component
	 *            the component that is to be called
	 * @param listener
	 *            the listener of that component that is to be called
	 */
	// TODO Post-1.3: We should create a listener on Application like
	// IComponentInstantiationListener
	// that forwards to IAuthorizationStrategy for RequestListenerInterface
	// invocations.
	public void beforeCallComponent(final Component component,
		final RequestListenerInterface listener)
	{
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
				renderedComponents = new HashSet<Component>();
			}
			if (renderedComponents.add(component) == false)
			{
				throw new MarkupException("The component " + component +
					" has the same wicket:id as another component already added at the same level");
			}
			if (log.isDebugEnabled())
			{
				log.debug("Rendered " + component);
			}
		}
	}

	/**
	 * Detaches any attached models referenced by this page.
	 */
	@Override
	public void detachModels()
	{
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * This method is called when a component was rendered standalone. If it is a <code>
	 * MarkupContainer</code>
	 * then the rendering for that container is checked.
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
	 * @return The current ajax version number of this page.
	 */
	public final int getAjaxVersionNumber()
	{
		return versionManager == null ? 0 : versionManager.getAjaxVersionNumber();
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
		return autoIndex++;
	}

	/**
	 * @return The current version number of this page. If the page has been changed once, the
	 *         return value will be 1. If the page has not yet been revised, the version returned
	 *         will be 0, indicating that the page is still in its original state.
	 */
	public final int getCurrentVersionNumber()
	{
		return versionManager == null ? 0 : versionManager.getCurrentVersionNumber();
	}

	/**
	 * @see org.apache.wicket.Component#getId()
	 */
	@Override
	public final String getId()
	{
		return Integer.toString(numericId);
	}

	/**
	 * @see org.apache.wicket.session.pagemap.IPageMapEntry#getNumericId()
	 */
	public int getNumericId()
	{
		return numericId;
	}

	/**
	 * @see org.apache.wicket.session.pagemap.IPageMapEntry#getPageClass()
	 */
	public final Class<? extends Page> getPageClass()
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
	 * @return Get a page map entry for this page. By default, this is the page itself. But if you
	 *         know of some way to compress the state for the page, you can return a custom
	 *         implementation that produces the page on-the-fly.
	 */
	public IPageMapEntry getPageMapEntry()
	{
		return this;
	}

	/**
	 * @return String The PageMap name
	 */
	public final String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * @return Size of this page in bytes
	 */
	@Override
	public final long getSizeInBytes()
	{
		pageMap = null;
		return Objects.sizeof(this);
	}

	/**
	 * Returns whether the page should try to be stateless. To be stateless, getStatelessHint() of
	 * every component on page (and it's behavior) must return true and the page must be
	 * bookmarkable.
	 * 
	 * @see org.apache.wicket.Component#getStatelessHint()
	 */
	@Override
	public final boolean getStatelessHint()
	{
		return getFlag(FLAG_STATELESS_HINT);
	}

	/**
	 * Override this method to implement a custom way of producing a version of a Page when it
	 * cannot be found in the Session.
	 * 
	 * @param versionNumber
	 *            The version desired
	 * @return A Page object with the component/model hierarchy that was attached to this page at
	 *         the time represented by the requested version.
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
				log.info("No version manager available to retrieve requested versionNumber " +
					versionNumber);
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
				if (page != null && page.getCurrentVersionNumber() == 0 &&
					page.getAjaxVersionNumber() == 0)
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
		visitChildren(new IVisitor<Component>()
		{
			public Object component(Component component)
			{
				int levels = 0;
				for (Component current = component; current != null; current = current.getParent())
				{
					levels++;
				}
				buffer.append(StringValue.repeat(levels, "	") + component.getPageRelativePath() +
					":" + Classes.simpleName(component.getClass()));
				return null;
			}
		});
		return buffer.toString();
	}

	/**
	 * Call this method when the current (ajax) request shouldn't merge the changes that are
	 * happening to the page with the previous version.
	 * 
	 * This is for example needed when you want to redirect to this page in an ajax request and then
	 * you do want to version normally..
	 * 
	 * This method doesn't do anything if the getRequest().mergeVersion doesn't return true.
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

	/**
	 * Bookmarkable page can be instantiated using a bookmarkable URL.
	 * 
	 * @return Returns true if the page is bookmarkable.
	 */
	public boolean isBookmarkable()
	{
		Boolean bookmarkable = pageClassToBookmarkableCache.get(getClass().getName());
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
			pageClassToBookmarkableCache.put(getClass().getName(), bookmarkable);
		}
		return bookmarkable.booleanValue();

	}

	/**
	 * Override this method and return true if your page is used to display Wicket errors. This can
	 * help the framework prevent infinite failure loops.
	 * 
	 * @return True if this page is intended to display an error to the end user.
	 */
	public boolean isErrorPage()
	{
		return false;
	}

	/**
	 * Determine the "statelessness" of the page while not changing the cached value.
	 * 
	 * @return boolean value
	 */
	private boolean peekPageStateless()
	{
		Boolean old = stateless;
		Boolean res = isPageStateless();
		stateless = old;
		return res;
	}

	/**
	 * Gets whether the page is stateless. Components on stateless page must not render any
	 * statefull urls, and components on statefull page must not render any stateless urls.
	 * Statefull urls are urls, which refer to a certain (current) page instance.
	 * 
	 * @return Whether this page is stateless
	 */
	public final boolean isPageStateless()
	{
		if (isBookmarkable() == false)
		{
			stateless = Boolean.FALSE;
			if (getStatelessHint())
			{
				log.warn("Page '" + this + "' is not stateless because it is not bookmarkable, " +
					"but the stateless hint is set to true!");
			}
		}

		if (getStatelessHint() == false)
		{
			return false;
		}

		if (stateless == null)
		{
			if (isStateless() == false)
			{
				stateless = Boolean.FALSE;
			}
		}

		if (stateless == null)
		{
			final Object[] returnArray = new Object[1];
			Object returnValue = visitChildren(Component.class, new IVisitor<Component>()
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

			// TODO (matej_k): The stateless hint semantics has been changed, this warning doesn't
			// work anymore. but we don't really have
			// alternative to this
			/*
			 * if (!stateless.booleanValue() && getStatelessHint()) { log.warn("Page '" + this + "'
			 * is not stateless because of '" + returnArray[0] + "' but the stateless hint is set to
			 * true!"); }
			 */
		}

		return stateless.booleanValue();
	}

	/**
	 * Redirect to this page.
	 * 
	 * @see org.apache.wicket.IRedirectListener#onRedirect()
	 */
	public final void onRedirect()
	{
	}

	/**
	 * Convenience method. Search for children of type fromClass and invoke their respective
	 * removePersistedFormData() methods.
	 * 
	 * @param <C>
	 * 
	 * @see Form#removePersistentFormComponentValues(boolean)
	 * 
	 * @param formClass
	 *            Form to be selected. Pages may have more than one Form.
	 * @param disablePersistence
	 *            if true, disable persistence for all FormComponents on that page. If false, it
	 *            will remain unchanged.
	 */
	public final <C extends Form<?>> void removePersistedFormData(final Class<C> formClass,
		final boolean disablePersistence)
	{
		// Check that formClass is an instanceof Form
		if (!Form.class.isAssignableFrom(formClass))
		{
			throw new WicketRuntimeException("Form class " + formClass.getName() +
				" is not a subclass of Form");
		}

		// Visit all children which are an instance of formClass
		visitChildren(formClass, new IVisitor<Component>()
		{
			public Object component(final Component component)
			{
				// They must be of type Form as well
				if (component instanceof Form)
				{
					// Delete persistent FormComponent data and disable
					// persistence
					((Form<?>)component).removePersistentFormComponentValues(disablePersistence);
				}
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 */
	public final void renderPage()
	{

		if (getApplication().getDebugSettings().isOutputMarkupContainerClassName())
		{
			Class<?> klass = getClass();
			while (klass.isAnonymousClass())
			{
				klass = klass.getSuperclass();
			}
			getResponse().write("<!-- Page Class ");
			getResponse().write(klass.getName());
			getResponse().write(" -->\n");
		}

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

		// if the page is stateless, reset the flag so that it is tested again
		if (Boolean.TRUE.equals(stateless))
		{
			stateless = null;
		}

		// Set form component values from cookies
		setFormComponentValuesFromCookies();

		try
		{
			prepareForRender();
		}
		catch (RuntimeException e)
		{
			// if an exception is thrown then we have to call after render
			// else the components could be in a wrong state (rendering)
			try
			{
				afterRender();
			}
			catch (RuntimeException e2)
			{
				// ignore this one could be a result off.
			}
			throw e;
		}


		// Handle request by rendering page
		try
		{
			render(null);
		}
		finally
		{
			afterRender();
		}

		// Check rendering if it happened fully
		checkRendering(this);

		// clean up debug meta data if component check is on
		if (Application.get().getDebugSettings().getComponentUseCheck())
		{
			visitChildren(new IVisitor<Component>()
			{
				public Object component(Component component)
				{
					component.setMetaData(Component.CONSTRUCTED_AT_KEY, null);
					component.setMetaData(Component.ADDED_AT_KEY, null);
					return CONTINUE_TRAVERSAL;
				}
			});
		}

		if (!isPageStateless())
		{
			// trigger creation of the actual session in case it was deferred
			Session.get().getSessionStore().getSessionId(RequestCycle.get().getRequest(), true);
			// Add/touch the response page in the session (its pagemap).
			getSession().touch(this);
		}
	}

	/**
	 * This returns a page instance that is rollbacked the number of versions that is specified
	 * compared to the current page.
	 * 
	 * This is a rollback including ajax versions.
	 * 
	 * @param numberOfVersions
	 *            to rollback
	 * @return rollbacked page instance
	 */
	public final Page rollbackPage(int numberOfVersions)
	{
		Page page = this;
		if (versionManager != null)
		{
			page = versionManager.rollbackPage(numberOfVersions);
		}
		getSession().touch(page);
		return page;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * Set the id for this Page. This method is called by PageMap when a Page is added because the
	 * id, which is assigned by PageMap, is not known until this time.
	 * 
	 * @param id
	 *            The id
	 */
	public final void setNumericId(final int id)
	{
		numericId = id;
	}

	/**
	 * Sets whether the page should try to be stateless. To be stateless, getStatelessHint() of
	 * every component on page (and it's behavior) must return true and the page must be
	 * bookmarkable.
	 * 
	 * @param value
	 *            whether the page should try to be stateless
	 */
	public final void setStatelessHint(boolean value)
	{
		if (value && !isBookmarkable())
		{
			throw new WicketRuntimeException(
				"Can't set stateless hint to true on a page when the page is not bookmarkable, page: " +
					this);
		}
		setFlag(FLAG_STATELESS_HINT, value);
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
	@Override
	public String toString()
	{
		if (versionManager != null)
		{
			return "[Page class = " + getClass().getName() + ", id = " + getId() + ", version = " +
				versionManager.getCurrentVersionNumber() + ", ajax = " +
				versionManager.getAjaxVersionNumber() + "]";
		}
		else
		{
			return "[Page class = " + getClass().getName() + ", id = " + getId() + ", version = " +
				0 + "]";
		}
	}

	/**
	 * Throw an exception if not all components rendered.
	 * 
	 * @param renderedContainer
	 *            The page itself if it was a full page render or the container that was rendered
	 *            standalone
	 */
	private final void checkRendering(final MarkupContainer renderedContainer)
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		final IDebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.getComponentUseCheck() && !getResponse().isRedirect())
		{
			final List<Component> unrenderedComponents = new ArrayList<Component>();
			final StringBuffer buffer = new StringBuffer();
			renderedContainer.visitChildren(new IVisitor<Component>()
			{
				public Object component(final Component component)
				{
					// If component never rendered
					if (renderedComponents == null || !renderedComponents.contains(component))
					{
						// If auto component ...
						if (!component.isAuto() && component.isVisibleInHierarchy())
						{
							// Increase number of unrendered components
							unrenderedComponents.add(component);

							// Add to explanatory string to buffer
							buffer.append(Integer.toString(unrenderedComponents.size()) + ". " +
								component + "\n");
							String metadata = component.getMetaData(Component.CONSTRUCTED_AT_KEY);
							if (metadata != null)
							{
								buffer.append(metadata);
							}
							metadata = component.getMetaData(Component.ADDED_AT_KEY);
							if (metadata != null)
							{
								buffer.append(metadata);
							}
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

			// Throw exception if any errors were found
			if (unrenderedComponents.size() > 0)
			{
				// Get rid of set
				renderedComponents = null;

				Iterator<Component> iterator = unrenderedComponents.iterator();

				while (iterator.hasNext())
				{
					Component component = iterator.next();
					// Now first test if the component has a sibling that is a transparent resolver.

					Iterator<? extends Component> iterator2 = component.getParent().iterator();
					while (iterator2.hasNext())
					{
						Component sibling = iterator2.next();
						if (!sibling.isVisible())
						{
							boolean isTransparentMarkupContainer = sibling instanceof MarkupContainer &&
								((MarkupContainer)sibling).isTransparentResolver();
							boolean isComponentResolver = sibling instanceof IComponentResolver;
							if (isTransparentMarkupContainer || isComponentResolver)
							{
								// we found a transparent container that isn't visible
								// then ignore this component and only do a debug statement here.
								log.debug(
									"Component {} wasn't rendered but most likely it has a transparent parent: {}",
									component, sibling);
								iterator.remove();
								break;
							}
						}
					}

					// Check if this component is a child of a border whose body is invisible and if
					// so ignore it
					Border border = component.findParent(Border.class);
					if (border != null && !border.getBodyContainer().isVisibleInHierarchy())
					{

						// Suppose:
						//						  
						// <div wicket:id="border"><div wicket:id="label"></div> suppose
						// border->label and border's body is hidden.
						//						  
						// The label is added to border not to its hidden body so as far as wicket
						// is concerned label is visible in hierarchy, but when rendering label wont
						// be rendered because in the markup it is inside the border's hidden body.
						// Thus component use check will fail even though it shouldnt - make sure it
						// doesnt.
						//						 

						// TODO it would be more accurate to determine that this component is inside
						// the border parent's markup not the border's itself
						iterator.remove();
					}
				}
				// if still > 0
				if (unrenderedComponents.size() > 0)
				{
					// Throw exception
					throw new WicketRuntimeException(
						"The component(s) below failed to render. A common problem is that you have added a component in code but forgot to reference it in the markup (thus the component will never be rendered).\n\n" +
							buffer.toString());
				}
			}
		}

		// Get rid of set
		renderedComponents = null;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE.
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
		if (isBookmarkable())
			setStatelessHint(true);

		// Set the page map
		if (pageMap != null)
		{
			setPageMap(pageMap);
		}
		else
		{
			throw new IllegalStateException("PageMap cannot be null");
		}

		setNextAvailableId();

		// Set versioning of page based on default
		setVersioned(Application.get().getPageSettings().getVersionPagesByDefault());

		// All Pages are born dirty so they get clustered right away
		dirty();
	}

	/**
	 * 
	 */
	private void setNextAvailableId()
	{
		if (getApplication().getSessionSettings().isPageIdUniquePerSession())
		{
			setNumericId(getSession().nextPageId());
		}
		else
		{
			// Set the numeric id on this page
			setNumericId(getPageMap().nextId());
		}
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
		// first call the method so that people can track dirty components
		componentChanged(component, parent);
		// Auto components do not participate in versioning since they are
		// added during the rendering phase (which is normally illegal).
		if (component.isAuto() || (parent == null && !component.isVersioned()) ||
			(parent != null && !parent.isVersioned()))
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
	 * This method will be called for all components that are changed on the page So also auto
	 * components or components that are not versioned.
	 * 
	 * If the parent is given that it was a remove or add from that parent of the given component.
	 * else it was just a internal property change of that component.
	 * 
	 * @param component
	 * @param parent
	 */
	protected void componentChanged(Component component, MarkupContainer parent)
	{
	}

	/**
	 * 
	 * @param s
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	void readPageObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException
	{
		int id = s.readShort();
		String name = (String)s.readObject();

		IPageSerializer ps = serializer.get();
		if (ps != null)
		{
			ps.deserializePage(id, name, this, s);
		}
		else
		{
			s.defaultReadObject();
		}
	}

	/**
	 * 
	 * @return serialized version of page
	 * @throws ObjectStreamException
	 */
	protected Object writeReplace() throws ObjectStreamException
	{

		IPageSerializer ps = serializer.get();

		if (ps != null)
		{
			return ps.getPageReplacementObject(this);
		}
		else
		{
			return this;
		}
	}

	/**
	 * 
	 * @param s
	 * @throws IOException
	 */
	void writePageObject(java.io.ObjectOutputStream s) throws IOException
	{
		s.writeShort(numericId);
		s.writeObject(pageMapName);

		IPageSerializer ps = serializer.get();

		if (ps != null)
		{
			ps.serializePage(this, s);
		}
		else
		{
			s.defaultWriteObject();
		}
	}

	/**
	 * Set-up response with appropriate content type, locale and encoding. The locale is set equal
	 * to the session's locale. The content type header contains information about the markup type
	 * (@see #getMarkupType()) and the encoding. The response (and request) encoding is determined
	 * by an application setting (@see ApplicationSettings#getResponseRequestEncoding()). In
	 * addition, if the page's markup contains a xml declaration like &lt?xml ... ?&gt; an xml
	 * declaration with proper encoding information is written to the output as well, provided it is
	 * not disabled by an application setting (@see
	 * ApplicationSettings#getStripXmlDeclarationFromOutput()).
	 * <p>
	 * Note: Prior to Wicket 1.1 the output encoding was determined by the page's markup encoding.
	 * Because this caused uncertainties about the /request/ encoding, it has been changed in favor
	 * of the new, much safer, approach. Please see the Wiki for more details.
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
		if ((markupStream != null) && (markupStream.getXmlDeclaration() != null) &&
			(application.getMarkupSettings().getStripXmlDeclarationFromOutput() == false))
		{
			// Gwyn - Wed, 21 May 2008 12:23:41
			// If the xml declaration in the markup used double-quotes, use them in the output too
			// Whether it should be or not, sometimes it's significant...
			final String quoteChar = (markupStream.getXmlDeclaration().indexOf('\"') == -1) ? "'"
				: "\"";

			response.write("<?xml version=");
			response.write(quoteChar);
			response.write("1.0");
			response.write(quoteChar);
			response.write(" encoding=");
			response.write(quoteChar);
			response.write(encoding);
			response.write(quoteChar);
			response.write("?>");
		}

		// Set response locale from session locale
		response.setLocale(getSession().getLocale());
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE.
	 * 
	 * @see org.apache.wicket.Component#internalOnModelChanged()
	 */
	@Override
	protected final void internalOnModelChanged()
	{
		visitChildren(new Component.IVisitor<Component>()
		{
			public Object component(final Component component)
			{
				// If form component is using form model
				if (component.sameInnermostModel(Page.this))
				{
					component.modelChanged();
				}
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE.
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

		setNextAvailableId();
	}

	/**
	 * @return Factory method that creates a version manager for this Page
	 * @deprecated TODO Remove in 1.4
	 */
	@Deprecated
	protected final IPageVersionManager newVersionManager()
	{
		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// If any of the components on page is not stateless, we need to bind the session
		// before we start rendering components, as then jsessionid won't be appended
		// for links rendered before first stateful component
		if (getSession().isTemporary() && !peekPageStateless())
		{
			getSession().bind();
		}
	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		if (log.isDebugEnabled())
		{
			log.debug("ending request for page " + this + ", request " + getRequest());
		}

		endVersion();

		dirty();

		super.onDetach();
	}

	/**
	 * Renders this container to the given response object.
	 * 
	 * @param markupStream
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		// Set page's associated markup stream
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream(true);
		setMarkupStream(associatedMarkupStream);

		// Configure response object with locale and content type
		configureResponse();

		// Render markup
		renderAll(associatedMarkupStream);
	}

	/**
	 * A component was added.
	 * 
	 * @param component
	 *            The component that was added
	 */
	final void componentAdded(final Component component)
	{
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
		dirty();
		if (mayTrackChangesFor(component, null))
		{
			// If it is an inhertiable model then don't call model changing
			// on the version manager.
			if (!component.getFlag(FLAG_INHERITABLE_MODEL))
			{
				versionManager.componentModelChanging(component);
			}
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
		dirty();
		if (mayTrackChangesFor(component, component.getParent()))
		{
			versionManager.componentRemoved(component);
		}
	}

	/**
	 * 
	 * @param component
	 * @param change
	 */
	final void componentStateChanging(final Component component, Change change)
	{
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
		visitChildren(Form.class, new Component.IVisitor<Component>()
		{
			// For each FormComponent found on the Page (not Form)
			public Object component(final Component component)
			{
				((Form<?>)component).loadPersistentFormComponentValues();
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * @param pageMap
	 *            Sets this page into the page map with the given name. If the page map does not yet
	 *            exist, it is automatically created.
	 */
	final void setPageMap(final IPageMap pageMap)
	{
		// Save transient reference to pagemap
		this.pageMap = pageMap;

		// Save name for restoring transient
		pageMapName = pageMap.getName();
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
	 * Called when the page is retrieved from Session.
	 */
	public void onPageAttached()
	{
	}

	@Override
	public String getMarkupType()
	{
		throw new UnsupportedOperationException(
			"Page does not support markup. This error can happen if you have extended Page directly, instead extend WebPage");
	}

	/**
	 * Gets page instance's unique identifier
	 * 
	 * @return instance unique identifier
	 */
	public PageId getPageId()
	{
		setStatelessHint(false);
		return new PageId(pageMapName, numericId, getCurrentVersionNumber());

	}

}
