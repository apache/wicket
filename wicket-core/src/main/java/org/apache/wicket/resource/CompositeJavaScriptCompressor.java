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

import org.apache.wicket.javascript.IJavaScriptCompressor;

/**
 * Used to apply several {@link IJavaScriptCompressor} to the javascript compression.<br>
 * <br>
 * Usage:
 * 
 * <pre>
 * CompositeJavaScriptCompressor compositeJavaScriptCompressor = new CompositeJavaScriptCompressor();
 * 
 * compositeJavaScriptCompressor.getCompressors().add(new MyJavaScriptCompressor());
 * compositeJavaScriptCompressor.getCompressors().add(new AnotherJavaScriptCompressor());
 * 
 * this.getResourceSettings().setJavaScriptCompressor(compositeJavaScriptCompressor);
 * </pre>
 * The compressors can also be given as constructor arguments.
 * 
 * @since 6.20.0
 * @author Tobias Soloschenko
 *
 */
public class CompositeJavaScriptCompressor implements IJavaScriptCompressor
{

	/* Compressors to compress javascript content */
	private final List<IJavaScriptCompressor> compressors = new ArrayList<>();

	/**
	 * Initializes the composite javascript compressor with the given {@link IJavaScriptCompressor}
	 * 
	 * @param compressors
	 *            several {@link IJavaScriptCompressor} the composite javascript compressor is
	 *            initialized with
	 */
	public CompositeJavaScriptCompressor(IJavaScriptCompressor... compressors)
	{
		this.compressors.addAll(Arrays.asList(compressors));
	}

	/**
	 * Compresses the given original content in the order of compressors. If no compressor has been
	 * given the original content is going to be returned.
	 */
	@Override
	public String compress(String original)
	{
		String compressed = original;
		for (IJavaScriptCompressor compressor : compressors)
		{
			compressed = compressor.compress(compressed);
		}
		return compressed;
	}

	/**
	 * Gets a list of {@link IJavaScriptCompressor} to be used for javascript compression. They are
	 * applied in the order of the List.
	 * 
	 * @return A list of {@link IJavaScriptCompressor} to be used for javascript compression.
	 */
	public List<IJavaScriptCompressor> getCompressors()
	{
		return compressors;
	}
}
