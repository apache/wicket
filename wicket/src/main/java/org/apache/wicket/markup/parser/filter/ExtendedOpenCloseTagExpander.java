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
package org.apache.wicket.markup.parser.filter;

import java.util.Arrays;
import java.util.List;

/**
 * MarkupFilter that expands all xhtml tag which are open-close in the markup but where the standard
 * requires it to have a body.
 * 
 * See WICKET-2650 for another use case where you want to avoid situations where your java code
 * wants to render a body text, but because of the open-close tag Wicket's render engine will not
 * call onComponentTagBody(). And you'll not get a warning or exception.
 * 
 * This filter is not added by default. You may add it like:
 * 
 * <pre>
 * Application#init() {
 *   getMarkupSettings().setMarkupParserFactory() {
 *      new MarkupParserFactory() {
 *      	MarkupParser newMarkupParser(final MarkupResourceStream resource) {
 *        	  MarkupParser parser=super.newMarkupParser(resource);
 *            parser.appendMarkupFilter(new ExtendedOpenCloseTagExpander());
 *            return parser;
 *          }
 *       }
 *    }
 * }
 * </pre>
 * 
 * You may subclass ExtendedOpenCloseTagExpander and implement your own {@link #contains(String)} to
 * throw an exception or log a warning instead of changing open-close to open-body-close.
 * 
 * @author Juergen Donnerstag
 */
public class ExtendedOpenCloseTagExpander extends OpenCloseTagExpander
{
	/** The complete xthtml list of tags which require a body tag */
	private static final List<String> replaceForTags = Arrays.asList(new String[] { "a", "q",
			"sub", "sup", "abbr", "acronym", "cite", "code", "del", "dfn", "em", "ins", "kbd",
			"samp", "var", "label", "textarea", "tr", "td", "th", "caption", "thead", "tbody",
			"tfoot", "dl", "dt", "dd", "li", "ol", "ul", "h1", "h2", "h3", "h4", "h5", "h6", "pre",
			"title" });

	/**
	 * @see org.apache.wicket.markup.parser.filter.OpenCloseTagExpander#contains(java.lang.String)
	 */
	@Override
	protected boolean contains(String name)
	{
		return replaceForTags.contains(name.toLowerCase());
	}
}
