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
package org.apache.wicket.markup.parser;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;



/**
 * The interface of a streaming XML parser as required by Wicket.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public interface IXmlPullParser
{
	/** The last element found */
	public enum HttpTagType {
		/** next() must be called at least once for the Type to be valid */
		NOT_INITIALIZED,

		/** <name ...> */
		TAG,

		/** Tag body in between two tags */
		BODY,

		/** <!-- ... --> */
		COMMENT,

		/** <!--[if ] ... --> */
		CONDITIONAL_COMMENT,

		/** <![endif]--> */
		CONDITIONAL_COMMENT_ENDIF,

		/** <![CDATA[ .. ]]> */
		CDATA,

		/** <?...> */
		PROCESSING_INSTRUCTION,

		/** <!DOCTYPE ...> */
		DOCTYPE,

		/** all other tags which look like <!.. > */
		SPECIAL_TAG,
	}

	/**
	 * Return the encoding applied while reading the markup resource. The encoding is determined by
	 * analyzing the &lt;?xml version=".." encoding=".." ?&gt; tag.
	 * 
	 * @return if null, JVM defaults have been used.
	 */
	String getEncoding();

	/**
	 * Gets the &lt;!DOCTYPE ...&gt; tag if found in the markup
	 * 
	 * @return Null, if not found
	 */
	CharSequence getDoctype();

	/**
	 * Wicket dissects the markup into Wicket relevant tags and raw markup, which is not further
	 * analyzed by Wicket. The method getInputFromPositionMarker() is used to access the raw markup.
	 * 
	 * @param toPos
	 *            To position
	 * @return The raw markup in between the position marker and toPos
	 */
	CharSequence getInputFromPositionMarker(int toPos);

	/**
	 * Wicket dissects the markup into Wicket relevant tags and raw markup, which is not further
	 * analyzed by Wicket. The getInputSubsequence() method is used to access the raw markup.
	 * 
	 * @param fromPos
	 *            From position
	 * @param toPos
	 *            To position
	 * @return The raw markup in between fromPos and toPos
	 */
	CharSequence getInput(final int fromPos, final int toPos);

	/**
	 * Parse the markup provided. Use nextTag() to access the tags contained one after another.
	 * <p>
	 * Note: xml character encoding is NOT applied. It is assumed the input provided does have the
	 * correct encoding already.
	 * 
	 * @param string
	 *            The markup to be parsed
	 * @throws IOException
	 *             Error while reading the resource
	 */
	void parse(final CharSequence string) throws IOException;

	/**
	 * Reads and parses markup from an input stream, using UTF-8 encoding by default when not
	 * specified in XML declaration. Use nextTag() to access the tags contained, one after another.
	 * 
	 * @param inputStream
	 *            The input stream to read and parse
	 * @throws IOException
	 *             Error while reading the resource
	 */
	void parse(final InputStream inputStream) throws IOException;

	/**
	 * Reads and parses markup from an input stream. Use nextTag() to access the tags contained, one
	 * after another.
	 * 
	 * @param inputStream
	 *            A resource like e.g. a file
	 * @param encoding
	 *            Use null to apply JVM/OS default
	 * @throws IOException
	 *             Error while reading the resource
	 */
	void parse(InputStream inputStream, final String encoding) throws IOException;

	/**
	 * Move to the next XML element
	 * 
	 * @return o, if end of file. Else a TAG, COMMENT etc.
	 * @throws ParseException
	 */
	HttpTagType next() throws ParseException;

	/**
	 * 
	 * @return The current element
	 */
	XmlTag getElement();

	/**
	 * @return The xml string from the last element
	 */
	CharSequence getString();

	/**
	 * Set the position marker of the markup at the current position.
	 */
	void setPositionMarker();

	/**
	 * Set the position marker of the markup
	 * 
	 * @param pos
	 */
	void setPositionMarker(final int pos);
}
