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

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.ComponentWicketTag;
import wicket.markup.IComponentResolver;
import wicket.markup.Markup;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.model.IModel;
import wicket.util.collections.MicroMap;
import wicket.util.collections.MiniMap;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.IResource;
import wicket.util.resource.ResourceNotFoundException;
import wicket.util.string.Strings;
import wicket.util.watch.ModificationWatcher;

/**
 * A markup container holds a map of child components. Children can be added by
 * calling the add() method and they can be looked up using a dotted path. For
 * example, if a container called "a" held a nested container "b" which held a
 * nested component "c", then a.get("b.c") would return the component named "c".
 * <p>
 * The number of children in a container can be determined by calling size().
 * And the whole hierarchy of children held by a container can be traversed by
 * calling visitChildren(), passing in an implementation of Component.IVisitor.
 * <p>
 * A container also holds markup information which is used to render the
 * container. As the markup stream for a container is rendered, component
 * references in the markup are resolved by using the container to look up
 * components by name. Each component referenced by the markup stream is given
 * an opportunity to render itself using the markup stream.
 * <p>
 * Components may alter the referring tag, replace the tag's body or insert
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

	/** Map of markup tags by class. */
	private static final Map markupCache = new HashMap();

	/** Size of MiniMaps. */
	private static final int MINIMAP_MAX_ENTRIES = 8;

	/** Whether to optimize maps of children with MicroMap and MiniMap. */
	private static final boolean optimizeChildMapsForSpace = false;

	/** Map of children by name. */
	private Map childForName = Collections.EMPTY_MAP;

	/** The markup stream for this container. */
	private transient MarkupStream markupStream;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public MarkupContainer(final String name)
	{
		super(name);
		optimize();
	}
	
	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public MarkupContainer(String name, IModel model)
	{
		super(name, model);
		optimize();
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public MarkupContainer(String name, Serializable object)
	{
		super(name, object);
		optimize();
	}

	/**
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public MarkupContainer(String name, Serializable object, String expression)
	{
		super(name, object, expression);
		optimize();
	}

	/**
	 * Adds a child component to this container.
	 * 
	 * @param child
	 *            The child
	 * @throws IllegalArgumentException
	 *             Thrown if a child with the same name is replaced by the add
	 *             operation.
	 * @return This
	 */
	public MarkupContainer add(final Component child)
	{
		if (child == this)
		{
			throw new IllegalArgumentException("Component can't be added to itself");
		}
		// Get child name
		final String childName = child.getName();

		if (log.isDebugEnabled())
		{
			log.debug("Add " + childName + " to " + this);
		}

		// Set child's parent
		child.setParent(this);

		// Are we using MicroMap optimization?
		if (optimizeChildMapsForSpace)
		{
			if (childForName.size() == MicroMap.MAX_ENTRIES)
			{
				// Reallocate MicroMap as MiniMap
				childForName = new MiniMap(childForName, MINIMAP_MAX_ENTRIES);
			}
			else if (childForName.size() == MINIMAP_MAX_ENTRIES)
			{
				// Reallocate MiniMap as full HashMap
				childForName = new HashMap(childForName);
			}
		}

		// Add to map
		final Object replaced = childForName.put(childName, child);

		// Look up to make sure it's not already in the map
		if (replaced != null)
		{
			throw new IllegalArgumentException(exceptionMessage("A child component with the name '"
					+ childName + "' already exists"));
		}

		return this;
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

		// Get child's name, if any
		final String childName = Strings.firstPathComponent(path, '.');

		// Get child by name
		final Component child = (Component)childForName.get(childName);

		// Found child?
		if (child != null)
		{
			// Recurse on latter part of path
			return child.get(Strings.afterFirstPathComponent(path, '.'));
		}

		// No child by that name
		return null;
	}

	/**
	 * Get the Iterator that iterates through children in an undefined order.
	 * 
	 * @return Iterator that iterates through children in an undefined order
	 */
	public final Iterator iterator()
	{
		if (childForName == null)
		{
			childForName = Collections.EMPTY_MAP;
		}
		return childForName.values().iterator();
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
				MarkupContainer parent = component.getParent();

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
	 * Removes all children from this container.
	 */
	public void removeAll()
	{
		childForName.clear();
	}

	/**
	 * Replaces a child component of this container with another
	 * 
	 * @param child
	 *            The child
	 * @throws IllegalArgumentException
	 *             Thrown if there was no child with the same name.
	 * @return This
	 */
	public MarkupContainer replace(final Component child)
	{
		// Get child name
		final String childName = child.getName();

		if (log.isDebugEnabled())
		{
			log.debug("Add " + childName + " to " + this);
		}

		if (child.getParent() != this)
		{
			// First reset the childs parent (can't set them at once with
			// another)
			child.setParent(null);

			// Set child's parent
			child.setParent(this);

			// Are we using MicroMap optimization?
			if (optimizeChildMapsForSpace)
			{
				if (childForName.size() == MicroMap.MAX_ENTRIES)
				{
					// Reallocate MicroMap as MiniMap
					childForName = new MiniMap(childForName, MINIMAP_MAX_ENTRIES);
				}
				else if (childForName.size() == MINIMAP_MAX_ENTRIES)
				{
					// Reallocate MiniMap as full HashMap
					childForName = new HashMap(childForName);
				}
			}

			// Add to map
			final Object replaced = childForName.put(childName, child);

			// Look up to make sure it was already in the map
			if (replaced == null)
			{
				throw new IllegalArgumentException(
						exceptionMessage("A child component with the name '" + childName
								+ "' didn't exists"));
			}
			((Component)replaced).setParent(null);
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
		return childForName.size();
	}

	/**
	 * Get the string representation of this container.
	 * 
	 * @return String representation of this container
	 */
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();

		buffer.append("[path = ");
		buffer.append(super.toString());

		if (markupStream != null)
		{
			buffer.append(", markupStream = " + markupStream);
		}

		if (childForName != null && childForName.size() != 0)
		{
			buffer.append(", children = " + childForName);
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
		while (c.markupStream == null)
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

		return c.markupStream;
	}

	/**
	 * Get the markup stream set on this container.
	 * 
	 * @return Returns the markup stream set on this container.
	 */
	protected final MarkupStream getMarkupStream()
	{
		return markupStream;
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
	protected String getMarkupType()
	{
		throw new IllegalStateException(
				exceptionMessage("You cannot directly subclass Page or MarkupContainer.  Instead, subclass a markup-specific class, such as WebPage or WebMarkupContainer"));
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
		if (!(associatedMarkupStream.atOpenTag(openTagName) && (associatedMarkupOpenTag instanceof ComponentWicketTag)))
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
	 * The MarkupContainer was not able to resolve the component name.
	 * Subclasses may augment the default strategy by subclassing
	 * resolveComponent().
	 * 
	 * @see wicket.markup.html.border.Border for an example.
	 *      <p>
	 *      Note: resolveComponent must also render the components created
	 * 
	 * @param markupStream
	 *            The current markup stream
	 * @param tag
	 *            The current component tag
	 * @return true, if MarkupContainer was able to resolve the component name
	 *         and to render the component
	 */
	protected boolean resolveComponent(final MarkupStream markupStream, final ComponentTag tag)
	{
		return false;
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
	 * Gets any (immutable) markup resource for this class.
	 * 
	 * @return Markup resource
	 */
	final Markup getAssociatedMarkup()
	{
		synchronized (markupCache)
		{
			// Look up markup tag list by class, locale, style and markup type
			final String key = markupKey();
			Markup markup = (Markup)markupCache.get(key);

			// If no markup in map
			if (markup == null)
			{
				// Locate markup resource, searching up class hierarchy
				IResource markupResource = null;
				Class containerClass = getClass();

				while ((markupResource == null) && (containerClass != MarkupContainer.class))
				{
					// Look for markup resource for containerClass
					markupResource = getApplication().getResourceLocator().locate(containerClass,
							getStyle(), getLocale(), getMarkupType());
					containerClass = containerClass.getSuperclass();
				}

				// Found markup?
				if (markupResource != null)
				{
					// load the markup and watch for changes
					markup = loadMarkupAndWatchForChanges(key, markupResource);
				}
				else
				{
					// flag markup as non-existent (as opposed to null, which
					// might mean that it's simply not loaded into the cache)
					markup = Markup.NO_MARKUP;
				}

				// Save any markup list (or absence of one) for next time
				markupCache.put(key, markup);
			}

			return markup;
		}
	}

	/**
	 * Gets a fresh markup stream that contains the (immutable) markup resource
	 * for this class.
	 * 
	 * @return A stream of MarkupElement elements
	 */
	final MarkupStream getAssociatedMarkupStream()
	{
		// Look for associated markup
		final Markup markup = getAssociatedMarkup();

		// If we found markup for this container
		if (markup != Markup.NO_MARKUP)
		{
			// return a MarkupStream for the markup
			return new MarkupStream(markup);
		}
		else
		{
			// throw exception since there is no associated markup
			throw new WicketRuntimeException(
					exceptionMessage("Markup of type '"
							+ getMarkupType()
							+ "' for component '"
							+ getClass().getName()
							+ "' not found."
							+ " Enable debug messages for wicket.util.resource.Resource to get a list of all filenames tried."));
		}
	}

	/**
	 * @return True if this markup container has associated markup
	 */
	final boolean hasAssociatedMarkup()
	{
		return getAssociatedMarkup() != Markup.NO_MARKUP;
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
	 * Loads markup.
	 * 
	 * @param application
	 *            Application
	 * @param key
	 *            Key under which markup should be cached
	 * @param markupResource
	 *            The markup resource to load
	 * @return The markup
	 * @throws ParseException
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private Markup loadMarkup(final Application application, final String key,
			final IResource markupResource) throws ParseException, IOException,
			ResourceNotFoundException
	{
		final Markup markup = application.getMarkupParser().readAndParse(markupResource);
		markupCache.put(key, markup);
		return markup;
	}

	/**
	 * Load markup and add a {@link ModificationWatcher}to the markup resource.
	 * 
	 * @param key
	 *            The key for the resource
	 * @param markupResource
	 *            The markup file to load and begin to watch
	 * @return The markup in the file
	 */
	private Markup loadMarkupAndWatchForChanges(final String key, final IResource markupResource)
	{
		final Application application = getApplication();

		try
		{
			// Watch file in the future
			final ModificationWatcher watcher = application.getResourceWatcher();

			if (watcher != null)
			{
				watcher.add(markupResource, new IChangeListener()
				{
					public void onChange()
					{
						synchronized (markupCache)
						{
							try
							{
								log.info("Reloading markup from " + markupResource);
								loadMarkup(application, key, markupResource);
							}
							catch (ParseException e)
							{
								log.error("Unable to parse markup from " + markupResource, e);
							}
							catch (ResourceNotFoundException e)
							{
								log.error("Unable to find markup from " + markupResource, e);
							}
							catch (IOException e)
							{
								log.error("Unable to read markup from " + markupResource, e);
							}
						}
					}
				});
			}

			log.info("Loading markup from " + markupResource);

			return loadMarkup(application, key, markupResource);
		}
		catch (ParseException e)
		{
			throw new MarkupException(markupResource,
					exceptionMessage("Unable to parse markup from " + markupResource), e);
		}
		catch (MarkupException e)
		{
			throw new MarkupException(markupResource, exceptionMessage(e.getMessage()));
		}
		catch (ResourceNotFoundException e)
		{
			throw new MarkupException(markupResource,
					exceptionMessage("Unable to find markup from " + markupResource), e);
		}
		catch (IOException e)
		{
			throw new MarkupException(markupResource,
					exceptionMessage("Unable to read markup from " + markupResource), e);
		}
	}

	/**
	 * @return Key that uniquely identifies any markup that might be associated
	 *         with this markup container.
	 */
	private String markupKey()
	{
		return getClass().getName() + getLocale() + getStyle() + getMarkupType();
	}

	/**
	 * Optimize child name mapping.
	 */
	private void optimize()
	{
		if (optimizeChildMapsForSpace)
		{
			childForName = new MicroMap();
		}
		else
		{
			childForName = new HashMap();
		}
	}

	/**
	 * Renders the next element of markup in the given markup stream.
	 * 
	 * @param markupStream
	 *            The markup stream
	 */
	private void renderNext(final MarkupStream markupStream)
	{
		// Get the current markup element
		final MarkupElement element = markupStream.get();

		// If it a tag like <wicket..> or <span id="wicket-..." >
		if (element instanceof ComponentTag && !markupStream.atCloseTag())
		{
			// Get element as tag
			final ComponentTag tag = (ComponentTag)element;

			// Get component name
			final String componentName = tag.getComponentName();

			// Get the component for the component name from the given container
			final Component component = get(componentName);

			// Failed to find it?
			if (component != null)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Begin render of sub-component " + component);
				}

				component.render();

				if (log.isDebugEnabled())
				{
					log.debug("End render of sub-component " + component);
				}
			}
			else
			{
				// 2nd try: all static name resolvers
				final List componentResolvers = this.getApplication().getComponentResolvers();
				final Iterator iter = componentResolvers.iterator();
				while (iter.hasNext())
				{
					final IComponentResolver resolver = (IComponentResolver)iter.next();
					if (resolver.resolve(this, markupStream, tag))
					{
						return;
					}
				}

				// 3rd try: a subclass replacing resolveComponent()
				MarkupContainer container = this;
				while (container != null)
				{
					if (container.resolveComponent(markupStream, tag))
					{
						return;
					}

					container = container.findParent(MarkupContainer.class);
				}

				// No one was able to handle the component name
				markupStream.throwMarkupException("Unable to find component named '"
						+ componentName + "' in " + this);
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
}