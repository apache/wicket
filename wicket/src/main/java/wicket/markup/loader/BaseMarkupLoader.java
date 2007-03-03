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
package wicket.markup.loader;

import java.io.IOException;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.markup.IMarkupParserFactory;
import wicket.markup.MarkupFragment;
import wicket.markup.MarkupParser;
import wicket.markup.MarkupResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;

/**
 * Load the markup via the MarkupParser, not more, not less. Caching is provided
 * separately as well as Inherited-Markup merging.
 * 
 * @author Juergen Donnerstag
 */
public class BaseMarkupLoader implements IMarkupLoader
{
	/**
	 * Constructor.
	 */
	public BaseMarkupLoader()
	{
	}

	/**
	 * @see wicket.markup.loader.IMarkupLoader#loadMarkup(wicket.MarkupContainer,
	 *      wicket.markup.MarkupResourceStream)
	 */
	public final MarkupFragment loadMarkup(final MarkupContainer container,
			final MarkupResourceStream markupResourceStream) throws IOException,
			ResourceStreamNotFoundException
	{
		final IMarkupParserFactory fac = Application.get().getMarkupSettings()
				.getMarkupParserFactory();
		final MarkupParser parser = fac.newMarkupParser(markupResourceStream);
		return parser.readAndParse();
	}
}
