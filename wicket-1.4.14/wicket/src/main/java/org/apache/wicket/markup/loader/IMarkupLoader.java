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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupCache;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupParserFactory;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

/**
 * IMarkupLoader are loading the actual markup for a specific Wicket container and resource stream.
 * In case of markup inheritance it means that 2+ markup files must be read and merged. In order to
 * be flexible the interface has been designed in a way that multiple IMarkupLoader can be chained
 * to easily build up more complex loaders.
 * 
 * @see MarkupCache
 * @see MarkupParser
 * @see MarkupParserFactory
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupLoader
{
	/**
	 * Loads markup from a resource stream.
	 * 
	 * @param container
	 *            The original requesting markup container
	 * @param markupResourceStream
	 *            The markup resource stream to load
	 * @param baseLoader
	 *            This parameter can be use to chain IMarkupLoaders
	 * @param enforceReload
	 *            The cache will be ignored and all, including inherited markup files, will be
	 *            reloaded. Whatever is in the cache, it will be ignored
	 * @return The markup
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	Markup loadMarkup(final MarkupContainer container,
		final MarkupResourceStream markupResourceStream, final IMarkupLoader baseLoader,
		final boolean enforceReload) throws IOException, ResourceStreamNotFoundException;
}