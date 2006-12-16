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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.feedback.IFeedback;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupException;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupNotFoundException;
import wicket.markup.MarkupStream;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.parser.onLoadListener.IMarkupLoadListener;
import wicket.markup.resolver.IComponentResolver;
import wicket.model.IInheritableModel;
import wicket.model.IModel;
import wicket.model.IWrapModel;
import wicket.util.string.Strings;
import wicket.version.undo.Change;

/**
 * A MarkupContainer holds a map of child components.
 * <ul>
 * <li><b>Children </b>- Children can be added by calling the add() method, and
 * they can be looked up using a dotted path. For example, if a container called
 * "a" held a nested container "b" which held a nested component "c", then
 * a.get("b.c") would return the Component with id "c". The number of children
 * in a MarkupContainer can be determined by calling size(), and the whole
 * hierarchy of children held by a MarkupContainer can be traversed by calling
 * visitChildren(), passing in an implementation of Component.IVisitor.
 * 
 * <li><b>Markup Rendering </b>- A MarkupContainer also holds/references
 * associated markup which is used to render the container. As the markup stream
 * for a container is rendered, component references in the markup are resolved
 * by using the container to look up Components in the container's component map
 * by id. Each component referenced by the markup stream is given an opportunity
 * to render itself using the markup stream.
 * <p>
 * Components may alter their referring tag, replace the tag's body or insert
 * markup after the tag. But components cannot remove tags from the markup
 * stream. This is an important guarantee because graphic designers may be
 * setting attributes on component tags that affect visual presentation.
 * <p>
 * The type of markup held in a given container subclass can be determined by
 * calling getMarkupType(). Markup is accessed via a MarkupStream object which
 * allows a component to traverse ComponentTag and RawMarkup MarkupElements
 * while rendering a response. Markup in the stream may be HTML or some other
 * kind of markup, such as VXML, as determined by the specific container
 * subclass.
 * <p>
 * A markup stream may be directly associated with a container via
 * setMarkupStream. However, a container which does not have a markup stream
 * (its getMarkupStream() returns null) may inherit a markup stream from a
 * container above it in the component hierarchy. The findMarkupStream() method
 * will locate the first container at or above this container which has a markup
 * stream.
 * <p>
 * All Page containers set a markup stream before rendering by calling the
 * method getAssociatedMarkupStream() to load the markup associated with the
 * page. Since Page is at the top of the container hierarchy, it is guaranteed
 * that findMarkupStream will always return a valid markup stream.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @see MarkupStream
 * @author Jonathan Locke
 */
public abstract class MarkupContainer<T> extends Component<T> implements Iterable<Component<?>>
{
	private static final long serialVersionUID = 1L;

	/** Log for reporting. */
	private static final Log log = LogFactory.getLog(MarkupContainer.class);

	/** List of children or single child */
	private Object children;

	/**
	 * The markup stream for this container. This variable is used only during
	 * the render phase to provide access to the current element within the
	 * stream.
	 */
	private transient MarkupStream markupStream;

	/** The markup fragments from the associated file */
	private transient MarkupFragment associatedMarkup;

	/**
	 * Package scope constructor, only used by pages.
	 * 
	 * @throws WicketRuntimeException
	 *             Thrown if the component has been given a null id.
	 */
	MarkupContainer()
	{
		super();
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public MarkupContainer(MarkupContainer<?> parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public MarkupContainer(MarkupContainer<?> parent, final String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Get the child markup fragment with the 'id'.
	 * <p>
	 * Note that component paths don't work.
	 * 
	 * @param id
	 *            The child component id
	 * @return MarkupFragment The childs markup
	 */
	public MarkupFragment getMarkupFragment(final String id)
	{
		return getMarkupFragment().getChildFragment(id, true, true);
	}

	/**
	 * Adds a child component to this container.
	 * <p>
	 * Be careful when overriding this method, if not implemented properly it
	 * may lead to a java component hierarchy which no longer matches the
	 * template hierarchy, which in turn will lead to an error.
	 * 
	 * @param child
	 *            The child
	 * 
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add
	 *             operation.
	 * @return This
	 */
	final MarkupContainer<?> add(final Component<?> child)
	{
		if (child == null)
		{
			throw new IllegalArgumentException("argument child may not be null");
		}

		if (log.isDebugEnabled())
		{
			log.debug("Add " + child.getId() + " to component " + this.getClass().getName()
					+ " with path " + getPath());
		}

		// Add to map
		Component<?> replaced = put(child);
		child.setFlag(FLAG_REMOVED_FROM_PARENT, false);
		if (replaced != null)
		{
			replaced.setFlag(FLAG_REMOVED_FROM_PARENT, true);
			removedComponent(replaced);
			// The position of the associated markup remains the same
			child.markupIndex = replaced.markupIndex;


			// The generated markup id remains the same
			String replacedId = (replaced.hasMarkupIdMetaData()) ? replaced.getMarkupId() : null;
			child.setMarkupIdMetaData(replacedId);
		}
		// now call addedComponent (after removedComponent)
		addedComponent(child);

		return this;
	}

	/**
	 * This method allows a component to be added by an auto-resolver such as
	 * AutoComponentResolver or AutoLinkResolver. While the component is being
	 * added, the component's FLAG_AUTO boolean is set. The isAuto() method of
	 * Component returns true if a component or any of its parents has this bit
	 * set. When a component is added via autoAdd(), the logic in Page that
	 * normally (a) checks for modifications during the rendering process, and
	 * (b) versions components, is bypassed if Component.isAuto() returns true.
	 * <p>
	 * The result of all this is that components added with autoAdd() are free
	 * from versioning and can add their own children without the usual
	 * exception that would normally be thrown when the component hierarchy is
	 * modified during rendering.
	 * 
	 * @param component
	 *            The component to add
	 */
//	final void autoAdd(final Component component)
//	{
//		component.setAuto(true);
//		add(component);
//	}

	/**
	 * @param component
	 *            The component to check
	 * @param recurse
	 *            True if all descendents should be considered
	 * @return True if the component is contained in this container
	 */
	public final boolean contains(final Component<?> component, final boolean recurse)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component may not be null");
		}

		if (recurse)
		{
			// Start at component and continue while we're not out of parents
			for (Component<?> current = component; current != null;)
			{
				// Get parent
				final MarkupContainer<?> parent = current.getParent();

				// If this container is the parent, then the component is
				// recursively contained by this container
				if (parent == this)
				{
					// Found it!
					return true;
				}

				// Move up the chain to the next parent
				current = parent;
			}

			// Failed to find this container in component's ancestry
			return false;
		}
		else
		{
			// Is the component contained in this container?
			return component.getParent() == this;
		}
	}

	/**
	 * Get a child component by looking it up with the given path.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 */
	@Override
	public final Component<?> get(final String path)
	{
		// Reference to this container
		if (path == null || path.trim().equals(""))
		{
			return this;
		}

		// Get child's id, if any
		final String id = Strings.firstPathComponent(path, Component.PATH_SEPARATOR);

		// Get child by id
		Component<?> child = children_get(id);

		// If the container is transparent, than ask its parent.
		// ParentResolver does something quite similar, but because of <head>,
		// <body>, <wicket:panel> etc. it is quite common to have transparent
		// components. Hence, this is little short cut for a tiny performance
		// optimization.
		if ((child == null) && isTransparentResolver() && (getParent() != null))
		{
			// Special tags like "_body", "_panel" must implement
			// IComponentResolver if they want to be transparent.
			if (path.startsWith("_") == false)
			{
				child = getParent().get(path);
			}
		}

		// Found child?
		final String path2 = Strings.afterFirstPathComponent(path, Component.PATH_SEPARATOR);
		if (child != null)
		{
			// Recurse on latter part of path
			return child.get(path2);
		}

		return child;
	}

	/**
	 * Get the type of associated markup for this component.
	 * 
	 * @return The type of associated markup for this component (for example,
	 *         "html", "wml" or "vxml"). The markup type for a component is
	 *         independent of whether or not the component actually has an
	 *         associated markup resource file (which is determined at runtime).
	 *         If there is no markup type for a component, null may be returned,
	 *         but this means that no markup can be loaded for the class.
	 */
	public String getMarkupType()
	{
		throw new IllegalStateException(
				exceptionMessage("You cannot directly subclass Page or MarkupContainer.	 Instead, subclass a markup-specific class, such as WebPage or WebMarkupContainer"));
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * Adds a child component to this container.
	 * 
	 * @param child
	 *            The child
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add
	 *             operation.
	 */
	public void internalAdd(final Component<?> child)
	{
		if (log.isDebugEnabled())
		{
			log.debug("internalAdd " + child.getId() + " to " + this);
		}

		// Add to map
		addedComponent(child);
		put(child);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request begins.
	 */
	@Override
	public void internalAttach()
	{
		// Handle begin request for the container itself
		try
		{
			super.internalAttach();

			// Loop through child components
			final int size = children_size();
			for (int i = 0; i < size; i++)
			{
				// Get next child
				final Component<?> child = children_get(i);

				// Ignore feedback as that was done in Page
				if (!(child instanceof IFeedback))
				{
					// Call begin request on the child
					child.internalAttach();
				}
			}
		}
		catch (RuntimeException ex)
		{
			if (ex instanceof WicketRuntimeException)
			{
				throw ex;
			}
			else
			{
				throw new WicketRuntimeException("Error attaching this container for rendering: "
						+ this, ex);
			}
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request ends.
	 */
	@Override
	public void internalDetach()
	{
		// Handle end request for the container itself
		super.internalDetach();

		// Loop through child components
		for (Component<?> child : this)
		{
			// Call end request on the child
			child.internalDetach();
		}
	}

	/**
	 * @return Iterator that iterates through children in the order they were
	 *         added
	 */
	public final Iterator<Component<?>> iterator()
	{
		return new Iterator<Component<?>>()
		{
			int index = 0;

			public boolean hasNext()
			{
				return index < children_size();
			}

			public Component<?> next()
			{
				return children_get(index++);
			}

			public void remove()
			{
				removedComponent(children_remove(--index));
			}
		};
	}

	/**
	 * @param comparator
	 *            The comparator
	 * @return Iterator that iterates over children in the order specified by
	 *         comparator
	 */
	public final Iterator<Component<?>> iterator(Comparator<Component<?>> comparator)
	{
		final List<Component<?>> sorted;
		if (children == null)
		{
			sorted = Collections.emptyList();
		}
		else
		{
			if (children instanceof Component)
			{
				sorted = new ArrayList<Component<?>>(1);
				sorted.add((Component<?>)children);
			}
			else
			{
				sorted = Arrays.asList((Component<?>[])children);
			}
		}
		Collections.sort(sorted, comparator);
		return sorted.iterator();
	}

	/**
	 * @param component
	 *            Component to remove from this container
	 */
	public void remove(final Component<?> component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component may not be null");
		}

		if (children_remove(component) != null)
		{
			component.setFlag(FLAG_REMOVED_FROM_PARENT, true);
			removedComponent(component);
		}
	}

	/**
	 * Removes the given component
	 * 
	 * @param id
	 *            The id of the component to remove
	 */
	public final void remove(final String id)
	{
		if (id == null)
		{
			throw new IllegalArgumentException("argument id may not be null");
		}

		final Component<?> component = get(id);
		if (component != null)
		{
			remove(component);
		}
		else
		{
			throw new WicketRuntimeException("Unable to find a component with id '" + id
					+ "' to remove");
		}
	}

	/**
	 * Removes all children from this container.
	 * <p>
	 * Note: implementation does not call
	 * {@link MarkupContainer#remove(Component) } for each component.
	 */
	public final void removeAll()
	{
		if (children != null)
		{
			addStateChange(new Change()
			{
				private static final long serialVersionUID = 1L;

				final Object removedChildren = MarkupContainer.this.children;

				@Override
				public void undo()
				{
					MarkupContainer.this.children = removedChildren;
				}

				@Override
				public String toString()
				{
					return "RemoveAllChange[component: " + getPath() + ", removed Children: "
							+ removedChildren + "]";
				}
			});

			this.children = null;
		}
	}

	/**
	 * Renders the entire associated markup stream for a container such as a
	 * Border or Panel. Any leading or trailing raw markup in the associated
	 * markup is skipped.
	 * 
	 * @param openTagName
	 *            the tag to render the associated markup for
	 * @param exceptionMessage
	 *            message that will be used for exceptions
	 */
	public final void renderAssociatedMarkup(final String openTagName, final String exceptionMessage)
	{
		// Get markup associated with Border or Panel component
		final MarkupStream originalMarkupStream = getMarkupStream();
		final MarkupStream associatedMarkupStream;
		try
		{
			associatedMarkupStream = new MarkupStream(getAssociatedMarkup(true).getWicketFragment(
					openTagName, true));
		}
		catch (WicketRuntimeException ex)
		{
			throw new MarkupException("Unable to load associated markup file for " + this, ex);
		}

		// skip until the targetted tag is found
		associatedMarkupStream.skipUntil(openTagName);
		setMarkupStream(associatedMarkupStream);

		// Get open tag in associated markup of border component
		final ComponentTag associatedMarkupOpenTag = associatedMarkupStream.getTag();

		// Check for required open tag name
		if (!((associatedMarkupOpenTag != null) && associatedMarkupOpenTag.isOpen() && associatedMarkupOpenTag
				.isWicketTag()))
		{
			associatedMarkupStream.throwMarkupException(exceptionMessage);
		}

		try
		{
			setIgnoreAttributeModifier(true);
			renderComponentTag(associatedMarkupOpenTag);
			associatedMarkupStream.next();
			renderComponentTagBody(associatedMarkupStream, associatedMarkupOpenTag);
			renderClosingComponentTag(associatedMarkupStream, associatedMarkupOpenTag, false);
			setMarkupStream(originalMarkupStream);
		}
		finally
		{
			setIgnoreAttributeModifier(false);
		}
	}

	/**
	 * @see wicket.Component#setModel(wicket.model.IModel)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Component setModel(final IModel<T> model)
	{
		final IModel<T> previous = getModel();
		super.setModel(model);
		if (previous instanceof IInheritableModel)
		{
			visitChildren(new IVisitor()
			{
				public Object component(Component component)
				{
					IModel compModel = component.getModel();
					if (compModel instanceof IWrapModel)
					{
						compModel = ((IWrapModel)compModel).getNestedModel();
					}
					if (compModel == previous)
					{
						component.setModel(null);
					}
					else if (compModel == model)
					{
						component.modelChanged();
					}
					return IVisitor.CONTINUE_TRAVERSAL;
				}

			});
		}
		return this;
	}

	/**
	 * Get the number of children in this container.
	 * 
	 * @return Number of children in this container
	 */
	public final int size()
	{
		return children_size();
	}

	/**
	 * @see wicket.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toString(false);
	}

	/**
	 * @param detailed
	 *            True if a detailed string is desired
	 * @return String representation of this container
	 */
	@Override
	public String toString(final boolean detailed)
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append("[MarkupContainer ");
		buffer.append(super.toString(true));
		if (detailed)
		{
			if (getMarkupStream() != null)
			{
				buffer.append(", markupStream = " + getMarkupStream());
			}

			if (children_size() != 0)
			{
				buffer.append(", children = ");

				// Loop through child components
				final int size = children_size();
				for (int i = 0; i < size; i++)
				{
					// Get next child
					final Component<?> child = children_get(i);
					if (i != 0)
					{
						buffer.append(' ');
					}
					buffer.append(child.toString());
				}
			}
		}
		buffer.append(']');
		return buffer.toString();
	}

	/**
	 * Traverses all child components of the given class in this container,
	 * calling the visitor's visit method at each one.
	 * 
	 * @param clazz
	 *            The class of child to visit, or null to visit all children
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or
	 *         null if the entire traversal occurred
	 */
	public final Object visitChildren(final Class<?> clazz, final IVisitor visitor)
	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("argument visitor may not be null");
		}

		// Iterate through children of this container
		for (int i = 0; i < children_size(); i++)
		{
			// Get next child component
			final Component<?> child = children_get(i);
			Object value = null;

			// Is the child of the correct class (or was no class specified)?
			if (clazz == null || clazz.isInstance(child))
			{
				// Call visitor
				value = visitor.component(child);

				// If visitor returns a non-null value, it halts the traversal
				if ((value != IVisitor.CONTINUE_TRAVERSAL)
						&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
				{
					return value;
				}
			}

			// If child is a container
			if ((child instanceof MarkupContainer)
					&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
			{
				// visit the children in the container
				value = ((MarkupContainer<?>)child).visitChildren(clazz, visitor);

				// If visitor returns a non-null value, it halts the traversal
				if ((value != IVisitor.CONTINUE_TRAVERSAL)
						&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
				{
					return value;
				}
			}
		}

		return null;
	}

	/**
	 * Traverses all child components in this container, calling the visitor's
	 * visit method at each one.
	 * 
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or
	 *         null if the entire traversal occurred
	 */
	public final Object visitChildren(final IVisitor visitor)
	{
		return visitChildren(null, visitor);
	}

	/**
	 * Get the markup stream for this component.
	 * 
	 * @return The markup stream for this component, or if it doesn't have one,
	 *         the markup stream for the nearest parent which does have one
	 */
	@Override
	protected final MarkupStream findMarkupStream()
	{
		// Start here
		MarkupContainer<?> c = this;

		// Walk up hierarchy until markup found
		while (c.getMarkupStream() == null)
		{
			// Check parent
			c = c.getParent();

			// Are we at the top of the hierarchy?
			if (c == null)
			{
				// Failed to find markup stream
				throw new WicketRuntimeException(exceptionMessage("No markup found"));
			}
		}

		return c.getMarkupStream();
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource
	 * for this class.
	 * 
	 * @param throwException
	 *            If true, throw an exception, if markup could not be found
	 * @return A stream of MarkupElement elements
	 */
	public final MarkupFragment getAssociatedMarkup(final boolean throwException)
	{
		if (this.associatedMarkup == null)
		{
			try
			{
				this.associatedMarkup = getApplication().getMarkupCache().getMarkup(this,
						throwException);
			}
			catch (MarkupException ex)
			{
				// re-throw it. The exception contains already all the
				// information
				// required.
				throw ex;
			}
			catch (WicketRuntimeException ex)
			{
				// throw exception since there is no associated markup
				throw new MarkupNotFoundException(
						exceptionMessage("Markup of type '"
								+ getMarkupType()
								+ "' for component '"
								+ getClass().getName()
								+ "' not found."
								+ " Enable debug messages for wicket.util.resource to get a list of all filenames tried"),
						ex);
			}

			onAssociatedMarkupLoaded(this.associatedMarkup);
		}

		return this.associatedMarkup;
	}

	/**
	 * Make sure changes to the locale and style are handled properly
	 * 
	 * @see wicket.Component#onAfterRender()
	 */
	protected void onAfterRender()
	{
		this.associatedMarkup = null;
		super.onAfterRender();
	}

	/**
	 * Components which whish to analyze the markup and automatically add
	 * Components to the MarkupConainer may sublcass this method.
	 * <p>
	 * As the associated markup gets cached with the MarkupContainer, this
	 * method is guaranteed to be called just once.
	 * 
	 * @param markup
	 *            The associated markup just loaded.
	 */
	protected void onAssociatedMarkupLoaded(final MarkupFragment markup)
	{
		// Call all register load listeners
		for (IMarkupLoadListener listener : getApplication().getMarkupSettings()
				.getMarkupLoadListeners())
		{
			listener.onAssociatedMarkupLoaded(this, markup);
		}
	}

	/**
	 * Get the markup stream set on this container.
	 * 
	 * @return Returns the markup stream set on this container.
	 */
	public final MarkupStream getMarkupStream()
	{
		return markupStream;
	}

	/**
	 * Handle the container's body. If your override of this method does not
	 * advance the markup stream to the close tag for the openTag, a runtime
	 * exception will be thrown by the framework.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 */
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		renderComponentTagBody(markupStream, openTag);
	}

	/**
	 * Renders this component. This implementation just calls renderComponent.
	 * 
	 * @param markupStream
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		renderComponent(markupStream);
	}

	/**
	 * Renders this component and all sub-components using the given markup
	 * stream.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	protected void renderAll(final MarkupStream markupStream)
	{
		// Loop through the markup in this container
		while (markupStream.hasMore())
		{
			// Element rendering is responsible for advancing markup stream!
			final int index = markupStream.getCurrentIndex();
			renderNext(markupStream);
			if (index == markupStream.getCurrentIndex())
			{
				markupStream.throwMarkupException("Component at markup stream index " + index
						+ " failed to advance the markup stream");
			}
		}
	}

	/**
	 * Renders markup for the body of a ComponentTag from the current position
	 * in the given markup stream. If the open tag passed in does not require a
	 * close tag, nothing happens. Markup is rendered until the closing tag for
	 * openTag is reached.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag
	 */
	protected final void renderComponentTagBody(final MarkupStream markupStream,
			final ComponentTag openTag)
	{
		// If the open tag requires a close tag
		boolean render = openTag.requiresCloseTag();
		if (render == false)
		{
			// Tags like <p> do not require a close tag, but they may have.
			render = !openTag.hasNoCloseTag();
		}
		if (render == true)
		{
			// Loop through the markup in this container
			while (markupStream.hasMore() && !markupStream.get().closes(openTag))
			{
				// Render markup element. Doing so must advance the markup
				// stream
				final int index = markupStream.getCurrentIndex();
				renderNext(markupStream);
				if (index == markupStream.getCurrentIndex())
				{
					markupStream.throwMarkupException("Markup element at index " + index
							+ " failed to advance the markup stream");
				}
			}
		}
	}

	/**
	 * Set markup stream for this container.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	@Override
	protected final void setMarkupStream(final MarkupStream markupStream)
	{
		this.markupStream = markupStream;
	}

	/**
	 * @return True if this markup container has associated markup
	 */
	final boolean hasAssociatedMarkup()
	{
		return getApplication().getMarkupCache().hasAssociatedMarkup(this);
	}

	/**
	 * @param component
	 *            Component being added
	 */
	@SuppressWarnings("unchecked")
	private final void addedComponent(final Component component)
	{
		// Check for degenerate case
		if (component == this)
		{
			throw new IllegalArgumentException("Component " + component
					+ " can't be added to itself");
		}

		if (component.getParent() != this)
		{
			throw new IllegalArgumentException("Component " + component
					+ " can't be added to another parent " + component.getParent() + " then "
					+ this);
		}

		// Tell the page a component was added
		final Page page = findPage();
		if (page != null)
		{
			page.componentAdded(component);
		}
	}

	/**
	 * @param child
	 *            Child to add
	 */
	private final void children_add(final Component<?> child)
	{
		if (this.children == null)
		{
			this.children = child;
		}
		else
		{
			// Get current list size
			final int size = children_size();

			// Create array that holds size + 1 elements
			final Component<?>[] children = new Component[size + 1];

			// Loop through existing children copying them
			for (int i = 0; i < size; i++)
			{
				children[i] = children_get(i);
			}

			// Add new child to the end
			children[size] = child;

			// Save new children
			this.children = children;
		}
	}

	private final Component<?> children_get(int index)
	{
		if (index == 0)
		{
			if (children instanceof Component)
			{
				return (Component<?>)children;
			}
			else
			{
				return ((Component[])children)[index];
			}
		}
		else
		{
			return ((Component[])children)[index];
		}
	}

	private final Component<?> children_get(final String id)
	{
		if (children instanceof Component)
		{
			final Component<?> component = (Component<?>)children;
			if (component.getId().equals(id))
			{
				return component;
			}
		}
		else
		{
			if (children != null)
			{
				final Component<?>[] components = (Component[])children;
				for (Component<?> element : components)
				{
					if (element.getId().equals(id))
					{
						return element;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Will search for this specific child instance in the current
	 * children. So it will do a identity check, it will not look if the
	 * id is already present in the children. Use indexOf(String) for that. 
	 * @param child
	 * @return The index of this child.
	 */
	private final int children_indexOf(Component<?> child)
	{
		if (children instanceof Component)
		{
			if (children == child)
			{
				return 0;
			}
		}
		else
		{
			if (children != null)
			{
				final Component<?>[] components = (Component[])children;
				for (int i = 0; i < components.length; i++)
				{
					if (components[i] == child)
					{
						return i;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Will search for the id if it is found in the current children.
	 * @param id The id to search for.
	 * @return The index of this child.
	 */
	private final int children_indexOf(String id)
	{
		if (children instanceof Component)
		{
			if (((Component<?>)children).getId().equals(id))
			{
				return 0;
			}
		}
		else
		{
			if (children != null)
			{
				final Component<?>[] components = (Component[])children;
				for (int i = 0; i < components.length; i++)
				{
					if (components[i].getId().equals(id))
					{
						return i;
					}
				}
			}
		}
		return -1;
	}

	private final Component<?> children_remove(Component<?> component)
	{
		int index = children_indexOf(component);
		if (index != -1)
		{
			return children_remove(index);
		}
		return null;
	}

	private final Component<?> children_remove(int index)
	{
		if (children instanceof Component)
		{
			if (index == 0)
			{
				final Component<?> removed = (Component<?>)children;
				this.children = null;
				return removed;
			}
			else
			{
				throw new IndexOutOfBoundsException();
			}
		}
		else
		{
			Component<?>[] c = ((Component[])children);
			final Component<?> removed = c[index];
			if (c.length == 2)
			{
				if (index == 0)
				{
					this.children = c[1];
				}
				else if (index == 1)
				{
					this.children = c[0];
				}
				else
				{
					throw new IndexOutOfBoundsException();
				}
			}
			else
			{
				Component<?>[] newChildren = new Component[c.length - 1];
				int j = 0;
				for (int i = 0; i < c.length; i++)
				{
					if (i != index)
					{
						newChildren[j++] = c[i];
					}
				}
				this.children = newChildren;
			}
			return removed;
		}
	}

	private final Component<?> children_set(int index, Component<?> child)
	{
		final Component<?> replaced;
		if (index < children_size())
		{
			if (children == null || children instanceof Component)
			{
				replaced = (Component<?>)children;
				children = child;
			}
			else
			{
				final Component<?>[] children = (Component[])this.children;
				replaced = children[index];
				children[index] = child;
			}
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
		return replaced != child ? replaced : null;
	}

	private final int children_size()
	{
		if (children == null)
		{
			return 0;
		}
		else
		{
			if (children instanceof Component)
			{
				return 1;
			}
			return ((Component[])children).length;
		}
	}

	/**
	 * Ensure that there is space in childForId map for a new entry before
	 * adding it.
	 * 
	 * @param child
	 *            The child to put into the map
	 * @return Any component that was replaced
	 */
	private final Component<?> put(final Component<?> child)
	{
		// search for the child by id. So that it will
		// find the right index for the id instead of looking
		// if the component itself is already children.
		int index = children_indexOf(child.getId());
		if (index == -1)
		{
			children_add(child);
			return null;
		}
		else
		{
			return children_set(index, child);
		}
	}

	/**
	 * @param component
	 *            Component being removed
	 */
	@SuppressWarnings("unchecked")
	private final void removedComponent(final Component component)
	{
		// Notify Page that component is being removed
		final Page page = component.findPage();
		if (page != null)
		{
			page.componentRemoved(component);
		}

		// detach children models
		if (component instanceof MarkupContainer)
		{
			((MarkupContainer<?>)component).visitChildren(new IVisitor()
			{
				public Object component(Component component)
				{
					try
					{
						// detach any models of the component
						component.detachModels();
					}
					catch (Exception e) // catch anything; we MUST detach all
					// models
					{
						log.error("detaching models of component " + component + " failed:", e);
					}
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});
		}

		// Detach model
		component.detachModels();
		// Component is removed
		// component.setParent(null);
	}

	/**
	 * Renders the next element of markup in the given markup stream.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	private final void renderNext(final MarkupStream markupStream)
	{
		// Get the current markup element
		final MarkupElement element = markupStream.get();

		// If it a tag like <wicket..> or <span wicket:id="..." >
		if ((element instanceof ComponentTag) && !markupStream.atCloseTag())
		{
			// Get element as tag
			final ComponentTag tag = (ComponentTag)element;

			// Get component id
			final String id = tag.getId();
			if (id == null)
			{
				throw new WicketRuntimeException("Prgamming error: tag id must not be null: "
						+ tag.toString());
			}

			if (log.isDebugEnabled())
			{
				log.debug("Render component: " + id);
			}

			// Get the component for the id from the given container
			final Component<?> component = get(id);

			// Failed to find it?
			if (component != null)
			{
				component.render(markupStream);
			}
			else
			{
				// 2rd try: Components like Border and Panel might implement
				// the ComponentResolver interface as well.
				MarkupContainer<?> container = this;
				while (container != null)
				{
					if (container instanceof IComponentResolver)
					{
						if (((IComponentResolver)container).resolve(this, markupStream, tag))
						{
							return;
						}
					}

					container = container.findParent(MarkupContainer.class);
				}

				// 3rd try: Try application's component resolvers
				final List<IComponentResolver> componentResolvers = this.getApplication()
						.getPageSettings().getComponentResolvers();
				for (IComponentResolver resolver : componentResolvers)
				{
					if (resolver.resolve(this, markupStream, tag))
					{
						return;
					}
				}

				if (tag.isWicketTag())
				{
					if (tag.isChildTag())
					{
						markupStream.throwMarkupException("Found " + tag.toString()
								+ " but no <wicket:extend>");
					}
					else
					{
						markupStream.throwMarkupException("Failed to handle: " + tag.toString());
					}
				}

				// No one was able to handle the component id
				markupStream.throwMarkupException("Unable to find component with id '" + id
						+ "' in " + this + ". This means that you declared wicket:id=" + id
						+ " in your markup, but that you either did not add the "
						+ "component to your page at all, or that the hierarchy does not match.");
			}
		}
		else
		{
			// Render as raw markup
			getResponse().write(element.toCharSequence());
			markupStream.next();
		}
	}

	/**
	 * Some MarkupContainers (e.g. HtmlHeaderContainer, BodyOnLoadContainer)
	 * have to be transparent with respect to there child components. A
	 * transparent container gets its children from its parent container.
	 * <p>
	 * 
	 * @see wicket.markup.resolver.ParentResolver
	 * 
	 * @return false. By default a MarkupContainer is not transparent.
	 */
	public boolean isTransparentResolver()
	{
		return false;
	}

	/**
	 * @see wicket.Component#renderHead(wicket.markup.html.IHeaderResponse)
	 */
	@Override
	public void renderHead(final IHeaderResponse response)
	{
		if (isVisible())
		{
			super.renderHead(response);

			for (Component<?> child : this)
			{
				child.renderHead(response);
			}
		}
	}
}
