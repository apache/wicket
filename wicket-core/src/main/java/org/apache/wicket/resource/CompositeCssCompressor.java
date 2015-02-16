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
package org.apache.wicket.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.css.ICssCompressor;

/**
 * Used to apply several {@link ICssCompressor} to the CSS compression.<br>
 * <br>
 * Usage:
 * 
 * <pre>
 * CompositeCssCompressor compositeCssCompressor = new CompositeCssCompressor();
 * 
 * compositeCssCompressor.add(new MyCssCompressor());
 * compositeCssCompressor.add(new AnotherCssCompressor());
 * 
 * this.getResourceSettings().setCssCompressor(compositeCssCompressor);
 * </pre>
 * 
 * The compressors can also be given as constructor arguments.
 * 
 * @since 6.20.0
 * @author Tobias Soloschenko
 */
public class CompositeCssCompressor implements IScopeAwareTextResourceProcessor, ICssCompressor
{
	/* Compressors to compress the CSS content */
	private final List<ICssCompressor> compressors = new ArrayList<>();

	/**
	 * Initializes the composite CSS compressor with the given {@link ICssCompressor}(s)
	 * 
	 * @param compressors
	 *            The {@link ICssCompressor}(s) this composite CSS compressor is initialized with
	 */
	public CompositeCssCompressor(ICssCompressor... compressors)
	{
		this.compressors.addAll(Arrays.asList(compressors));
	}

	/**
	 * Compresses the given original content in the order of compressors. If no compressor has been
	 * given the original content is going to be returned.
	 */
	@Override
	public String process(String input, Class<?> scope, String name)
	{
		String compressed = input;
		for (ICssCompressor compressor : compressors)
		{
			if (compressor instanceof IScopeAwareTextResourceProcessor)
			{
				IScopeAwareTextResourceProcessor processor = (IScopeAwareTextResourceProcessor)compressor;
				compressed = processor.process(compressed, scope, name);
			}
			else
			{
				compressed = compressor.compress(compressed);
			}
		}
		return compressed;
	}

	@Override
	public String compress(String original)
	{
		throw new UnsupportedOperationException(CompositeCssCompressor.class.getSimpleName() +
			".process() should be used instead!");
	}

	/**
	 * Adds a ICssCompressor to the list of delegates.
	 *
	 * @return {@code this} instance, for chaining
	 */
	public CompositeCssCompressor add(ICssCompressor compressor)
	{
		compressors.add(compressor);
		return this;
	}
}
