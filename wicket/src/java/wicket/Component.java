/*
 * $Id$ $Revision:
 * 1.17 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.parser.XmlTag;
import wicket.model.IDetachableModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.response.NullResponse;
import wicket.util.string.Strings;

/**
 * Component serves as the highest level abstract base class for all components.
 * A component has a parent. If a component is an instance of Container, it may
 * have children. In this way it has a place in the hierarchy of components
 * contained on a given page.
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
 * component is simply the concatenation with dot separators of each name along
 * the way. For example, the path "a.b.c" would refer to the component named "c"
 * inside the container named "b" inside the container named "a". The path to a
 * component can be retrieved by calling getPath(). This path is an absolute
 * path beginning with the name of the page at the root. Pages bear a
 * session-relative identifier as their name, so each absolute path will begin
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
	/** Log. */
	private static Log log = LogFactory.getLog(Component.class);

	/**
	 * Collections to hold instances of modifiers to be applied for this
	 * component.
	 */
	private List attributeModifiers = null;

	/** The model for this component. */
	private IModel model;

	/** Component name. */
	private final String name;

	/** Any parent container. */
	private Container parent;

	/** Component rendering number useful in debugging. */
	private int rendering = 0;

	/** True if the component allows unescaped HTML. */
	private boolean shouldEscapeModelStrings = true;

	/** True if this component is visible. */
	private boolean visible = true;

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
	 * Constructor. All components have names. A component's name cannot be
	 * null. This is the minimal constructor of component. It does not register
	 * a model.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Component(final String name)
	{
		if (name == null && !(this instanceof Page))
		{
			throw new WicketRuntimeException("Null component name is not allowed.");
		}
		this.name = name;
	}

	/**
	 * Constructor that uses the provided {@link IModel}as its model. All
	 * components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param model
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Component(String name, IModel model)
	{
		this(name);
		setModel(model);
	}

	/**
	 * Constructor that uses the provided instance of {@link IModel}as a
	 * dynamic model. This model will be wrapped in an instance of
	 * {@link PropertyModel}using the provided OGNL expression. Thus, using
	 * this constructor is a short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(myIModel, expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param model
	 *            The instance of {@link IModel}from which the model object
	 *            will be used as the subject for the given expression
	 * @param expression
	 *            The OGNL expression that works on the given object
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Component(String name, IModel model, String expression)
	{
		this(name);
		setModel(new PropertyModel(model, expression));
	}

	/**
	 * Constructor that uses the provided object as a simple model. This object
	 * will be wrapped in an instance of {@link Model}. All components have
	 * names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param object
	 *            the object that will be used as a simple model
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Component(String name, Serializable object)
	{
		this(name, new Model(object));
	}

	/**
	 * Constructor that uses the provided object as a dynamic model. This object
	 * will be wrapped in an instance of {@link Model}that will be wrapped in
	 * an instance of {@link PropertyModel}using the provided expression. Thus,
	 * using this constructor is a short-hand for:
	 * 
	 * <pre>
	 * new MyComponent(name, new PropertyModel(new Model(object), expression));
	 * </pre>
	 * 
	 * All components have names. A component's name cannot be null.
	 * 
	 * @param name
	 *            The non-null name of this component
	 * @param object
	 *            the object that will be used as the subject for the given
	 *            expression
	 * @param expression
	 *            the OGNL expression that works on the given object
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null name.
	 */
	public Component(String name, Serializable object, String expression)
	{
		this(name, new Model(object), expression);
	}

	/**
	 * Adds an attribute modifier to the component.
	 * 
	 * @param modifier
	 *            The attribute modifier to be added
	 * @return this (to allow method call chaining)
	 */
	public final Component add(final ComponentTagAttributeModifier modifier)
	{
		if (attributeModifiers == null)
		{
			attributeModifiers = new ArrayList();
		}
		attributeModifiers.add(modifier);
		return this;
	}
    
    /**
     * Registers a debug message for this component
     * @param message The message
     */
    public final void debug(final String message)
    {
        getSession().getFeedbackMessages().debug(this, message);   
    }
    
    /**
     * Registers an error message for this component
     * @param message The message
     */
    public final void error(final String message)
    {
        getSession().getFeedbackMessages().error(this, message);   
    }
    
    /**
     * Registers an fatal error message for this component
     * @param message The message
     */
    public final void fatal(final String message)
    {
        getSession().getFeedbackMessages().fatal(this, message);   
    }

	/**
	 * Finds the first container parent of this component of the given class.
	 * 
	 * @param c
	 *            Container class to search for
	 * @return First container parent that is an instance of the given class, or
	 *         null if none can be found
	 */
	public final Container findParent(final Class c)
	{
		// Start with immediate parent
		Container current = parent;

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
     * @return Any feedback message for this component
     */
    public FeedbackMessage getFeedbackMessage()
    {
        return getSession().getFeedbackMessages().getMessageFor(this);   
    }

	/**
	 * Gets the model. It returns the object that wraps the backing model.
	 * 
	 * @return The model
	 */
	public final IModel getModel()
	{
		if (model != null && model instanceof IDetachableModel)
		{
			((IDetachableModel)model).attach();
		}
		return model;
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
			return model.getObject();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Gets a model property as a string.
	 * 
	 * @return Model property for this component as a string
	 */
	public final String getModelObjectAsString()
	{
		IModel model = getModel();
		if (model != null)
		{
			final Object property = model.getObject();
			if (property != null)
			{
				// Model string from property
				final String modelString = (property instanceof String) ? (String)property : String
						.valueOf(property);

				// If we should escape the markup
				if (shouldEscapeModelStrings)
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
	 * Gets the name of this component.
	 * 
	 * @return The name of this component
	 */
	public String getName()
	{
		return name;
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
	public final Container getParent()
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
			buffer.insert(0, c.getName());
		}
		return buffer.toString();
	}

	/**
	 * Gets the number of times this component has been rendered.
	 * 
	 * @return The number of times this component has been rendered. This can be
	 *         useful in debugging.
	 */
	public final int getRendering()
	{
		return rendering;
	}

	/**
	 * @return The request for this component's active request cycle
	 */
	public final Request getRequest()
	{
		return getRequestCycle().getRequest();
	}

	/**
	 * Gets the request parameter for this component as a boolean.
	 * 
	 * @return The value in the request for this component
	 */
	public final Boolean getRequestBoolean()
	{
		return Boolean.valueOf(!Strings.isEmpty(getRequestString()));
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
	 * Gets the request parameter for this component as an int.
	 * 
	 * @return The value in the request for this component
	 */
	public final int getRequestInt()
	{
		final String string = getRequestString();
		try
		{
			return Integer.parseInt(string);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException(exceptionMessage("Internal error.  Request string '"
					+ string + "' not a valid integer"));
		}
	}

	/**
	 * Gets the request parameter for this component as an int, using the given
	 * default in case no corresponding request parameter was found.
	 * 
	 * @param defaultValue
	 *            Default value to return if request does not have an integer
	 *            for this component
	 * @return The value in the request for this component
	 */
	public final int getRequestInt(final int defaultValue)
	{
		final String string = getRequestString();
		if (string != null)
		{
			try
			{
				return Integer.parseInt(string);
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException(exceptionMessage("Request string '" + string
						+ "' is not a valid integer"));
			}
		}
		else
		{
			return defaultValue;
		}
	}

	/**
	 * Gets the request parameters for this component as ints.
	 * 
	 * @return The values in the request for this component
	 */
	public final int[] getRequestInts()
	{
		final String[] strings = getRequestStrings();
		if (strings != null)
		{
			final int[] ints = new int[strings.length];
			for (int i = 0; i < strings.length; i++)
			{
				ints[i] = Integer.parseInt(strings[i]);
			}
			return ints;
		}
		return null;
	}

	/**
	 * Gets the request parameter for this component as a string.
	 * 
	 * @return The value in the request for this component
	 */
	public final String getRequestString()
	{
		return getRequest().getParameter(getPath());
	}

	/**
	 * Gets the request parameters for this component as strings.
	 * 
	 * @return The valuess in the request for this component
	 */
	public final String[] getRequestStrings()
	{
		return getRequest().getParameters(getPath());
	}

	/**
	 * @return The response for this component's active request cycle
	 */
	public final Response getResponse()
	{
		return getRequestCycle().getResponse();
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
	 * Gets whether model strings should be escaped.
	 * 
	 * @return Returns whether model strings should be escaped
	 */
	public final boolean getShouldEscapeModelStrings()
	{
		return shouldEscapeModelStrings;
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
    	return getSession().getFeedbackMessages().hasErrorMessageFor(this);
    }
    
    /**
     * @return True if this component has some kind of feedback message
     */
    public final boolean hasFeedbackMessage()
    {
        return getSession().getFeedbackMessages().hasMessageFor(this);    
    }
    
    /**
     * Registers a info message for this component
     * @param message The message
     */
    public final void info(final String message)
    {
        getSession().getFeedbackMessages().info(this, message);   
    }

	/**
	 * Gets whether this component and any children are visible.
	 * 
	 * @return True if component and any children are visible
	 */
	public final boolean isVisible()
	{
		return visible;
	}

	/**
	 * Performs a render of this component.
	 */
	public void render()
	{
		// Get request cycle to render to
		final RequestCycle cycle = getRequestCycle();

		// Save original Response
		final Response originalResponse = cycle.getResponse();

		// If component is not visible, set response to NullResponse
		if (!visible)
		{
			cycle.setResponse(NullResponse.getInstance());
		}

		// Call implementation to render component
		handleRender();

		// Restore original response
		cycle.setResponse(originalResponse);

		// Increase render count for component
		rendering++;
	}

	/**
	 * Sets the given model.
	 * 
	 * @param model
	 *            the model
	 * @return This
	 */
	public final Component setModel(final IModel model)
	{
		final IModel currentModel = getModel();
		if (currentModel != null && currentModel instanceof IDetachableModel)
		{
			((IDetachableModel)currentModel).detach();
		}
		this.model = (IModel)model;
		return this;
	}

	/**
	 * Sets the backing model object; shorthand for getModel().setObject(value).
	 * 
	 * @param value
	 *            The value to set
	 */
	public final void setModelObject(final Object value)
	{
		final IModel model = getModel();
		if (model != null)
		{
			model.setObject(value);
		}
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
		this.shouldEscapeModelStrings = escapeMarkup;
		return this;
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
		this.visible = visible;
		return this;
	}

	/**
	 * Gets the string representation of this component, which in this case is
	 * its path.
	 * 
	 * @return The path to this component
	 */
	public String toString()
	{
		return getPath();
	}
    
    /**
     * Registers a warning message for this component
     * @param message The message
     */
    public final void warn(final String message)
    {
        getSession().getFeedbackMessages().warn(this, message);   
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
	protected final void checkAttribute(final ComponentTag tag, final String key, final String value)
	{
		if (key != null)
		{
			final String tagAttributeValue = tag.getAttributes().getString(key);
			if (tagAttributeValue == null || !value.equalsIgnoreCase(tagAttributeValue))
			{
				findMarkupStream().throwMarkupException(
						"Component " + getName() + " must be applied to a tag with '" + key
								+ "' attribute matching '" + value + "', not '" + tagAttributeValue
								+ "'");
			}
		}
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
	protected final void checkTag(final ComponentTag tag, final String name)
	{
		if (!tag.getName().equalsIgnoreCase(name))
		{
			findMarkupStream().throwMarkupException(
					"Component " + getName() + " must be applied to a tag of type '" + name
							+ "', not " + tag.toUserDebugString());
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
		String s = "Page " + getPage();
		if (!(this instanceof Page))
		{
			s += ", component " + this;
		}
		return s + ": " + message;
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
	 * Processes the body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	protected void handleBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		markupStream.throwMarkupException("No handleBody implementation found");
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 */
	protected void handleComponentTag(final ComponentTag tag)
	{
		// Optionally strip componentName tag
		stripComponentName(tag);
	}

	/**
	 * Renders this component.
	 */
	protected abstract void handleRender();

	/**
	 * Invalidates the model attached to this component. Traverses all pages in
	 * the session associated with this component. Within each page in the
	 * session, traverses all components looking for a component attached to the
	 * same model and having the same name as this component. For each such
	 * model, the corresponding page is made stale. In addition, all previous
	 * renderings of the page holding this component are made stale.
	 */
	protected void invalidateModel()
	{
		// Find the page where this component lives
		final Page page = getPage();

		if (page == null)
		{
			return;
		}

		// Make all previous renderings of the page stale
		page.setStaleRendering(page.getRendering());

		// Visit all pages in the session
		getSession().visitPages(new Session.IPageVisitor()
		{
			public void page(final Page currentPage)
			{
				// If page is not the component's own page
				if (currentPage != page)
				{
					// Visit child components on page
					currentPage.visitChildren(new IVisitor()
					{
						public Object component(final Component current)
						{
							// If the components have the same equals identity
							// (which is
							// assumed to be implemented in terms of database
							// identity)
							// and component is accessing the same property of
							// the model
							if (current.getModel() != null && getModel() != null
									&& current.getModel().equals(getModel())
									&& current.getName().equals(getName()))
							{
								// then make the page holding the component
								// stale
								currentPage.setStale(true);
							}
							return CONTINUE_TRAVERSAL;
						}
					});
				}
			}
		});
	}

	/**
	 * Renders the component at the current position in the given markup stream.
	 * The method handleComponentTag() is called to allow the component to
	 * mutate the start tag. The method handleBody() is then called to permit
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
		handleComponentTag(tag);

		// If we're an openclose tag
		final XmlTag.Type type = tag.getType();
		if (tag.isOpenClose())
		{
			// Change type to open tag
			tag.setType(XmlTag.OPEN);
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
		renderTag(tag);
		markupStream.next();

		// Render body using original tag type so implementors
		// of handleBody will know if the tag has a body or not
		tag.setType(type);
		handleBody(markupStream, tag);

		// Render close tag
		renderCloseTag(markupStream, tag);
	}

	/**
	 * Writes a simple tag out to the response stream. Any components that might
	 * be referenced by the tag are ignored. Also undertakes any tag attribute
	 * modifications if they have been added to the component.
	 * 
	 * @param tag
	 *            The tag to write
	 */
	protected final void renderTag(ComponentTag tag)
	{
		// Apply attribute tag modifiers
		if ((attributeModifiers != null) && (tag.getType() != XmlTag.CLOSE))
		{
			tag = tag.mutable();
			for (Iterator it = attributeModifiers.iterator(); it.hasNext();)
			{
				((ComponentTagAttributeModifier)it.next()).replaceAttibuteValue(tag);
			}
		}

		// Write the tag
		getResponse().write(tag);
	}

	/**
	 * Writes a simple tag out to the response stream. Any components that might
	 * be referenced by the tag are ignored.
	 * 
	 * @param markupStream
	 *            The markup stream to advance (where the tag came from)
	 */
	protected final void renderTag(final MarkupStream markupStream)
	{
		renderTag(markupStream, markupStream.getTag());
	}

	/**
	 * Writes a simple tag out to the response stream. Any components that might
	 * be referenced by the tag are ignored.
	 * 
	 * @param markupStream
	 *            The markup stream to advance (where the tag came from)
	 * @param tag
	 *            The tag to write
	 */
	protected final void renderTag(final MarkupStream markupStream, final ComponentTag tag)
	{
		// Optionally strip componentName tag
		stripComponentName(tag);

		// Write to output
		renderTag(tag);
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
	protected final void replaceBody(final MarkupStream markupStream, final ComponentTag tag,
			final String body)
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
	 * Check if all components rendered and throw an exception when this is not
	 * the case.
	 * 
	 * @param page
	 *            the page
	 */
	final void checkRendering(final Page page)
	{
		page.visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
				// If component never rendered
				if (component.rendering == 0)
				{
					// Throw exception
					throw new WicketRuntimeException(component
							.exceptionMessage("Component never rendered. You probably failed to "
									+ "reference it in your markup."));
				}
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Detach all models that the components of this page have.
	 * 
	 * @param page
	 *            the page
	 */
	final void detachModels(final Page page)
	{
		page.visitChildren(new IVisitor()
		{
			public Object component(final Component component)
			{
				IModel componentModel = component.getModel();
				if ((componentModel != null) && (componentModel instanceof IDetachableModel))
				{
					((IDetachableModel)componentModel).detach();
				}

				// Also detach models from any contained attribute modifiers
				if (component.attributeModifiers != null)
				{
					for (Iterator iterator = component.attributeModifiers.iterator(); iterator
							.hasNext();)
					{
						IModel modifierModel = (IModel)((ComponentTagAttributeModifier)iterator
								.next()).getReplaceModel();
						if ((modifierModel != null) && (modifierModel instanceof IDetachableModel))
						{
							((IDetachableModel)modifierModel).detach();
						}
					}
				}
				return CONTINUE_TRAVERSAL;
			}
		});
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
	 * Renders the close tag at the current position in the markup stream.
	 * 
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the tag to render
	 */
	final void renderCloseTag(final MarkupStream markupStream, final ComponentTag openTag)
	{
		// Tag should be open tag and not openclose tag
		if (openTag.isOpen())
		{
			// If we found a close tag and it closes the open tag, we're good
			if (markupStream.atCloseTag() && markupStream.getTag().closes(openTag))
			{
				// Get the close tag from the stream
				ComponentTag closeTag = markupStream.getTag();

				// If the open tag had its name changed
				if (openTag.getNameChanged())
				{
					// change the name of the close tag
					closeTag = closeTag.mutable();
					closeTag.setName(openTag.getName());
				}

				// Render the close tag
				renderTag(markupStream, closeTag);
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
	}

	/**
	 * Sets the parent of a component.
	 * 
	 * @param parent
	 *            The parent container
	 */
	final void setParent(final Container parent)
	{
		if (this.parent != null && log.isDebugEnabled())
		{
			log.debug("replacing parent " + this.parent + " with " + parent);
		}
		this.parent = parent;
	}

	/**
	 * If this Component is a Page, returns self. Otherwise, searches for the
	 * nearest Page parent in the component hierarchy. If no Page parent can be
	 * found, null is returned
	 * 
	 * @return The Page or null if none can be found
	 */
	private final Page findPage()
	{
		// Search for page
		return (Page)(this instanceof Page ? this : findParent(Page.class));
	}

	/**
	 * Strips the component name of stuff we do not need.
	 * 
	 * @param tag
	 *            The tag to strip
	 */
	private final void stripComponentName(final ComponentTag tag)
	{
		// Strip component name attribute if desired
		final ApplicationSettings settings = getApplication().getSettings();
		if (settings.getStripComponentNames())
		{
			// Get mutable copy of tag and remove component name
			tag.mutable().removeComponentName(settings.getComponentNameAttribute());
		}
	}
}


