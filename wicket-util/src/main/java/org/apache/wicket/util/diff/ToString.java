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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * This class delegates handling of the to a StringBuilder based version.
 * 
 * @version $Revision: 1.1 $ $Date: 2006/03/12 00:24:21 $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 */
public class ToString
{
	/**
	 * Construct.
	 */
	public ToString()
	{
	}

	/**
	 * Default implementation of the {@link java.lang.Object#toString toString() } method that
	 * delegates work to a {@link java.lang.StringBuffer StringBuffer} base version.
	 * 
	 * @return String
	 */
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		toString(s);
		return s.toString();
	}

	/**
	 * Place a string image of the object in a StringBuffer.
	 * 
	 * @param s
	 *            the string buffer.
	 */
	public void toString(final StringBuilder s)
	{
		s.append(super.toString());
	}

	/**
	 * Breaks a string into an array of strings. Use the value of the <code>line.separator</code>
	 * system property as the linebreak character.
	 * 
	 * @param value
	 *            the string to convert.
	 * @return String[]
	 */
	public static String[] stringToArray(final String value)
	{
		BufferedReader reader = new BufferedReader(new StringReader(value));
		List<String> l = new LinkedList<>();
		String s;
		try
		{
			while ((s = reader.readLine()) != null)
			{
				l.add(s);
			}
		}
		catch (java.io.IOException ignored)
		{
		}
		return l.toArray(new String[l.size()]);
	}

	/**
	 * Converts an array of {@link Object Object} to a string Use the value of the
	 * <code>line.separator</code> system property the line separator.
	 * 
	 * @param o
	 *            the array of objects.
	 * @return String
	 */
	public static String arrayToString(final Object[] o)
	{
		return arrayToString(o, System.getProperty("line.separator"));
	}

	/**
	 * Converts an array of {@link Object Object} to a string using the given line separator.
	 * 
	 * @param o
	 *            the array of objects.
	 * @param EOL
	 *            the string to use as line separator.
	 * @return String
	 */
	public static String arrayToString(final Object[] o, final String EOL)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < o.length - 1; i++)
		{
			buf.append(o[i]);
			buf.append(EOL);
		}
		buf.append(o[o.length - 1]);
		return buf.toString();
	}
}
