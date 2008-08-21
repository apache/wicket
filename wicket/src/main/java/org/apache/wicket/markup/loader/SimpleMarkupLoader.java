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
package org.apache.wicket.markup.loader;

import java.io.IOException;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

/**
 * Load the markup via the MarkupParser, not more, not less. Caching is provided separately as well
 * as Inherited-Markup merging.
 * 
 * @author Juergen Donnerstag
 */
public class SimpleMarkupLoader implements IMarkupLoader
{
	/**
	 * Constructor.
	 */
	public SimpleMarkupLoader()
	{
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.loader.IMarkupLoader#loadMarkup(org.apache.wicket.MarkupContainer,
	 *      org.apache.wicket.markup.MarkupResourceStream,
	 *      org.apache.wicket.markup.loader.IMarkupLoader, boolean)
	 */
	public final Markup loadMarkup(final MarkupContainer container,
		final MarkupResourceStream markupResourceStream, final IMarkupLoader baseLoader,
		final boolean enforceReload) throws IOException, ResourceStreamNotFoundException
	{
		return Application.get().getMarkupSettings().getMarkupParserFactory().newMarkupParser(
			markupResourceStream).parse();
	}
}
