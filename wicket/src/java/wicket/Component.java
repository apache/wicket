/*
 * $Id$
 * $Revision$ $Date$
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
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.WicketTag;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.parser.XmlTag;
import wicket.model.IModel;
import wicket.response.NullResponse;
import wicket.util.convert.IConverter;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;

/**
 * Component serves as the highest level abstract base class for all components.
 * A component has a parent. If a component is an instance of MarkupContainer,
 * it may have children. In this way it has a place in the hierarchy of
 * components contained on a given page.
 * <p>
 * The page containing any given component can be retrieved by calling
 * getPage(). The page itself points back to the Session that contains the page.
 * The Session for a component can be accessed with the convenience method
 * getSession(), which simply calls getPage().getSession(). Various attributes
 * of Session are also exposed in this way, including: getStyle() (for
 * page/component "skins"), getLocale() (for localization), getApplication(),
 * getApplicationPages() and getApplicationSettings().
 * <p>
 * The path from the Page at the root of the component hierarchy to a given
 * component is simply the concatenation with dot separators of each id along
 * the way. For example, the path "a.b.c" would refer to the component named "c"
 * inside the container named "b" inside the container named "a". The path to a
 * component can be retrieved by calling getPath(). This path is an absolute
 * path beginning with the id of the page at the root. Pages bear a
 * session-relative identifier as their id, so each absolute path will begin
 * with a number, such as "0.a.b.c". To get a component path relative to the
 * page that contains it, you can call getPageRelativePath().
 * <p>
 * The primary responsibility of a component is to use its model, which can be
 * set via setModel() and retrieved via getModel() to render a response in an
 * appropriate markup language, such as HTML. In addition, form components know
 * how to update their models based on request information.
 * <p>
 * Components participate in four phases of processing to produce output.
 * <p>
 * 1. An incoming request is processed by the servlet for the protocol in use,
 * for example, WicketServlet. The associated application object creates
 * Session, Request and Response objects for use by a component in updating is
 * model and rendering a response. These objects are stored inside a container
 * called RequestCycle.
 * <p>
 * 2. If a form has been submitted, the form component's model is updated by a
 * call to FormComponent.updateModel()
 * <p>
 * 3. If a form has been submitted, the form component's model is validated by a
 * call to FormComponent.validate()
 * <p>
 * 4. A response page is rendered via render()
 * <p>
 * Pages which have setVisible(false) will return false from isVisible() and
 * will not render a response (nor will their children).
 * <p>
 * Values from the request can be retrieved by a set of convenience methods,
 * including getRequestString(), getRequestStrings(), getRequestInt(),
 * getRequestInts() and getRequestBoolean(). Each of these methods uses the path
 * to the component to retrieve a value from the request.
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public abstract class Component implements Serializable
{
	/** True when a component is being auto-added */
	static final byte FLAG_AUTO = 0x08;

	/** Flag for escaping HTML in model strings */
	private static final byte FLAG_ESCAPE_MODEL_STRINGS = 0x04;

	/** Versioning boolean */
	private static final byte FLAG_VERSIONED = 0x02;

	/** Visibility boolean */
	private static final byte FLAG_VISIBLE = 0x01;

	/** Log. */
	private static Log log = LogFactory.getLog(Component.class);

	/** List of AttributeModifiers to be applied for this Component */
	private AttributeModifier attributeModifiers = null;

	/** Component flags. See FLAG_* for possible non-exclusive flag values. */
	private byte flags = FLAG_VISIBLE | FLAG_ESCAPE_MODEL_STRINGS | FLAG_VERSIONED;

	/** Component id. */
	private String id;

	/** The model for this component. */
	private IModel model;

	/** Any parent container. */
	private MarkupContainer parent;

	/**
	 * Generic component visitor interface for component traversals.
	 */
	public static interface IVisitor
	{
		/**
		 * Value to return to continue a traversal.
		 */
		public static Object CONTINUE_TRAVERSAL = null;

		/**
		 * A generic value to return to stop a traversal.
		 */
		public static Object STOP_TRAVERSAL = new Object();

		/**
		 * Called at each component in a traversal.
		 * 
		 * @param component
		 *            The component
		 * @return CONTINUE_TRAVERSAL (null) if the traversal should continue,
		 *         or a non-null return value for the traversal method if it
		 *         should stop. If no return value is useful, the generic
		 *         non-null value STOP_TRAVERSAL can be used.
		 */
		public Object component(Component component);
	}

	/**
	 * Constructor. All components have names. A component's id cannot be null.
	 * This is the minimal constructor of component. It does not register a
	 * model.
	 * 
	 * @param id
	 *            The non-null id of this component
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null id.
	 */
	public Component(final String id)
	{
		setId(id);
	}

	/**
	 * Constructor. All components have names. A component's id cannot be null.
	 * This is constructor includes a model.
	 * 
	 * @param id
	 *            The non-null id of this component
	 * @param model
	 *            The component's model
	 * 
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null id.
	 */
	public Component(final String id, final IModel model)
	{
		setId(id);
		setModel(model);
	}

	/**
	 * Adds an attribute modifier to the component.
	 * 
	 * @param modifier
	 *            The attribute modifier to be added
	 * @return this (to allow method call chaining)
	 */
	public final Component add(final AttributeModifier modifier)
	{
		modifier.next = attributeModifiers;
		attributeModifiers = modifier;
		return this;
	}

	/**
	 * Registers a debug message for this component
	 * 
	 * @param message
	 *            The message
	 */
	public final void debug(final String message)
	{
		getPage().getFeedbackMessages().debug(this, message);
	}

	/**
	 * Registers an error message for this component
	 * 
	 * @param message
	 *            The message
	 */
	public final void error(final String message)
	{
		getPage().getFeedbackMessages().error(this, message);
	}

	/**
	 * Registers an fatal error message for this component
	 * 
	 * @param message
	 *            The message
	 */
	public final void fatal(final String message)
	{
		getPage().getFeedbackMessages().fatal(this, message);
	}

	/**
	 * Finds the first container parent of this component of the given class.
	 * 
	 * @param c
	 *            MarkupContainer class to search for
	 * @return First container parent that is an instance of the given class, or
	 *         null if none can be found
	 */
	public final MarkupContainer findParent(final Class c)
	{
		// Start with immediate parent
		MarkupContainer current = parent;

		// Walk up containment hierarchy
		while (current != null)
		{
			// Is current an instance of this class?
			if (c.isInstance(current))
			{
				return current;
			}

			// Check parent
			current = current.getParent();
		}

		// Failed to find component
		return null;
	}

	/**
	 * @return The nearest markup container with associated markup
	 */
	public final MarkupContainer findParentWithAssociatedMarkup()
	{
		MarkupContainer container = parent;
		while (container != null)
		{
			if (container.hasAssociatedMarkup())
			{
				return container;
			}
			container = container.getParent();
		}

		// This should never happen since Page always has associated markup
		throw new WicketRuntimeException("Unable to find parent with associated markup");
	}

	/**
	 * Gets interface to application that this component is a part of.
	 * 
	 * @return The application associated with the session that this component
	 *         is in.
	 * @see Application
	 */
	public final Application getApplication()
	{
		return getSession().getApplication();
	}

	/**
	 * Gets the application pages from the application that this component
	 * belongs to.
	 * 
	 * @return The application pages
	 * @see ApplicationPages
	 */
	public final ApplicationPages getApplicationPages()
	{
		return getApplication().getPages();
	}

	/**
	 * Gets the application settings from the application that this component
	 * belongs to.
	 * 
	 * @return The application settings from the application that this component
	 *         belongs to
	 * @see ApplicationSettings
	 */
	public final ApplicationSettings getApplicationSettings()
	{
		return getApplication().getSettings();
	}

	/**
	 * @return A path of the form <page-class-name>. <page-relative-path>
	 * @see Component#getPageRelativePath()
	 */
	public final String getClassRelativePath()
	{
		return getClass().getName() + "." + getPageRelativePath();
	}

	/**
	 * Gets the converter that should be used by this component.
	 * 
	 * @return The converter that should be used by this component
	 */
	public IConverter getConverter()
	{
		return getSession().getConverter();
	}

	/**
	 * Gets whether model strings should be escaped.
	 * 
	 * @return Returns whether model strings should be escaped
	 */
	public final boolean getEscapeModelStrings()
	{
		return getFlag(FLAG_ESCAPE_MODEL_STRINGS);
	}

	/**
	 * @return Any feedback message for this component
	 */
	public final FeedbackMessage getFeedbackMessage()
	{
		return getPage().getFeedbackMessages().messageForComponent(this);
	}

	/**
	 * Gets the id of this component.
	 * 
	 * @return The id of this component
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Gets the locale for the session holding this component.
	 * 
	 * @return The locale for the session holding this component
	 * @see Component#getSession()
	 */
	public final Locale getLocale()
	{
		return getSession().getLocale();
	}

	/**
	 * Convenience method to provide easy access to the localizer object within
	 * any component.
	 * 
	 * @return The localizer object
	 */
	public final Localizer getLocalizer()
	{
		return getApplication().getLocalizer();
	}

	/**
	 * Gets the model. It returns the object that wraps the backing model.
	 * 
	 * @return The model
	 */
	public IModel getModel()
	{
		// If model is null
		if (model == null)
		{
			// give subclass a chance to lazy-init model
			this.model = initModel();
		}

		return model;
	}

	/**
	 * @return Lock object to synchronize on when reading or updating a model
	 *         which might require synchronization, such as a list
	 */
	public final Object getModelLock()
	{
		return model != null ? model : new Object();
	}

	/**
	 * Gets the backing model object; this is shorthand for
	 * getModel().getObject().
	 * 
	 * @return the backing model object
	 */
	public final Object getModelObject()
	{
		final IModel model = getModel();
		if (model != null)
		{
			return model.getObject(this);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Gets a model object as a string.
	 * 
	 * @return Model object for this component as a string
	 */
	public final String getModelObjectAsString()
	{
		final IModel model = getModel();
		if (model != null)
		{
			final Object modelObject = model.getObject(this);
			if (modelObject != null)
			{
				// Get converter
				final IConverter converter = getConverter();

				// Model string from property
				final String modelString = (String)converter.convert(modelObject, String.class);

				// If we should escape the markup
				if (getFlag(FLAG_ESCAPE_MODEL_STRINGS))
				{
					// Escape it
					return Strings.escapeMarkup(modelString);
				}
				return modelString;
			}
		}
		return "";
	}

	/**
	 * Gets the page holding this component.
	 * 
	 * @return The page holding this component
	 */
	public final Page getPage()
	{
		// Search for nearest Page
		final Page page = findPage();

		// If no Page was found
		if (page == null)
		{
			// Give up with a nice exception
			throw new IllegalStateException("No Page found for component " + this);
		}

		return page;
	}

	/**
	 * @return The page factory for the session that this component is in
	 */
	public final IPageFactory getPageFactory()
	{
		return getSession().getPageFactory();
	}

	/**
	 * Gets the path to this component relative to the page it is in.
	 * 
	 * @return The path to this component relative to the page it is in
	 */
	public final String getPageRelativePath()
	{
		return Strings.afterFirstPathComponent(getPath(), '.');
	}

	/**
	 * Gets any parent container, or null if there is none.
	 * 
	 * @return Any parent container, or null if there is none
	 */
	public final MarkupContainer getParent()
	{
		return parent;
	}

	/**
	 * Gets the components' path.
	 * 
	 * @return Dotted path to this component in the component hierarchy
	 */
	public final String getPath()
	{
		final StringBuffer buffer = new StringBuffer();
		for (Component c = this; c != null; c = c.getParent())
		{
			if (buffer.length() > 0)
			{
				buffer.insert(0, '.');
			}
			buffer.insert(0, c.getId());
		}
		return buffer.toString();
	}

	/**
	 * @return The resource for this component
	 */
	public Resource getResource()
	{
		return getApplication().getResource(Application.class, getId(), getLocale(), getStyle());
	}

	/**
	 * @return The request for this component's active request cycle
	 */
	public final Request getRequest()
	{
		return getRequestCycle().getRequest();
	}

	/**
	 * Gets the active request cycle for this component
	 * 
	 * @return The request cycle
	 */
	public final RequestCycle getRequestCycle()
	{
		return getSession().getRequestCycle();
	}

	/**
	 * @return The response for this component's active request cycle
	 */
	public final Response getResponse()
	{
		return getRequestCycle().getResponse();
	}

	/**
	 * @return The root model (innermost nested model) for this component
	 */
	public final Object getRootModelObject()
	{
		return getRootModel(getModel());
	}

	/**
	 * Gets the current session object. Although this method is not final
	 * (because Page overrides it), it is not intended to be overridden by
	 * clients and clients of the framework should not do so!
	 * 
	 * @return The session that this component is in
	 */
	public Session getSession()
	{
		// Fetch page if possible
		final Page page = findPage();

		// If this component is attached to a page
		if (page != null)
		{
			// Get Session from Page object for this component
			final Session session = page.getSession();

			// Did we find the session?
			if (session != null)
			{
				return session;
			}
			else
			{
				// This should NEVER happen. But if it does, we'll want a nice
				// error message.
				throw new IllegalStateException(exceptionMessage("Page not attached to session"));
			}
		}
		else
		{
			return Session.get();
		}
	}

	/**
	 * Gets the (skin) style of this component.
	 * 
	 * @return The (skin) style of this component
	 * @see wicket.Session#getStyle()
	 */
	public final String getStyle()
	{
		return getSession().getStyle();
	}

	/**
	 * @return True if this component has an error message
	 */
	public final boolean hasErrorMessage()
	{
		return getPage().getFeedbackMessages().hasErrorMessageFor(this);
	}

	/**
	 * @return True if this component has some kind of feedback message
	 */
	public final boolean hasFeedbackMessage()
	{
		return getPage().getFeedbackMessages().hasMessageFor(this);
	}

	/**
	 * Registers a info message for this component
	 * 
	 * @param message
	 *            The message
	 */
	public final void info(final String message)
	{
		getPage().getFeedbackMessages().info(this, message);
	}

	/**
	 * Returns true if this component is an ancestor of the given component
	 * 
	 * @param component
	 *            The component to check
	 * @return True if the given component has this component as an ancestor
	 */
	public boolean isAncestorOf(final Component component)
	{
		// Walk up containment hierarchy
		for (MarkupContainer current = component.parent; current != null; current = current
				.getParent())
		{
			// Is this an ancestor?
			if (current == this)
			{
				return true;
			}
		}

		// This component is not an ancestor of the given component
		return false;
	}

	/**
	 * @return Returns the isVersioned.
	 */
	public boolean isVersioned()
	{
		// Is the component itself versioned?
		if (!getFlag(FLAG_VERSIONED))
		{
			return false;
		}
		else
		{
			// If there's a parent and this component is versioned
			if (parent != null)
			{
				// Check if the parent is unversioned. If any parent
				// (recursively) is unversioned, then this component is too
				if (!parent.isVersioned())
				{
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Gets whether this component and any children are visible.
	 * 
	 * @return True if component and any children are visible
	 */
	public boolean isVisible()
	{
		return getFlag(FLAG_VISIBLE);
	}

	/**
	 * Called to indicate that the model for this component has been changed
	 */
	public final void modelChanged()
	{
		// Call user code
		internalOnModelChanged();
		onModelChanged();
	}

	/**
	 * Called to indicate that the model for this component has been changed
	 */
	public final void modelChanging()
	{
		// Tell the page that our model changed
		final Page page = findPage();
		if (page != null)
		{
			page.componentModelChanging(this);
		}
	}

	/**
	 * Creates a new page using the component's page factory
	 * 
	 * @param c
	 *            The class of page to create
	 * @return The new page
	 */
	public final Page newPage(final Class c)
	{
		return getPageFactory().newPage(c);
	}

	/**
	 * Creates a new page using the component's page factory
	 * 
	 * @param c
	 *            The class of page to create
	 * @param parameters
	 *            Any parameters to pass to the constructor
	 * @return The new page
	 */
	public final Page newPage(final Class c, final PageParameters parameters)
	{
		return getPageFactory().newPage(c, parameters);
	}

	/**
	 * Removes this component from its parent. It's important to remember that a
	 * component that is removed cannot be referenced from the markup still.
	 */
	public void remove()
	{
		parent.remove(getId());
	}

	/**
	 * Performs a render of this component.
	 */
	public final void render()
	{
		// Determine if component is visible
		final boolean isVisible = isVisible();

		// Get request cycle to render to
		final RequestCycle cycle = getRequestCycle();

		// Save original Response
		final Response originalResponse;

		// If component is not visible, set response to NullResponse
		if (isVisible)
		{
			// No response to restore
			originalResponse = null;

			// Rendering is beginning
			if (log.isDebugEnabled())
			{
				log.debug("Begin render " + this);
			}
		}
		else
		{
			// Since component is invisible, pipe all output to NullResponse
			originalResponse = cycle.getResponse();
			cycle.setResponse(NullResponse.getInstance());
		}

		// Synchronize on model lock while rendering to help ensure
		// that the model doesn't change while its being read
		synchronized (getModelLock())
		{
			// Call implementation to render component
			onRender();

			// Component has been rendered
			rendered();

			// Detach models now that rendering is fully completed
			detachModels();
		}

		// Restore original response if any
		if (isVisible)
		{
			if (log.isDebugEnabled())
			{
				log.debug("End render " + this);
			}
		}
		else
		{
			cycle.setResponse(originalResponse);
		}
	}

	/**
	 * Called to indicate that a component has been rendered. This method should
	 * only very rarely be called at all. One usage is in ImageMap, which
	 * renders its link children its own special way (without calling render()
	 * on them). If ImageMap did not call rendered() to indicate that its child
	 * components were actually rendered, the framework would think they had
	 * never been rendered, and in development mode this would result in a
	 * runtime exception.
	 */
	public void rendered()
	{
		// Tell the page that the component rendered
		getPage().componentRendered(this);
	}

	/**
	 * @param component
	 *            The component to compare with
	 * @return True if the given component's model is the same as this
	 *         component's model.
	 */
	public final boolean sameRootModel(final Component component)
	{
		return sameRootModel(component.getModel());
	}

	/**
	 * @param model
	 *            The model to compare with
	 * @return True if the given component's model is the same as this
	 *         component's model.
	 */
	public final boolean sameRootModel(final IModel model)
	{
		// Get the two models
		IModel thisModel = getModel();
		IModel thatModel = model;

		// If both models are non-null they could be the same
		if (thisModel != null && thatModel != null)
		{
			return getRootModel(thisModel) == getRootModel(thatModel);
		}

		return false;
	}

	/**
	 * Sets the given model.
	 * 
	 * @param model
	 *            the model
	 */
	public final void setModel(final IModel model)
	{
		// Detach current model
		if (this.model != null)
		{
			this.model.detach();
		}

		// Change model
		if (this.model != model)
		{
			modelChanging();
			this.model = (IModel)model;
			modelChanged();
		}
	}

	/**
	 * Sets the backing model object; shorthand for
	 * getModel().setObject(object).
	 * 
	 * @param object
	 *            The object to set
	 */
	public final void setModelObject(final Object object)
	{
		final IModel model = getModel();
		if (model != null)
		{
			if (model.getObject(this) != object)
			{
				modelChanging();
				model.setObject(this, object);
				modelChanged();
			}
		}
		else
		{
			throw new IllegalStateException("Attempt to set model object on null model");
		}
	}

	/**
	 * @param redirect
	 *            True if the response should be redirected to
	 * @see RequestCycle#setRedirect(boolean)
	 */
	public final void setRedirect(final boolean redirect)
	{
		getRequestCycle().setRedirect(redirect);
	}

	/**
	 * Sets the page that will respond to this request
	 * 
	 * @param page
	 *            The response page
	 * @see RequestCycle#setResponsePage(Page)
	 */
	public final void setResponsePage(final Page page)
	{
		getRequestCycle().setResponsePage(page);
	}

	/**
	 * Sets whether model strings should be escaped.
	 * 
	 * @param escapeMarkup
	 *            True is model strings should be escaped
	 * @return This
	 */
	public final Component setShouldEscapeModelStrings(final boolean escapeMarkup)
	{
		setFlag(FLAG_ESCAPE_MODEL_STRINGS, escapeMarkup);
		return this;
	}

	/**
	 * @param versioned
	 *            True to turn on versioning for this component, false to turn
	 *            it off for this component and any children.
	 */
	public void setVersioned(boolean versioned)
	{
		setFlag(FLAG_VERSIONED, versioned);
	}

	/**
	 * Sets whether this component and any children are visible.
	 * 
	 * @param visible
	 *            True if this component and any children should be visible
	 * @return This
	 */
	public final Component setVisible(final boolean visible)
	{
		// Is new visibility state a change?
		if (visible != isVisible())
		{
			// Change visibility
			setFlag(FLAG_VISIBLE, visible);

			// Tell the page that this component's visibility was changed
			final Page page = findPage();
			if (page != null)
			{
				page.componentVisibilityChanged(this);
			}
		}
		return this;
	}

	/**
	 * Gets the string representation of this component.
	 * 
	 * @return The path to this component
	 */
	public String toString()
	{
		final Page page = findPage();
		return "[Component id = " + getId() + ", page = "
				+ (page == null ? "<No Page>" : getPage().getClass().getName()) + ", path = "
				+ getPath() + "." + Classes.name(getClass()) + ", isVisible = " + isVisible()
				+ ", isVersioned = " + isVersioned() + "]";
	}

	/**
	 * @param listenerInterface
	 *            The listener interface that the URL should call
	 * @return The URL
	 */
	public final String urlFor(final Class listenerInterface)
	{
		return getPage().urlFor(this, listenerInterface);
	}

	/**
	 * Registers a warning message for this component
	 * 
	 * @param message
	 *            The message
	 */
	public final void warn(final String message)
	{
		getPage().getFeedbackMessages().warn(this, message);
	}

	/**
	 * Checks whether the given type has the expected name.
	 * 
	 * @param tag
	 *            The tag to check
	 * @param name
	 *            The expected tag name
	 * @throws MarkupException
	 *             Thrown if the tag is not of the right name
	 */
	protected final void checkComponentTag(final ComponentTag tag, final String name)
	{
		if (!tag.getName().equalsIgnoreCase(name))
		{
			findMarkupStream().throwMarkupException(
					"Component " + getId() + " must be applied to a tag of type '" + name
							+ "', not " + tag.toUserDebugString());
		}
	}

	/**
	 * Checks that a given tag has a required attribute value.
	 * 
	 * @param tag
	 *            The tag
	 * @param key
	 *            The attribute key
	 * @param value
	 *            The required value for the attribute key
	 * @throws MarkupException
	 *             Thrown if the tag does not have the required attribute value
	 */
	protected final void checkComponentTagAttribute(final ComponentTag tag, final String key,
			final String value)
	{
		if (key != null)
		{
			final String tagAttributeValue = tag.getAttributes().getString(key);
			if (tagAttributeValue == null || !value.equalsIgnoreCase(tagAttributeValue))
			{
				findMarkupStream().throwMarkupException(
						"Component " + getId() + " must be applied to a tag with '" + key
								+ "' attribute matching '" + value + "', not '" + tagAttributeValue
								+ "'");
			}
		}
	}

	/**
	 * Detaches the model for this component if it is detachable
	 */
	protected void detachModel()
	{
		if (model != null)
		{
			model.detach();
		}
	}

	/**
	 * Prefixes an exception message with useful information about this
	 * component.
	 * 
	 * @param message
	 *            The message
	 * @return The modified message
	 */
	protected final String exceptionMessage(final String message)
	{
		return message + ":\n" + toString();
	}

	/**
	 * Finds the markup stream for this component.
	 * 
	 * @return The markup stream for this component. Since a Component cannot
	 *         have a markup stream, we ask this component's parent to search
	 *         for it.
	 */
	protected MarkupStream findMarkupStream()
	{
		return parent.findMarkupStream();
	}

	/**
	 * If this Component is a Page, returns self. Otherwise, searches for the
	 * nearest Page parent in the component hierarchy. If no Page parent can be
	 * found, null is returned
	 * 
	 * @return The Page or null if none can be found
	 */
	protected final Page findPage()
	{
		// Search for page
		return (Page)(this instanceof Page ? this : findParent(Page.class));
	}


	/**
	 * Called when a null model is about to be retrieved in order to allow a
	 * subclass to provide an initial model. This gives FormComponent, for
	 * example, an opportunity to instantiate a model on the fly using the
	 * containing Form's model.
	 * 
	 * @return The model
	 */
	protected IModel initModel()
	{
		final IModel model = getPage().getModel();
		if (model != null)
		{
			return model;
		}
		return null;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request begins.
	 */
	protected void internalBeginRequest()
	{
		onBeginRequest();
		internalOnBeginRequest();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request ends.
	 */
	protected void internalEndRequest()
	{
		internalOnEndRequest();
		onEndRequest();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request begins.
	 */
	protected void internalOnBeginRequest()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request ends.
	 */
	protected void internalOnEndRequest()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called anytime a model is changed via setModel or setModelObject.
	 */
	protected void internalOnModelChanged()
	{
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called anytime a model is changed via setModel or setModelObject.
	 */
	protected void internalOnModelChanging()
	{
	}

	/**
	 * Called when a request begins.
	 */
	protected void onBeginRequest()
	{
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
	}

	/**
	 * Processes the body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}

	/**
	 * Called when a request ends.
	 */
	protected void onEndRequest()
	{
	}

	/**
	 * Called anytime a model is changed after the change has occurred
	 */
	protected void onModelChanged()
	{
	}

	/**
	 * Called anytime a model is changed, but before the change actually occurs
	 */
	protected void onModelChanging()
	{
	}

	/**
	 * Implementation that renders this component.
	 */
	protected abstract void onRender();

	/**
	 * Redirects browser to the given page
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	protected void redirectTo(final Page page)
	{
		getRequestCycle().redirectTo(page);
	}

	/**
	 * Renders the component at the current position in the given markup stream.
	 * The method onComponentTag() is called to allow the component to mutate
	 * the start tag. The method onComponentTagBody() is then called to permit
	 * the component to render its body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	protected final void renderComponent(final MarkupStream markupStream)
	{
		// Get mutable copy of next tag
		final ComponentTag tag = markupStream.getTag().mutable();

		// Call any tag handler
		onComponentTag(tag);

		// If we're an openclose tag
		final XmlTag.Type type = tag.getType();
		final boolean isImageTag = tag.getName().equalsIgnoreCase("img");
		if (tag.isOpenClose())
		{
			// Change type to open tag
			if (!isImageTag)
			{
				tag.setType(XmlTag.OPEN);
			}
		}
		else
		{
			// Must be an open tag
			if (!tag.isOpen())
			{
				// We were something other than <tag> or <tag/>
				markupStream
						.throwMarkupException("Method renderComponent called on bad markup element "
								+ tag);
			}
		}

		// Render open tag
		renderComponentTag(tag);
		markupStream.next();

		// Render body using original tag type so implementors of
		// onComponentTagBody will know if the tag has a body or not
		tag.setType(type);
		onComponentTagBody(markupStream, tag);

		// Render close tag
		if (!isImageTag)
		{
			renderClosingComponentTag(markupStream, tag);
		}
	}

	/**
	 * Writes a simple tag out to the response stream. Any components that might
	 * be referenced by the tag are ignored. Also undertakes any tag attribute
	 * modifications if they have been added to the component.
	 * 
	 * @param tag
	 *            The tag to write
	 */
	protected final void renderComponentTag(ComponentTag tag)
	{
		final ApplicationSettings settings = getApplication().getSettings();
		if (!(tag instanceof WicketTag) || !settings.getStripWicketTags())
		{
			// Apply attribute modifiers
			if (attributeModifiers != null && tag.getType() != XmlTag.CLOSE)
			{
				tag = tag.mutable();
				for (AttributeModifier current = attributeModifiers; current != null; current = current.next)
				{
					current.replaceAttibuteValue(this, tag);
				}
			}

			// Write the tag
			getResponse().write(tag.toString(settings.getStripWicketTags()));
		}
	}

	/**
	 * Writes a simple tag out to the response stream. Any components that might
	 * be referenced by the tag are ignored.
	 * 
	 * @param markupStream
	 *            The markup stream to advance (where the tag came from)
	 */
	protected final void renderComponentTag(final MarkupStream markupStream)
	{
		renderComponentTag(markupStream.getTag());
		markupStream.next();
	}

	/**
	 * Replaces the body with the given one.
	 * 
	 * @param markupStream
	 *            The markup stream to replace the tag body in
	 * @param tag
	 *            The tag
	 * @param body
	 *            The new markup
	 */
	protected final void replaceComponentTagBody(final MarkupStream markupStream,
			final ComponentTag tag, final String body)
	{
		// If tag has body
		if (tag.isOpen())
		{
			// skip any raw markup in the body
			markupStream.skipRawMarkup();
		}

		// Write the new body
		getResponse().write(body);

		// If we had an open tag (and not an openclose tag) and we found a
		// close tag, we're good
		if (tag.isOpen())
		{
			// Open tag must have close tag
			if (!markupStream.atCloseTag())
			{
				// There must be a component in this discarded body
				markupStream
						.throwMarkupException("Expected close tag.  Possible attempt to embed component(s) "
								+ "in the body of a component which discards its body");
			}
		}
	}

	/**
	 * Visits the parents of this component.
	 * 
	 * @param c
	 *            Class
	 * @param visitor
	 *            The visitor to call at each parent of the given type
	 * @return First non-null value returned by visitor callback
	 */
	protected final Object visitParents(final Class c, final IVisitor visitor)
	{
		// Start here
		Component current = this;

		// Walk up containment hierarchy
		while (current != null)
		{
			// Is current an instance of this class?
			if (c.isInstance(current))
			{
				final Object object = visitor.component(current);
				if (object != IVisitor.CONTINUE_TRAVERSAL)
				{
					return object;
				}
			}

			// Check parent
			current = current.getParent();
		}
		return null;
	}

	/**
	 * Gets the component at the given path.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 */
	Component get(final String path)
	{
		// Path to this component is an empty path
		if (path.equals(""))
		{
			return this;
		}
		throw new IllegalArgumentException(
				exceptionMessage("Component is not a container and so does not contain the path "
						+ path));
	}

	/**
	 * @param flag
	 *            The flag to test
	 * @return True if the flag is set
	 */
	final boolean getFlag(final int flag)
	{
		return (this.flags & flag) != 0;
	}

	/**
	 * Renders the close tag at the current position in the markup stream.
	 * 
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the tag to render
	 */
	final void renderClosingComponentTag(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Tag should be open tag and not openclose tag
		if (openTag.isOpen())
		{
			// If we found a close tag and it closes the open tag, we're good
			if (markupStream.atCloseTag() && markupStream.getTag().closes(openTag))
			{
				// Get the close tag from the stream
				ComponentTag closeTag = markupStream.getTag();

				// If the open tag had its id changed
				if (openTag.getNameChanged())
				{
					// change the id of the close tag
					closeTag = closeTag.mutable();
					closeTag.setName(openTag.getName());
				}

				// Render the close tag
				renderComponentTag(closeTag);
				markupStream.next();
			}
			else
			{
				if (openTag.requiresCloseTag())
				{
					// Missing close tag
					markupStream.throwMarkupException("Expected close tag for " + openTag);
				}
			}
		}
		else if (openTag.isOpenClose())
		{
			// Write synthetic close tag
			getResponse().write(openTag.syntheticCloseTagString());
		}
	}

	/**
	 * @param flag
	 *            The flag to set
	 * @param set
	 *            True to turn the flag on, false to turn it off
	 */
	final void setFlag(final byte flag, final boolean set)
	{
		if (set)
		{
			this.flags |= flag;
		}
		else
		{
			this.flags &= ~flag;
		}
	}

	/**
	 * Sets the parent of a component.
	 * 
	 * @param parent
	 *            The parent container
	 */
	final void setParent(final MarkupContainer parent)
	{
		if (this.parent != null && log.isDebugEnabled())
		{
			log.debug("replacing parent " + this.parent + " with " + parent);
		}
		this.parent = parent;
	}

	/**
	 * @return True if this component or any of its parents is in auto-add mode
	 */
	final boolean isAuto()
	{
		// Search up hierarchy for FLAG_AUTO
		for (Component current = this; current != null; current = current.getParent())
		{
			if (current.getFlag(FLAG_AUTO))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param auto
	 *            True to put component into auto-add mode
	 */
	final void setAuto(final boolean auto)
	{
		setFlag(FLAG_AUTO, auto);
	}

	/**
	 * Detaches all models
	 */
	private final void detachModels()
	{
		// Detach any detachable model from this component
		detachModel();

		// Also detach models from any contained attribute modifiers
		if (attributeModifiers != null)
		{
			for (AttributeModifier current = attributeModifiers; current != null; current = current.next)
			{
				current.detachModel();
			}
		}
	}

	/**
	 * Finds the root object for an IModel
	 * 
	 * @param model
	 *            The model
	 * @return The root object
	 */
	private Object getRootModel(final IModel model)
	{
		Object nestedModelObject = model.getNestedModel();
		while (nestedModelObject instanceof IModel)
		{
			final Object next = ((IModel)nestedModelObject).getNestedModel();
			if (nestedModelObject == next)
			{
				throw new WicketRuntimeException("Model for " + nestedModelObject
						+ " is self-referential");
			}
			nestedModelObject = next;
		}
		return nestedModelObject;
	}

	/**
	 * Sets the id of this component. This method is private because the only
	 * time a component's id can be set is in its constructor.
	 * 
	 * @param id
	 *            The non-null id of this component
	 */
	private final void setId(final String id)
	{
		if (id == null && !(this instanceof Page))
		{
			throw new WicketRuntimeException("Null component id is not allowed.");
		}
		this.id = id;
	}
}
