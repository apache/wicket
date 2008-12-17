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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IComponentInheritedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.settings.IDebugSettings;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.version.undo.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A MarkupContainer holds a map of child components.
 * <ul>
 * <li><b>Children </b>- Children can be added by calling the add() method, and they can be looked
 * up using a dotted path. For example, if a container called "a" held a nested container "b" which
 * held a nested component "c", then a.get("b:c") would return the Component with id "c". The number
 * of children in a MarkupContainer can be determined by calling size(), and the whole hierarchy of
 * children held by a MarkupContainer can be traversed by calling visitChildren(), passing in an
 * implementation of Component.IVisitor.
 * 
 * <li><b>Markup Rendering </b>- A MarkupContainer also holds/references associated markup which is
 * used to render the container. As the markup stream for a container is rendered, component
 * references in the markup are resolved by using the container to look up Components in the
 * container's component map by id. Each component referenced by the markup stream is given an
 * opportunity to render itself using the markup stream.
 * <p>
 * Components may alter their referring tag, replace the tag's body or insert markup after the tag.
 * But components cannot remove tags from the markup stream. This is an important guarantee because
 * graphic designers may be setting attributes on component tags that affect visual presentation.
 * <p>
 * The type of markup held in a given container subclass can be determined by calling
 * getMarkupType(). Markup is accessed via a MarkupStream object which allows a component to
 * traverse ComponentTag and RawMarkup MarkupElements while rendering a response. Markup in the
 * stream may be HTML or some other kind of markup, such as VXML, as determined by the specific
 * container subclass.
 * <p>
 * A markup stream may be directly associated with a container via setMarkupStream. However, a
 * container which does not have a markup stream (its getMarkupStream() returns null) may inherit a
 * markup stream from a container above it in the component hierarchy. The findMarkupStream() method
 * will locate the first container at or above this container which has a markup stream.
 * <p>
 * All Page containers set a markup stream before rendering by calling the method
 * getAssociatedMarkupStream() to load the markup associated with the page. Since Page is at the top
 * of the container hierarchy, it is guaranteed that findMarkupStream will always return a valid
 * markup stream.
 * 
 * @see MarkupStream
 * @author Jonathan Locke
 * 
 */
public abstract class MarkupContainer extends Component
{
	private static final long serialVersionUID = 1L;

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupContainer.class);

	/** List of children or single child */
	private Object children;

	/**
	 * The markup stream for this container. This variable is used only during the render phase to
	 * provide access to the current element within the stream.
	 */
	private transient MarkupStream markupStream;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public MarkupContainer(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public MarkupContainer(final String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Adds a child component to this container.
	 * 
	 * @param childs
	 *            The child(s)
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add operation.
	 * @return This
	 */
	public final MarkupContainer add(final Component... childs)
	{
		for (Component child : childs)
		{
			if (child == null)
			{
				throw new IllegalArgumentException("argument child may not be null");
			}

			checkHierarchyChange(child);

			if (log.isDebugEnabled())
			{
				log.debug("Add " + child.getId() + " to " + this);
			}

			// Add to map
			addedComponent(child);
			if (put(child) != null)
			{
				throw new IllegalArgumentException(exceptionMessage("A child with id '" +
					child.getId() + "' already exists"));
			}

		}
		return this;
	}

	/**
	 * Replaces a child component of this container with another or just adds it in case no child
	 * with the same id existed yet.
	 * 
	 * @param childs
	 *            The child(s) to be added or replaced
	 * @return This
	 */
	public final MarkupContainer addOrReplace(final Component... childs)
	{
		for (Component child : childs)
		{

			checkHierarchyChange(child);

			if (child == null)
			{
				throw new IllegalArgumentException("argument child must be not null");
			}

			if (get(child.getId()) == null)
			{
				add(child);
			}
			else
			{
				replace(child);
			}
		}

		return this;
	}

	/**
	 * This method allows a component to be added by an auto-resolver such as AutoComponentResolver
	 * or AutoLinkResolver. While the component is being added, the component's FLAG_AUTO boolean is
	 * set. The isAuto() method of Component returns true if a component or any of its parents has
	 * this bit set. When a component is added via autoAdd(), the logic in Page that normally (a)
	 * checks for modifications during the rendering process, and (b) versions components, is
	 * bypassed if Component.isAuto() returns true.
	 * <p>
	 * The result of all this is that components added with autoAdd() are free from versioning and
	 * can add their own children without the usual exception that would normally be thrown when the
	 * component hierarchy is modified during rendering.
	 * 
	 * @param component
	 *            The component to add
	 * @param markupStream
	 *            Null, if the parent container is able to provide the markup. Else the markup
	 *            stream to be used to render the component.
	 * @return True, if component has been added
	 */
	public final boolean autoAdd(final Component component, final MarkupStream markupStream)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component may not be null");
		}

		/* Replace strategy */
		component.setAuto(true);

		int index = children_indexOf(component);
		if (index >= 0)
		{
			children_remove(index);
		}
		add(component);
		component.prepareForRender();
		try
		{
			if (markupStream == null)
			{
				component.render();
			}
			else
			{
				component.render(markupStream);
			}
		}
		finally
		{
			component.afterRender();
		}
		return true;
	}

	/**
	 * 
	 * @param component
	 *            The component to add
	 * @return True, if component has been added
	 * 
	 * @deprecated since 1.3 Please use {@link #autoAdd(Component, MarkupStream)} instead
	 */
	@Deprecated
	public final boolean autoAdd(final Component component)
	{
		return autoAdd(component, null);
	}

	/**
	 * @param component
	 *            The component to check
	 * @param recurse
	 *            True if all descendents should be considered
	 * @return True if the component is contained in this container
	 */
	public final boolean contains(final Component component, final boolean recurse)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component may not be null");
		}

		if (recurse)
		{
			// Start at component and continue while we're not out of parents
			for (Component current = component; current != null;)
			{
				// Get parent
				final MarkupContainer parent = current.getParent();

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
	public final Component get(final String path)
	{
		// Reference to this container
		if (path == null || path.trim().equals(""))
		{
			return this;
		}

		// Get child's id, if any
		final String id = Strings.firstPathComponent(path, Component.PATH_SEPARATOR);

		// Get child by id
		Component child = children_get(id);

		// If the container is transparent, than ask its parent.
		// ParentResolver does something quite similar, but because of <head>,
		// <body>, <wicket:panel> etc. it is quite common to have transparent
		// components. Hence, this is little short cut for a tiny performance
		// optimization.
		if ((child == null) && isTransparentResolver() && (getParent() != null))
		{
			child = getParent().get(path);
		}

		// Found child?
		if (child != null)
		{
			final String path2 = Strings.afterFirstPathComponent(path, Component.PATH_SEPARATOR);
			// Recurse on latter part of path
			return child.get(path2);
		}

		return child;
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource for this class.
	 * 
	 * @param throwException
	 *            If true, throw an exception, if markup could not be found
	 * @return A stream of MarkupElement elements
	 */
	public MarkupStream getAssociatedMarkupStream(final boolean throwException)
	{
		try
		{
			return getApplication().getMarkupSettings().getMarkupCache().getMarkupStream(this,
				false, throwException);
		}
		catch (MarkupException ex)
		{
			// re-throw it. The exception contains already all the information
			// required.
			throw ex;
		}
		catch (WicketRuntimeException ex)
		{
			// throw exception since there is no associated markup
			throw new MarkupNotFoundException(
				exceptionMessage("Markup of type '" + getMarkupType() + "' for component '" +
					getClass().getName() + "' not found." +
					" Enable debug messages for org.apache.wicket.util.resource to get a list of all filenames tried"),
				ex);
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
	 * Get the type of associated markup for this component.
	 * 
	 * @return The type of associated markup for this component (for example, "html", "wml" or
	 *         "vxml"). The markup type for a component is independent of whether or not the
	 *         component actually has an associated markup resource file (which is determined at
	 *         runtime). If there is no markup type for a component, null may be returned, but this
	 *         means that no markup can be loaded for the class.
	 */
	public String getMarkupType()
	{
		return getPage().getMarkupType();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * Adds a child component to this container.
	 * 
	 * @param child
	 *            The child
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add operation.
	 */
	public void internalAdd(final Component child)
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
	 * Some MarkupContainers (e.g. HtmlHeaderContainer) have to be transparent with respect to their
	 * child components. A transparent container gets its children from its parent container.
	 * <p>
	 * 
	 * @see org.apache.wicket.markup.resolver.ParentResolver
	 * 
	 * @return false. By default a MarkupContainer is not transparent.
	 */
	public boolean isTransparentResolver()
	{
		return false;
	}

	/**
	 * @return Iterator that iterates through children in the order they were added
	 */
	public Iterator<? extends Component> iterator()
	{
		return new Iterator<Component>()
		{
			int index = 0;

			public boolean hasNext()
			{
				return index < children_size();
			}

			public Component next()
			{
				return children_get(index++);
			}

			public void remove()
			{
				final Component removed = children_remove(--index);
				checkHierarchyChange(removed);
				removedComponent(removed);
			}
		};
	}

	/**
	 * @param comparator
	 *            The comparator
	 * @return Iterator that iterates over children in the order specified by comparator
	 */
	public final Iterator<Component> iterator(Comparator<Component> comparator)
	{
		final List<Component> sorted;
		if (children == null)
		{
			sorted = Collections.emptyList();
		}
		else
		{
			if (children instanceof Component)
			{
				sorted = new ArrayList<Component>(1);
				sorted.add((Component)children);
			}
			else
			{
				int size = children_size();
				sorted = new ArrayList<Component>(size);
				for (int i = 0; i < size; i++)
				{
					sorted.add(children_get(i));
				}

			}
		}
		Collections.sort(sorted, comparator);
		return sorted.iterator();
	}

	/**
	 * NOT USED ANYMORE; it's here for helping people migrate from Wicket 1.2 to Wicket 1.3
	 * 
	 * @param <C>
	 * 
	 * @param containerClass
	 * @return nothing
	 * @throws IllegalStateException
	 *             throws an {@link IllegalStateException}
	 */
	// TODO remove after release 1.3.0
	public final <C extends Component> IResourceStream newMarkupResourceStream(
		Class<C> containerClass)
	{
		throw new IllegalStateException(
			"this method is not used any more (and shouldn't be called by clients anyway)");
	}

	/**
	 * @param component
	 *            Component to remove from this container
	 */
	public void remove(final Component component)
	{
		checkHierarchyChange(component);

		if (component == null)
		{
			throw new IllegalArgumentException("argument component may not be null");
		}

		children_remove(component);
		removedComponent(component);
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

		final Component component = get(id);
		if (component != null)
		{
			remove(component);
		}
		else
		{
			throw new WicketRuntimeException("Unable to find a component with id '" + id +
				"' to remove");
		}
	}

	/**
	 * Removes all children from this container.
	 * <p>
	 * Note: implementation does not call {@link MarkupContainer#remove(Component) } for each
	 * component.
	 */
	public final void removeAll()
	{
		if (children != null)
		{
			addStateChange(new Change()
			{
				private static final long serialVersionUID = 1L;

				final Object removedChildren = children;

				@Override
				public String toString()
				{
					return "RemoveAllChange[component: " + getPath() + ", removed Children: " +
						removedChildren + "]";
				}

				@Override
				public void undo()
				{
					children = removedChildren;
					int size = children_size();
					for (int i = 0; i < size; i++)
					{
						// Get next child
						final Component child = children_get(i);
						child.setParent(MarkupContainer.this);
					}
				}
			});

			// Loop through child components
			int size = children_size();
			for (int i = 0; i < size; i++)
			{
				Object childObject = children_get(i, false);
				if (childObject instanceof Component)
				{
					// Get next child
					final Component child = (Component)childObject;

					// Do not call remove() because the state change would than be
					// recorded twice.
					child.detachModel();
					child.setParent(null);
				}
			}

			children = null;
		}
	}

	/**
	 * Renders the entire associated markup stream for a container such as a Border or Panel. Any
	 * leading or trailing raw markup in the associated markup is skipped.
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
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream(true);

		// skip until the targetted tag is found
		associatedMarkupStream.skipUntil(openTagName);
		setMarkupStream(associatedMarkupStream);

		// Get open tag in associated markup of border component
		final ComponentTag associatedMarkupOpenTag = associatedMarkupStream.getTag();

		// Check for required open tag name
		if (!((associatedMarkupOpenTag != null) && associatedMarkupOpenTag.isOpen() && (associatedMarkupOpenTag instanceof WicketTag)))
		{
			associatedMarkupStream.throwMarkupException(exceptionMessage);
		}

		try
		{
			setIgnoreAttributeModifier(true);
			renderComponentTag(associatedMarkupOpenTag);
			associatedMarkupStream.next();

			String className = null;

			if (getApplication().getDebugSettings().isOutputMarkupContainerClassName())
			{
				Class<?> klass = getClass();
				while (klass.isAnonymousClass())
				{
					klass = klass.getSuperclass();
				}
				className = klass.getName();
				getResponse().write("<!-- MARKUP FOR ");
				getResponse().write(className);
				getResponse().write(" BEGIN -->");
			}

			renderComponentTagBody(associatedMarkupStream, associatedMarkupOpenTag);

			if (getApplication().getDebugSettings().isOutputMarkupContainerClassName())
			{
				getResponse().write("<!-- MARKUP FOR ");
				getResponse().write(className);
				getResponse().write(" END -->");
			}

			renderClosingComponentTag(associatedMarkupStream, associatedMarkupOpenTag, false);
			setMarkupStream(originalMarkupStream);
		}
		finally
		{
			setIgnoreAttributeModifier(false);
		}
	}

	/**
	 * Replaces a child component of this container with another
	 * 
	 * @param child
	 *            The child
	 * @throws IllegalArgumentException
	 *             Thrown if there was no child with the same id.
	 * @return This
	 */
	public final MarkupContainer replace(final Component child)
	{
		checkHierarchyChange(child);

		if (child == null)
		{
			throw new IllegalArgumentException("argument child must be not null");
		}

		if (log.isDebugEnabled())
		{
			log.debug("Replacing " + child.getId() + " in " + this);
		}

		if (child.getParent() != this)
		{
			// Add to map
			final Component replaced = put(child);

			// Look up to make sure it was already in the map
			if (replaced == null)
			{
				throw new WicketRuntimeException(
					exceptionMessage("Cannot replace a component which has not been added: id='" +
						child.getId() + "', component=" + child));
			}

			// first remove the component.
			removedComponent(replaced);

			// then add the other one.
			addedComponent(child);

			// The position of the associated markup remains the same
			child.markupIndex = replaced.markupIndex;

			// The generated markup id remains the same
			child.setMarkupIdImpl(replaced.getMarkupIdImpl());
		}

		return this;
	}

	/**
	 * @see org.apache.wicket.Component#setDefaultModel(org.apache.wicket.model.IModel)
	 */
	@Override
	public MarkupContainer setDefaultModel(final IModel<?> model)
	{
		final IModel<?> previous = getModelImpl();
		super.setDefaultModel(model);
		if (previous instanceof IComponentInheritedModel)
		{
			visitChildren(new IVisitor<Component>()
			{
				public Object component(Component component)
				{
					IModel<?> compModel = component.getDefaultModel();
					if (compModel instanceof IWrapModel)
					{
						compModel = ((IWrapModel<?>)compModel).getWrappedModel();
					}
					if (compModel == previous)
					{
						component.setDefaultModel(null);
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
	 * @see org.apache.wicket.Component#toString()
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
		buffer.append(super.toString(detailed));
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
					final Component child = children_get(i);
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
	 * Traverses all child components of the given class in this container, calling the visitor's
	 * visit method at each one.
	 * 
	 * Make sure that if you give a type S that the clazz parameter will only resolve to those
	 * types. Else a class cast exception will occur.
	 * 
	 * @param <S>
	 *            The type that goes into the Visitor.component() method.
	 * 
	 * @param clazz
	 *            The class of child to visit, or null to visit all children
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or null if the entire
	 *         traversal occurred
	 */
	public final <S extends Component> Object visitChildren(final Class<?> clazz,
		final IVisitor<S> visitor)

	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("argument visitor may not be null");
		}

		// Iterate through children of this container
		for (int i = 0; i < children_size(); i++)
		{
			// Get next child component
			final Component child = children_get(i);
			Object value = null;

			// Is the child of the correct class (or was no class specified)?
			if (clazz == null || clazz.isInstance(child))
			{
				// Call visitor
				@SuppressWarnings("unchecked")
				S s = (S)child;
				value = visitor.component(s);

				// If visitor returns a non-null value, it halts the traversal
				if ((value != IVisitor.CONTINUE_TRAVERSAL) &&
					(value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
				{
					return value;
				}
			}

			// If child is a container
			if ((child instanceof MarkupContainer) &&
				(value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
			{
				// visit the children in the container
				value = ((MarkupContainer)child).visitChildren(clazz, visitor);

				// If visitor returns a non-null value, it halts the traversal
				if ((value != IVisitor.CONTINUE_TRAVERSAL) &&
					(value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
				{
					return value;
				}
			}
		}

		return null;
	}

	/**
	 * Traverses all child components in this container, calling the visitor's visit method at each
	 * one.
	 * 
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or null if the entire
	 *         traversal occurred
	 */
	public final Object visitChildren(final IVisitor<Component> visitor)
	{
		return visitChildren(null, visitor);
	}

	/**
	 * @param component
	 *            Component being added
	 */
	private final void addedComponent(final Component component)
	{
		// Check for degenerate case
		if (component == this)
		{
			throw new IllegalArgumentException("Component can't be added to itself");
		}

		MarkupContainer parent = component.getParent();
		if (parent != null)
		{
			parent.remove(component);
		}

		// Set child's parent
		component.setParent(this);

		final Page page = findPage();

		final IDebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.isLinePreciseReportingOnAddComponentEnabled())
		{
			component.setMetaData(ADDED_AT_KEY, Strings.toString(component, new MarkupException(
				"added")));
		}

		if (page != null)
		{
			page.componentAdded(component);
		}

		// if the PREPARED_FOR_RENDER flag is set, we have already called
		// beforeRender on this
		// component's children. So we need to initialize the newly added one
		if (isPreparedForRender())
		{
			component.beforeRender();
		}
	}

	/**
	 * @param child
	 *            Child to add
	 */
	private final void children_add(final Component child)
	{
		if (children == null)
		{
			children = child;
		}
		else
		{
			if (!(children instanceof ChildList))
			{
				// Save new children
				children = new ChildList(children);
			}
			((ChildList)children).add(child);
		}
	}

	/**
	 * Returns child component at the specified index
	 * 
	 * @param index
	 * @throws ArrayIndexOutOfBoundsException
	 * @return child component at the specified index
	 */
	public final Component get(int index)
	{
		return children_get(index);
	}

	/**
	 * 
	 * @param index
	 * @return The child component
	 */
	private final Component children_get(int index)
	{
		return (Component)children_get(index, true);
	}

	/**
	 * If the given object is a {@link ComponentSourceEntry} instance and <code>reconstruct</code>
	 * is true, it reconstructs the component and returns it. Otherwise it just returns the object
	 * passed as parameter
	 * 
	 * @param object
	 * @param reconstruct
	 * @param parent
	 * @param index
	 * @return The object directly or the reconstructed component
	 */
	private final Object postprocess(Object object, boolean reconstruct, MarkupContainer parent,
		int index)
	{
		if (reconstruct && object instanceof ComponentSourceEntry)
		{
			object = ((ComponentSourceEntry)object).reconstruct(parent, index);
		}
		return object;
	}

	/**
	 * 
	 * @param index
	 * @param reconstruct
	 * @return the child component
	 */
	private final Object children_get(int index, boolean reconstruct)
	{
		Object component = null;
		if (children != null)
		{
			if (children instanceof Object[] == false && children instanceof ChildList == false)
			{
				if (index != 0)
					throw new ArrayIndexOutOfBoundsException("index " + index +
						" is greater then 0");
				component = postprocess(children, reconstruct, this, 0);
				if (children != component)
				{
					children = component;
				}
			}
			else
			{
				Object[] children = null;
				if (this.children instanceof ChildList)
				{
					// we have a list
					children = ((ChildList)this.children).childs;
				}
				else
				{
					// we have a object array
					children = (Object[])this.children;
				}
				component = postprocess(children[index], reconstruct, this, index);
				if (children[index] != component)
				{
					children[index] = component;
				}
			}
		}
		return component;
	}

	/**
	 * Returns the wicket:id of the given object, that can be either a {@link Component} or a
	 * {@link ComponentSourceEntry}
	 * 
	 * @param object
	 * @return The id of the object (object can be component or componentsourcentry)
	 */
	private final String getId(Object object)
	{
		if (object instanceof Component)
		{
			return ((Component)object).getId();
		}
		else if (object instanceof ComponentSourceEntry)
		{
			return ((ComponentSourceEntry)object).id;
		}
		else
		{
			throw new IllegalArgumentException("Unknown type of object " + object);
		}
	}

	/**
	 * 
	 * @param id
	 * @return The child component
	 */
	private final Component children_get(final String id)
	{
		if (children == null)
		{
			return null;
		}
		Component component = null;
		if ((children instanceof Object[] == false) && (children instanceof List == false))
		{
			if (getId(children).equals(id))
			{
				component = (Component)postprocess(children, true, this, 0);
				if (children != component)
				{
					children = component;
				}
			}
		}
		else
		{
			Object[] children = null;
			int size = 0;
			if (this.children instanceof ChildList)
			{
				children = ((ChildList)this.children).childs;
				size = ((ChildList)this.children).size;
			}
			else
			{
				children = (Object[])this.children;
				size = children.length;
			}
			for (int i = 0; i < size; i++)
			{
				if (getId(children[i]).equals(id))
				{
					component = (Component)postprocess(children[i], true, this, i);
					if (children[i] != component)
					{
						children[i] = component;
					}
					break;
				}
			}
		}
		return component;
	}

	/**
	 * 
	 * @param child
	 * @return The index of the given child component
	 */
	private final int children_indexOf(Component child)
	{
		if (children == null)
		{
			return -1;
		}
		if (children instanceof Object[] == false && children instanceof ChildList == false)
		{
			if (getId(children).equals(child.getId()))
			{
				return 0;
			}
		}
		else
		{
			int size = 0;
			Object[] children;
			if (this.children instanceof Object[])
			{
				children = (Object[])this.children;
				size = children.length;
			}
			else
			{
				children = ((ChildList)this.children).childs;
				size = ((ChildList)this.children).size;
			}

			for (int i = 0; i < size; i++)
			{
				if (getId(children[i]).equals(child.getId()))
				{
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param component
	 * @return The component that is removed.
	 */
	private final Component children_remove(Component component)
	{
		int index = children_indexOf(component);
		if (index != -1)
		{
			return children_remove(index);
		}
		return null;
	}

	/**
	 * 
	 * @param index
	 * @return The component that is removed
	 */
	private final Component children_remove(int index)
	{
		if (children == null)
			return null;

		if (children instanceof Component || children instanceof ComponentSourceEntry)
		{
			if (index == 0)
			{
				final Component removed = (Component)postprocess(children, true, null, -1);
				children = null;
				return removed;
			}
			else
			{
				throw new IndexOutOfBoundsException();
			}
		}
		else
		{
			if (children instanceof Object[])
			{
				Object[] c = ((Object[])children);
				final Object removed = c[index];
				if (c.length == 2)
				{
					if (index == 0)
					{
						children = c[1];
					}
					else if (index == 1)
					{
						children = c[0];
					}
					else
					{
						throw new IndexOutOfBoundsException();
					}
					return (Component)postprocess(removed, true, null, -1);
				}
				children = new ChildList(children);
			}

			ChildList lst = (ChildList)children;
			Object removed = lst.remove(index);
			if (lst.size == 1)
			{
				children = lst.get(0);
			}
			return (Component)postprocess(removed, true, null, -1);
		}
	}

	/**
	 * 
	 * @param index
	 * @param child
	 * @param reconstruct
	 * @return The replaced child
	 */
	private final Object children_set(int index, Object child, boolean reconstruct)
	{
		Object replaced;
		if (index >= 0 && index < children_size())
		{
			if (children instanceof Component || children instanceof ComponentSourceEntry)
			{
				replaced = children;
				children = child;
			}
			else
			{
				if (children instanceof ChildList)
				{
					replaced = ((ChildList)children).set(index, child);
				}
				else
				{
					final Object[] children = (Object[])this.children;
					replaced = children[index];
					children[index] = child;
				}
			}
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
		return postprocess(replaced, reconstruct, null, -1);
	}

	/**
	 * 
	 * @param index
	 * @param child
	 * @return The component that is replaced
	 */
	private final Component children_set(int index, Component child)
	{
		return (Component)children_set(index, child, true);
	}

	/**
	 * 
	 * @return The size of the children
	 */
	private final int children_size()
	{
		if (children == null)
		{
			return 0;
		}
		else
		{
			if (children instanceof Component || children instanceof ComponentSourceEntry)
			{
				return 1;
			}
			else if (children instanceof ChildList)
			{
				return ((ChildList)children).size;
			}
			return ((Object[])children).length;
		}
	}

	/**
	 * Ensure that there is space in childForId map for a new entry before adding it.
	 * 
	 * @param child
	 *            The child to put into the map
	 * @return Any component that was replaced
	 */
	private final Component put(final Component child)
	{
		int index = children_indexOf(child);
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
	private final void removedComponent(final Component component)
	{
		// Notify Page that component is being removed
		final Page page = component.findPage();
		if (page != null)
		{
			page.componentRemoved(component);
		}

		component.detach();

		// Component is removed
		component.setParent(null);
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

			// Get the component for the id from the given container
			final Component component = get(id);

			// Failed to find it?
			if (component != null)
			{
				component.render(markupStream);
			}
			else
			{
				// 2rd try: Components like Border and Panel might implement
				// the ComponentResolver interface as well.
				MarkupContainer container = this;
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
				final List<IComponentResolver> componentResolvers = getApplication().getPageSettings()
					.getComponentResolvers();
				final Iterator<IComponentResolver> iterator = componentResolvers.iterator();
				while (iterator.hasNext())
				{
					final IComponentResolver resolver = iterator.next();
					if (resolver.resolve(this, markupStream, tag))
					{
						return;
					}
				}

				if (tag instanceof WicketTag)
				{
					if (((WicketTag)tag).isChildTag())
					{
						markupStream.throwMarkupException("Found " + tag.toString() +
							" but no <wicket:extend>");
					}
					else
					{
						markupStream.throwMarkupException("Failed to handle: " + tag.toString());
					}
				}

				// No one was able to handle the component id
				markupStream.throwMarkupException("Unable to find component with id '" + id +
					"' in " + this + ". This means that you declared wicket:id=" + id +
					" in your markup, but that you either did not add the " +
					"component to your page at all, or that the hierarchy does not match.");
			}
		}
		else
		{
			// Render as raw markup
			if (log.isDebugEnabled())
			{
				log.debug("Rendering raw markup");
			}
			getResponse().write(element.toCharSequence());
			markupStream.next();
		}
	}

	/**
	 * Get the markup stream for this component.
	 * 
	 * @return The markup stream for this component, or if it doesn't have one, the markup stream
	 *         for the nearest parent which does have one
	 */
	@Override
	protected final MarkupStream findMarkupStream()
	{
		// Start here
		MarkupContainer c = this;

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
	 * Handle the container's body. If your override of this method does not advance the markup
	 * stream to the close tag for the openTag, a runtime exception will be thrown by the framework.
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
	 * Renders this component and all sub-components using the given markup stream.
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
				markupStream.throwMarkupException("Component at markup stream index " + index +
					" failed to advance the markup stream");
			}
		}
	}

	/**
	 * Renders markup for the body of a ComponentTag from the current position in the given markup
	 * stream. If the open tag passed in does not require a close tag, nothing happens. Markup is
	 * rendered until the closing tag for openTag is reached.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag
	 */
	protected final void renderComponentTagBody(final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		if ((markupStream != null) && (markupStream.getCurrentIndex() > 0))
		{
			// If the original tag has been changed from open-close to open-body-close,
			// than historically renderComponentTagBody gets called, but actually
			// it shouldn't do anything since there is no body for that tag.
			ComponentTag origOpenTag = (ComponentTag)markupStream.get(markupStream.getCurrentIndex() - 1);
			if (origOpenTag.isOpenClose())
			{
				return;
			}
		}

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
					markupStream.throwMarkupException("Markup element at index " + index +
						" failed to advance the markup stream");
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
	 * 
	 */
	private static class ComponentSourceEntry extends org.apache.wicket.ComponentSourceEntry
	{
		private ComponentSourceEntry(MarkupContainer container, Component component,
			IComponentSource componentSource)
		{
			super(container, component, componentSource);
		}

		private static final long serialVersionUID = 1L;

		@Override
		protected void setChild(MarkupContainer parent, int index, Component child)
		{
			parent.children_set(index, child, false);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#detachChildren()
	 */
	@Override
	void detachChildren()
	{
		super.detachChildren();

		for (int i = children_size(); i-- > 0;)
		{
			Object child = children_get(i, false);
			if (child instanceof Component)
			{
				Component component = (Component)child;
				component.detach();

				if (child instanceof IComponentSourceProvider)
				{
					ComponentSourceEntry entry = new ComponentSourceEntry(this, component,
						((IComponentSourceProvider)child).getComponentSource());
					children_set(i, entry, false);
				}
				else if (component.isAuto())
				{
					children_remove(i);
				}
			}
		}
		if (children instanceof ChildList)
		{
			ChildList lst = (ChildList)children;
			Object[] tmp = new Object[lst.size];
			System.arraycopy(lst.childs, 0, tmp, 0, lst.size);
			children = tmp;
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#internalMarkRendering()
	 */
	@Override
	void internalMarkRendering(boolean setRenderingFlag)
	{
		super.internalMarkRendering(setRenderingFlag);
		final int size = children_size();
		for (int i = 0; i < size; i++)
		{
			final Component child = children_get(i);
			child.internalMarkRendering(setRenderingFlag);
		}
	}

	/**
	 * @return a copy of the children array.
	 */
	private Component[] copyChildren()
	{
		int size = children_size();
		Component result[] = new Component[size];
		for (int i = 0; i < size; ++i)
		{
			result[i] = children_get(i);
		}
		return result;
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onBeforeRenderChildren()
	 */
	@Override
	void onBeforeRenderChildren()
	{
		super.onBeforeRenderChildren();

		// We need to copy the children list because the children components can
		// modify the hierarchy in their onBeforeRender.
		Component[] children = copyChildren();
		try
		{
			// Loop through child components
			for (int i = 0; i < children.length; i++)
			{
				// Get next child
				final Component child = children[i];

				// Call begin request on the child
				// We need to check whether the child's wasn't removed from the
				// component in the meanwhile (e.g. from another's child
				// onBeforeRender)
				if (child.getParent() == this)
				{
					child.beforeRender();
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
				throw new WicketRuntimeException("Error attaching this container for rendering: " +
					this, ex);
			}
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.Component#onAfterRenderChildren()
	 */
	@Override
	void onAfterRenderChildren()
	{
		// Loop through child components
		final Iterator<? extends Component> iter = iterator();
		while (iter.hasNext())
		{
			// Get next child
			final Component child = iter.next();

			// Call end request on the child
			child.afterRender();
		}
		super.onAfterRenderChildren();
	}

	/**
	 * @return True if this markup container has associated markup
	 */
	public boolean hasAssociatedMarkup()
	{
		return getApplication().getMarkupSettings().getMarkupCache().hasAssociatedMarkup(this);
	}

	/**
	 * @see org.apache.wicket.Component#setRenderAllowed()
	 */
	@Override
	void setRenderAllowed()
	{
		super.setRenderAllowed();

		visitChildren(new IVisitor<Component>()
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
	}

	/**
	 * 
	 */
	private static class ChildList extends AbstractList<Object> implements IClusterable
	{
		private static final long serialVersionUID = -7861580911447631127L;
		private int size;
		private Object[] childs;

		/**
		 * Construct.
		 * 
		 * @param children
		 */
		public ChildList(Object children)
		{
			if (children instanceof Object[])
			{
				childs = (Object[])children;
				size = childs.length;
			}
			else
			{
				childs = new Object[3];
				add(children);
			}
		}

		@Override
		public Object get(int index)
		{
			return childs[index];
		}

		@Override
		public int size()
		{
			return size;
		}

		@Override
		public boolean add(Object o)
		{
			ensureCapacity(size + 1);
			childs[size++] = o;
			return true;
		}

		@Override
		public void add(int index, Object element)
		{
			if (index > size || index < 0)
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

			ensureCapacity(size + 1);
			System.arraycopy(childs, index, childs, index + 1, size - index);
			childs[index] = element;
			size++;
		}

		@Override
		public Object set(int index, Object element)
		{
			if (index >= size)
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

			Object oldValue = childs[index];
			childs[index] = element;
			return oldValue;
		}

		@Override
		public Object remove(int index)
		{
			if (index >= size)
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

			Object oldValue = childs[index];

			int numMoved = size - index - 1;
			if (numMoved > 0)
				System.arraycopy(childs, index + 1, childs, index, numMoved);
			childs[--size] = null; // Let gc do its work

			return oldValue;
		}

		/**
		 * @param minCapacity
		 */
		public void ensureCapacity(int minCapacity)
		{
			int oldCapacity = childs.length;
			if (minCapacity > oldCapacity)
			{
				Object oldData[] = childs;
				int newCapacity = oldCapacity * 2;
				if (newCapacity < minCapacity)
					newCapacity = minCapacity;
				childs = new Object[newCapacity];
				System.arraycopy(oldData, 0, childs, 0, size);
			}
		}
	}

	/**
	 * Swaps position of children. This method is particularly useful for adjusting positions of
	 * repeater's items without rebuilding the component hierarchy
	 * 
	 * @param idx1
	 *            index of first component to be swapped
	 * @param idx2
	 *            index of second component to be swapped
	 */
	public final void swap(int idx1, int idx2)
	{
		int size = children_size();
		if (idx1 < 0 || idx1 >= size)
		{
			throw new IndexOutOfBoundsException("Argument idx is out of bounds: " + idx1 + "<>[0," +
				size + ")");
		}

		if (idx2 < 0 || idx2 >= size)
		{
			throw new IndexOutOfBoundsException("Argument idx is out of bounds: " + idx2 + "<>[0," +
				size + ")");
		}

		if (idx1 == idx2)
		{
			return;
		}

		if (children instanceof Object[])
		{
			final Object[] array = (Object[])children;
			Object tmp = array[idx1];
			array[idx1] = array[idx2];
			array[idx2] = tmp;
		}
		else
		{
			ChildList list = (ChildList)children;
			Object tmp = list.childs[idx1];
			list.childs[idx1] = list.childs[idx2];
			list.childs[idx2] = tmp;
		}

	}
}
