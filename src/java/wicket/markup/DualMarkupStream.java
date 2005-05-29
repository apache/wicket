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

import java.util.Stack;

import wicket.util.resource.IResourceStream;


/**
 * THIS IS PART OF MARKUP INHERITANCE AND CURRENTLY EXPERIMENTAL ONLY.
 * 
 * This markup stream read markup from two different source markup stream
 * with a switch to swap the current reader stream.
 * 
 * How does this markup stream support nested inheritance of markup streams.
 * Assuming you have three classes derived from each other: A => B => C. "C"
 * being subclass of "B" and "B" being derived from "A". The output generated
 * must be like shown below, with additional comments added to better when and
 * why to switch between the different markup stream involved.
 * 
 * "output"                    "active stream"   "comment"
 * class C (pre-text ignored)  Class C           init => push
 * <wicket:extend>             Class B           extend => push
 *   <wicket:extend>           Class A           extend => push
 *     class A                 Class A
 *     <wicket:child>          Class A           child => current -= 1
 *       class B               Class B
 *       <wicket:child>        Class B           child => current -= 1
 *         class C             Class C
 *       </wicket:child>       Class B           /child => current += 1
 *       class B end           Class B
 *     </wicket:child>         Class B           /child => current += 1
 *     class A end             Class A
 *   </wicket:extend>          Class A           /extend => pop
 * </wicket:extend>            Class B           /extend => pop
 * 
 * "current" being and index pointing to a stream relativ to the 
 * top (last added) one.
 * 
 * @author Juergen Donnerstag
 */
public class DualMarkupStream extends MarkupStream
{
    /** Array of markup source streams */
	private final Stack markupStreams = new Stack();

	/** The current markup stream used */
	private MarkupStream activeMarkupStream;

	/** relative index from the top of the stack, pointing to the 
	 * active markup stream
	 */
	private int index = 0;
	
	/**
	 * Construct.
	 * 
	 * @param stream Source stream
	 */
	public DualMarkupStream(final MarkupStream stream)
	{
		push(stream);
	}

	/**
	 * Set the active markup stream depending on size of stack and 
	 * a relative index.
	 */
	private void setActiveMarkupStream()
	{
		activeMarkupStream = (MarkupStream)markupStreams.elementAt(markupStreams.size() - this.index - 1);
	}
	
	/**
	 * Get the index pointing to the currently active markup stream
	 * @return index
	 */
	public int getMarkupStreamIndex()
	{
	    return this.index;
	}
	
	/**
	 * Add another markup stream on top of the stack. Depending on the 
	 * "index" the new active markup stream will be selected.
	 * 
	 * @param stream A markup stream
	 */
	public void push(final MarkupStream stream)
	{
	    markupStreams.push(stream);
	    setActiveMarkupStream();
	}

	/**
	 * Remove a markup stream from the top of the stack. Depending on the 
	 * "index" the new active markup stream will be selected.
	 */
	public void pop()
	{
	    markupStreams.pop();
	    setActiveMarkupStream();
	}

	/**
	 * Increment the index determining the active markup stream.
	 */
	public void incrementMarkupStreamIndex()
	{
	    index += 1;
	    if (index > markupStreams.size())
	    {
	        throw new MarkupException("index can not be larger than number of markup streams on the stack");
	    }
	    setActiveMarkupStream();
	}

	/**
	 * Decrement the index determining the active markup stream.
	 */
	public void decrementMarkupStreamIndex()
	{
	    index -= 1;
	    if (index < 0)
	    {
	        throw new MarkupException("index can not be less than 0");
	    }
	    setActiveMarkupStream();
	}
	
	/**
	 * Get the current markup source stream
	 * 
	 * @return The current markup stream
	 */
	public MarkupStream getCurrentMarkupStream()
	{
	    return activeMarkupStream;
	}
	
	/**
	 * @see wicket.markup.MarkupStream#atCloseTag()
	 */
	public boolean atCloseTag()
	{
		return activeMarkupStream.atCloseTag();
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenCloseTag()
	 */
	public boolean atOpenCloseTag()
	{
		return activeMarkupStream.atOpenCloseTag();
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenCloseTag(java.lang.String)
	 */
	public boolean atOpenCloseTag(String componentId)
	{
		return activeMarkupStream.atOpenCloseTag(componentId);
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenTag()
	 */
	public boolean atOpenTag()
	{
		return activeMarkupStream.atOpenTag();
	}

	/**
	 * @see wicket.markup.MarkupStream#atOpenTag(java.lang.String)
	 */
	public boolean atOpenTag(String componentId)
	{
		return activeMarkupStream.atOpenTag(componentId);
	}

	/**
	 * @see wicket.markup.MarkupStream#atTag()
	 */
	public boolean atTag()
	{
		return activeMarkupStream.atTag();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0)
	{
		return activeMarkupStream.equals(arg0);
	}

	/**
	 * @see wicket.markup.MarkupStream#get()
	 */
	public MarkupElement get()
	{
		return activeMarkupStream.get();
	}

	/**
	 * @see wicket.markup.MarkupStream#getCurrentIndex()
	 */
	public int getCurrentIndex()
	{
		return activeMarkupStream.getCurrentIndex();
	}

	/**
	 * @see wicket.markup.MarkupStream#getResource()
	 */
	public IResourceStream getResource()
	{
		return activeMarkupStream.getResource();
	}

	/**
	 * @see wicket.markup.MarkupStream#getTag()
	 */
	public ComponentTag getTag()
	{
		return activeMarkupStream.getTag();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return activeMarkupStream.hashCode();
	}

	/**
	 * @see wicket.markup.MarkupStream#hasMore()
	 */
	public boolean hasMore()
	{
		return activeMarkupStream.hasMore();
	}

	/**
	 * @see wicket.markup.MarkupStream#next()
	 */
	public MarkupElement next()
	{
		return activeMarkupStream.next();
	}

	/**
	 * @see wicket.markup.MarkupStream#setCurrentIndex(int)
	 */
	public void setCurrentIndex(int currentIndex)
	{
		activeMarkupStream.setCurrentIndex(currentIndex);
	}

	/**
	 * @see wicket.markup.MarkupStream#skipRawMarkup()
	 */
	public void skipRawMarkup()
	{
		activeMarkupStream.skipRawMarkup();
	}
	
	/**
	 * Get the component/container's Class which is directly associated with 
	 * the stream.
	 * 
	 * @return The component's class
	 */
	public Class getContainerClass()
	{
	    return activeMarkupStream.getContainerClass();
	}

	/**
	 * @see wicket.markup.MarkupStream#throwMarkupException(java.lang.String)
	 */
	public void throwMarkupException(String message)
	{
		activeMarkupStream.throwMarkupException(message);
	}

	/**
	 * @see wicket.markup.MarkupStream#toHtmlDebugString()
	 */
	public String toHtmlDebugString()
	{
		return activeMarkupStream.toHtmlDebugString();
	}

	/**
	 * @see wicket.markup.MarkupStream#toString()
	 */
	public String toString()
	{
		return activeMarkupStream.toString();
	}
}
