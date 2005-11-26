/*
 * $Id$ $Revision:
 * 1.197 $ $Date$
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.authorization.AuthorizationException;
import wicket.authorization.CreationNotAllowedException;
import wicket.authorization.EnabledNotAllowedException;
import wicket.feedback.FeedbackMessage;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.model.CompoundPropertyModel;
import wicket.model.ICompoundModel;
import wicket.model.IModel;
import wicket.util.convert.IConverter;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;
import wicket.version.undo.Change;

/**
 * Component serves as the highest level abstract base class for all components.
 * 
 * <ul>
 * <li><b>Identity </b>- All Components must have a non-null id which is
 * retrieved by calling getId(). The id must be unique within the
 * MarkupContainer that holds the Component, but does not have to be globally
 * unique or unique within a Page's component hierarchy.
 * 
 * <li><b>Hierarchy </b>- A component has a parent which can be retrieved with
 * getParent(). If a component is an instance of MarkupContainer, it may have
 * children. In this way it has a place in the hierarchy of components contained
 * on a given page. The {@link Component#isAncestorOf(Component)} method returns
 * true if this Component is an ancestor of the given Component.
 * 
 * <li><b>Component Paths </b>- The path from the Page at the root of the
 * component hierarchy to a given Component is simply the concatenation with dot
 * separators of each id along the way. For example, the path "a.b.c" would
 * refer to the component named "c" inside the MarkupContainer named "b" inside
 * the container named "a". The path to a component can be retrieved by calling
 * getPath(). This path is an absolute path beginning with the id of the Page at
 * the root. Pages bear a PageMap/Session-relative identifier as their id, so
 * each absolute path will begin with a number, such as "0.a.b.c". To get a
 * Component path relative to the page that contains it, you can call
 * getPageRelativePath().
 * 
 * <li><b>LifeCycle </b>- Components participate in the following lifecycle
 * phases:
 * <ul>
 * <li><b>Construction </b>- A Component is constructed with the Java language
 * new operator. Children may be added during construction if the Component is a
 * MarkupContainer.
 * 
 * <li><b>Cluster Attachment </b>- When a Component is freshly replicated in a
 * clustered environment, it may need to initialize transient state. This is
 * possible by overriding the {@link Component#onSessionAttach()} method.
 * 
 * <li><b>Request Handling </b>- An incoming request is processed by a protocol
 * request handler such as WicketServlet. An associated Application object
 * creates Session, Request and Response objects for use by a given Component in
 * updating its model and rendering a response. These objects are stored inside
 * a container called {@link RequestCycle} which is accessible via
 * {@link Component#getRequestCycle()}. The convenience methods
 * {@link Component#getRequest()}, {@link Component#getResponse()} and
 * {@link Component#getSession()} provide easy access to the contents of this
 * container.
 * 
 * <li><b>Listener Invocation </b>- If the request references a listener on an
 * existing Component, that listener is called, allowing arbitrary user code to
 * handle events such as link clicks or form submits. Although arbitrary
 * listeners are supported in Wicket, the need to implement a new class of
 * listener is unlikely for a web application and even the need to implement a
 * listener interface directly is highly discouraged. Instead, calls to
 * listeners are routed through logic specific to the event, resulting in calls
 * to user code through other overridable methods. For example, the
 * {@link wicket.markup.html.form.IFormSubmitListener#onFormSubmitted()} method
 * implemented by the Form class is really a private implementation detail of
 * the Form class that is not designed to be overridden (although unfortunately,
 * it must be public since all interface methods in Java must be public).
 * Instead, Form subclasses should override user-oriented methods such as
 * onValidate(), onSubmit() and onError() (although only the latter two are
 * likely to be overridden in practice).
 * 
 * <li><b>onBeginRequest </b>- The {@link Component#onBeginRequest()} method is
 * called.
 * 
 * <li><b>Form Submit </b>- If a Form has been submitted and the Component is a
 * FormComponent, the component's model is validated by a call to
 * FormComponent.validate().
 * 
 * <li><b>Form Model Update </b>- If a valid Form has been submitted and the
 * Component is a FormComponent, the component's model is updated by a call to
 * FormComponent.updateModel().
 * 
 * <li><b>Rendering </b>- A markup response is generated by the Component via
 * {@link Component#render()}, which calls subclass implementation code
 * contained in {@link Component#onRender()}. Once this phase begins, a
 * Component becomes immutable. Attempts to alter the Component will result in a
 * WicketRuntimeException.
 * 
 * <li><b>onEndRequest </b>() - The {@link Component#onEndRequest()} method is
 * called.
 * </ul>
 * 
 * <li><b>Component Models </b>- The primary responsibility of a component is
 * to use its model (an object that implements IModel), which can be set via
 * {@link Component#setModel(IModel model)} and retrieved via
 * {@link Component#getModel()}, to render a response in an appropriate markup
 * language, such as HTML. In addition, form components know how to update their
 * models based on request information. Since the IModel interface is a wrapper
 * around an actual model object, a convenience method
 * {@link Component#getModelObject()} is provided to retrieve the model Object
 * from its IModel wrapper. A further convenience method,
 * {@link Component#getModelObjectAsString()}, is provided for the very common
 * operation of converting the wrapped model Object to a String.
 * 
 * <li><b>Visibility </b>- Components which have setVisible(false) will return
 * false from isVisible() and will not render a response (nor will their
 * children).
 * 
 * <li><b>Page </b>- The Page containing any given Component can be retrieved
 * by calling {@link Component#getPage()}. If the Component is not attached to
 * a Page, an IllegalStateException will be thrown. An equivalent method,
 * {@link Component#findPage()} is available for special circumstances where it
 * might be desirable to get a null reference back instead.
 * 
 * <li><b>Session </b>- The Page for a Component points back to the Session
 * that contains the Page. The Session for a component can be accessed with the
 * convenience method getSession(), which simply calls getPage().getSession().
 * 
 * <li><b>Locale </b>- The Locale for a Component is available through the
 * convenience method getLocale(), which is equivalent to
 * getSession().getLocale().
 * 
 * <li><b>String Resources </b>- Components can have associated String
 * resources via the Application's Localizer, which is available through the
 * method {@link Component#getLocalizer()}. The convenience methods
 * {@link Component#getString(String key)} and
 * {@link Component#getString(String key, IModel model)} wrap the identical
 * methods on the Application Localizer for easy access in Components.
 * 
 * <li><b>Style </b>- The style ("skin") for a component is available through
 * {@link Component#getStyle()}, which is equivalent to
 * getSession().getStyle(). Styles are intended to give a particular look to a
 * Component or Resource that is independent of its Locale. For example, a style
 * might be a set of resources, including images and markup files, which gives
 * the design look of "ocean" to the user. If the Session's style is set to
 * "ocean" and these resources are given names suffixed with "_ocean", Wicket's
 * resource management logic will be prefer these resources to other resources,
 * such as default resources, which are not as good of a match.
 * 
 * <li><b>AttributeModifiers </b>- You can add one or more
 * {@link AttributeModifier}s to any component if you need to programmatically
 * manipulate attributes of the markup tag to which a Component is attached.
 * 
 * <li><b>Application, ApplicationSettings and ApplicationPages </b>- The
 * getApplication() method provides convenient access to the Application for a
 * Component via getSession().getApplication(). The getApplicationSettings()
 * method is equivalent to getApplication().getSettings(). The
 * getApplicationPages is equivalent to getApplication().getPages().
 * 
 * <li><b>Feedback Messages </b>- The {@link Component#debug(String)},
 * {@link Component#info(String)}, {@link Component#warn(String)},
 * {@link Component#error(String)} and {@link Component#fatal(String)} methods
 * associate feedback messages with a Component. It is generally not necessary
 * to use these methods directly since Wicket validators automatically register
 * feedback messages on Components. Any feedback message for a given Component
 * can be retrieved with {@link Component#getFeedbackMessage}.
 * 
 * <li><b>Page Factory </b>- It is possible to change the way that Pages are
 * constructed by overriding the {@link Component#getPageFactory()} method,
 * returning your own implementation of {@link wicket.IPageFactory}.
 * 
 * <li><b>Versioning </b>- Pages are the unit of versioning in Wicket, but
 * fine-grained control of which Components should participate in versioning is
 * possible via the {@link Component#setVersioned(boolean)} method. The
 * versioning participation of a given Component can be retrieved with
 * {@link Component#isVersioned()}.
 * 
 * 
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public abstract class Component implements Serializable, IBehaviourListener
{
	/** True when a component is being auto-added */
	private static final short FLAG_AUTO = 0x0001;

	/** Flag for escaping HTML in model strings */
	private static final short FLAG_ESCAPE_MODEL_STRINGS = 0x0002;

	/** Flag for Component holding root compound model */
	private static final short FLAG_HAS_ROOT_MODEL = 0x0004;

	/** Versioning boolean */
	private static final short FLAG_VERSIONED = 0x0008;

	/** Visibility boolean */
	private static final short FLAG_VISIBLE = 0x0010;

	/** Render tag boolean */
	private static final short FLAG_RENDER_BODY_ONLY = 0x0020;

	/** Ignore attribute modifiers */
	private static final short FLAG_IGNORE_ATTRIBUTE_MODIFIER = 0x0040;

	/** True when a component is enabled for model updates and is reachable. */
	private static final short FLAG_ENABLED = 0x0080;
	/** Reserved subclass-definable flag bit */

	protected static final short FLAG_RESERVED1 = 0x0100;

	/** Reserved subclass-definable flag bit */
	protected static final short FLAG_RESERVED2 = 0x0200;

	/** Reserved subclass-definable flag bit */
	protected static final short FLAG_RESERVED3 = 0x0400;

	/** Reserved subclass-definable flag bit */
	protected static final short FLAG_RESERVED4 = 0x0800;

	/** boolean whether this component was rendered once for tracking changes. */
	private static final short FLAG_IS_RENDERED_ONCE = 0x1000;

	/** Component flags. See FLAG_* for possible non-exclusive flag values. */
	private short flags = FLAG_VISIBLE | FLAG_ESCAPE_MODEL_STRINGS | FLAG_VERSIONED 
			| FLAG_ENABLED;

	private static final IComponentValueComparator comparator = new IComponentValueComparator()
	{
		public boolean compareValue(Component component, Object newObject)
		{
			IModel model = component.getModel();
			Object previous = model.getObject(component);
			if (newObject == null && previous == null)
			{
				return true;
			}
			if (newObject == null || previous == null)
			{
				return false;
			}
			return newObject.equals(previous);
		}
	};

	/** Log. */
	private static Log log = LogFactory.getLog(Component.class);

	/** List of behaviours to be applied for this Component */
	private List behaviours = null;

	/** Component id. */
	private String id;

	/** The model for this component. */
	private IModel model;

	/** Any parent container. */
	private MarkupContainer parent;

	/**
	 * The position within the markup stream, where the markup for the component
	 * begins
	 */
	private int markupStreamPosition = -1;

	/**
	 * Internal indicator of whether this component may be rendered given the
	 * current context's authorization. It overrides the visible flag in case
	 * this is false. Authorization is done before trying to render any
	 * component (otherwise we would end up with a half rendered page in the
	 * buffer), and as an optimization, the result for the current request is
	 * stored in this variable.
	 */
	private transient boolean renderAllowed = true;

	/**
	 * Change record of a model.
	 */
	public class ComponentModelChange extends Change
	{
		private static final long serialVersionUID = 1L;

		/** former model. */
		private IModel model;

		/**
		 * Construct.
		 * 
		 * @param model
		 */
		public ComponentModelChange(IModel model)
		{
			super();
			this.model = model;
		}

		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		public void undo()
		{
			setModel(this.model);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "ComponentModelChange[component: " + getPath() + "]";
		}
	}

	/**
	 * Generic component visitor interface for component traversals.
	 */
	public static interface IVisitor
	{
		/**
		 * Value to return to continue a traversal.
		 */
		public static final Object CONTINUE_TRAVERSAL = null;

		/**
		 * A generic value to return to stop a traversal.
		 */
		public static final Object STOP_TRAVERSAL = new Object();

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
	 * A enabled change operation.
	 */
	protected final static class EnabledChange extends Change
	{
		private static final long serialVersionUID = 1L;

		/** subject. */
		private final Component component;

		/** former value. */
		private final boolean enabled;

		/**
		 * Construct.
		 * 
		 * @param component
		 */
		EnabledChange(final Component component)
		{
			this.component = component;
			this.enabled = component.getFlag(FLAG_ENABLED);
		}

		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		public void undo()
		{
			component.setEnabled(enabled);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "EnabledChange[component: " + component.getPath() + ",enabled: " + enabled + "]";
		}
	}

	/**
	 * A visibility change operation.
	 */
	protected final static class VisibilityChange extends Change
	{
		private static final long serialVersionUID = 1L;

		/** subject. */
		private final Component component;

		/** former value. */
		private final boolean visible;

		/**
		 * Construct.
		 * 
		 * @param component
		 */
		VisibilityChange(final Component component)
		{
			this.component = component;
			this.visible = component.getFlag(FLAG_VISIBLE);
		}

		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		public void undo()
		{
			component.setVisible(visible);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "VisibilityChange[component: " + component.getPath() + ", visible: " + visible
					+ "]";
		}
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
		checkAuthorization();
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
		checkAuthorization();
		setId(id);
		setModel(model);
	}

	/**
	 * Adds an behaviour modifier to the component.
	 * 
	 * @param behaviour
	 *            The behaviour modifier to be added
	 * @return this (to allow method call chaining)
	 */
	public final Component add(final IBehaviour behaviour)
	{
		if (behaviour == null)
		{
			throw new NullPointerException("argument may not be null");
		}

		// Lazy create
		if (behaviours == null)
		{
			behaviours = new ArrayList(1);
		}

		behaviours.add(behaviour);

		// Give handler the opportunity to bind this component
		behaviour.bind(this);
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
		return Application.get();
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
	public final String getId()
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
	 * Gets metadata for this component using the given key.
	 * 
	 * @param key
	 *            The key for the data
	 * @return The metadata
	 * @see MetaDataKey
	 */
	public final Serializable getMetaData(final MetaDataKey key)
	{
		return getPage().getMetaData(this, key);
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
			// If this component has the root model for a compound model
			if (getFlag(FLAG_HAS_ROOT_MODEL))
			{
				// we need to return the root model and not a property of the
				// model
				return getRootModel(model).getObject(null);
			}

			// Get model value for this component
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
		return Strings.afterFirstPathComponent(getPath(), ':');
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
				buffer.insert(0, ':');
			}
			buffer.insert(0, c.getId());
		}
		return buffer.toString();
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
	 * @return The shared resource for this component
	 */
	public final Resource getResource()
	{
		return getApplication().getSharedResources().get(Application.class, getId(), getLocale(),
				getStyle(), false);
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
	public final Session getSession()
	{
		// Fetch page, if possible
		final Page page = findPage();

		// If this component is attached to a page
		if (page != null)
		{
			// Get Session from Page (which should generally be
			// faster than a thread local lookup via Session.get())
			return page.getSessionInternal();
		}
		else
		{
			// Use ThreadLocal storage to get Session since this
			// component is apparently not yet attached to a Page.
			return Session.get();
		}
	}

	/**
	 * @param key
	 *            Key of string resource in property file
	 * @return The String
	 * @see Localizer
	 */
	public final String getString(final String key)
	{
		return getString(key, getModel());
	}

	/**
	 * @param key
	 *            The resource key
	 * @param model
	 *            The model
	 * @return The formatted string
	 * @see Localizer
	 */
	public final String getString(final String key, final IModel model)
	{
		return getLocalizer().getString(key, this, model);
	}

	/**
	 * @param key
	 *            The resource key
	 * @param model
	 *            The model
	 * @param defaultValue
	 *            A default value if the string cannot be found
	 * @return The formatted string
	 * @see Localizer
	 */
	public final String getString(final String key, final IModel model, final String defaultValue)
	{
		return getLocalizer().getString(key, this, model, defaultValue);
	}

	/**
	 * Gets the style of this component (see {@link wicket.Session}).
	 * 
	 * @return The style of this component.
	 * 
	 * @see wicket.Session
	 * @see wicket.Session#getStyle()
	 */
	public final String getStyle()
	{
		String variation = getVariation();
		String style = getSession().getStyle();
		if (variation != null && !"".equals(variation))
		{
			if (style != null && !"".equals(style))
			{
				style = variation + "_" + style;
			}
			else
			{
				style = variation;
			}
		}
		return style;
	}


	/**
	 * Gets the variation string of this component that will be used to look up
	 * markup for this component. Subclasses can override this method to define
	 * by an instance what markup variation should be picked up. By default it
	 * will return null.
	 * 
	 * @return The variation of this component.
	 * 
	 */
	public String getVariation()
	{
		return null;
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
	public final boolean isAncestorOf(final Component component)
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
	 * Gets whether this component is enabled. Specific components may decide to
	 * implement special behaviour that uses this property, like web form
	 * components that add a disabled='disabled' attribute when enabled is
	 * false.
	 * 
	 * @return whether this component is enabled.
	 */
	public boolean isEnabled()
	{
		return getFlag(FLAG_ENABLED);
	}

	/**
	 * @return Returns the isVersioned.
	 */
	public boolean isVersioned()
	{
		// Is the component itself versioned?
		if (!getFlag(FLAG_VERSIONED) || (!getFlag(FLAG_IS_RENDERED_ONCE)))
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
	 * Checks if the component itself and all its parents are visible.
	 * 
	 * @return true if the component and all its parents are visible.
	 */
	public final boolean isVisibleInHierarchy()
	{
		Component component = this;
		while (component != null)
		{
			if (renderAllowed && component.isVisible())
			{
				component = component.getParent();
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Called to indicate that the model content for this component has been
	 * changed
	 */
	public final void modelChanged()
	{
		// Call user code
		internalOnModelChanged();
		onModelChanged();
	}

	/**
	 * Called to indicate that the model content for this component is about to
	 * change
	 */
	public final void modelChanging()
	{
		// Call user code
		internalOnModelChanging();
		onModelChanging();

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
	 * Component has to implement {@link IBehaviourListener} to be able to pass
	 * through events to behaviours without ending up with many if/else blocks.
	 * 
	 * @see wicket.IBehaviourListener#onRequest()
	 */
	public void onRequest()
	{
		String id = getRequest().getParameter("id");

		if (id == null)
		{
			throw new WicketRuntimeException(
					"parameter id was not provided: unable to locate listener");
		}

		int IdAsInt = Integer.parseInt(id);
		IBehaviourListener behaviourListener = (IBehaviourListener)behaviours.get(IdAsInt);

		if (behaviourListener == null)
		{
			throw new WicketRuntimeException("no behaviour listener found with id " + id);
		}

		behaviourListener.onRequest();
	}

	/**
	 * Removes this component from its parent. It's important to remember that a
	 * component that is removed cannot be referenced from the markup still.
	 */
	public final void remove()
	{
		if (parent == null)
		{
			throw new IllegalStateException("cannot remove " + this + " from null parent!");
		}

		parent.remove(this);
	}

	/**
	 * Performs a render of this component.
	 */
	public final void render()
	{
		setFlag(FLAG_IS_RENDERED_ONCE, true);

		// Determine if component is visible using it's authorization status
		// and the isVisible property.
		if (renderAllowed && isVisible())
		{
			// Rendering is beginning
			if (log.isDebugEnabled())
			{
				log.debug("Begin render " + this);
			}

			// Call implementation to render component
			onRender();

			// Component has been rendered
			rendered();

			if (log.isDebugEnabled())
			{
				log.debug("End render " + this);
			}
		}
		else
		{
			findMarkupStream().skipComponent();
		}
	}

	/**
	 * THIS IS PART OF WICKETS INTERNAL API. DO NOT RELY ON IT WITHIN YOUR CODE.
	 * <p>
	 * Renders the component at the current position in the given markup stream.
	 * The method onComponentTag() is called to allow the component to mutate
	 * the start tag. The method onComponentTagBody() is then called to permit
	 * the component to render its body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	public final void renderComponent(final MarkupStream markupStream)
	{
		// If yet unknown, set the markup stream position with the current position
		// of markupStream. Else set the markupStream.setCurrentPosition based
		// on the position already known to the component.
		validateMarkupStream(markupStream);

		// Get mutable copy of next tag
		final ComponentTag tag = markupStream.getTag().mutable();

		// Call any tag handler
		onComponentTag(tag);

		// If we're an openclose tag
		if (!tag.isOpenClose() && !tag.isOpen())
		{
			// We were something other than <tag> or <tag/>
			markupStream
					.throwMarkupException("Method renderComponent called on bad markup element "
							+ tag);
		}

		// Render open tag
		if (getRenderBodyOnly() == false)
		{
			renderComponentTag(tag);
		}
		markupStream.next();

		// Render the body only if open-body-close. Do not render if open-close.
		if (tag.isOpen())
		{
			// Render the body
			onComponentTagBody(markupStream, tag);
		}

		// Render close tag
		if (tag.isOpen())
		{
			renderClosingComponentTag(markupStream, tag, getRenderBodyOnly());
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
	public final void rendered()
	{
		// Tell the page that the component rendered
		getPage().componentRendered(this);

		if (behaviours != null)
		{
			for (Iterator i = behaviours.iterator(); i.hasNext();)
			{
				IBehaviour behaviour = (IBehaviour)i.next();
				behaviour.rendered(this);
			}
		}
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
	 * Sets whether this component is enabled. Specific components may decide to
	 * implement special behaviour that uses this property, like web form
	 * components that add a disabled='disabled' attribute when enabled is
	 * false. If it is not enabled, it will not be allowed to call any listener
	 * method on it (e.g. Link.onClick) and the model object will be protected
	 * (for the common use cases, not for programmer's misuse)
	 * 
	 * @param enabled
	 *            whether this component is enabled
	 * @return This
	 */
	public final Component setEnabled(final boolean enabled)
	{
		// Is new enabled state a change?
		if (enabled != getFlag(FLAG_ENABLED))
		{
			// TODO we can't record any state change as Link.onComponentTag
			// potentially sets this property
			// we probably don't need to support this, but I'll keep this
			// commented so that we can
			// think about it
			// // Tell the page that this component's enabled was changed
			// final Page page = findPage();
			// if (page != null)
			// {
			// addStateChange(new EnabledChange(this));
			// }

			// Change visibility
			setFlag(FLAG_ENABLED, enabled);
		}
		return this;
	}

	/**
	 * If yet unknown, set the markup stream position with the current position
	 * of markupStream. Else set the markupStream.setCurrentPosition based the
	 * position already known to the component.
	 * <p>
	 * Note: Parameter markupStream.getCurrentPosition() will be updated, if
	 * re-render is allowed.
	 * 
	 * @param markupStream
	 */
	protected final void validateMarkupStream(final MarkupStream markupStream)
	{
		// Allow the component to be re-rendered without a page. Partial
		// re-rendering is a requirement of AJAX.
		final Page page = getPage();
		if (this.isAuto() || (page != null) && page.isRequiresFullRender())
		{
			// Remember the position while rendering the component the first
			// time
			this.markupStreamPosition = markupStream.getCurrentIndex();
		}
		else if (this.markupStreamPosition < 0)
		{
			// ListItems are created on the fly, which is why we can not throw an exception.
			// Because they are created on the fly (similar to autoAdd), they can 
			// not be re-rendered  
			this.markupStreamPosition = markupStream.getCurrentIndex();
//			throw new WicketRuntimeException(
//					"The markup stream of the component should be known by now, but isn't: " 
//					+ this.toString());
		}
		else
		{
			// Re-set the markups index to the beginning of the component tag
			markupStream.setCurrentIndex(this.markupStreamPosition);
		}
	}

	/**
	 * Sets whether model strings should be escaped.
	 * 
	 * @param escapeMarkup
	 *            True is model strings should be escaped
	 * @return This
	 */
	public final Component setEscapeModelStrings(final boolean escapeMarkup)
	{
		setFlag(FLAG_ESCAPE_MODEL_STRINGS, escapeMarkup);
		return this;
	}

	/**
	 * Sets the metadata for this component using the given key. If the metadata
	 * object is not of the correct type for the metadata key, an
	 * InvalidMetaDataTypeException will be thrown. For information on creating
	 * MetaDataKeys, see {@link MetaDataKey}.
	 * 
	 * @param key
	 *            The singleton key for the metadata
	 * @param object
	 *            The metadata object
	 * @throws InvalidMetaDataTypeException
	 * @see MetaDataKey
	 */
	public final void setMetaData(final MetaDataKey key, final Serializable object)
	{
		getPage().setMetaData(this, key, object);
	}

	/**
	 * Sets the given model.
	 * <p>
	 * WARNING: DO NOT OVERRIDE THIS METHOD UNLESS YOU HAVE A VERY GOOD REASON
	 * FOR IT. OVERRIDING THIS MIGHT OPEN UP SECURITY LEAKS AND BROKEN
	 * BACK-BUTTON SUPPORT.
	 * </p>
	 * 
	 * @param model
	 *            the model
	 * @return This
	 */
	public Component setModel(final IModel model)
	{
		// Detach current model
		if (this.model != null)
		{
			this.model.detach();
		}

		// Change model
		if (this.model != model)
		{
			addStateChange(new ComponentModelChange(this.model));
			this.model = model;
		}

		// If a compound model is explicitly set on this component
		if (model instanceof ICompoundModel)
		{
			// we need to remember this for getModelObject()
			setFlag(FLAG_HAS_ROOT_MODEL, true);
		}

		modelChanged();
		return this;
	}

	/**
	 * Sets the backing model object; shorthand for
	 * getModel().setObject(object).
	 * 
	 * @param object
	 *            The object to set
	 * @return This
	 */
	public final Component setModelObject(final Object object)
	{
		final IModel model = getModel();

		// check whether anything can be set at all
		if (model == null)
		{
			throw new IllegalStateException(
					"Attempt to set model object on null model of component: "
							+ getPageRelativePath());
		}

		// check authorization
		if (!getApplication().getAuthorizationStrategy().allowEnabled(this))
		{
			throw new EnabledNotAllowedException(
					"operation not allowed in the current authorization context");
		}

		// check whether this will result in a actual change
		if (!getComparator().compareValue(this, object))
		{
			modelChanging();

			if (getFlag(FLAG_HAS_ROOT_MODEL))
			{
				getRootModel(model).setObject(null, object);
			}
			else
			{
				model.setObject(this, object);
			}
			modelChanged();
		}
		return this;
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
	 * If false the component's tag will be printed as well as its body (which
	 * is default). If true only the body will be printed, but not the
	 * component's tag.
	 * 
	 * @param renderTag
	 *            If true, the component tag will not be printed
	 * @return This
	 */
	public final Component setRenderBodyOnly(final boolean renderTag)
	{
		this.setFlag(FLAG_RENDER_BODY_ONLY, renderTag);
		return this;
	}

	/**
	 * Sets the page that will respond to this request
	 * 
	 * @param cls
	 *            The response page class
	 * @see RequestCycle#setResponsePage(Class)
	 */
	public final void setResponsePage(final Class cls)
	{
		getRequestCycle().setResponsePage(cls);
	}

	/**
	 * Sets the page class and its parameters that will respond to this request
	 * 
	 * @param cls
	 *            The response page class
	 * @param parameters
	 *            The parameters for thsi bookmarkable page.
	 * @see RequestCycle#setResponsePage(Class, PageParameters)
	 */
	public final void setResponsePage(final Class cls, PageParameters parameters)
	{
		getRequestCycle().setResponsePage(cls, parameters);
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
	 * @param versioned
	 *            True to turn on versioning for this component, false to turn
	 *            it off for this component and any children.
	 * @return This
	 */
	public Component setVersioned(boolean versioned)
	{
		setFlag(FLAG_VERSIONED, versioned);
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
		// Is new visibility state a change?
		if (visible != getFlag(FLAG_VISIBLE))
		{
			// Tell the page that this component's visibility was changed
			final Page page = findPage();
			if (page != null)
			{
				addStateChange(new VisibilityChange(this));
			}

			// Change visibility
			setFlag(FLAG_VISIBLE, visible);
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
		return toString(true);
	}

	/**
	 * @param detailed
	 *            True if a detailed string is desired
	 * @return The string
	 */
	public String toString(final boolean detailed)
	{
		if (detailed)
		{
			final Page page = findPage();
			if (page == null)
			{
				return new StringBuffer("[Component id = ").append(getId()).append(
						", page = <No Page>, path = ").append(getPath()).append(".").append(
						Classes.name(getClass())).append("]").toString();
			}
			else
			{
				return new StringBuffer("[Component id = ").append(getId()).append(", page = ")
						.append(getPage().getClass().getName()).append(", path = ").append(
								getPath()).append(".").append(Classes.name(getClass())).append(
								", isVisible = ").append((renderAllowed && isVisible())).append(
								", isVersioned = ").append(isVersioned()).append("]").toString();
			}
		}
		else
		{
			return "[Component id = " + getId() + "]";
		}
	}

	/**
	 * Gets the url for the provided behaviour listener.
	 * 
	 * @param behaviourListener
	 *            the behaviour listener to get the url for
	 * @return The URL
	 * @see Page#urlFor(Component, Class)
	 */
	public final String urlFor(final IBehaviourListener behaviourListener)
	{
		if (behaviourListener == null)
		{
			throw new NullPointerException("argument behaviourListener must be not null");
		}

		if (behaviours == null)
		{
			throw new IllegalArgumentException("behaviourListener " + behaviourListener
					+ " was not registered with this component");
		}

		int index = behaviours.indexOf(behaviourListener);
		if (index == -1)
		{
			throw new IllegalArgumentException("behaviourListener " + behaviourListener
					+ " was not registered with this component");
		}

		return urlFor(IBehaviourListener.class) + "&id=" + index;
	}

	/**
	 * Gets the url for the listener interface (e.g. ILinkListener).
	 * 
	 * @param listenerInterface
	 *            The listener interface that the URL should call
	 * @return The URL
	 * @see Page#urlFor(Component, Class)
	 */
	public final String urlFor(final Class listenerInterface)
	{
		return getPage().urlFor(this, listenerInterface);
	}

	/**
	 * Registers a warning message for this component.
	 * 
	 * @param message
	 *            The message
	 */
	public final void warn(final String message)
	{
		getPage().getFeedbackMessages().warn(this, message);
	}

	/**
	 * Adds state change to page.
	 * 
	 * @param change
	 *            The change
	 */
	protected final void addStateChange(Change change)
	{
		Page page = findPage();
		if (page != null)
		{
			page.componentStateChanging(this, change);
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
	 * Detaches the model for this component if it is detachable.
	 */
	protected void detachModel()
	{
		// If the model is compound and it's not the root model, then it can
		// be reconstituted via initModel() after replication
		if (model instanceof CompoundPropertyModel && !getFlag(FLAG_HAS_ROOT_MODEL))
		{
			// Get rid of model which can be lazy-initialized again
			this.model = null;
		}
		else
		{
			if (model != null)
			{
				model.detach();
			}
		}
	}

	/**
	 * Prefixes an exception message with useful information about this.
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
		if (parent == null)
		{
			throw new IllegalStateException("cannot find markupstream for " + this
					+ " as there is no parent");
		}

		return parent.findMarkupStream();
	}

	/**
	 * If this Component is a Page, returns self. Otherwise, searches for the
	 * nearest Page parent in the component hierarchy. If no Page parent can be
	 * found, null is returned.
	 * 
	 * @return The Page or null if none can be found
	 */
	protected final Page findPage()
	{
		// Search for page
		return (Page)(this instanceof Page ? this : findParent(Page.class));
	}

	/**
	 * Gets the value comparator. Implementations of this interface can be used
	 * in the Component.getComparator() for testing the current value of the
	 * components model data with the new value that is given.
	 * 
	 * @return the value comparator
	 */
	protected IComponentValueComparator getComparator()
	{
		return comparator;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to test
	 * @return True if the flag is set
	 */
	protected final boolean getFlag(final short flag)
	{
		return (this.flags & flag) != 0;
	}

	/**
	 * If false the component's tag will be printed as well as its body (which
	 * is default). If true only the body will be printed, but not the
	 * component's tag.
	 * 
	 * @return If true, the component tag will not be printed
	 */
	protected final boolean getRenderBodyOnly()
	{
		return getFlag(FLAG_RENDER_BODY_ONLY);
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
		// Search parents for CompoundPropertyModel
		for (Component current = getParent(); current != null; current = current.getParent())
		{
			// Get model
			final IModel model = current.getModel();
			if (model instanceof ICompoundModel)
			{
				// we turn off versioning as we share the model with another
				// component
				// that is the owner of the model (that component has to decide
				// whether to version or not
				setVersioned(false);

				// return the shared compound model
				return model;
			}
		}

		// No model for this component!
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
	 * Called when this component is deserialized as part of session replication
	 */
	protected void onSessionAttach()
	{
	}

	/**
	 * Redirects browser to the given page. note: usually, you should never call
	 * this method directly, but work with {@link #setResponsePage(Page)} or
	 * {@link #setResponsePage(Class)} instead. this method is part of wicket's
	 * internal behaviour and should only be used when you want to circumvent
	 * the normal framework behaviour and issue the redirect directly.
	 * 
	 * @param page
	 *            The page to redirect to
	 */
	protected void redirectTo(final Page page)
	{
		try
		{
			getRequestCycle().redirectTo(page);
		}
		catch (ServletException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}

	/**
	 * Gets the currently coupled {@link IBehaviour}s as a unmodifiable list.
	 * Returns an empty list rather than null if there are no behaviours coupled
	 * to this component.
	 * 
	 * @return the currently coupled behaviours as a unmodifiable list
	 */
	protected final List/* <IBehaviour> */getBehaviours()
	{
		if (behaviours == null)
		{
			return Collections.EMPTY_LIST;
		}

		return Collections.unmodifiableList(behaviours);
	}

	/**
	 * Gets the subset of the currently coupled {@link IBehaviour}s that are of
	 * the provided type as a unmodifiable list or null if there are no
	 * behaviours attached. Returns an empty list rather than null if there are
	 * no behaviours coupled to this component.
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return the subset of the currently coupled behaviours that are of the
	 *         provided type as a unmodifiable list or null
	 */
	protected final List/* <IBehaviour> */getBehaviours(Class type)
	{
		if (behaviours == null)
		{
			return Collections.EMPTY_LIST;
		}

		List subset = new ArrayList(behaviours.size()); // avoid growing
		for (Iterator i = behaviours.iterator(); i.hasNext();)
		{
			Object behaviour = i.next();
			if (type.isAssignableFrom(behaviour.getClass()))
			{
				subset.add(behaviour);
			}
		}
		return Collections.unmodifiableList(subset);
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
			// Apply behaviour modifiers
			if (behaviours != null && !behaviours.isEmpty())
			{
				tag = tag.mutable();

				for (Iterator i = behaviours.iterator(); i.hasNext();)
				{
					IBehaviour behaviour = (IBehaviour)i.next();
					// components may reject some behaviour components
					if (isBehaviourAccepted(behaviour))
					{
						behaviour.onComponentTag(this, tag);
					}
				}
			}

			// Write the tag
			tag.writeOutput(getResponse(), settings.getStripWicketTags(), this.findMarkupStream()
					.getWicketNamespace());
		}
	}

	/**
	 * Components are allowed to reject behaviour modifiers.
	 * 
	 * @param behaviour
	 * @return false, if the component should not apply this behaviour
	 */
	protected boolean isBehaviourAccepted(final IBehaviour behaviour)
	{

		// Ignore AttributeModifiers when FLAG_IGNORE_ATTRIBUTE_MODIFIER is set
		if (behaviour instanceof AttributeModifier
				&& getFlag(FLAG_IGNORE_ATTRIBUTE_MODIFIER) != false)
			return false;

		return true;
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
						.throwMarkupException("Expected close tag.	Possible attempt to embed component(s) "
								+ "in the body of a component which discards its body");
			}
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT!
	 * 
	 * @param flag
	 *            The flag to set
	 * @param set
	 *            True to turn the flag on, false to turn it off
	 */
	protected final void setFlag(final short flag, final boolean set)
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
	 * If true, all attribute modifiers will be ignored
	 * 
	 * @param ignore
	 *            If true, all attribute modifiers will be ignored
	 * @return This
	 */
	protected final Component setIgnoreAttributeModifier(final boolean ignore)
	{
		this.setFlag(FLAG_IGNORE_ATTRIBUTE_MODIFIER, ignore);
		return this;
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
	 * Detaches all models
	 */
	final void detachModels()
	{
		// Detach any detachable model from this component
		detachModel();

		// Also detach models from any contained attribute modifiers
		if (behaviours != null)
		{
			for (Iterator i = behaviours.iterator(); i.hasNext();)
			{
				IBehaviour behaviour = (IBehaviour)i.next();
				behaviour.detachModel();
			}
		}
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
	 * Renders the close tag at the current position in the markup stream.
	 * 
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the tag to render
	 * @param renderTagOnly
	 *            if true, the tag will not be written to the output
	 */
	final void renderClosingComponentTag(final MarkupStream markupStream,
			final ComponentTag openTag, final boolean renderTagOnly)
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
				if (renderTagOnly == false)
				{
					renderComponentTag(closeTag);
				}
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
	 * Sets the id of this component. This method is private because the only
	 * time a component's id can be set is in its constructor.
	 * 
	 * @param id
	 *            The non-null id of this component
	 */
	final void setId(final String id)
	{
		if (id == null && !(this instanceof Page))
		{
			throw new WicketRuntimeException("Null component id is not allowed.");
		}
		this.id = id;
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
	 * Sets the render allowed flag.
	 * 
	 * @param renderAllowed
	 */
	final void setRenderAllowed(boolean renderAllowed)
	{
		this.renderAllowed = renderAllowed;
	}

	/**
	 * Check whether this component may be created at all. Throws a
	 * {@link AuthorizationException} when it may not be created
	 * 
	 */
	private final void checkAuthorization()
	{
		if (!getApplication().getAuthorizationStrategy().allowCreateComponent(getClass()))
		{
			throw new CreationNotAllowedException("insufficiently authorized to create component "
					+ getClass());
		}
	}

	/**
	 * Finds the root object for an IModel
	 * 
	 * @param model
	 *            The model
	 * @return The root object
	 */
	private final IModel getRootModel(final IModel model)
	{
		IModel nestedModelObject = model;
		while (true)
		{
			final IModel next = ((IModel)nestedModelObject).getNestedModel();
			if (next == null)
			{
				break;
			}
			if (nestedModelObject == next)
			{
				throw new WicketRuntimeException("Model for " + nestedModelObject
						+ " is self-referential");
			}
			nestedModelObject = next;
		}
		return nestedModelObject;
	}
}
