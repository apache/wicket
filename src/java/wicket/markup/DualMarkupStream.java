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
package wicket.markup;

import wicket.util.resource.IResourceStream;


/**
 * THIS IS PART OF MARKUP INHERITANCE AND CURRENTLY EXPERIMENTAL ONLY.
 * 
 * This markup stream read markup from two different source markup stream
 * with a switch to swap the current reader stream.
 * 
 * @author Juergen Donnerstag
 */
public class DualMarkupStream extends MarkupStream
{
    /** Array of markup source streams */
	private final MarkupStream[] markupStreams = new MarkupStream[2];

	/** The current markup stream used */
	private MarkupStream current;

	/**
	 * Construct.
	 * 
	 * @param stream1 Source stream 1
	 * @param stream2 Source stream 2
	 */
	public DualMarkupStream(final MarkupStream stream1, final MarkupStream stream2)
	{
		markupStreams[0] = stream1;
		markupStreams[1] = stream2;

		current = stream1;
	}

	/**
	 * Swap the current markup source stream
	 */
	public void swapCurrentMarkupStream()
	{
		if (current == markupStreams[0])
		{
			current = markupStreams[1];
		}
		else
		{
			current = markupStreams[0];
		}
	}

	/**
	 * Get the current markup source stream
	 * 
	 * @return The current markup stream
	 */
	public MarkupStream getCurrentMarkupStream()
	{
	    return current;
	}
	
	/**
	 * @see wicket.markup.MarkupStream#atCloseTag()
	 */
	public boolean atCloseTag()
	{
		return current.atCloseTag();
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenCloseTag()
	 */
	public boolean atOpenCloseTag()
	{
		return current.atOpenCloseTag();
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenCloseTag(java.lang.String)
	 */
	public boolean atOpenCloseTag(String componentId)
	{
		return current.atOpenCloseTag(componentId);
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenTag()
	 */
	public boolean atOpenTag()
	{
		return current.atOpenTag();
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenTag(java.lang.String)
	 */
	public boolean atOpenTag(String componentId)
	{
		return current.atOpenTag(componentId);
	}

	/**
	 * @see wicket.markup.MarkupStream#atTag()
	 */
	public boolean atTag()
	{
		return current.atTag();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0)
	{
		return current.equals(arg0);
	}

	/**
	 * @see wicket.markup.MarkupStream#get()
	 */
	public MarkupElement get()
	{
		return current.get();
	}

	/**
	 * @see wicket.markup.MarkupStream#getCurrentIndex()
	 */
	public int getCurrentIndex()
	{
		return current.getCurrentIndex();
	}

	/**
	 * @see wicket.markup.MarkupStream#getResource()
	 */
	public IResourceStream getResource()
	{
		return current.getResource();
	}

	/**
	 * @see wicket.markup.MarkupStream#getTag()
	 */
	public ComponentTag getTag()
	{
		return current.getTag();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return current.hashCode();
	}

	/**
	 * @see wicket.markup.MarkupStream#hasMore()
	 */
	public boolean hasMore()
	{
		return current.hasMore();
	}

	/**
	 * @see wicket.markup.MarkupStream#next()
	 */
	public MarkupElement next()
	{
		return current.next();
	}

	/**
	 * @see wicket.markup.MarkupStream#setCurrentIndex(int)
	 */
	public void setCurrentIndex(int currentIndex)
	{
		current.setCurrentIndex(currentIndex);
	}

	/**
	 * @see wicket.markup.MarkupStream#skipRawMarkup()
	 */
	public void skipRawMarkup()
	{
		current.skipRawMarkup();
	}

	/**
	 * @see wicket.markup.MarkupStream#throwMarkupException(java.lang.String)
	 */
	public void throwMarkupException(String message)
	{
		current.throwMarkupException(message);
	}

	/**
	 * @see wicket.markup.MarkupStream#toHtmlDebugString()
	 */
	public String toHtmlDebugString()
	{
		return current.toHtmlDebugString();
	}

	/**
	 * @see wicket.markup.MarkupStream#toString()
	 */
	public String toString()
	{
		return current.toString();
	}
}
