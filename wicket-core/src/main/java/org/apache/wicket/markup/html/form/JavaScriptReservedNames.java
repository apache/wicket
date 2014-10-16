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
package org.apache.wicket.markup.html.form;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.util.lang.Args;

/**
 * Utility class for names used by JavaScript DOM API. These names should not be used as form element names, as they would interfere
 * with JavaScripts that attempt to use DOM API.
 *
 * @author Jesse Long
 */
class JavaScriptReservedNames
{
	/**
	 * Set of names reserved by JavaScript DOM API.
	 */
	private static final Set<String> RESERVED_NAMES = new HashSet<String>(100);

	static
	{
		/*
		 * DOM 3 CORE Node interface
		 * http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-1950641247
		 */
		RESERVED_NAMES.add("nodeName");
		RESERVED_NAMES.add("nodeValue");
		RESERVED_NAMES.add("nodeType");
		RESERVED_NAMES.add("parentNode");
		RESERVED_NAMES.add("childNodes");
		RESERVED_NAMES.add("firstChild");
		RESERVED_NAMES.add("lastChild");
		RESERVED_NAMES.add("previousSibling");
		RESERVED_NAMES.add("nextSibling");
		RESERVED_NAMES.add("attributes");
		RESERVED_NAMES.add("ownerDocument");
		RESERVED_NAMES.add("insertBefore");
		RESERVED_NAMES.add("replaceChild");
		RESERVED_NAMES.add("removeChild");
		RESERVED_NAMES.add("appendChild");
		RESERVED_NAMES.add("hasChildNodes");
		RESERVED_NAMES.add("cloneNode");
		RESERVED_NAMES.add("normalize");
		RESERVED_NAMES.add("isSupported");
		RESERVED_NAMES.add("namespaceURI");
		RESERVED_NAMES.add("prefix");
		RESERVED_NAMES.add("localName");
		RESERVED_NAMES.add("hasAttributes");
		RESERVED_NAMES.add("createDocumentPosition");
		RESERVED_NAMES.add("textContent");
		RESERVED_NAMES.add("isSameNode");
		RESERVED_NAMES.add("lookupPrefix");
		RESERVED_NAMES.add("isDefaultNamespace");
		RESERVED_NAMES.add("lookupNamespaceURI");
		RESERVED_NAMES.add("isEqualNode");
		RESERVED_NAMES.add("getFeature");
		RESERVED_NAMES.add("setUserData");
		RESERVED_NAMES.add("getUserData");

		/*
		 * DOM 3 CORE Element interface
		 * http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-745549614
		 */
		RESERVED_NAMES.add("tagName");
		RESERVED_NAMES.add("getAttribute");
		RESERVED_NAMES.add("setAttribute");
		RESERVED_NAMES.add("removeAttribute");
		RESERVED_NAMES.add("getAttributeNode");
		RESERVED_NAMES.add("setAttributeNode");
		RESERVED_NAMES.add("removeAttributeNode");
		RESERVED_NAMES.add("getElementsByTagName");
		RESERVED_NAMES.add("getAttributeNS");
		RESERVED_NAMES.add("setAttributeNS");
		RESERVED_NAMES.add("removeAttributeNS");
		RESERVED_NAMES.add("getAttributeNodeNS");
		RESERVED_NAMES.add("setAttributeNodeNS");
		RESERVED_NAMES.add("getElementsByTagNameNS");
		RESERVED_NAMES.add("hasAttribute");
		RESERVED_NAMES.add("hasAttributeNS");
		RESERVED_NAMES.add("schemaTypeInfo");
		RESERVED_NAMES.add("setIdAttribute");
		RESERVED_NAMES.add("setIdAttributeNS");
		RESERVED_NAMES.add("setIdAttributeNode");

		/*
		 * DOM 2 HTML HTMLElement interface
		 * http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-58190037
		 */
		RESERVED_NAMES.add("id");
		RESERVED_NAMES.add("title");
		RESERVED_NAMES.add("lang");
		RESERVED_NAMES.add("dir");
		RESERVED_NAMES.add("className");

		/*
		 * DOM 2 HTML HTMLFormElement interface
		 * http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-40002357
		 */
		RESERVED_NAMES.add("elements");
		RESERVED_NAMES.add("length");
		RESERVED_NAMES.add("name");
		RESERVED_NAMES.add("acceptCharset");
		RESERVED_NAMES.add("action");
		RESERVED_NAMES.add("enctype");
		RESERVED_NAMES.add("method");
		RESERVED_NAMES.add("target");
		RESERVED_NAMES.add("submit");
		RESERVED_NAMES.add("reset");
	}

	/**
	 * Private constructor for utility class.
	 */
	private JavaScriptReservedNames()
	{
	}

	/**
	 * Returns {@code true} if the name is used by the JavaScript DOM API. If the name is used in the JavaScript DOM API, we
	 * should not name a form element with this name, as it would interfere with a JavaScript's ability to use the DOM API on
	 * the form element.
	 *
	 * @param name
	 *		The name to check.
	 *
	 * @return {@code true} if the name is used by the JavaScript DOM API.
	 */
	public static boolean isNameReserved(String name)
	{
		Args.notNull(name, "name");

		return RESERVED_NAMES.contains(name);
	}
}
