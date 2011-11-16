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
package org.apache.wicket.markup.html.image.resource;

import java.util.Locale;

import org.apache.wicket.IResourceFactory;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.parse.metapattern.Group;
import org.apache.wicket.util.parse.metapattern.IntegerGroup;
import org.apache.wicket.util.parse.metapattern.MetaPattern;
import org.apache.wicket.util.parse.metapattern.OptionalMetaPattern;
import org.apache.wicket.util.parse.metapattern.parsers.MetaPatternParser;


/**
 * A factory which creates default button images.
 * 
 * @author Jonathan Locke
 */
public class DefaultButtonImageResourceFactory implements IResourceFactory
{
	/**
	 * 
	 * @see org.apache.wicket.IResourceFactory#newResource(java.lang.String, java.util.Locale,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public IResource newResource(final String specification, final Locale locale,
		final String style, final String variation)
	{
		final Parser parser = new Parser(specification);
		if (parser.matches())
		{
			return new DefaultButtonImageResource(parser.getWidth(), parser.getHeight(),
				parser.getLabel());
		}
		else
		{
			throw new WicketRuntimeException(
				"DefaultButtonImageResourceFactory does not recognized the specification " +
					specification);
		}
	}

	/**
	 * Parses image value specifications.
	 * 
	 * @author Jonathan Locke
	 */
	private static final class Parser extends MetaPatternParser
	{
		/** Group value. */
		private static final IntegerGroup width = new IntegerGroup();

		/** Group value. */
		private static final IntegerGroup height = new IntegerGroup();

		/** Label */
		private static final Group label = new Group(MetaPattern.ANYTHING);

		/** Meta pattern. */
		private static final MetaPattern pattern = new MetaPattern(
			new OptionalMetaPattern(
				new MetaPattern[] { width, MetaPattern.COMMA, height, MetaPattern.COLON }), label);

		/**
		 * Construct.
		 * 
		 * @param input
		 *            to parse
		 */
		public Parser(final CharSequence input)
		{
			super(pattern, input);
		}

		/**
		 * @return The label
		 */
		public String getLabel()
		{
			return label.get(matcher());
		}

		/**
		 * @return Any width
		 */
		public int getWidth()
		{
			return width.getInt(matcher(), -1);
		}

		/**
		 * @return Any height
		 */
		public int getHeight()
		{
			return height.getInt(matcher(), -1);
		}
	}
}
