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
package wicket.markup.loader;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupFragment;

/**
 * Utilities for searching specific tag with a markup fragment tree structure
 * 
 * @author Juergen Donnerstag
 */
public class MarkupFragmentUtils
{
	/**
	 * Lookup result as returned by some methods
	 */
	public static class LookupResult
	{
		/** */
		public MarkupFragment fragment;

		/** */
		public int index;

		/**
		 * Construct.
		 * 
		 * @param fragment
		 * @param index
		 */
		public LookupResult(final MarkupFragment fragment, final int index)
		{
			this.fragment = fragment;
			this.index = index;
		}
	}

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(MarkupFragmentUtils.class);

	/**
	 * 
	 * @param fragment
	 * @return Null, if &ltbody&gt; was not found
	 */
	public static final MarkupFragment getBodyTag(final MarkupFragment fragment)
	{
		if (isBodyTag(fragment))
		{
			return fragment;
		}

		for (MarkupElement element : fragment)
		{
			if (element instanceof MarkupFragment)
			{
				MarkupFragment frag = (MarkupFragment)element;
				frag = getBodyTag(frag);
				if (frag != null)
				{
					return frag;
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @param fragment
	 * @return Null, if no &lt;body&gt; tag was found
	 */
	public static final LookupResult getBodyTagPosition(final MarkupFragment fragment)
	{
		for (int i = 0; i < fragment.size(); i++)
		{
			MarkupElement element = fragment.get(i);
			if (element instanceof MarkupFragment)
			{
				MarkupFragment frag = (MarkupFragment)element;
				if (isBodyTag(frag))
				{
					return new LookupResult(fragment, i);
				}
				LookupResult result = getBodyTagPosition(frag);
				if (result != null)
				{
					return result;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the &lt;head&gt; fragment
	 * 
	 * @param fragment
	 * @return Null, if not found
	 */
	public static final MarkupFragment getHeadTag(final MarkupFragment fragment)
	{
		if (isHeadTag(fragment))
		{
			return fragment;
		}

		for (MarkupElement element : fragment)
		{
			if (element instanceof MarkupFragment)
			{
				MarkupFragment frag = (MarkupFragment)element;
				frag = getHeadTag(frag);
				if (frag != null)
				{
					return frag;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the parent fragment of &lt;head&gt; and the position of the
	 * &lt;head&gt; tag
	 * 
	 * @param fragment
	 * @return Null, if no &lt;head&gt; was found
	 */
	public static final LookupResult getHeadTagPosition(final MarkupFragment fragment)
	{
		for (int i = 0; i < fragment.size(); i++)
		{
			MarkupElement element = fragment.get(i);
			if (element instanceof MarkupFragment)
			{
				MarkupFragment frag = (MarkupFragment)element;
				if (isHeadTag(frag))
				{
					return new LookupResult(frag, frag.size() - 1);
				}
				LookupResult result = getHeadTagPosition(frag);
				if (result != null)
				{
					return result;
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @param fragment
	 * @return Null, if no wicket:child was found
	 */
	public static final MarkupFragment getWicketChildTag(final MarkupFragment fragment)
	{
		// Search for wicket:child. Make sure it is the wicket:child of the base
		// markup and not the base-base markup
		final MarkupFragment holder[] = new MarkupFragment[1];
		if (isChildTag(fragment))
		{
			holder[0] = fragment;
		}

		fragment.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
		{
			public Object visit(final MarkupElement element, final MarkupFragment parent)
			{
				MarkupFragment fragment = (MarkupFragment)element;
				if (isChildTag(fragment))
				{
					holder[0] = fragment;
				}
				return CONTINUE_TRAVERSAL;
			}
		});

		return holder[0];
	}

	/**
	 * 
	 * @param fragment
	 * @return Null, if no wicket:extend was found
	 */
	public static final MarkupFragment getWicketExtendTag(final MarkupFragment fragment)
	{
		if (isWicketExtendTag(fragment))
		{
			return fragment;
		}

		for (MarkupElement element : fragment)
		{
			if (element instanceof MarkupFragment)
			{
				MarkupFragment frag = (MarkupFragment)element;
				frag = getWicketExtendTag(frag);
				if (frag != null)
				{
					return frag;
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @param fragment
	 * @return A list of all wicket:head fragments found
	 */
	public static final List<MarkupFragment> getWicketHeaders(final MarkupFragment fragment)
	{
		final List<MarkupFragment> headers = new ArrayList<MarkupFragment>();

		fragment.visitChildren(MarkupFragment.class, new MarkupFragment.IVisitor()
		{
			public Object visit(final MarkupElement element, final MarkupFragment parent)
			{
				MarkupFragment fragment = (MarkupFragment)element;
				if (isWicketHeadTag(fragment))
				{
					headers.add(fragment);
					return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
				}
				return CONTINUE_TRAVERSAL;
			}
		});

		return (headers.size() == 0 ? null : headers);
	}

	/**
	 * Gets the &lt;wicket:head&gt; markup
	 * 
	 * @param fragment
	 * @return Null, if no wicket:head found
	 */
	public static final LookupResult getWicketHeadTagPosition(final MarkupFragment fragment)
	{
		if (isWicketHeadTag(fragment))
		{
			return new LookupResult(fragment, 0);
		}

		for (int i = 0; i < fragment.size(); i++)
		{
			MarkupElement elem = fragment.get(i);
			if (elem instanceof MarkupFragment)
			{
				MarkupFragment frag = (MarkupFragment)elem;
				if (isWicketHeadTag(frag))
				{
					return new LookupResult(fragment, i + 1);
				}
				return getWicketHeadTagPosition(frag);
			}
		}

		return null;
	}

	/**
	 * 
	 * @param fragment
	 * @return True, if fragment is a &lt;body&gt; tag
	 */
	public static final boolean isBodyTag(final MarkupFragment fragment)
	{
		MarkupElement elem = fragment.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag tag = (ComponentTag)elem;
			if (tag.isBodyTag())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param fragment
	 * @return True, if fragment is a &lt;wicket:child&gt; tag
	 */
	public static final boolean isChildTag(final MarkupFragment fragment)
	{
		MarkupElement elem = fragment.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag tag = (ComponentTag)elem;
			if (tag.isChildTag())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param fragment
	 * @return True, if fragment is a &lt;head&gt; tag
	 */
	public static final boolean isHeadTag(final MarkupFragment fragment)
	{
		MarkupElement elem = fragment.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag tag = (ComponentTag)elem;
			if (tag.isHeadTag())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param fragment
	 * @return True, if fragment is a &lt;wicket:extend&gt;
	 */
	public static final boolean isWicketExtendTag(final MarkupFragment fragment)
	{
		MarkupElement elem = fragment.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag tag = (ComponentTag)elem;
			if (tag.isExtendTag())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param fragment
	 * @return True, if fragment is a &lt;wicket:head&gt; tag
	 */
	public static final boolean isWicketHeadTag(final MarkupFragment fragment)
	{
		MarkupElement elem = fragment.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag tag = (ComponentTag)elem;
			if (tag.isWicketHeadTag())
			{
				return true;
			}
		}

		return false;
	}
}
