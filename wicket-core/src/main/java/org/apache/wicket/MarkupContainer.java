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

import org.apache.wicket.core.util.string.ComponentStrings;
import org.apache.wicket.markup.ComponentTag;
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
import org.apache.wicket.markup.html.internal.InlineEnclosure;
import org.apache.wicket.markup.resolver.ComponentResolvers;
import org.apache.wicket.model.IComponentInheritedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.settings.DebugSettings;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
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
 * 
 */
public abstract class MarkupContainer extends Component implements Iterable<Component>
{
	private static final long serialVersionUID = 1L;

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupContainer.class);

	/** List of children or single child */
	private Object children;

	private List<Component> queuedComponents = null;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public MarkupContainer(final String id)
	{
		this(id, null);
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
	 *            The child(ren) to add.
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add operation.
	 * @return This
	 */
	public MarkupContainer add(final Component... childs)
	{
		for (Component child : childs)
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
	public MarkupContainer addOrReplace(final Component... childs)
	{
		for (Component child : childs)
		{
			if (child == null)
			{
				throw new IllegalArgumentException("argument child must be not null");
			}

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
		int index = children_indexOf(component);
		if (index >= 0)
		{
			children_remove(index);
		}
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
	 * Get the childs markup
	 * 
	 * @see Component#getMarkup()
	 * 
	 * @param child
	 *            The child component. If null, the container's markup will be returned. See Border,
	 *            Panel or Enclosure where getMarkup(null) != getMarkup().
	 * @return The childs markup
	 */
	public IMarkupFragment getMarkup(final Component child)
	{
		// Delegate request to attached markup sourcing strategy.
		return getMarkupSourcingStrategy().getMarkup(this, child);
	}

	/**
	 * Get the type of associated markup for this component.
	 * 
	 * @return The type of associated markup for this component (for example, "html", "wml" or
	 *         "vxml"). The markup type for a component is independent of whether or not the
	 *         component actually has an associated markup resource file (which is determined at
	 *         runtime). If there is no markup type for a component, null may be returned, but this
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
	 * @return Iterator that iterates through children in the order they were added
	 */
	@Override
	public Iterator<Component> iterator()
	{
		return new Iterator<Component>()
		{
			int index = 0;

			@Override
			public boolean hasNext()
			{
				return index < children_size();
			}

			@Override
			public Component next()
			{
				return children_get(index++);
			}

			@Override
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
	 * @param component
	 *            Component to remove from this container
	 * @return {@code this} for chaining
	 */
	public MarkupContainer remove(final Component component)
	{
		checkHierarchyChange(component);

		Args.notNull(component, "component");

		children_remove(component);
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
					child.internalOnRemove();
					child.detach();
					child.setParent(null);
				}
			}

			children = null;
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

			// The generated markup id remains the same
			child.setMarkupId(replaced);

			// then add the other one.
			addedComponent(child);
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
		final StringBuilder buffer = new StringBuilder();
		buffer.append('[').append(Classes.simpleName(this.getClass())).append(' ');
		buffer.append(super.toString(detailed));
		if (detailed && children_size() != 0)
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
	 * @return A iterator which iterators over all children and grand-children the Component
	 */
	public final ComponentHierarchyIterator visitChildren()
	{
		return new ComponentHierarchyIterator(this);
	}

	/**
	 * @param clazz
	 *            Filter condition
	 * @return A iterator which iterators over all children and grand-children the Component,
	 *         returning only components which implement (instanceof) the provided clazz.
	 */
	public final ComponentHierarchyIterator visitChildren(final Class<?> clazz)
	{
		return new ComponentHierarchyIterator(this).filterByClass(clazz);
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
		if (parent != null)
		{
			parent.remove(child);
		}

		// Set child's parent
		child.setParent(this);

		final DebugSettings debugSettings = Application.get().getDebugSettings();
		if (debugSettings.isLinePreciseReportingOnAddComponentEnabled() &&
			debugSettings.getComponentUseCheck())
		{
			child.setMetaData(ADDED_AT_KEY,
				ComponentStrings.toString(child, new MarkupException("added")));
		}

		final Page page = findPage();
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

	/**
	 * @param child
	 *            Child to add
	 */
	private void children_add(final Component child)
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
	private Component children_get(int index)
	{
		return (Component)children_get(index, true);
	}

	/**
	 * 
	 * @param index
	 * @param reconstruct
	 * @return the child component
	 */
	private Object children_get(int index, boolean reconstruct)
	{
		Object component = null;
		if (children != null)
		{
			if (children instanceof Object[] == false && children instanceof ChildList == false)
			{
				if (index != 0)
				{
					throw new ArrayIndexOutOfBoundsException("index " + index +
						" is greater then 0");
				}
				component = children;
				if (children != component)
				{
					children = component;
				}
			}
			else
			{
				Object[] children;
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
				component = children[index];
			}
		}
		return component;
	}

	/**
	 * Returns the wicket:id of the given object if it is a {@link Component}
	 * 
	 * @param object
	 * @return The id of the object (object can be component)
	 */
	private String getId(Object object)
	{
		if (object instanceof Component)
		{
			return ((Component)object).getId();
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
	private Component children_get(final String id)
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
				component = (Component)children;
			}
		}
		else
		{
			Object[] children;
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
					component = (Component)children[i];
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
	private int children_indexOf(Component child)
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
	private Component children_remove(Component component)
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
	private Component children_remove(int index)
	{
		if (children == null)
		{
			return null;
		}

		if (children instanceof Component)
		{
			if (index == 0)
			{
				final Component removed = (Component)children;
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
					return (Component)removed;
				}
				children = new ChildList(children);
			}

			ChildList lst = (ChildList)children;
			Object removed = lst.remove(index);
			if (lst.size == 1)
			{
				children = lst.get(0);
			}
			return (Component)removed;
		}
	}

	/**
	 * 
	 * @param index
	 * @param child
	 * @param reconstruct
	 * @return The replaced child
	 */
	private Object children_set(int index, Object child, boolean reconstruct)
	{
		Object replaced;
		if (index >= 0 && index < children_size())
		{
			if (children instanceof Component)
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
		return replaced;
	}

	/**
	 * 
	 * @param index
	 * @param child
	 * @return The component that is replaced
	 */
	private Component children_set(int index, Component child)
	{
		return (Component)children_set(index, child, true);
	}

	/**
	 * 
	 * @return The size of the children
	 */
	private int children_size()
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
	private Component put(final Component child)
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
				getResponse().write(element.toCharSequence());
				return true;
			}
			else
			{
				if (tag instanceof WicketTag)
				{
					if (((WicketTag)tag).isChildTag())
					{
						markupStream.throwMarkupException("Found " + tag.toString() +
							" but no <wicket:extend>");
					}
					else
					{
						markupStream.throwMarkupException("Failed to handle: " +
							tag.toString() +
							". It might be that no resolver has been registered to handle this special tag. " +
							" But it also could be that you declared wicket:id=" + id +
							" in your markup, but that you either did not add the " +
							"component to your page at all, or that the hierarchy does not match.");
					}
				}

				List<String> names = findSimilarComponents(id);

				// No one was able to handle the component id
				StringBuffer msg = new StringBuffer(500);
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
				msg.append("'");

				log.error(msg.toString());
				markupStream.throwMarkupException(msg.toString());
			}
		}
		else
		{
			// Render as raw markup
			getResponse().write(element.toCharSequence());
			return true;
		}

		return false;
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

	/**
	 * @see org.apache.wicket.Component#onRender()
	 */
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

		if (render == true)
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
		while (markupStream.hasMore())
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

	/**
	 * @see org.apache.wicket.Component#removeChildren()
	 */
	@Override
	void removeChildren()
	{
		super.removeChildren();

		for (int i = children_size(); i-- > 0;)
		{
			Object child = children_get(i, false);
			if (child instanceof Component)
			{
				Component component = (Component)child;
				component.internalOnRemove();
			}
		}
	}

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

				// We need to keep InlineEnclosures for Ajax request handling.
				// TODO this is really ugly. Feature request for 1.5: change auto-component that
				// they don't need to be removed anymore.
				if (component.isAuto() && !(component instanceof InlineEnclosure))
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
	 * @see org.apache.wicket.Component#internalMarkRendering(boolean)
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
			for (final Component child : children)
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
			{
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}

			ensureCapacity(size + 1);
			System.arraycopy(childs, index, childs, index + 1, size - index);
			childs[index] = element;
			size++;
		}

		@Override
		public Object set(int index, Object element)
		{
			if (index >= size)
			{
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}

			Object oldValue = childs[index];
			childs[index] = element;
			return oldValue;
		}

		@Override
		public Object remove(int index)
		{
			if (index >= size)
			{
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			}

			Object oldValue = childs[index];

			int numMoved = size - index - 1;
			if (numMoved > 0)
			{
				System.arraycopy(childs, index + 1, childs, index, numMoved);
			}
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
				{
					newCapacity = minCapacity;
				}
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

	@Override
	final void buildComponentTree()
	{
		if (isMarkupDrivenComponentTreeEnabled())
		{
			ComponentTreeBuilder builder = new ComponentTreeBuilder();
			builder.rebuild(this);
		}
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();

		if (isInitialized())
		{
			// the queued components should be preserved for pages which
			// will be rendered in a following request
			queuedComponents = null;
		}
	}

	protected boolean isMarkupDrivenComponentTreeEnabled()
	{
		// TODO this method is called once per MarkupContainer's life
		// Check whether this traversal to the parent is causing perf regression
		MarkupContainer parent = getParent();
		if (parent != null)
		{
			return parent.isMarkupDrivenComponentTreeEnabled();
		}
		return false;
	}

	/**
	 * Adds component(s) that will be available for markup driven
	 * component tree construction
	 *
	 * @param components
	 *          the components
	 */
	public final void enqueue(Component... components)
	{
		if (components != null)
		{
			if (queuedComponents == null)
			{
				queuedComponents = new ArrayList<>();
			}

			for (Component c : components)
			{
				queuedComponents.add(c);
			}
		}
	}
}
