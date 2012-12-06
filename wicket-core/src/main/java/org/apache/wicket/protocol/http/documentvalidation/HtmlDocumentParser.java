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
package org.apache.wicket.protocol.http.documentvalidation;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Lightweight document parser for HTML. This parser is only intended to process well formed and
 * simple HTML of the kind that would generally be utilized during testing.
 * 
 * @author Chris Turner
 */
public class HtmlDocumentParser
{
	private static final Logger log = LoggerFactory.getLogger(HtmlDocumentParser.class);

	/** Constant for close tag token. */
	public static final int CLOSE_TAG = 4;

	/** Constant for comment token. */
	public static final int COMMENT = 1;

	/** Constant for end token. */
	public static final int END = 0;

	/** Constant for open tag token. */
	public static final int OPEN_TAG = 2;

	/** Constant for open/close tag token. */
	public static final int OPENCLOSE_TAG = 3;

	/** Constant for text token. */
	public static final int TEXT = 5;

	/** constant for unknown token. */
	public static final int UNKNOWN = -1;

	private Map<String, String> attributes;

	/** Extracted content */
	private String comment;

	/** Document parse elements */
	private final String document;

	private int pos;

	private String tag;

	private String text;

	/**
	 * Create the parser for the current document.
	 * 
	 * @param document
	 *            The document to parse
	 */
	public HtmlDocumentParser(final String document)
	{
		CharSequence tmp = Strings.replaceAll(document, "\n", "");
		tmp = Strings.replaceAll(tmp, "\r", "");
		this.document = Strings.replaceAll(tmp, "\t", " ").toString();
		pos = 0;
	}

	/**
	 * Get the attributes of the tag.
	 * 
	 * @return The attributes
	 */
	public Map<String, String> getAttributes()
	{
		return attributes;
	}

	/**
	 * Get the comment.
	 * 
	 * @return The comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * Iterates through the document searching for tokens. Returns the type of token that was found.
	 * If an unexpected token was encountered then the parser writes this fact to the console and
	 * continues
	 * 
	 * @return The token that was found
	 */
	public int getNextToken()
	{
		if (pos < document.length())
		{
			char ch = document.charAt(pos);
			if (ch == '<')
			{
				return processDirective();
			}
			else
			{
				return processText();
			}
		}
		return END;
	}

	/**
	 * Get the tag name.
	 * 
	 * @return The tag name
	 */
	public String getTag()
	{
		return tag;
	}

	/**
	 * Get the text.
	 * 
	 * @return The text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Extract attributes from the given string.
	 * 
	 * @param attributeString
	 *            The string
	 * @return The map of attributes
	 */
	private Map<String, String> extractAttributes(String attributeString)
	{
		Map<String, String> m = new HashMap<String, String>();
		attributeString = Strings.replaceAll(attributeString.trim(), "\t", " ").toString();
		attributeString = Strings.replaceAll(attributeString, " = ", "=").toString();
		String[] attributeElements = attributeString.split(" ");
		for (String attributeElement : attributeElements)
		{
			String[] bits = attributeElement.split("=");
			if (bits.length == 1)
			{
				m.put(bits[0].trim().toLowerCase(), "");
			}
			else
			{
				bits[0] = bits[0].trim();
				StringBuilder value = new StringBuilder();
				for (int j = 1; j < bits.length; j++)
				{
					value.append(bits[j]);
					if (j < (bits.length - 1))
					{
						value.append('=');
					}
				}
				bits[1] = value.toString().trim();
				if (bits[1].startsWith("\""))
				{
					bits[1] = bits[1].substring(1);
				}
				if (bits[1].endsWith("\""))
				{
					bits[1] = bits[1].substring(0, bits[1].length() - 1);
				}
				m.put(bits[0].toLowerCase(), bits[1]);
			}
		}
		return m;
	}

	/**
	 * Process a directive starting at the current position.
	 * 
	 * @return The token found
	 */
	private int processDirective()
	{
		String part = document.substring(pos);
		if (part.matches("<!--.*-->.*"))
		{
			// This is a comment
			comment = part.substring(4, part.indexOf("-->")).trim();
			pos += part.indexOf("-->") + 3;
			return COMMENT;
		}
		else if (part.matches("</.*>.*"))
		{
			// This is a closing tag
			tag = part.substring(2, part.indexOf('>')).trim().toLowerCase();
			pos += part.indexOf(">") + 1;
			return CLOSE_TAG;
		}
		else if (part.matches("<[^/]+[^>]*/>.*"))
		{
			// This is an openclose tag
			if (part.matches("<([a-zA-Z]+:)?[a-zA-Z]+/>.*"))
			{
				// No attributes
				tag = part.substring(1, part.indexOf("/>")).toLowerCase();
				attributes = new HashMap<String, String>();
			}
			else
			{
				// Attributes
				tag = part.substring(1, part.indexOf(' ')).toLowerCase();
				String attributeString = part.substring(part.indexOf(' '), part.indexOf("/>"));
				attributes = extractAttributes(attributeString);
			}
			pos += part.indexOf("/>") + 2;
			return OPENCLOSE_TAG;
		}
		else if (part.matches("<[^/>]+.*>.*"))
		{
			// This is an opening tag
			if (part.matches("<([a-zA-Z]+:)?[a-zA-Z0-9]*>.*"))
			{
				// No attributes
				tag = part.substring(1, part.indexOf('>')).toLowerCase();
				attributes = new HashMap<String, String>();
			}
			else
			{
				// Attributes
				tag = part.substring(1, part.indexOf(' ')).toLowerCase();
				String attributeString = part.substring(part.indexOf(' '), part.indexOf('>'));
				attributes = extractAttributes(attributeString);
			}
			pos += part.indexOf(">") + 1;
			return OPEN_TAG;
		}
		else
		{
			int size = (part.length() > 30) ? 30 : part.length();
			log.error("Unexpected markup found: " + part.substring(0, size) + "...");
			return UNKNOWN;
		}
	}

	/**
	 * Process text up to the next token.
	 * 
	 * @return The token code
	 */
	private int processText()
	{
	 StringBuilder buf = new StringBuilder();
		while (pos < document.length())
		{
			char ch = document.charAt(pos);
			if (ch == '<')
			{
				text = buf.toString();
				return TEXT;
			}
			else
			{
				buf.append(ch);
			}
			pos++;
		}
		if (buf.length() > 0)
		{
			text = buf.toString();
			return TEXT;
		}
		return END;
	}
}