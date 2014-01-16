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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.page.IPageManager;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.DebugSettings;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract base class for pages. As a {@link MarkupContainer} subclass, a Page can contain a
 * component hierarchy and markup in some markup language such as HTML. Users of the framework
 * should not attempt to subclass Page directly. Instead they should subclass a subclass of Page
 * that is appropriate to the markup type they are using, such as {@link WebPage} (for HTML markup).
 * <p>
 * Page has the following differences to {@link Component}s main concepts:
 * <ul>
 * <li><b>Identity </b>- Page numerical identifiers start at 0 for each {@link Session} and
 * increment for each new page. This numerical identifier is used as the component identifier
 * accessible via {@link #getId()}.</li>
 * <li><b>Construction </b>- When a page is constructed, it is automatically registerd with the
 * application's {@link IPageManager}. <br>
 * Pages can be constructed with any constructor like any other component, but if you wish to link
 * to a Page using a URL that is "bookmarkable" (which implies that the URL will not have any
 * session information encoded in it, and that you can call this page directly without having a
 * session first directly from your browser), you need to implement your Page with a no-arg
 * constructor or with a constructor that accepts a {@link PageParameters} argument (which wraps any
 * query string parameters for a request). In case the page has both constructors, the constructor
 * with PageParameters will be used.</li>
 * <li><b>Versioning </b>- Pages support the browser's back button when versioning is enabled via
 * {@link #setVersioned(boolean)}. By default all pages are versioned if not configured differently
 * in {@link org.apache.wicket.settings.PageSettings#setVersionPagesByDefault(boolean)}</li>
 * </ul>
 * 
 * @see org.apache.wicket.markup.html.WebPage
 * @see org.apache.wicket.MarkupContainer
 * @see org.apache.wicket.model.CompoundPropertyModel
 * @see org.apache.wicket.Component
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Johan Compagner
 * 
 */
public abstract class Page extends MarkupContainer implements IRedirectListener, IRequestablePage
{
	/** True if the page hierarchy has been modified in the current request. */
	private static final int FLAG_IS_DIRTY = FLAG_RESERVED3;

	/** Set to prevent marking page as dirty under certain circumstances. */
	private static final int FLAG_PREVENT_DIRTY = FLAG_RESERVED4;

	/** True if the page should try to be stateless */
	private static final int FLAG_STATELESS_HINT = FLAG_RESERVED5;

	/** Flag that indicates if the page was created using one of its bookmarkable constructors */
	private static final int FLAG_WAS_CREATED_BOOKMARKABLE = FLAG_RESERVED8;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Page.class);

	private static final long serialVersionUID = 1L;

	/** Used to create page-unique numbers */
	private int autoIndex;

	/** Numeric version of this page's id */
	private int numericId;

	/** Set of components that rendered if component use checking is enabled */
	private transient Set<Component> renderedComponents;

	/**
	 * Boolean if the page is stateless, so it doesn't have to be in the page map, will be set in
	 * urlFor
	 */
	private transient Boolean stateless = null;

	/** Page parameters used to construct this page */
	private final PageParameters pageParameters;

	/**
	 * @see IRequestablePage#getRenderCount()
	 */
	private int renderCount = 0;

	/**
	 * Constructor.
	 */
	protected Page()
	{
		this(null, null);
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
		this(null, model);
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
		this(parameters, null);
	}

	/**
	 * Construct.
	 * 
	 * @param parameters
	 * @param model
	 */
	private Page(final PageParameters parameters, IModel<?> model)
	{
		super(null, model);

		if (parameters == null)
		{
			pageParameters = new PageParameters();
		}
		else
		{
			pageParameters = parameters;
		}
		init();
	}

	/**
	 * The {@link PageParameters} object that was used to construct this page. This will be used in
	 * creating stateless/bookmarkable links to this page
	 * 
	 * @return {@link PageParameters} The construction page parameter
	 */
	@Override
	public PageParameters getPageParameters()
	{
		return pageParameters;
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
		if (getApplication().getDebugSettings().getComponentUseCheck())
		{
			if (renderedComponents == null)
			{
				renderedComponents = new HashSet<Component>();
			}
			if (renderedComponents.add(component) == false)
			{
				throw new MarkupException(
					"The component " +
						component +
						" was rendered already. You can render it only once during a render phase. Class relative path: " +
						component.getClassRelativePath());
			}
			log.debug("Rendered {}", component);

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
	 * @see org.apache.wicket.Component#internalPrepareForRender(boolean)
	 */
	@Override
	public void internalPrepareForRender(boolean setRenderingFlag)
	{
		if (!isInitialized())
		{
			// initialize the page if not yet initialized
			internalInitialize();
		}
		super.internalPrepareForRender(setRenderingFlag);
	}

	/**
	 * @see #dirty(boolean)
	 */
	public final void dirty()
	{
		dirty(false);
	}

	/** {@inheritDoc} */
	@Override
	public boolean setFreezePageId(boolean freeze)
	{
		boolean frozen = getFlag(FLAG_PREVENT_DIRTY);
		setFlag(FLAG_PREVENT_DIRTY, freeze);
		return frozen;
	}

	/**
	 * Mark this page as modified in the session. If versioning is supported then a new version of
	 * the page will be stored in {@link IPageStore page store}
	 * 
	 * @param isInitialization
	 *            a flag whether this is a page instantiation
	 */
	public void dirty(final boolean isInitialization)
	{
		checkHierarchyChange(this);

		if (getFlag(FLAG_PREVENT_DIRTY))
		{
			return;
		}

		final IPageManager pageManager = getSession().getPageManager();
		if (!getFlag(FLAG_IS_DIRTY) && (isVersioned() && pageManager.supportsVersioning() ||

		// we need to get pageId for new page instances even when the page doesn't need
		// versioning, otherwise pages override each other in the page store and back button
		// support is broken
			isInitialization))
		{
			setFlag(FLAG_IS_DIRTY, true);
			setNextAvailableId();

			if (isInitialization == false)
			{
				pageManager.touchPage(this);
			}
		}
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		final IPageManager pageManager = getSession().getPageManager();
		pageManager.touchPage(this);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * This method is called when a component was rendered standalone. If it is a <code>
	 * MarkupContainer</code> then the rendering for that container is checked.
	 * 
	 * @param component
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Get a page unique number, which will be increased with each call.
	 * 
	 * @return A page unique number
	 */
	public final int getAutoIndex()
	{
		return autoIndex++;
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
	 * 
	 * @return page class
	 */
	public final Class<? extends Page> getPageClass()
	{
		return getClass();
	}

	/**
	 * @return Size of this page in bytes
	 */
	@Override
	public final long getSizeInBytes()
	{
		return WicketObjects.sizeof(this);
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
	 * @return This page's component hierarchy as a string
	 */
	public final String hierarchyAsString()
	{
		final StringBuilder buffer = new StringBuilder();
		buffer.append("Page ").append(getId());
		visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				int levels = 0;
				for (Component current = component; current != null; current = current.getParent())
				{
					levels++;
				}
				buffer.append(StringValue.repeat(levels, "	"))
					.append(component.getPageRelativePath())
					.append(':')
					.append(Classes.simpleName(component.getClass()));
			}
		});
		return buffer.toString();
	}

	/**
	 * Bookmarkable page can be instantiated using a bookmarkable URL.
	 * 
	 * @return Returns true if the page is bookmarkable.
	 */
	@Override
	public boolean isBookmarkable()
	{
		return getApplication().getPageFactory().isBookmarkable(getClass());
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
	 * Gets whether the page is stateless. Components on stateless page must not render any stateful
	 * urls, and components on stateful page must not render any stateless urls. Stateful urls are
	 * urls, which refer to a certain (current) page instance.
	 * 
	 * @return Whether this page is stateless
	 */
	@Override
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
			internalInitialize();

			if (isStateless() == false)
			{
				stateless = Boolean.FALSE;
			}
		}

		if (stateless == null)
		{
			Component statefulComponent = visitChildren(Component.class,
				new IVisitor<Component, Component>()
				{
					@Override
					public void component(final Component component, final IVisit<Component> visit)
					{
						if (!component.isStateless())
						{
							visit.stop(component);
						}
					}
				});

			stateless = statefulComponent == null;

			if (log.isDebugEnabled() && !stateless.booleanValue() && getStatelessHint())
			{
				log.debug("Page '{}' is not stateless because of component with path '{}'.", this,
					statefulComponent.getPageRelativePath());
			}

		}

		return stateless;
	}

	/**
	 * Redirect to this page.
	 * 
	 * @see org.apache.wicket.IRedirectListener#onRedirect()
	 */
	@Override
	public final void onRedirect()
	{
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
		return "[Page class = " + getClass().getName() + ", id = " + getId() + ", render count = " +
			getRenderCount() + "]";
	}

	/**
	 * Throw an exception if not all components rendered.
	 * 
	 * @param renderedContainer
	 *            The page itself if it was a full page render or the container that was rendered
	 *            standalone
	 */
	private void checkRendering(final MarkupContainer renderedContainer)
	{
		// If the application wants component uses checked and
		// the response is not a redirect
		final DebugSettings debugSettings = getApplication().getDebugSettings();
		if (debugSettings.getComponentUseCheck())
		{
			final List<Component> unrenderedComponents = new ArrayList<Component>();
			final StringBuilder buffer = new StringBuilder();
			renderedContainer.visitChildren(new IVisitor<Component, Void>()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					// If component never rendered
					if (renderedComponents == null || !renderedComponents.contains(component))
					{
						// If not an auto component ...
						if (!component.isAuto() && component.isVisibleInHierarchy())
						{
							// Increase number of unrendered components
							unrenderedComponents.add(component);

							// Add to explanatory string to buffer
							buffer.append(Integer.toString(unrenderedComponents.size()))
								.append(". ")
								.append(component)
								.append('\n');
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
							visit.dontGoDeeper();
						}
					}
				}
			});

			// Throw exception if any errors were found
			if (unrenderedComponents.size() > 0)
			{
				renderedComponents = null;

				List<Component> transparentContainerChildren = Generics.newArrayList();

				Iterator<Component> iterator = unrenderedComponents.iterator();
				outerWhile : while (iterator.hasNext())
				{
					Component component = iterator.next();

					// If any of the transparentContainerChildren is a parent to component, then
					// ignore it.
					for (Component transparentContainerChild : transparentContainerChildren)
					{
						MarkupContainer parent = component.getParent();
						while (parent != null)
						{
							if (parent == transparentContainerChild)
							{
								iterator.remove();
								continue outerWhile;
							}
							parent = parent.getParent();
						}
					}

					if (hasInvisibleTransparentChild(component.getParent(), component))
					{
						// If we found a transparent container that isn't visible then ignore this
						// component and only do a debug statement here.
						if (log.isDebugEnabled())
						{
							log.debug(
								"Component {} wasn't rendered but might have a transparent parent.",
								component);
						}

						transparentContainerChildren.add(component);
						iterator.remove();
						continue outerWhile;
					}
				}

				// if still > 0
				if (unrenderedComponents.size() > 0)
				{
					// Throw exception
					throw new WicketRuntimeException(
						"The component(s) below failed to render. Possible reasons could be that: 1) you have added a component in code but forgot to reference it in the markup (thus the component will never be rendered), 2) if your components were added in a parent container then make sure the markup for the child container includes them in <wicket:extend>.\n\n" +
							buffer.toString());
				}
			}
		}

		// Get rid of set
		renderedComponents = null;
	}

	private boolean hasInvisibleTransparentChild(final MarkupContainer root, final Component self)
	{
		for (Component sibling : root)
		{
			if ((sibling != self) && (sibling instanceof IComponentResolver) &&
				(sibling instanceof MarkupContainer))
			{
				if (!sibling.isVisible())
				{
					return true;
				}
				else
				{
					boolean rtn = hasInvisibleTransparentChild((MarkupContainer)sibling, self);
					if (rtn == true)
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Initializes Page by adding it to the Session and initializing it.
	 */
	private void init()
	{
		if (isBookmarkable() == false)
		{
			setStatelessHint(false);
		}

		// Set versioning of page based on default
		setVersioned(getApplication().getPageSettings().getVersionPagesByDefault());

		// All Pages are born dirty so they get clustered right away
		dirty(true);

		// this is a bit of a dirty hack, but calling dirty(true) results in isStateless called
		// which is bound to set the stateless cache to true as there are no components yet
		stateless = null;
	}

	/**
	 * 
	 */
	private void setNextAvailableId()
	{
		setNumericId(getSession().nextPageId());
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
		if (!component.isAuto())
		{
			dirty();
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR OVERRIDE.
	 * 
	 * @see org.apache.wicket.Component#internalOnModelChanged()
	 */
	@Override
	protected final void internalOnModelChanged()
	{
		visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				// If form component is using form model
				if (component.sameInnermostModel(Page.this))
				{
					component.modelChanged();
				}
			}
		});
	}

	@Override
	void internalOnAfterConfigure()
	{
		super.internalOnAfterConfigure();

		// first try to check if the page can be rendered:
		if (!isRenderAllowed())
		{
			if (log.isDebugEnabled())
			{
				log.debug("Page not allowed to render: " + this);
			}
			throw new UnauthorizedActionException(this, Component.RENDER);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		// Make sure it is really empty
		renderedComponents = null;

		// rendering might remove or add stateful components, so clear flag to force reevaluation
		stateless = null;

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
	 * @see org.apache.wicket.Component#onAfterRender()
	 */
	@Override
	protected void onAfterRender()
	{
		super.onAfterRender();

		// Check rendering if it happened fully
		checkRendering(this);

		// clean up debug meta data if component check is on
		if (getApplication().getDebugSettings().getComponentUseCheck())
		{
			visitChildren(new IVisitor<Component, Void>()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					component.setMetaData(Component.CONSTRUCTED_AT_KEY, null);
					component.setMetaData(Component.ADDED_AT_KEY, null);
				}
			});
		}

		if (!isPageStateless())
		{
			// trigger creation of the actual session in case it was deferred
			getSession().getSessionStore().getSessionId(RequestCycle.get().getRequest(), true);

			// Add/touch the response page in the session.
			getSession().getPageManager().touchPage(this);
		}

		if (getApplication().getDebugSettings().isOutputMarkupContainerClassName())
		{
			String className = Classes.name(getClass());
			getResponse().write("<!-- Page Class ");
			getResponse().write(className);
			getResponse().write(" END -->\n");
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

		setFlag(FLAG_IS_DIRTY, false);

		super.onDetach();
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#onRender()
	 */
	@Override
	protected void onRender()
	{
		// Loop through the markup in this container
		MarkupStream markupStream = new MarkupStream(getMarkup());
		renderAll(markupStream, null);
	}

	/**
	 * A component was added.
	 * 
	 * @param component
	 *            The component that was added
	 */
	final void componentAdded(final Component component)
	{
		if (!component.isAuto())
		{
			dirty();
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
	}

	/**
	 * A component was removed.
	 * 
	 * @param component
	 *            The component that was removed
	 */
	final void componentRemoved(final Component component)
	{
		if (!component.isAuto())
		{
			dirty();
		}
	}

	/**
	 * 
	 * @param component
	 */
	final void componentStateChanging(final Component component)
	{
		if (!component.isAuto())
		{
			dirty();
		}
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
	 * @see org.apache.wicket.MarkupContainer#getMarkupType()
	 */
	@Override
	public MarkupType getMarkupType()
	{
		throw new UnsupportedOperationException(
			"Page does not support markup. This error can happen if you have extended Page directly, instead extend WebPage");
	}

	/**
	 * Gets page instance's unique identifier
	 * 
	 * @return instance unique identifier
	 */
	public PageReference getPageReference()
	{
		setStatelessHint(false);

		// make sure the page will be available on following request
		getSession().getPageManager().touchPage(this);

		return new PageReference(numericId);
	}

	/**
	 * @see org.apache.wicket.page.IManageablePage#getPageId()
	 */
	@Override
	public int getPageId()
	{
		return numericId;
	}

	@Override
	public int getRenderCount()
	{
		return renderCount;
	}

	/**
	 * Sets the flag that determines whether or not this page was created using one of its
	 * bookmarkable constructors
	 * 
	 * @param wasCreatedBookmarkable
	 */
	public final void setWasCreatedBookmarkable(boolean wasCreatedBookmarkable)
	{
		setFlag(FLAG_WAS_CREATED_BOOKMARKABLE, wasCreatedBookmarkable);
	}

	/**
	 * Checks if this page was created using one of its bookmarkable constructors
	 * 
	 * @see org.apache.wicket.request.component.IRequestablePage#wasCreatedBookmarkable()
	 */
	@Override
	public final boolean wasCreatedBookmarkable()
	{
		return getFlag(FLAG_WAS_CREATED_BOOKMARKABLE);
	}

	/**
	 * @see org.apache.wicket.request.component.IRequestablePage#renderPage()
	 */
	@Override
	public void renderPage()
	{
		// page id is frozen during the render
		final boolean frozen = setFreezePageId(true);
		try
		{
			++renderCount;
			render();

			// stateless = null;
		}
		finally
		{
			setFreezePageId(frozen);
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL.
	 * 
	 * @param component
	 * @return if this component was render in this page
	 */
	public final boolean wasRendered(Component component)
	{
		return renderedComponents != null && renderedComponents.contains(component);
	}
}
