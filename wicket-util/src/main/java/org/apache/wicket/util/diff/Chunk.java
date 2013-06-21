/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowledgement: "This product includes software
 * developed by the Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgement may appear in the software itself, if and
 * wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived from this
 * software without prior written permission. For written permission, please
 * contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" nor may
 * "Apache" appear in their names without prior written permission of the Apache
 * Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE APACHE
 * SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the Apache Software Foundation. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 *
 */

package org.apache.wicket.util.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds a information about a part of the text involved in a differencing or patching operation.
 * 
 * @version $Id: Chunk.java,v 1.1 2006/03/12 00:24:21 juanca Exp $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @see Diff
 * @see Delta
 */
public class Chunk extends ToString
{

	protected int anchor;

	protected int count;

	protected List<Object> chunk;

	/**
	 * Creates a chunk that doesn't copy the original text.
	 * 
	 * @param pos
	 *            the start position in the text.
	 * @param count
	 *            the size of the chunk.
	 */
	public Chunk(final int pos, final int count)
	{
		anchor = pos;
		this.count = (count >= 0 ? count : 0);
	}

	/**
	 * Creates a chunk and saves a copy the original chunk's text.
	 * 
	 * @param iseq
	 *            the original text.
	 * @param pos
	 *            the start position in the text.
	 * @param count
	 *            the size of the chunk.
	 */
	public Chunk(final Object[] iseq, final int pos, final int count)
	{
		this(pos, count);
		chunk = slice(iseq, pos, count);
	}

	/**
	 * Creates a chunk that will be displaced in the resulting text, and saves a copy the original
	 * chunk's text.
	 * 
	 * @param iseq
	 *            the original text.
	 * @param pos
	 *            the start position in the text.
	 * @param count
	 *            the size of the chunk.
	 * @param offset
	 *            the position the chunk should have in the resulting text.
	 */
	public Chunk(final Object[] iseq, final int pos, final int count, final int offset)
	{
		this(offset, count);
		chunk = slice(iseq, pos, count);
	}

	/**
	 * Creates a chunk and saves a copy the original chunk's text.
	 * 
	 * @param iseq
	 *            the original text.
	 * @param pos
	 *            the start position in the text.
	 * @param count
	 *            the size of the chunk.
	 */
	public Chunk(final List<Object> iseq, final int pos, final int count)
	{
		this(pos, count);
		chunk = slice(iseq, pos, count);
	}

	/**
	 * Creates a chunk that will be displaced in the resulting text, and saves a copy the original
	 * chunk's text.
	 * 
	 * @param iseq
	 *            the original text.
	 * @param pos
	 *            the start position in the text.
	 * @param count
	 *            the size of the chunk.
	 * @param offset
	 *            the position the chunk should have in the resulting text.
	 */
	public Chunk(final List<Object> iseq, final int pos, final int count, final int offset)
	{
		this(offset, count);
		chunk = slice(iseq, pos, count);
	}

	/**
	 * Returns the anchor position of the chunk.
	 * 
	 * @return the anchor position.
	 */
	public int anchor()
	{
		return anchor;
	}

	/**
	 * Returns the size of the chunk.
	 * 
	 * @return the size.
	 */
	public int size()
	{
		return count;
	}

	/**
	 * Returns the index of the first line of the chunk.
	 * 
	 * @return int
	 */
	public int first()
	{
		return anchor();
	}

	/**
	 * Returns the index of the last line of the chunk.
	 * 
	 * @return int
	 */
	public int last()
	{
		return anchor() + size() - 1;
	}

	/**
	 * Returns the <i>from</i> index of the chunk in RCS terms.
	 * 
	 * @return int
	 */
	public int rcsfrom()
	{
		return anchor + 1;
	}

	/**
	 * Returns the <i>to</i> index of the chunk in RCS terms.
	 * 
	 * @return int
	 */
	public int rcsto()
	{
		return anchor + count;
	}

	/**
	 * Returns the text saved for this chunk.
	 * 
	 * @return the text.
	 */
	public List<Object> chunk()
	{
		return chunk;
	}

	/**
	 * Verifies that this chunk's saved text matches the corresponding text in the given sequence.
	 * 
	 * @param target
	 *            the sequence to verify against.
	 * @return true if the texts match.
	 */
	public boolean verify(final List<Object> target)
	{
		if (chunk == null)
		{
			return true;
		}
		if (last() > target.size())
		{
			return false;
		}
		for (int i = 0; i < count; i++)
		{
			if (!target.get(anchor + i).equals(chunk.get(i)))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Delete this chunk from he given text.
	 * 
	 * @param target
	 *            the text to delete from.
	 */
	public void applyDelete(final List<Object> target)
	{
		for (int i = last(); i >= first(); i--)
		{
			target.remove(i);
		}
	}

	/**
	 * Add the text of this chunk to the target at the given position.
	 * 
	 * @param start
	 *            where to add the text.
	 * @param target
	 *            the text to add to.
	 */
	public void applyAdd(int start, final List<Object> target)
	{
		for (Object aChunk : chunk)
		{
			target.add(start++, aChunk);
		}
	}

	/**
	 * Provide a string image of the chunk using the an empty prefix and postfix.
	 * 
	 * @param s
	 */
	@Override
	public void toString(final StringBuilder s)
	{
		toString(s, "", "");
	}

	/**
	 * Provide a string image of the chunk using the given prefix and postfix.
	 * 
	 * @param s
	 *            where the string image should be appended.
	 * @param prefix
	 *            the text that should prefix each line.
	 * @param postfix
	 *            the text that should end each line.
	 * @return StringBuilder
	 */
	public StringBuilder toString(final StringBuilder s, final String prefix, final String postfix)
	{
		if (chunk != null)
		{
			for (Object aChunk : chunk)
			{
				s.append(prefix);
				s.append(aChunk);
				s.append(postfix);
			}
		}
		return s;
	}

	/**
	 * Retrieves the specified part from a {@link List List}.
	 * 
	 * @param <T>
	 *            the type of objects contained in <code>seq</code>
	 * 
	 * @param seq
	 *            the list to retrieve a slice from.
	 * @param pos
	 *            the start position.
	 * @param count
	 *            the number of items in the slice.
	 * @return a {@link List List} containing the specified items.
	 */
	public static <T> List<T> slice(final List<T> seq, final int pos, final int count)
	{
		if (count <= 0)
		{
			return new ArrayList<>();
		}
		else
		{
			return new ArrayList<>(seq.subList(pos, pos + count));
		}
	}

	/**
	 * Retrieves a slice from an {@link Object Object} array.
	 * 
	 * @param seq
	 *            the list to retrieve a slice from.
	 * @param pos
	 *            the start position.
	 * @param count
	 *            the number of items in the slice.
	 * @return a {@link List List} containing the specified items.
	 */
	public static List<Object> slice(final Object[] seq, final int pos, final int count)
	{
		return slice(Arrays.asList(seq), pos, count);
	}

	/**
	 * Provide a string representation of the numeric range of this chunk.
	 * 
	 * @return String
	 */
	public String rangeString()
	{
		StringBuilder result = new StringBuilder();
		rangeString(result);
		return result.toString();
	}

	/**
	 * Provide a string representation of the numeric range of this chunk.
	 * 
	 * @param s
	 *            where the string representation should be appended.
	 */
	public void rangeString(final StringBuilder s)
	{
		rangeString(s, ",");
	}

	/**
	 * Provide a string representation of the numeric range of this chunk.
	 * 
	 * @param s
	 *            where the string representation should be appended.
	 * @param separ
	 *            what to use as line separator.
	 */
	public void rangeString(final StringBuilder s, final String separ)
	{
		if (size() <= 1)
		{
			s.append(Integer.toString(rcsfrom()));
		}
		else
		{
			s.append(Integer.toString(rcsfrom()));
			s.append(separ);
			s.append(Integer.toString(rcsto()));
		}
	}
}