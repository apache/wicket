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

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.wicket.core.util.string.ComponentStrings;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.ComponentTag.IAutoComponentFactory;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupFactory;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.AutoLabelResolver;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.apache.wicket.model.IComponentInheritedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.settings.DebugSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.visit.ClassVisitFilter;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MarkupContainer holds a map of child components.
 * <ul>
 * <li><b>Children </b>- Children can be added by calling the {@link #add(Component...)} method, and
 * they can be looked up using a colon separated path. For example, if a container called "a" held a
 * nested container "b" which held a nested component "c", then a.get("b:c") would return the
 * Component with id "c". The number of children in a MarkupContainer can be determined by calling
 * size(), and the whole hierarchy of children held by a MarkupContainer can be traversed by calling
 * visitChildren(), passing in an implementation of IVisitor.
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
 * {@link #getMarkupType()}. Markup is accessed via a MarkupStream object which allows a component
 * to traverse ComponentTag and RawMarkup MarkupElements while rendering a response. Markup in the
 * stream may be HTML or some other kind of markup, such as VXML, as determined by the specific
 * container subclass.
 * <p>
 * A markup stream may be directly associated with a container via setMarkupStream. However, a
 * container which does not have a markup stream (its getMarkupStream() returns null) may inherit a
 * markup stream from a container above it in the component hierarchy. The
 * {@link #findMarkupStream()} method will locate the first container at or above this container
 * which has a markup stream.
 * <p>
 * All Page containers set a markup stream before rendering by calling the method
 * {@link #getAssociatedMarkupStream(boolean)} to load the markup associated with the page. Since
 * Page is at the top of the container hierarchy, it is guaranteed that {@link #findMarkupStream()}
 * will always return a valid markup stream.
 * 
 * @see MarkupStream
 * @author Jonathan Locke
 */
public abstract class MarkupContainer extends Component implements Iterable<Component>
{
	private static final long serialVersionUID = 1L;

	private static final int INITIAL_CHILD_LIST_CAPACITY = 12;

	/**
	 * The threshold where we start using a Map to store children in, replacing a List. Adding
	 * components to a list is O(n), and to a map O(1). The magic number is 24, due to a Map using
	 * more memory to store its elements and below 24 children there's no discernible difference
	 * between adding to a Map or a List.
	 * 
	 * We have focused on adding elements to a list, instead of indexed lookups because adding is an
	 * action that is performed very often, and lookups often are done by component IDs, not index.
	 */
	static final int MAPIFY_THRESHOLD = 24; // 32 * 0.75

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupContainer.class);

	/**
	 * Metadata key for looking up the list of removed children necessary for tracking modifications
	 * during iteration of the children of this markup container.
	 * 
	 * This is stored in meta data because it only is necessary when a child is removed, and this
	 * saves the memory necessary for a field on a widely used class.
	 */
	private static final MetaDataKey<LinkedList<RemovedChild>> REMOVALS_KEY = new MetaDataKey<LinkedList<RemovedChild>>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Administrative class for detecting removed children during child iteration. Not intended to
	 * be serializable but for e.g. determining the size of the component it has to be serializable.
	 */
	private static class RemovedChild implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private transient final Component removedChild;
		private transient final Component previousSibling;

		private RemovedChild(Component removedChild, Component previousSibling)
		{
			this.removedChild = removedChild;
			this.previousSibling = previousSibling;
		}
	}

	/**
	 * Administrative counter to keep track of modifications to the list of children during
	 * iteration.
	 * 
	 * When the {@link #children_size()} changes due to an addition or removal of a child component,
	 * the modCounter is increased. This way iterators that iterate over the children of this
	 * container can keep track when they need to change their iteration strategy.
	 */
	private transient int modCounter = 0;

	/**
	 * The children of this markup container, if any. Can be a Component when there's only one
	 * child, a List when the number of children is fewer than {@link #MAPIFY_THRESHOLD} or a Map
	 * when there are more children.
	 */
	private Object children;

	public MarkupContainer(final String id)
	{
		this(id, null);
	}

	public MarkupContainer(final String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Adds the child component(s) to this container.
	 * 
	 * @param children
	 *            The child(ren) to add.
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add operation.
	 * @return This
	 */
	public MarkupContainer add(final Component... children)
	{
		for (Component child : children)
		{
			Args.notNull(child, "child");

			if (this == child)
			{
				throw new IllegalArgumentException(
					exceptionMessage("Trying to add this component to itself."));
			}

			MarkupContainer parent = getParent();
			while (parent != null)
			{
				if (child == parent)
				{
					String msg = "You can not add a component's parent as child to the component (loop): Component: " +
						this.toString(false) + "; parent == child: " + parent.toString(false);

					if (child instanceof Border.BorderBodyContainer)
					{
						msg += ". Please consider using Border.addToBorder(new " +
							Classes.simpleName(this.getClass()) + "(\"" + this.getId() +
							"\", ...) instead of add(...)";
					}

					throw new WicketRuntimeException(msg);
				}

				parent = parent.getParent();
			}

			checkHierarchyChange(child);

			if (log.isDebugEnabled())
			{
				log.debug("Add " + child.getId() + " to " + this);
			}

			// Add the child to my children
			Component previousChild = children_put(child);
			if (previousChild != null && previousChild != child)
			{
				throw new IllegalArgumentException(
					exceptionMessage("A child '" + previousChild.getClass().getSimpleName() +
						"' with id '" + child.getId() + "' already exists"));
			}

			addedComponent(child);

		}
		return this;
	}

	/**
	 * Replaces a child component of this container with another or just adds it in case no child
	 * with the same id existed yet.
	 * 
	 * @param children
	 *            The child(ren) to be added or replaced
	 * @return this markup container
	 */
	public MarkupContainer addOrReplace(final Component... children)
	{
		for (Component child : children)
		{
			Args.notNull(child, "child");

			checkHierarchyChange(child);

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
	 * This method allows a component to be added by an auto-resolver such as AutoLinkResolver.
	 * While the component is being added, the component's FLAG_AUTO boolean is set. The isAuto()
	 * method of Component returns true if a component or any of its parents has this bit set. When
	 * a component is added via autoAdd(), the logic in Page that normally (a) checks for
	 * modifications during the rendering process, and (b) versions components, is bypassed if
	 * Component.isAuto() returns true.
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
	public final boolean autoAdd(final Component component, MarkupStream markupStream)
	{
		Args.notNull(component, "component");

		// Replace strategy
		component.setAuto(true);

		if (markupStream != null)
		{
			component.setMarkup(markupStream.getMarkupFragment());
		}

		// Add the child to the parent.

		// Arguably child.setParent() can be used as well. It connects the child to the parent and
		// that's all what most auto-components need. Unfortunately child.onDetach() will not / can
		// not be invoked, since the parent doesn't known its one of his children. Hence we need to
		// properly add it.
		children_remove(component.getId());
		add(component);

		return true;
	}

	/**
	 * @param component
	 *            The component to check
	 * @param recurse
	 *            True if all descendents should be considered
	 * @return True if the component is contained in this container
	 */
	public boolean contains(final Component component, final boolean recurse)
	{
		Args.notNull(component, "component");

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
	 * <p>
	 * A component path consists of component ids separated by colons, e.g. "b:c" identifies a
	 * component "c" inside container "b" inside this container.
	 * 
	 * @param path
	 *            path to component
	 * @return The component at the path
	 */
	@Override
	public final Component get(String path)
	{
		// Reference to this container
		if (Strings.isEmpty(path))
		{
			return this;
		}

		// process parent .. references

		MarkupContainer container = this;

		String id = Strings.firstPathComponent(path, Component.PATH_SEPARATOR);

		while (Component.PARENT_PATH.equals(id))
		{
			container = container.getParent();
			if (container == null)
			{
				return null;
			}
			path = path.length() == id.length() ? "" : path.substring(id.length() + 1);
			id = Strings.firstPathComponent(path, Component.PATH_SEPARATOR);
		}

		if (Strings.isEmpty(id))
		{
			return container;
		}

		// Get child by id
		Component child = container.children_get(id);

		// Found child?
		if (child != null)
		{
			String path2 = Strings.afterFirstPathComponent(path, Component.PATH_SEPARATOR);

			// Recurse on latter part of path
			return child.get(path2);
		}

		return null;
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
		IMarkupFragment markup = getAssociatedMarkup();

		// If we found markup for this container
		if (markup != null)
		{
			return new MarkupStream(markup);
		}

		if (throwException == true)
		{
			// throw exception since there is no associated markup
			throw new MarkupNotFoundException(
				"Markup of type '" +
					getMarkupType().getExtension() +
					"' for component '" +
					getClass().getName() +
					"' not found." +
					" Enable debug messages for org.apache.wicket.util.resource to get a list of all filenames tried.: " +
					toString());
		}

		return null;
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource for this class.
	 * 
	 * @return A stream of MarkupElement elements. Null if not found.
	 */
	public Markup getAssociatedMarkup()
	{
		try
		{
			Markup markup = MarkupFactory.get().getMarkup(this, false);

			// If we found markup for this container
			if ((markup != null) && (markup != Markup.NO_MARKUP))
			{
				return markup;
			}

			return null;
		}
		catch (MarkupException ex)
		{
			// re-throw it. The exception contains already all the information
			// required.
			throw ex;
		}
		catch (MarkupNotFoundException ex)
		{
			// re-throw it. The exception contains already all the information
			// required.
			throw ex;
		}
		catch (WicketRuntimeException ex)
		{
			// throw exception since there is no associated markup
			throw new MarkupNotFoundException(
				exceptionMessage("Markup of type '" + getMarkupType().getExtension() +
					"' for component '" + getClass().getName() + "' not found." +
					" Enable debug messages for org.apache.wicket.util.resource to get a list of all filenames tried"),
				ex);
		}
	}

	/**
	 * Get the markup of the child.
	 * 
	 * @see Component#getMarkup()
	 * 
	 * @param child
	 *            The child component. If null, the container's markup will be returned. See Border,
	 *            Panel or Enclosure where getMarkup(null) != getMarkup().
	 * @return The child's markup
	 */
	public IMarkupFragment getMarkup(final Component child)
	{
		// Delegate request to attached markup sourcing strategy.
		return getMarkupSourcingStrategy().getMarkup(this, child);
	}

	/**
	 * Get the type of associated markup for this component. The markup type for a component is
	 * independent of whether or not the component actually has an associated markup resource file
	 * (which is determined at runtime).
	 * 
	 * @return The type of associated markup for this component (for example, "html", "wml" or
	 *         "vxml"). If there is no markup type for a component, null may be returned, but this
	 *         means that no markup can be loaded for the class. Null is also returned if the
	 *         component, or any of its parents, has not been added to a Page.
	 */
	public MarkupType getMarkupType()
	{
		MarkupContainer parent = getParent();
		if (parent != null)
		{
			return parent.getMarkupType();
		}
		return null;
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
		children_put(child);
		addedComponent(child);
	}

	/**
	 * Gives an iterator that allow you to iterate through the children of this markup container in
	 * the order the children were added. The iterator supports additions and removals from the list
	 * of children during iteration.
	 * 
	 * @return Iterator that iterates through children in the order they were added
	 */
	@Override
	public Iterator<Component> iterator()
	{
		/**
		 * Iterator that knows how to change between a single child, list of children and map of
		 * children. Keeps track when the iterator was last sync'd with the markup container's
		 * tracking of changes to the list of children.
		 */
		class MarkupChildIterator implements Iterator<Component>
		{
			private int indexInRemovalsSinceLastUpdate = removals_size();
			private int expectedModCounter = -1;
			private Component currentComponent = null;
			private Iterator<Component> internalIterator = null;

			@Override
			public boolean hasNext()
			{
				refreshInternalIteratorIfNeeded();
				return internalIterator.hasNext();
			}

			@Override
			public Component next()
			{
				refreshInternalIteratorIfNeeded();
				return currentComponent = internalIterator.next();
			}

			@Override
			public void remove()
			{
				MarkupContainer.this.remove(currentComponent);
				refreshInternalIteratorIfNeeded();
			}

			private void refreshInternalIteratorIfNeeded()
			{
				if (modCounter != 0 && expectedModCounter >= modCounter)
					return;

				if (children == null)
				{
					internalIterator = Collections.emptyIterator();
				}
				else if (children instanceof Component)
				{
					internalIterator = Collections.singleton((Component)children).iterator();
				}
				else if (children instanceof List)
				{
					List<Component> childrenList = children();
					internalIterator = childrenList.iterator();
				}
				else
				{
					Map<String, Component> childrenMap = children();
					internalIterator = childrenMap.values().iterator();
				}

				// since we now have a new iterator, we need to set it to the last known position
				currentComponent = findLastExistingChildAlreadyReturned(currentComponent);
				expectedModCounter = modCounter;
				indexInRemovalsSinceLastUpdate = removals_size();

				if (currentComponent != null)
				{
					// move the new internal iterator to the place of the last processed component
					while (internalIterator.hasNext() &&
						internalIterator.next() != currentComponent)
						// noop
						;
				}
			}

			private Component findLastExistingChildAlreadyReturned(Component target)
			{
				while (true)
				{
					if (target == null)
						return null;

					RemovedChild removedChild = null;
					for (int i = indexInRemovalsSinceLastUpdate; i < removals_size(); i++)
					{
						RemovedChild curRemovedChild = removals_get(i);
						if (curRemovedChild.removedChild == target ||
							curRemovedChild.removedChild == null)
						{
							removedChild = curRemovedChild;
							break;
						}
					}
					if (removedChild == null)
					{
						return target;
					}
					else
					{
						target = removedChild.previousSibling;
					}
				}
			}
		};
		return new MarkupChildIterator();
	}

	/**
	 * Creates an iterator that iterates over children in the order specified by comparator. This
	 * works on a copy of the children list.
	 * 
	 * @param comparator
	 *            The comparator
	 * @return Iterator that iterates over children in the order specified by comparator
	 */
	public final Iterator<Component> iterator(Comparator<Component> comparator)
	{
		final List<Component> sorted = copyChildren();
		Collections.sort(sorted, comparator);
		return sorted.iterator();
	}

	/**
	 * Removes a component from the children identified by the {@code component.getId()}
	 * 
	 * @param component
	 *            Component to remove from this container
	 * @return {@code this} for chaining
	 */
	public MarkupContainer remove(final Component component)
	{
		checkHierarchyChange(component);

		Args.notNull(component, "component");

		children_remove(component.getId());
		removedComponent(component);

		return this;
	}

	/**
	 * Removes the given component
	 * 
	 * @param id
	 *            The id of the component to remove
	 * @return {@code this} for chaining
	 */
	public MarkupContainer remove(final String id)
	{
		Args.notNull(id, "id");

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

		return this;
	}

	/**
	 * Removes all children from this container.
	 * <p>
	 * Note: implementation does not call {@link MarkupContainer#remove(Component) } for each
	 * component.
	 * 
	 * @return {@code this} for method chaining
	 */
	public MarkupContainer removeAll()
	{
		if (children != null)
		{
			addStateChange();

			for (Component child : this)
			{
				// Do not call remove() because the state change would then be
				// recorded twice.
				child.internalOnRemove();
				child.detach();
				child.setParent(null);
			}

			children = null;
			removals_add(null, null);
		}

		return this;
	}

	/**
	 * Renders the entire associated markup for a container such as a Border or Panel. Any leading
	 * or trailing raw markup in the associated markup is skipped.
	 * 
	 * @param openTagName
	 *            the tag to render the associated markup for
	 * @param exceptionMessage
	 *            message that will be used for exceptions
	 */
	public final void renderAssociatedMarkup(final String openTagName, final String exceptionMessage)
	{
		// Get associated markup file for the Border or Panel component
		final MarkupStream associatedMarkupStream = new MarkupStream(getMarkup(null));

		// Get open tag in associated markup of border component
		MarkupElement elem = associatedMarkupStream.get();
		if ((elem instanceof ComponentTag) == false)
		{
			associatedMarkupStream.throwMarkupException("Expected the open tag. " +
				exceptionMessage);
		}

		// Check for required open tag name
		ComponentTag associatedMarkupOpenTag = (ComponentTag)elem;
		if (!(associatedMarkupOpenTag.isOpen() && (associatedMarkupOpenTag instanceof WicketTag)))
		{
			associatedMarkupStream.throwMarkupException(exceptionMessage);
		}

		try
		{
			setIgnoreAttributeModifier(true);
			renderComponentTag(associatedMarkupOpenTag);
			associatedMarkupStream.next();

			String className = null;

			final boolean outputClassName = getApplication().getDebugSettings()
				.isOutputMarkupContainerClassName();
			if (outputClassName)
			{
				className = Classes.name(getClass());
				getResponse().write("<!-- MARKUP FOR ");
				getResponse().write(className);
				getResponse().write(" BEGIN -->");
			}

			renderComponentTagBody(associatedMarkupStream, associatedMarkupOpenTag);

			if (outputClassName)
			{
				getResponse().write("<!-- MARKUP FOR ");
				getResponse().write(className);
				getResponse().write(" END -->");
			}

			renderClosingComponentTag(associatedMarkupStream, associatedMarkupOpenTag, false);
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
	public MarkupContainer replace(final Component child)
	{
		Args.notNull(child, "child");

		checkHierarchyChange(child);

		if (log.isDebugEnabled())
		{
			log.debug("Replacing " + child.getId() + " in " + this);
		}

		if (child.getParent() != this)
		{
			final Component replaced = children_put(child);

			// Look up to make sure it was already in the map
			if (replaced == null)
			{
				throw new WicketRuntimeException(
					exceptionMessage("Cannot replace a component which has not been added: id='" +
						child.getId() + "', component=" + child));
			}

			// first remove the component.
			removedComponent(replaced);

			// The generated markup id remains the same
			child.setMarkupId(replaced);

			// then add the other one.
			addedComponent(child);
		}

		return this;
	}

	@Override
	public MarkupContainer setDefaultModel(final IModel<?> model)
	{
		final IModel<?> previous = getModelImpl();
		super.setDefaultModel(model);
		if (previous instanceof IComponentInheritedModel)
		{
			visitChildren(new IVisitor<Component, Void>()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
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
	public int size()
	{
		return children_size();
	}

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
		final StringBuilder buffer = new StringBuilder();
		buffer.append('[').append(Classes.simpleName(this.getClass())).append(' ');
		buffer.append(super.toString(detailed));
		if (detailed && children_size() != 0)
		{

			buffer.append(", children = ");

			// Loop through child components
			boolean first = true;
			for (Component child : this)
			{
				if (first)
				{
					buffer.append(' ');
					first = false;
				}
				buffer.append(child.toString());
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
	 * @param <R>
	 * @param clazz
	 *            The class of child to visit
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or null if the entire
	 *         traversal occurred
	 */
	public final <S extends Component, R> R visitChildren(final Class<?> clazz,
		final IVisitor<S, R> visitor)
	{
		return Visits.visitChildren(this, visitor, new ClassVisitFilter(clazz));
	}

	/**
	 * Traverses all child components in this container, calling the visitor's visit method at each
	 * one.
	 * 
	 * @param <R>
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or null if the entire
	 *         traversal occurred
	 */
	public final <R> R visitChildren(final IVisitor<Component, R> visitor)
	{
		return Visits.visitChildren(this, visitor);
	}

	/**
	 * @param child
	 *            Component being added
	 */
	private void addedComponent(final Component child)
	{
		// Check for degenerate case
		Args.notNull(child, "child");

		MarkupContainer parent = child.getParent();
		if (parent != null && parent != this)
		{
			parent.remove(child);
		}

		// Set child's parent
		child.setParent(this);

		final DebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.isLinePreciseReportingOnAddComponentEnabled()
			&& debugSettings.getComponentUseCheck())
		{
			child.setMetaData(ADDED_AT_KEY,
				ComponentStrings.toString(child, new MarkupException("added")));
		}

		Page page = findPage();
		
		// if we have a path to page, dequeue any container children.
		if (page != null && child instanceof MarkupContainer)
		{
		    MarkupContainer childContainer = (MarkupContainer)child;
		    // if we are already dequeueing there is no need to dequeue again
		    if (!childContainer.getRequestFlag(RFLAG_CONTAINER_DEQUEING))
			{				
				childContainer.dequeue();
			}
		}

		if (page != null)
		{
			// tell the page a component has been added first, to allow it to initialize
			page.componentAdded(child);

			// initialize the component
			if (page.isInitialized())
			{
				child.internalInitialize();
			}
		}

		// if the PREPARED_FOR_RENDER flag is set, we have already called
		// beforeRender on this component's children. So we need to initialize the newly added one
		if (isPreparedForRender())
		{
			child.beforeRender();
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE PUBLIC API, DO NOT CALL IT
	 * 
	 * Overrides {@link Component#internalInitialize()} to call {@link Component#fireInitialize()}
	 * for itself and for all its children.
	 * 
	 * @see org.apache.wicket.Component#fireInitialize()
	 */
	@Override
	public final void internalInitialize()
	{
		super.fireInitialize();
		visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(final Component component, final IVisit<Void> visit)
			{
				component.fireInitialize();
			}
		});
	}

	/*
	 * === Internal management for keeping track of child components ===
	 * 
	 * A markup container is the base component for containing child objects. It is one of the most
	 * heavily used components so we should keep it's (memory and CPU) footprint small.
	 * 
	 * The goals for the internal management of the list of child components are:
	 * 
	 * - as low big-O complexity as possible, preferrably O(1)
	 * 
	 * - as low memory consumption as possible (don't use more memory than strictly necessary)
	 * 
	 * - ensure that iterating through the (list of) children be as consistent as possible
	 * 
	 * - retain the order of addition in the iteration
	 * 
	 * These goals are attained by storing the children in a single field that is implemented using:
	 * 
	 * - a component when there's only one child
	 * 
	 * - a list of components when there are more than 1 children
	 * 
	 * - a map of components when the number of children makes looking up children by id more costly
	 * than an indexed search (see MAPIFY_THRESHOLD)
	 * 
	 * To ensure that iterating through the list of children keeps working even when children are
	 * added, replaced and removed without throwing a ConcurrentModificationException a special
	 * iterator is used. The markup container tracks removals from and additions to the children
	 * during the request, enabling the iterator to skip over those items and adjust to changing
	 * internal data structures.
	 */

	/**
	 * A type washing accessor method for getting the children without having to cast the field
	 * explicitly.
	 * 
	 * @return the children as a T
	 */
	@SuppressWarnings("unchecked")
	private <T> T children()
	{
		return (T)children;
	}

	/**
	 * Gets the child with the given {@code childId}
	 * 
	 * @param childId
	 *            the component identifier
	 * @return The child component or {@code null} when no child with the given identifier exists
	 */
	private Component children_get(final String childId)
	{
		if (children == null)
		{
			return null;
		}
		if (children instanceof Component)
		{
			Component child = children();
			return child.getId().equals(childId) ? child : null;
		}
		if (children instanceof List)
		{
			List<Component> kids = children();
			for (Component child : kids)
			{
				if (child.getId().equals(childId))
				{
					return child;
				}
			}
			return null;
		}
		Map<String, Component> kids = children();
		return kids.get(childId);
	}

	/**
	 * Removes the child component identified by {@code childId} from the list of children.
	 * 
	 * Will change the internal list or map to a single component when the number of children hits
	 * 1, but not change the internal map to a list when the threshold is reached (the memory was
	 * already claimed, so there's little to be gained other than wasting CPU cycles for the
	 * conversion).
	 * 
	 * @param childId
	 *            the id of the child component to remove
	 */
	private void children_remove(String childId)
	{
		if (children instanceof Component)
		{
			Component oldChild = children();
			if (oldChild.getId().equals(childId))
			{
				children = null;
				removals_add(oldChild, null);
			}
		}
		else if (children instanceof List)
		{
			List<Component> childrenList = children();
			Iterator<Component> it = childrenList.iterator();
			Component prevChild = null;
			while (it.hasNext())
			{
				Component child = it.next();
				if (child.getId().equals(childId))
				{
					it.remove();
					removals_add(child, prevChild);
					if (childrenList.size() == 1)
					{
						children = childrenList.get(0);
					}
					return;
				}
				prevChild = child;
			}
		}
		else if (children instanceof LinkedMap)
		{
			LinkedMap<String, Component> childrenMap = children();
			if (childrenMap.containsKey(childId))
			{
				String prevSiblingId = childrenMap.previousKey(childId);
				Component oldChild = childrenMap.remove(childId);
				removals_add(oldChild, childrenMap.get(prevSiblingId));
				if (childrenMap.size() == 1)
				{
					children = childrenMap.values().iterator().next();
				}
			}
		}
	}

	/**
	 * Gets the number of child components of this markup container.
	 * 
	 * @return The number of children
	 */
	private int children_size()
	{
		if (children == null)
		{
			return 0;
		}
		if (children instanceof Component)
		{
			return 1;
		}
		if (children instanceof List)
		{
			List<?> kids = children();
			return kids.size();
		}
		return ((Map<?, ?>)children).size();
	}

	/**
	 * Puts the {@code child} component into the list of children of this markup container. If a
	 * component existed with the same {@code child.getId()} it is replaced and the old component is
	 * returned.
	 * 
	 * When a component is replaced, the internal structure of the children is not modified, so we
	 * don't have to update the internal {@link #modCounter} in those circumstances. When a
	 * component is added, we do have to increase the {@link #modCounter} to notify iterators of
	 * this change.
	 * 
	 * @param child
	 *            The child
	 * @return Any component that was replaced
	 */
	private Component children_put(final Component child)
	{
		if (children == null)
		{
			children = child;

			// it is an addtion, so we need to notify the iterators of this change.
			modCounter++;

			return null;
		}

		if (children instanceof Component)
		{
			/* first see if the child replaces the existing child */
			Component oldChild = children();
			if (oldChild.getId().equals(child.getId()))
			{
				children = child;
				return oldChild;
			}
			else
			{
				/*
				 * the put doesn't replace the existing child, so we need to increase the children
				 * storage to a list and add the existing and new child to it
				 */
				Component originalChild = children();
				List<Component> newChildren = new ArrayList<>(INITIAL_CHILD_LIST_CAPACITY);
				newChildren.add(originalChild);
				newChildren.add(child);
				children = newChildren;

				// it is an addtion, so we need to notify the iterators of this change.
				modCounter++;
				return null;
			}
		}

		if (children instanceof List)
		{
			List<Component> childrenList = children();

			// first see if the child replaces an existing child
			for (int i = 0; i < childrenList.size(); i++)
			{
				Component curChild = childrenList.get(i);
				if (curChild.getId().equals(child.getId()))
				{
					return childrenList.set(i, child);
				}
			}

			// it is an addtion, so we need to notify the iterators of this change.
			modCounter++;

			/*
			 * If it still fits in the allotted number of items of a List, just add it, otherwise
			 * change the internal data structure to a Map for speedier lookups.
			 */
			if (childrenList.size() < MAPIFY_THRESHOLD)
			{
				childrenList.add(child);
			}
			else
			{
				Map<String, Component> newChildren = new LinkedMap<>(MAPIFY_THRESHOLD * 2);
				for (Component curChild : childrenList)
				{
					newChildren.put(curChild.getId(), curChild);
				}
				newChildren.put(child.getId(), child);
				children = newChildren;
			}
			return null;
		}

		Map<String, Component> childrenMap = children();
		Component oldChild = childrenMap.put(child.getId(), child);

		if (oldChild == null)
		{
			// it is an addtion, so we need to notify the iterators of this change.
			modCounter++;
		}
		return oldChild;
	}

	/**
	 * Retrieves the during the request removed children. These are stored in the metadata and
	 * cleared at the end of the request {@link #onDetach()}
	 * 
	 * @return the list of removed children, may be {@code null}
	 */
	private LinkedList<RemovedChild> removals_get()
	{
		return getMetaData(REMOVALS_KEY);
	}

	/**
	 * Sets the during the request removed children. These are stored in the metadata and cleared at
	 * the end of the request, see {@link #onDetach()}.
	 * 
	 * @param removals
	 *            the new list of removals
	 */
	private void removals_set(LinkedList<RemovedChild> removals)
	{
		setMetaData(REMOVALS_KEY, removals);
	}

	/**
	 * Removes the list of removals from the metadata.
	 */
	private void removals_clear()
	{
		setMetaData(REMOVALS_KEY, null);
	}

	/**
	 * Adds the {@code removedChild} to the list of removals and links it to the
	 * {@code previousSibling}
	 * 
	 * @param removedChild
	 *            the child that was removed
	 * @param prevSibling
	 *            the child that was the previous sibling of the removed child
	 */
	private void removals_add(Component removedChild, Component prevSibling)
	{
		modCounter++;

		LinkedList<RemovedChild> removals = removals_get();
		if (removals == null)
		{
			removals = new LinkedList<>();
			removals_set(removals);
		}
		removals.add(new RemovedChild(removedChild, prevSibling));
	}

	/**
	 * Gets the {@link RemovedChild} from the list of removals at given position.
	 * 
	 * @param i
	 *            the position
	 * @return the removed child
	 */
	private RemovedChild removals_get(int i)
	{
		return getMetaData(REMOVALS_KEY).get(i);
	}

	/**
	 * Gets the number of removals that happened during the request.
	 * 
	 * @return the number of removals
	 */
	private int removals_size()
	{
		LinkedList<RemovedChild> removals = removals_get();
		return removals == null ? 0 : removals.size();
	}

	/**
	 * @param component
	 *            Component being removed
	 */
	private void removedComponent(final Component component)
	{
		// Notify Page that component is being removed
		final Page page = component.findPage();
		if (page != null)
		{
			page.componentRemoved(component);
		}

		component.detach();

		component.internalOnRemove();

		// Component is removed
		component.setParent(null);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE OR OVERWRITE IT.
	 * 
	 * Renders the next element of markup in the given markup stream.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @return true, if element was rendered as RawMarkup
	 */
	protected boolean renderNext(final MarkupStream markupStream)
	{
		// Get the current markup element
		final MarkupElement element = markupStream.get();

		// If it's a tag like <wicket..> or <span wicket:id="..." >
		if ((element instanceof ComponentTag) && !markupStream.atCloseTag())
		{
			// Get element as tag
			final ComponentTag tag = (ComponentTag)element;

			if (tag instanceof WicketTag && ((WicketTag)tag).isFragmentTag()){
				return false;
			}

			// Get component id
			final String id = tag.getId();

			// Get the component for the id from the given container
			Component component = get(id);

			if (component == null)
			{
				component = ComponentResolvers.resolve(this, markupStream, tag, null);
				if ((component != null) && (component.getParent() == null))
				{
					autoAdd(component, markupStream);
				}
				else if (component != null)
				{
					component.setMarkup(markupStream.getMarkupFragment());
				}
			}
		
			// Failed to find it?
			if (component != null)
			{
				component.render();
			}
			else if (tag.getFlag(ComponentTag.RENDER_RAW))
			{
				// No component found, but "render as raw markup" flag found
				if (canRenderRawTag(tag))
				{					
					getResponse().write(element.toCharSequence());
				} 
				return true;
			}
			else
			{
				throwException(markupStream, tag);
			}
		}
		else
		{
			// Render as raw markup
			if (canRenderRawTag(element))
			{
				getResponse().write(element.toCharSequence());
			}
			return true;
		}

		return false;
	}
	
	/**
	 * Says if the given tag can be handled as a raw markup.
	 * 
	 * @param tag
	 * 			the current tag.
	 * @return true if the tag can be handled as raw markup, false otherwise.
	 */
	private boolean canRenderRawTag(MarkupElement tag)
	{
		boolean isWicketTag = tag instanceof WicketTag;
		
		boolean stripTag = isWicketTag ? Application.get().getMarkupSettings().getStripWicketTags() : false; 
		
		return !stripTag;
	}
	
	/**
	 * Throws a {@code org.apache.wicket.markup.MarkupException} when the
	 * component markup is not consistent.
	 * 
	 * @param markupStream
	 * 			the source stream for the component markup.
	 * @param tag
	 * 			the tag that can not be handled.
	 */
	private void throwException(final MarkupStream markupStream, final ComponentTag tag)
	{
		final String id = tag.getId();
		
		if (tag instanceof WicketTag)
		{
			if (((WicketTag)tag).isChildTag())
			{
				markupStream.throwMarkupException("Found " + tag.toString() +
					" but no <wicket:extend>. Container: " + toString());
			}
			else
			{
				markupStream.throwMarkupException("Failed to handle: " +
					tag.toString() +
					". It might be that no resolver has been registered to handle this special tag. " +
					" But it also could be that you declared wicket:id=" + id +
					" in your markup, but that you either did not add the " +
					"component to your page at all, or that the hierarchy does not match. " +
					"Container: " + toString());
			}
		}

		List<String> names = findSimilarComponents(id);

		// No one was able to handle the component id
		StringBuilder msg = new StringBuilder(500);
		msg.append("Unable to find component with id '");
		msg.append(id);
		msg.append("' in ");
		msg.append(this.toString());
		msg.append("\n\tExpected: '");
		msg.append(getPageRelativePath());
		msg.append(PATH_SEPARATOR);
		msg.append(id);
		msg.append("'.\n\tFound with similar names: '");
		msg.append(Strings.join("', ", names));
		msg.append('\'');

		log.error(msg.toString());
		markupStream.throwMarkupException(msg.toString());
	}

	private List<String> findSimilarComponents(final String id)
	{
		final List<String> names = Generics.newArrayList();

		Page page = findPage();
		if (page != null)
		{
			page.visitChildren(new IVisitor<Component, Void>()
			{
				@Override
				public void component(Component component, IVisit<Void> visit)
				{
					if (Strings.getLevenshteinDistance(id.toLowerCase(), component.getId()
						.toLowerCase()) < 3)
					{
						names.add(component.getPageRelativePath());
					}
				}
			});
		}

		return names;
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
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		renderComponentTagBody(markupStream, openTag);
	}

	@Override
	protected void onRender()
	{
		internalRenderComponent();
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
	private void renderComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		if ((markupStream != null) && (markupStream.getCurrentIndex() > 0))
		{
			// If the original tag has been changed from open-close to open-body-close, than we are
			// done. Other components, e.g. BorderBody, rely on this method being called.
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

		if (render)
		{
			renderAll(markupStream, openTag);
		}
	}

	/**
	 * Loop through the markup in this container
	 * 
	 * @param markupStream
	 * @param openTag
	 */
	protected final void renderAll(final MarkupStream markupStream, final ComponentTag openTag)
	{
		while (markupStream.isCurrentIndexInsideTheStream())
		{
			// In case of Page we need to render the whole file. For all other components just what
			// is in between the open and the close tag.
			if ((openTag != null) && markupStream.get().closes(openTag))
			{
				break;
			}

			// Remember where we are
			final int index = markupStream.getCurrentIndex();

			// Render the markup element
			boolean rawMarkup = renderNext(markupStream);

			// Go back to where we were and move the markup stream forward to whatever the next
			// element is.
			markupStream.setCurrentIndex(index);

			if (rawMarkup)
			{
				markupStream.next();
			}
			else if (!markupStream.getTag().isClose())
			{
				markupStream.skipComponent();
			}
			else
			{
				throw new WicketRuntimeException("Ups. This should never happen. " +
					markupStream.toString());
			}
		}
	}

	@Override
	void removeChildren()
	{
		super.removeChildren();

		for (Component component : this)
		{
			component.internalOnRemove();
		}
	}

	@Override
	void detachChildren()
	{
		super.detachChildren();

		for (Component component : this)
		{
			component.detach();
		}
	}

	@Override
	void internalMarkRendering(boolean setRenderingFlag)
	{
		super.internalMarkRendering(setRenderingFlag);

		for (Component child : this)
		{
			child.internalMarkRendering(setRenderingFlag);
		}
	}

	/**
	 * @return a copy of the children array.
	 */
	@SuppressWarnings("unchecked")
	private List<Component> copyChildren()
	{
		if (children == null)
		{
			return Collections.emptyList();
		}
		else if (children instanceof Component)
		{
			return Collections.singletonList((Component)children);
		}
		else if (children instanceof List)
		{
			return new ArrayList<>((List<Component>)children);
		}
		else
		{
			return new ArrayList<>(((Map<String, Component>)children).values());
		}
	}

	@Override
	void onBeforeRenderChildren()
	{
		super.onBeforeRenderChildren();

		try
		{
			// Loop through child components
			for (final Component child : this)
			{
				// Get next child
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

	@Override
	void onEnabledStateChanged()
	{
		super.onEnabledStateChanged();
		visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component component, IVisit<Void> visit)
			{
				component.clearEnabledInHierarchyCache();
			}
		});
	}

	@Override
	void onVisibleStateChanged()
	{
		super.onVisibleStateChanged();
		visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component component, IVisit<Void> visit)
			{
				component.clearVisibleInHierarchyCache();
			}
		});
	}

	@Override
	protected void onAfterRenderChildren()
	{
		for (Component child : this)
		{
			// set RENDERING_FLAG to false for auto-component's children (like Enclosure)
			child.markRendering(false);
		}
		super.onAfterRenderChildren();
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();

		modCounter = 0;
		removals_clear();

		if (queue != null && !queue.isEmpty() && hasBeenRendered())
		{
			throw new WicketRuntimeException(
					String.format("Detach called on component with id '%s' while it had a non-empty queue: %s",
							getId(), queue));
		}
	}

	private transient ComponentQueue queue;

	/**
	 * Queues one or more components to be dequeued later. The advantage of this method over the
	 * {@link #add(Component...)} method is that the component does not have to be added to its
	 * direct parent, only to a parent upstream; it will be dequeued into the correct parent using
	 * the hierarchy defined in the markup. This allows the component hierarchy to be maintained only
	 * in markup instead of in markup and in java code; affording designers and developers more
	 * freedom when moving components in markup.
	 * 
	 * @param components
	 *             the components to queue
	 * @return {@code this} for method chaining             
	 */
	public MarkupContainer queue(Component... components)
	{
		if (queue == null)
		{
			queue = new ComponentQueue();
		}
		queue.add(components);
		
		Page page = findPage();

		if (page != null)
		{
			dequeue();			
		}

		return this;
	}

	/**
	 * @see IQueueRegion#dequeue()
	 */
	public void dequeue()
	{
		if (this instanceof IQueueRegion)
		{
			DequeueContext dequeue = newDequeueContext();
			dequeuePreamble(dequeue);
		}
		else
		{
			MarkupContainer containerWithQueue = this;

			// check if there are any parent containers that have queued components, up till our
			// queue region
			while (containerWithQueue.isQueueEmpty() &&
				!(containerWithQueue instanceof IQueueRegion))
			{
				containerWithQueue = containerWithQueue.getParent();
				if (containerWithQueue == null)
				{
					// no queued components are available for dequeuing, so we can stop
					return;
				}
			}

			// when there are no components to be dequeued, just stop
			if (containerWithQueue.isQueueEmpty())
				return;

			// get the queue region where we are going to dequeue components in
			MarkupContainer queueRegion = containerWithQueue;

			// the container with queued components could be a queue region, if not, find the region
			// to dequeue in
			if (!queueRegion.isQueueRegion())
			{
				queueRegion = (MarkupContainer)queueRegion.findParent(IQueueRegion.class);
			}

			if (queueRegion != null && !queueRegion.getRequestFlag(RFLAG_CONTAINER_DEQUEING))
			{
				queueRegion.dequeue();
			}
		}
	}

	/**
	 * @return {@code true} when one or more components are queued
	 */
	private boolean isQueueEmpty()
	{
		return queue == null || queue.isEmpty();
	}

	/**
	 * @return {@code true} when this markup container is a queue region
	 */
	private boolean isQueueRegion() 
	{
		return IQueueRegion.class.isInstance(this);
	}

	/**
	 * Run preliminary operations before running {@link #dequeue(DequeueContext)}. More in detail it
	 * throws an exception if the container is already dequeuing, and it also takes care of setting
	 * flag {@code RFLAG_CONTAINER_DEQUEING} to true before running {@link #dequeue(DequeueContext)}
	 * and setting it back to false after dequeuing is completed.
	 * 
	 * @param dequeue
	 *            the dequeue context to use
	 */
	protected void dequeuePreamble(DequeueContext dequeue)
	{
		if (getRequestFlag(RFLAG_CONTAINER_DEQUEING))
		{
			throw new IllegalStateException("This container is already dequeing: " + this);
		}

		setRequestFlag(RFLAG_CONTAINER_DEQUEING, true);
		try
		{
			if (dequeue == null)
			{
				return;
			}

			if (dequeue.peekTag() != null)
			{
				dequeue(dequeue);
			}
		}
		finally
		{
			setRequestFlag(RFLAG_CONTAINER_DEQUEING, false);
		}
	}

	/**
	 * Dequeues components. The default implementation iterates direct children of this container
	 * found in its markup and tries to find matching
	 * components in queues filled by a call to {@link #queue(Component...)}. It then delegates the
	 * dequeueing to these children.
	 * 
	 * 
	 * Certain components that implement custom markup behaviors (such as repeaters and borders)
	 * override this method to bring dequeueing in line with their custom markup handling.
	 * 
	 * @param dequeue
	 *             the dequeue context to use     
	 */
	public void dequeue(DequeueContext dequeue)
	{
		while (dequeue.isAtOpenOrOpenCloseTag())
		{
			ComponentTag tag = dequeue.takeTag();
	
			// see if child is already added to parent
			Component child = get(tag.getId());

			if (child == null)
			{
				// the container does not yet have a child with this id, see if we can
				// dequeue
				child = dequeue.findComponentToDequeue(tag);
				
				//if tag has an autocomponent factory let's use it
				if (child == null && tag.getAutoComponentFactory() != null)
				{
					IAutoComponentFactory autoComponentFactory = tag.getAutoComponentFactory();
					child = autoComponentFactory.newComponent(this, tag);
				}
				
				if (child != null)
				{
					addDequeuedComponent(child, tag);
				}
			}
			
			if (tag.isOpen() && !tag.hasNoCloseTag())
            {
			    dequeueChild(child, tag, dequeue);
            }
		}

	}
	
	/**
	 * Propagates dequeuing to child component.
	 * 
	 * @param child
	 *             the child component
	 * @param tag
	 *             the child tag
	 * @param dequeue
	 *             the dequeue context to use
	 */
	private void dequeueChild(Component child, ComponentTag tag, DequeueContext dequeue)
	{
		ChildToDequeueType childType = ChildToDequeueType.fromChild(child);
		
		if (childType == ChildToDequeueType.QUEUE_REGION ||
			childType == ChildToDequeueType.BORDER)
		{
			((IQueueRegion)child).dequeue();			
		}
		
		if (childType == ChildToDequeueType.MARKUP_CONTAINER)
		{
			// propagate dequeuing to containers
			MarkupContainer childContainer = (MarkupContainer)child;
			
			dequeue.pushContainer(childContainer);
			childContainer.dequeue(dequeue);
			dequeue.popContainer();			
		}
		
		if (childType == ChildToDequeueType.NULL || 
			childType == ChildToDequeueType.QUEUE_REGION)
		{
			dequeue.skipToCloseTag();
		}

		// pull the close tag off
		ComponentTag close = dequeue.takeTag();
		do
		{
			if (close != null && close.closes(tag))
			{
				return;
			}
		} while ((close = dequeue.takeTag()) != null);

		throw new IllegalStateException(String.format("Could not find the closing tag for '%s'", tag));
	}

    /** @see IQueueRegion#newDequeueContext() */
	public DequeueContext newDequeueContext()
	{
		IMarkupFragment markup = getRegionMarkup();
		if (markup == null)
		{
			return null;
		}

		return new DequeueContext(markup, this, false);
	}

	/** @see IQueueRegion#getRegionMarkup() */
	public IMarkupFragment getRegionMarkup()
	{
		return getAssociatedMarkup();
	}

	/**
	 * Checks if this container can dequeue a child represented by the specified tag. This method
	 * should be overridden when containers can dequeue components represented by non-standard tags.
	 * For example, borders override this method and dequeue their body container when processing
	 * the body tag.
	 * 
	 * By default all {@link ComponentTag}s are supported as well as {@link WicketTag}s that return
	 * a non-null value from {@link WicketTag#getAutoComponentFactory()} method.
	 * 
	 * @param tag
	 */
	protected DequeueTagAction canDequeueTag(ComponentTag tag)
	{
		if (tag instanceof WicketTag)
		{
			WicketTag wicketTag = (WicketTag)tag;
			if (wicketTag.isContainerTag())
			{
				return DequeueTagAction.DEQUEUE;
			}
			else if (wicketTag.getAutoComponentFactory() != null)
			{
				return DequeueTagAction.DEQUEUE;
			}
			else if (wicketTag.isFragmentTag())
			{
				return DequeueTagAction.SKIP;
			}
			else if (wicketTag.isChildTag())
			{
				return DequeueTagAction.IGNORE;
			}
			else if (wicketTag.isHeadTag())
			{
				return DequeueTagAction.SKIP;
			}
			else if (wicketTag.isLinkTag())
			{
				return DequeueTagAction.DEQUEUE;
			}
			else
			{
				return null; // don't know
			}
		}
		
		//if is a label tag, ignore it
		if (tag.isAutoComponentTag() 
			&& tag.getId().startsWith(AutoLabelResolver.LABEL_ATTR))
		{
			return DequeueTagAction.IGNORE;
		}
		
		return DequeueTagAction.DEQUEUE;
	}

	/**
	 * Queries this container to find a child that can be dequeued that matches the specified tag.
	 * The default implementation will check if there is a component in the queue that has the same
	 * id as a tag, but sometimes custom tags can be dequeued and in those situations this method
	 * should be overridden.
	 * 
	 * @param tag
	 * @return
	 */
	public Component findComponentToDequeue(ComponentTag tag)
	{
		return queue == null ? null : queue.remove(tag.getId());
	}

	/**
	 * Adds a dequeued component to this container. This method should rarely be overridden because
	 * the common case of simply forwarding the component to
	 * {@link MarkupContainer#add(Component...))} method should cover most cases. Components that
	 * implement a custom hierarchy, such as borders, may wish to override it to support edge-case
	 * non-standard behavior.
	 * 
	 * @param component
	 * @param tag
	 */
	protected void addDequeuedComponent(Component component, ComponentTag tag)
	{
		add(component);
	}

	/**
	 * Returns a sequential {@code Stream} with the direct children of this markup container as its
	 * source. This stream doesn't traverse the component tree.
	 *
	 * @return a sequential {@code Stream} over the direct children of this markup container
	 * @since 8.0
	 */
	public Stream<Component> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Returns a sequential {@code Stream} with the all children of this markup container as its
	 * source. This stream does traverse the component tree.
	 * 
	 * @return a sequential {@code Stream} over the all children of this markup container
	 * @since 8.0
	 */
	@SuppressWarnings("unchecked")
	public Stream<Component> streamChildren()
	{
		class ChildrenIterator<C> implements Iterator<C>
		{
			private Iterator<C> currentIterator;

			private Deque<Iterator<C>> iteratorStack = new ArrayDeque<>();

			private ChildrenIterator(Iterator<C> iterator)
			{
				currentIterator = iterator;
			}

			@Override
			public boolean hasNext()
			{
				if (!currentIterator.hasNext() && !iteratorStack.isEmpty())
				{
					currentIterator = iteratorStack.pop();
				}
				return currentIterator.hasNext();
			}

			@Override
			public C next()
			{
				C child = currentIterator.next();
				if (child instanceof Iterable)
				{
					iteratorStack.push(currentIterator);
					currentIterator = ((Iterable<C>)child).iterator();
				}
				return child;
			}
		}
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(new ChildrenIterator<>(iterator()), 0), false);
	}
}
