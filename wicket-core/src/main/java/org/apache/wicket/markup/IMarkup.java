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
package org.apache.wicket.markup;

/**
 * Holds markup as a resource (the stream that the markup came from) and a list of MarkupElements
 * (the markup itself).
 * 
 * @see MarkupElement
 * @see ComponentTag
 * @see org.apache.wicket.markup.RawMarkup
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @deprecated This class is not used by the framework anymore. Will be removed in Wicket 9.0.0
 */
@Deprecated
public interface IMarkup
{
	/**
	 * Find the markup element index of the component with 'path'
	 * 
	 * @param path
	 *            The component path expression
	 * @param id
	 *            The component's id to search for
	 * @return -1, if not found
	 */
	int findComponentIndex(final String path, final String id);

	/**
	 * For Wicket it would be sufficient for this method to be package protected. However to allow
	 * wicket-bench easy access to the information ...
	 * 
	 * @param index
	 *            Index into markup list
	 * @return Markup element
	 */
	MarkupElement get(final int index);

	/**
	 * Gets the markup encoding. A markup encoding may be specified in a markup file with an XML
	 * encoding specifier of the form &lt;?xml ... encoding="..." ?&gt;.
	 * 
	 * @return Encoding, or null if not found.
	 */
	String getEncoding();

	/**
	 * Gets the resource that contains this markup
	 * 
	 * @return The resource where this markup came from
	 */
	MarkupResourceStream getResource();

	/**
	 * Get the wicket namespace valid for this specific markup
	 * 
	 * @return wicket namespace
	 */
	String getWicketNamespace();

	/**
	 * Return the XML declaration string, in case if found in the markup.
	 * 
	 * @return Null, if not found.
	 */
	String getXmlDeclaration();

	/**
	 * For Wicket it would be sufficient for this method to be package protected. However to allow
	 * wicket-bench easy access to the information ...
	 * 
	 * @return Number of markup elements
	 */
	int size();

	/**
	 * @return String representation of markup list
	 */
	String toDebugString();

	/**
	 * @return String representation of markup list
	 */
	@Override
	String toString();
}
