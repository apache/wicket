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

import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.settings.IMarkupSettings;

/**
 * Default implementation of IMarkupParserFactory. It creates a MarkupParser with a standard set of
 * IMarkupFilter. You may add IMarkupFilters like shown below, but don't forget to register your own
 * MarkupParserFactory with Settings to become activated.
 * 
 * <pre>
 * class MyMarkupParserFactory
 * {
 *   ...
 *   public MarkupParser newMarkupParser(final MarkupResourceStream resource)
 *   {
 *      MarkupParser parser = new MarkupParser(new XmlPullParser(), resource);
 *      parser.appendMarkupFilter(new MyOwnFilter());
 *      return parser;
 *   }
 *   ...
 * }
 * </pre>
 * 
 * @see IMarkupFilter
 * @see MarkupParser
 * @see IMarkupSettings
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Juergen Donnerstag
 */
public class MarkupParserFactory implements IMarkupParserFactory
{
	/**
	 * Construct.
	 * 
	 */
	public MarkupParserFactory()
	{
	}

	/**
	 * @see org.apache.wicket.markup.IMarkupParserFactory#newMarkupParser(org.apache.wicket.markup.MarkupResourceStream)
	 */
	public MarkupParser newMarkupParser(final MarkupResourceStream resource)
	{
		return new MarkupParser(new XmlPullParser(), resource);
	}
}
