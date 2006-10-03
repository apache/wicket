/*
 * $Id: MarkupFragment.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
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
package wicket.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;

/**
 * A list of markup elements associated with a Markup. Might be all elements of
 * a markup resource, might be just the elements associated with a specific tag.
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
	private/* final */List<MarkupElement> markupElements;

	/** The associate markup */
	private final IMarkup markup;

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
		this.markupElements = new ArrayList<MarkupElement>();

		if (openTag != null)
		{
			this.markupElements.add(openTag);
		}
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
	public final ComponentTag getTag(final int index)
	{
		return (ComponentTag)markupElements.get(index);
	}

	/**
	 * Get the wicket:id of the underlying tag
	 * 
	 * @return id
	 */
	public final String getId()
	{
		MarkupElement elem = get(0);
		if (elem instanceof ComponentTag)
		{
			return ((ComponentTag)elem).getId();
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

		String tagId = getId();
		if ((tagId != null) && tagId.equals(id))
		{
			return this;
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
					tagId = fragment.getId();
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
					if (tagId.startsWith(Component.AUTO_COMPONENT_PREFIX))
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
			throw new MarkupException("Markup with path '" + id + "' not found in fragment: "
					+ this.getId());
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
		MarkupElement element = get(0);
		if (element instanceof ComponentTag)
		{
			ComponentTag tag = getTag(0);
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
			throw new MarkupException("Wicket tag with name 'wicket:" + name + "' not found");
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
		return markupElements.size();
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
	 * Remove the element from the list
	 * 
	 * @param elem
	 */
	public final void removeMarkupElement(final MarkupElement elem)
	{
		this.markupElements.remove(elem);
	}

	/**
	 * Add a MarkupElement
	 * 
	 * @param pos
	 * @param markupElement
	 */
	public final void addMarkupElement(final int pos, final MarkupElement markupElement)
	{
		this.markupElements.add(pos, markupElement);
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

		this.markupElements = Collections.unmodifiableList(this.markupElements);
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
	 * MarkupParser until now creates a flat list of RawMarkup and ComponentTag
	 * elements. However, what we want is a tree like structure with one
	 * fragment per Component.
	 * 
	 * @return MarkupFragment A converted copy of the fragment
	 */
	public final MarkupFragment translateFlatIntoTreeStructure()
	{
		MarkupFragment rootFragment = new MarkupFragment(this.getMarkup());

		// Remember the path associated with a ComponentTag to properly walk up
		// and down the hierarchy of wicket markup tags
		Stack<String> stack = new Stack<String>();
		String basePath = null;

		Stack<MarkupFragment> fragmentStack = new Stack<MarkupFragment>();
		MarkupFragment current = rootFragment;

		// For all markup element in the external markup file
		for (MarkupElement elem : this.markupElements)
		{
			// If RawMarkup simply add the element to the current fragment
			if (elem instanceof RawMarkup)
			{
				current.addMarkupElement(elem);
			}
			else
			// if (elem instanceof ComponentTag)
			{
				// Construct the markup path for the tag
				final ComponentTag tag = (ComponentTag)elem;
				final String path = (basePath == null ? tag.getId() : basePath
						+ Component.PATH_SEPARATOR + tag.getId());

				// Depending on tag type (open, close, open-close) ...
				if (tag.isOpen())
				{
					// Open tags with no close tags (HTML) are treated like
					// open-close.
					if (tag.hasNoCloseTag())
					{
						current.addMarkupElement(new MarkupFragment(this.getMarkup(), tag));
					}
					else
					{
						// If open tag and auto component (BODY, HEAD, etc.)
						// than the markup path gets not updated as the markup
						// for BODY e.g. does not have a wicket:id.

						stack.push(basePath);
						fragmentStack.push(current);

						MarkupFragment newFragment = new MarkupFragment(this.getMarkup(), tag);
						current.addMarkupElement(newFragment);
						current = newFragment;

						if (!tag.getId().startsWith(Component.AUTO_COMPONENT_PREFIX))
						{
							basePath = path;
						}
					}
				}
				else if (tag.isOpenClose())
				{
					MarkupFragment newFragment = new MarkupFragment(this.getMarkup(), tag);
					current.addMarkupElement(newFragment);
				}
				else
				// if (tag.isClose()
				{
					current.addMarkupElement(tag);
					current = fragmentStack.pop();
					basePath = stack.pop();
				}
			}
		}

		if ((rootFragment.size() == 1) && (rootFragment.get(0) instanceof MarkupFragment))
		{
			rootFragment = (MarkupFragment)rootFragment.get(0);
		}

		return rootFragment;
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
		for (MarkupElement element : this)
		{
			Object value = null;

			// Is the child of the correct class (or was no class specified)?
			if ((clazz == null) || clazz.isInstance(element))
			{
				// Call visitor
				value = visitor.visit(element);

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
		 * @return CONTINUE_TRAVERSAL (null) if the traversal should continue,
		 *         or a non-null return value for the traversal method if it
		 *         should stop. If no return value is useful, the generic
		 *         non-null value STOP_TRAVERSAL can be used.
		 */
		public Object visit(MarkupElement element);
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
		return toString();
	}
}
