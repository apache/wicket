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
package wicket.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * Wicket organizes the Markup in a tree structure similar to DOM. Compared to
 * DOM not all XML tags are used to build the tree but rather wicket tags only.
 * Wicket tags are all XML/HTML tags with either wicket:id or &lt;wicket:xxx
 * ...&gt;. The wicket:id is used to indentify the tag inside the DOM.
 * 
 * @see wicket.markup.Markup
 * @see wicket.markup.MarkupElement
 * @see wicket.markup.ComponentTag
 * @see wicket.markup.RawMarkup
 * 
 * @author Juergen Donnerstag
 */
public class MarkupFragment extends MarkupElement implements Iterable<MarkupElement>
{
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(MarkupFragment.class);

	/** Placeholder that indicates no markup */
	public static final MarkupFragment NO_MARKUP_FRAGMENT = new MarkupFragment(IMarkup.NO_MARKUP);

	/** The list of markup elements */
	private/* final */List<MarkupElement> markupElements = new ArrayList<MarkupElement>();

	/** The associate markup */
	private final IMarkup markup;

	private boolean mutable;

	/**
	 * Constructor.
	 * 
	 * @param markup
	 *            The associated Markup
	 */
	public MarkupFragment(final IMarkup markup)
	{
		this(markup, null);
	}

	/**
	 * Constructor
	 * <p>
	 * This constructor should be used for tags inside a markup file.
	 * 
	 * @param markup
	 *            The associated Markup
	 * @param openTag
	 *            The initial (open) tag
	 */
	public MarkupFragment(final IMarkup markup, final ComponentTag openTag)
	{
		this.markup = markup;

		if (openTag != null)
		{
			this.markupElements.add(openTag);
		}
	}

	/**
	 * This is a very special constructor and must only be used with care. It
	 * create a <b>flat</b> MarkupFragment of current tag.
	 * 
	 * @param stream
	 */
	public MarkupFragment(final MarkupStream stream)
	{
		this.markup = stream.getMarkupFragments();
		this.mutable = false;

		int index = stream.getCurrentIndex();
		ComponentTag openTag = stream.getTag();
		while (stream.hasMore())
		{
			MarkupElement elem = stream.next();
			this.markupElements.add(elem);
			if (elem instanceof ComponentTag)
			{
				ComponentTag closeTag = (ComponentTag)elem;
				if (closeTag.closes(openTag))
				{
					break;
				}
			}
		}

		stream.setCurrentIndex(index);
	}

	/**
	 * For Wicket it would be sufficient for this method to be package
	 * protected. However to allow wicket-bench easy access to the information
	 * ...
	 * 
	 * @param index
	 *            Index into markup list
	 * @return Markup element
	 */
	public final MarkupElement get(final int index)
	{
		return markupElements.get(index);
	}

	/**
	 * Same as get(index), except that is returns a ComponentTag, of course
	 * assuming that 'index' references a ComponentTag.
	 * 
	 * @param index
	 * @return ComponentTag
	 */
	public final ComponentTag getTag()
	{
		final MarkupElement elem = get(0);
		if (elem instanceof ComponentTag)
		{
			return (ComponentTag)elem;
		}
		return null;
	}

	/**
	 * Get the wicket:id of the underlying tag
	 * 
	 * @return id
	 */
	public final String getId()
	{
		final ComponentTag tag = getTag();
		if (tag != null)
		{
			return tag.getId();
		}

		return null;
	}

	/**
	 * Get the markup fragment associated with the id. The id might as well be a
	 * path to get grand child markup.
	 * 
	 * @param id
	 *            The id of the child tag
	 * @param throwException
	 *            If true and tag was not found, than throw an exception
	 * @return Markup fragment
	 */
	public final MarkupFragment getChildFragment(final String id, final boolean throwException)
	{
		return getChildFragment(id, false, throwException);
	}

	/**
	 * Get the markup fragment associated with the id. The id might as well be a
	 * path to get grand child markup.
	 * 
	 * @param id
	 *            The id of the child tag
	 * @param ignoreFirstTag
	 *            If true, do not check the very first ComponentTag.
	 * @param throwException
	 *            If true and tag was not found, than throw an exception
	 * @return Markup fragment
	 */
	public final MarkupFragment getChildFragment(final String id, final boolean ignoreFirstTag,
			final boolean throwException)
	{
		if ((id == null) || (id.length() == 0))
		{
			return null;
		}

		if (ignoreFirstTag == false)
		{
			String tagId = getId();
			if ((tagId != null) && tagId.equals(id))
			{
				return this;
			}
		}

		// If id has not further path elements, than ...
		if (id.indexOf(Component.PATH_SEPARATOR) == -1)
		{
			// .. search all (immediate) child fragments
			for (MarkupElement elem : this)
			{
				if (elem instanceof MarkupFragment)
				{
					MarkupFragment fragment = (MarkupFragment)elem;
					String tagId = fragment.getId();
					if ((tagId != null) && tagId.equals(id))
					{
						return fragment;
					}

					/*
					 * if this component tag represents an auto component we
					 * need to recurse into it because auto components are
					 * transparent from the point of view of the markup path of
					 * a component
					 * 
					 * eg <wicket:extend> generates an auto component that is
					 * not in the markup path expressions
					 */
					if ((tagId != null) && tagId.startsWith(Component.AUTO_COMPONENT_PREFIX))
					{
						MarkupFragment frag = fragment.getChildFragment(id, false);
						if (frag != null)
						{
							return frag;
						}
					}
				}
			}
		}
		else
		{
			// Split the 'id' into the first element (which is the immediate
			// child) and the remaining path. Get the immediate child and
			// recursively call getChildFragment() with the remaining path ids.
			String root = Strings.firstPathComponent(id, Component.PATH_SEPARATOR);
			MarkupFragment child = getChildFragment(root, false);
			if (child != null)
			{
				String remainingPath = Strings.afterFirst(id, Component.PATH_SEPARATOR);
				return child.getChildFragment(remainingPath, throwException);
			}
		}

		if (throwException == true)
		{
			throw new MarkupException(getMarkup().getResource(), "Markup with path '" + id
					+ "' not found in fragment: " + getId());
		}

		return null;
	}

	/**
	 * Recursively search for a Wicket tag with 'name', such as wicket:panel,
	 * wicket:border, etc.
	 * 
	 * @param name
	 *            Such as "panel"
	 * @param throwException
	 *            If true and tag not found, than throw an exception
	 * @return Null, if not found
	 */
	public final MarkupFragment getWicketFragment(final String name, final boolean throwException)
	{
		ComponentTag tag = getTag();
		if (tag != null)
		{
			if (tag.isWicketTag(name))
			{
				return this;
			}
		}

		for (MarkupElement elem : this)
		{
			if (elem instanceof MarkupFragment)
			{
				MarkupFragment fragment = (MarkupFragment)elem;
				fragment = fragment.getWicketFragment(name, false);
				if (fragment != null)
				{
					return fragment;
				}
			}
		}

		if (throwException == true)
		{
			throw new MarkupException(new MarkupStream(this), "Wicket tag with name 'wicket:"
					+ name + "' not found.");
		}
		return null;
	}

	/**
	 * Gets the associate markup
	 * 
	 * @return The associated markup
	 */
	public final IMarkup getMarkup()
	{
		return this.markup;
	}

	/**
	 * For Wicket it would be sufficient for this method to be package
	 * protected. However to allow wicket-bench easy access to the information
	 * ...
	 * 
	 * @return Number of markup elements
	 */
	public int size()
	{
		return this.markupElements.size();
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param markupElement
	 */
	public final void addMarkupElement(final MarkupElement markupElement)
	{
		this.markupElements.add(markupElement);
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param pos
	 * @param markupElement
	 */
	public final void addMarkupElement(final int pos, final MarkupElement markupElement)
	{
//		if ((markupElement instanceof ComponentTag) && (pos > 0) && (pos < size()))
//		{
//			throw new WicketRuntimeException(
//					"ComponentTag's within a MarkupFragment can only be at the first or last position: "
//							+ markupElement.toUserDebugString());
//		}
		this.markupElements.add(pos, markupElement);
	}

	/**
	 * Remove the element from the list
	 * 
	 * @param index
	 * @return the element removed
	 */
	public final MarkupElement removeMarkupElement(final int index)
	{
		return this.markupElements.remove(index);
	}

	/**
	 * Remove the element from the list
	 * 
	 * @param element
	 * @return true, if removed
	 */
	public final boolean removeMarkupElement(final MarkupElement element)
	{
		return this.markupElements.remove(element);
	}

	/**
	 * Make all tags immutable and the list of elements unmodifable.
	 */
	public final void makeImmutable()
	{
		for (MarkupElement elem : this)
		{
			if (elem instanceof ComponentTag)
			{
				// Make the tag immutable
				((ComponentTag)elem).makeImmutable();
			}
			else if (elem instanceof MarkupFragment)
			{
				((MarkupFragment)elem).makeImmutable();
			}
		}
		if (mutable)
		{
			this.markupElements = Collections.unmodifiableList(this.markupElements);
			mutable = false;
		}
	}

	/**
	 * Create a copy of the whole fragment tree including all sub-fragments
	 * 
	 * @return MarkupFragment
	 */
	public final MarkupFragment makeCopy()
	{
		final MarkupFragment fragment = new MarkupFragment(this.markup);
		for (MarkupElement element : this)
		{
			if (element instanceof MarkupFragment)
			{
				fragment.addMarkupElement(((MarkupFragment)element).makeCopy());
			}
			else
			{
				fragment.addMarkupElement(element);
			}
		}

		return fragment;
	}

	/**
	 * Iterator for MarkupElements
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public final Iterator<MarkupElement> iterator()
	{
		return new Iterator<MarkupElement>()
		{
			private int index = 0;

			public boolean hasNext()
			{
				return (index < size());
			}

			public MarkupElement next()
			{
				return get(index++);
			}

			public void remove()
			{
				throw new WicketRuntimeException(
						"remomve() not supported by MarkupFragment Iterator");
			}
		};
	}

	/**
	 * Flatten the hierarchy of MarkupFragments and RawMarkup and return a list
	 * of ComponentTag and RawMarkup
	 * 
	 * @return List
	 * @TODO This is a temporary helper which will be removed once work in
	 *       progress is completed
	 */
	public List<MarkupElement> getAllElementsFlat()
	{
		List<MarkupElement> elems = new ArrayList<MarkupElement>();

		for (MarkupElement elem : this.markupElements)
		{
			if (elem instanceof RawMarkup)
			{
				elems.add(elem);
			}
			else if (elem instanceof ComponentTag)
			{
				elems.add(elem);
			}
			else
			{
				elems.addAll(((MarkupFragment)elem).getAllElementsFlat());
			}
		}

		return elems;
	}

	/**
	 * Re-balance the markup tree which became out-of-balance due to unclosed
	 * HTML tags.
	 */
	public final void handleUnclosedTags()
	{
		for (int i = 0; i < size(); i++)
		{
			MarkupElement element = get(i);
			if (element instanceof MarkupFragment)
			{
				MarkupFragment frag = (MarkupFragment)element;
				if (frag.getTag().hasNoCloseTag())
				{
					while (frag.size() > 1)
					{
						addMarkupElement(i + 1, frag.removeMarkupElement(frag.size() - 1));
					}
				}
				else
				{
					frag.handleUnclosedTags();
				}
			}
		}
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
	public final Object visitChildren(final Class<? extends MarkupElement> clazz,
			final IVisitor visitor)
	{
		if (visitor == null)
		{
			throw new IllegalArgumentException("Argument visitor may not be null");
		}

		// Iterate through children of this container
		final Iterator iter = iterator();
		while (iter.hasNext())
		{
			MarkupElement element = (MarkupElement)iter.next();
			Object value = null;

			// Is the child of the correct class (or was no class specified)?
			if ((clazz == null) || clazz.isInstance(element))
			{
				// Call visitor
				value = visitor.visit(element, this);

				// If visitor returns a non-null value, it halts the traversal
				if ((value != IVisitor.CONTINUE_TRAVERSAL)
						&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
				{
					return value;
				}
			}

			// If child is a container
			if ((element instanceof MarkupFragment)
					&& (value != IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER))
			{
				// visit the children in the container
				value = ((MarkupFragment)element).visitChildren(clazz, visitor);

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
	 * Generic component visitor interface for component traversals.
	 */
	public static interface IVisitor
	{
		/**
		 * Value to return to continue a traversal.
		 */
		public static final Object CONTINUE_TRAVERSAL = null;

		/**
		 * A generic value to return to contiue a traversal, but if the
		 * component is a container, don't visit its children.
		 */
		public static final Object CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER = new Object();

		/**
		 * A generic value to return to stop a traversal.
		 */
		public static final Object STOP_TRAVERSAL = new Object();

		/**
		 * Called at each component in a traversal.
		 * 
		 * @param element
		 *            The markup element
		 * @param parent
		 *            The parent markup fragment
		 * @return CONTINUE_TRAVERSAL (null) if the traversal should continue,
		 *         or a non-null return value for the traversal method if it
		 *         should stop. If no return value is useful, the generic
		 *         non-null value STOP_TRAVERSAL can be used.
		 */
		public Object visit(MarkupElement element, MarkupFragment parent);
	}

	/**
	 * @return String representation of markup list
	 */
	@Override
	public final String toString()
	{
		return this.markupElements.toString();
	}

	/**
	 * @see wicket.markup.MarkupElement#toCharSequence()
	 */
	@Override
	public CharSequence toCharSequence()
	{
		final AppendingStringBuffer buf = new AppendingStringBuffer(500);
		for (MarkupElement elem : this)
		{
			buf.append(elem);
			buf.append(",");
		}
		return buf;
	}

	/**
	 * @see wicket.markup.MarkupElement#toUserDebugString()
	 */
	@Override
	public String toUserDebugString()
	{
		return get(0).toUserDebugString() + "; Resource: " + getMarkup().toString();
	}
}
