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
import java.util.List;

import org.apache.wicket.css.ICssCompressor;

/**
 * Used to apply several {@link ICssCompressor} to the css compression.<br>
 * <br>
 * Usage:
 * 
 * <pre>
 * CompositeCssCompressor compositeCssCompressor = new CompositeCssCompressor();
 * 
 * compositeCssCompressor.getCompressors().add(new MyCssCompressor());
 * compositeCssCompressor.getCompressors().add(new AnotherCssCompressor());
 * 
 * this.getResourceSettings().setCssCompressor(compositeCssCompressor);
 * </pre>
 * @since 6.20.0
 * @author Tobias Soloschenko
 * 
 */
public class CompositeCssCompressor implements ICssCompressor
{

	/* Compressors to compress the CSS content */
	private List<ICssCompressor> compressors;

	/**
	 * Compresses the given original content in the order of compressors.
	 */
	@Override
	public String compress(String original)
	{
		if (compressors != null)
		{
			String compressed = original;
			for (ICssCompressor compressor : compressors)
			{
				compressed = compressor.compress(compressed);
			}
			return compressed;
		}
		else
		{
			return original;
		}
	}

	/**
	 * Gets a list of {@link ICssCompressor} to be used for CSS compression. They are applied in the
	 * order of the List.
	 * 
	 * @return A list of {@link ICssCompressor} to be used for CSS compression.
	 */
	public List<ICssCompressor> getCompressors()
	{
		if (compressors == null)
		{
			compressors = new ArrayList<ICssCompressor>();
		}
		return compressors;
	}
}
