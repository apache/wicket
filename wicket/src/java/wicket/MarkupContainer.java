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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.util.collections.MicroMap;
import wicket.util.collections.MiniMap;
import wicket.util.string.Strings;

/**
 * A MarkupContainer holds a map of child components.
 * <ul>
 * <li><b>Children </b>- Children can be added by calling the add() method,
 * and they can be looked up using a dotted path. For example, if a container
 * called "a" held a nested container "b" which held a nested component "c",
 * then a.get("b.c") would return the Component with id "c". The number of
 * children in a MarkupContainer can be determined by calling size(), and the
 * whole hierarchy of children held by a MarkupContainer can be traversed by
 * calling visitChildren(), passing in an implementation of Component.IVisitor.
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
 * @see MarkupStream
 * @author Jonathan Locke
 */
public abstract class MarkupContainer extends Component
{
	/** Log for reporting. */
	private static final Log log = LogFactory.getLog(MarkupContainer.class);

	/** Size of MiniMaps. */
	private static final int MINIMAP_MAX_ENTRIES = 6;

	/** Whether to optimize maps of children with MicroMap and MiniMap. */
	private static final boolean optimizeChildMapsForSpace = false;

	/** Map of children by id. */
	private Map childForId = Collections.EMPTY_MAP;

	/** The markup stream for this container. */
	private transient MarkupStream markupStream;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public MarkupContainer(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public MarkupContainer(final String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Adds a child component to this container.
	 * 
	 * @param child
	 *            The child
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add
	 *             operation.
	 * @return This
	 */
	public MarkupContainer add(final Component child)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Add " + child.getId() + " to " + this);
		}

		// Add to map
		addedComponent(child);
		if (put(child) != null)
		{
			throw new IllegalArgumentException(exceptionMessage("A child with id '" + child.getId()
					+ "' already exists"));
		}

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
	public final void autoAdd(final Component component)
	{
		component.setAuto(true);
		add(component);
		component.internalBeginRequest();
		component.render();
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
	public final Component get(final String path)
	{
		// Reference to this container
		if (path == null || path.trim().equals(""))
		{
			return this;
		}

		// Get child's id, if any
		final String id = Strings.firstPathComponent(path, '.');

		// Get child by id
		final Component child = (Component)childForId.get(id);

		// Found child?
		if (child != null)
		{
			// Recurse on latter part of path
			return child.get(Strings.afterFirstPathComponent(path, '.'));
		}

		// No child with the given id
		return null;
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.  DO NOT USE IT.
	 * 
	 * Adds a child component to this container.
	 * 
	 * @param child
	 *            The child
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same id is replaced by the add
	 *             operation.
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
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request begins.
	 */
	public void internalBeginRequest()
	{
		// Handle begin request for the container itself
		super.internalBeginRequest();

		// Loop through child components
		for (final Iterator iterator = childForId.values().iterator(); iterator.hasNext();)
		{
			Component child = (Component)iterator.next();
			if(!(child instanceof IFeedback)) // ignore feedback as that was done in Page
			{
				// Call begin request on the child
				(child).internalBeginRequest();
			}
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL OR
	 * OVERRIDE.
	 * 
	 * Called when a request ends.
	 */
	public void internalEndRequest()
	{
		// Handle end request for the container itself
		super.internalEndRequest();

		// Loop through child components
		for (final Iterator iterator = childForId.values().iterator(); iterator.hasNext();)
		{
			// Call end request on the child
			((Component)iterator.next()).internalEndRequest();
		}
	}

	/**
	 * @return Iterator that iterates through children in an undefined order
	 */
	public final Iterator iterator()
	{
		if (childForId == null)
		{
			childForId = Collections.EMPTY_MAP;
		}

		final Iterator iterator = childForId.values().iterator();
		return new Iterator()
		{
			private Component component;

			/**
			 * @see java.util.Iterator#hasNext()
			 */
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			/**
			 * @see java.util.Iterator#next()
			 */
			public Object next()
			{
				return component = (Component)iterator.next();
			}

			/**
			 * @see java.util.Iterator#remove()
			 */
			public void remove()
			{
				iterator.remove();
				removedComponent(component);
			}
		};
	}

	/**
	 * Removes the given component
	 * 
	 * @param id
	 *            The id of the component to remove
	 */
	public void remove(final String id)
	{
		final Component component = get(id);
		if (component != null)
		{
			childForId.remove(id);
			removedComponent(component);
		}
		else
		{
			throw new WicketRuntimeException("Unable to find a component with id '" + id
					+ "' to remove");
		}
	}

	/**
	 * Removes all children from this container.
	 */
	public void removeAll()
	{
		for (final Iterator iterator = iterator(); iterator.hasNext();)
		{
			iterator.next();
			iterator.remove();
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
		if (log.isDebugEnabled())
		{
			log.debug("Replacing " + child.getId() + " in " + this);
		}

		if (child.getParent() != this)
		{
			// Add to map
			final Component replaced = put(child);
			addedComponent(child);
			removedComponent(replaced);

			// Look up to make sure it was already in the map
			if (replaced == null)
			{
				throw new IllegalArgumentException(
						exceptionMessage("A child component with the id '" + child.getId()
								+ "' didn't exist"));
			}
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
		return childForId.size();
	}

	/**
	 * @see wicket.Component#toString()
	 */
	public String toString()
	{
		return toString(false);
	}

	/**
	 * @param detailed
	 *            True if a detailed string is desired
	 * @return String representation of this container
	 */
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

			if (childForId != null && childForId.size() != 0)
			{
				buffer.append(", children = " + childForId.values());
			}
		}
		buffer.append(']');
		return buffer.toString();
	}

	/**
	 * Traverses all child components of the given class in this container,
	 * calling the visitor's visit method at each one.
	 * 
	 * @param c
	 *            The class of child to visit, or null to visit all children
	 * @param visitor
	 *            The visitor to call back to
	 * @return The return value from a visitor which halted the traversal, or
	 *         null if the entire traversal occurred
	 */
	public final Object visitChildren(final Class c, final IVisitor visitor)
	{
		// Iterate through children on this container
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			// Get next child component
			final Component child = (Component)iterator.next();

			// Is the child of the correct class (or was no class specified)?
			if ((c == null) || c.isInstance(child))
			{
				// Call visitor
				final Object value = visitor.component(child);

				// If visitor returns a non-null value, it halts the traversal
				if (value != IVisitor.CONTINUE_TRAVERSAL)
				{
					return value;
				}
			}

			// If child is a container
			if (child instanceof MarkupContainer)
			{
				// visit the children in the container
				final Object value = ((MarkupContainer)child).visitChildren(c, visitor);

				// If visitor returns a non-null value, it halts the traversal
				if (value != IVisitor.CONTINUE_TRAVERSAL)
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
	 * Get the markup stream set on this container.
	 * 
	 * @return Returns the markup stream set on this container.
	 */
	protected MarkupStream getMarkupStream()
	{
		return markupStream;
	}
	
	/**
	 * Get the base class' markup stream
	 * 
	 * @return Returns the markup stream set on inherited component
	 */
	protected final MarkupStream getInheritedMarkupStream()
	{
		// TODO this is not necessarily just super.getClass().
	    return getApplication().getMarkupCache().getMarkupStream(
	            this, 
	            this.getClass().getSuperclass());
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
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		renderComponentTagBody(markupStream, openTag);
	}

	/**
	 * Renders this component.
	 */
	protected void onRender()
	{
		renderAll(findMarkupStream());
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
	protected final void renderAssociatedMarkup(final String openTagName,
			final String exceptionMessage)
	{
		// Get markup associated with Border or Panel component
		final MarkupStream originalMarkupStream = getMarkupStream();
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream();

		associatedMarkupStream.skipRawMarkup();
		setMarkupStream(associatedMarkupStream);

		// Get open tag in associated markup of border component
		final ComponentTag associatedMarkupOpenTag = associatedMarkupStream.getTag();

		// Check for required open tag name
		if (!(associatedMarkupStream.atOpenTag(openTagName) && (associatedMarkupOpenTag instanceof WicketTag)))
		{
			associatedMarkupStream.throwMarkupException(exceptionMessage);
		}

		renderComponentTag(associatedMarkupOpenTag);
		associatedMarkupStream.next();
		renderComponentTagBody(associatedMarkupStream, associatedMarkupOpenTag);
		renderClosingComponentTag(associatedMarkupStream, associatedMarkupOpenTag);
		setMarkupStream(originalMarkupStream);
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
		if (openTag.requiresCloseTag())
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
	 * @see wicket.Component#setModel(wicket.model.IModel)
	 */
	public void setModel(final IModel model)
	{
		final IModel previous = getModel();
		super.setModel(model);
		if(previous instanceof CompoundPropertyModel)
		{
			visitChildren(new IVisitor()
			{
			
				public Object component(Component component)
				{
					IModel compModel = component.getModel();
					if(compModel == previous)
					{
						component.setModel(null);
					}
					else if(compModel == model)
					{
						component.modelChanged();
					}
					return IVisitor.CONTINUE_TRAVERSAL;
				}
			
			});
		}
	}
	
	
	/**
	 * Set markup stream for this container.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	protected final void setMarkupStream(final MarkupStream markupStream)
	{
		this.markupStream = markupStream;
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource
	 * for this class.
	 * 
	 * @return A stream of MarkupElement elements
	 */
	final MarkupStream getAssociatedMarkupStream()
	{
	    try
	    {
	        return getApplication().getMarkupCache().getMarkupStream(this, null);
	    }
	    catch (WicketRuntimeException ex)
		{
			// throw exception since there is no associated markup
			throw new WicketRuntimeException(
					exceptionMessage("Markup of type '"
							+ getMarkupType()
							+ "' for component '"
							+ getClass().getName()
							+ "' not found or invalid"
							+ " Enable debug messages for wicket.util.resource.Resource to get a list of all filenames tried"),
							ex);
		}
	}

	/**
	 * @return True if this markup container has associated markup
	 */
	final boolean hasAssociatedMarkup()
	{
        return getApplication().getMarkupCache().hasAssociatedMarkup(this, null);
	}

	/**
	 * Renders this component and all sub-components using the given markup
	 * stream.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	final void renderAll(final MarkupStream markupStream)
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

		// Set child's parent
		component.setParent(this);

		// Tell the page a component was added
		final Page page = findPage();
		if (page != null)
		{
			page.componentAdded(component);
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
	private final Component put(final Component child)
	{
		if (optimizeChildMapsForSpace)
		{
			if (childForId == Collections.EMPTY_MAP)
			{
				childForId = new MicroMap();
			}
			else if (childForId.size() == MicroMap.MAX_ENTRIES)
			{
				// Reallocate MicroMap as MiniMap
				childForId = new MiniMap(childForId, MINIMAP_MAX_ENTRIES);
			}
			else if (childForId.size() == MINIMAP_MAX_ENTRIES)
			{
				// Reallocate MiniMap as full HashMap
				childForId = new HashMap(childForId);
			}
		}
		else
		{
			if (childForId == Collections.EMPTY_MAP)
			{
				childForId = new HashMap();
			}
		}

		return (Component)childForId.put(child.getId(), child);
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
		if (element instanceof ComponentTag && !markupStream.atCloseTag())
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
				component.render();
			}
			else
			{
				// Try application's component resolvers
				final List componentResolvers = this.getApplication().getComponentResolvers();
				final Iterator iterator = componentResolvers.iterator();
				while (iterator.hasNext())
				{
					final IComponentResolver resolver = (IComponentResolver)iterator.next();
					if (resolver.resolve(this, markupStream, tag))
					{
						return;
					}
				}

				// 3rd try: Components like Border and Panel might implement
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

				// No one was able to handle the component id
				markupStream.throwMarkupException("Unable to find component with id '" + id
						+ "' in " + this);
			}
		}
		else
		{
			// Render as raw markup
			log.debug("Rendering raw markup");
			getResponse().write(element.toString());
			markupStream.next();
		}
	}

	/**
	 * @param component
	 *            Component being removed
	 */
	private static final void removedComponent(final Component component)
	{
		// Notify Page that component is being removed
		final Page page = component.findPage();
		if (page != null)
		{
			page.componentRemoved(component);
		}

		// Detach model
		component.detachModel();

		// Component is removed
		component.setParent(null);
	}
}
