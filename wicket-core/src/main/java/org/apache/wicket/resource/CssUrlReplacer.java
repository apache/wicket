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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * This compressor is used to replace url within css files with resources that belongs to their
 * corresponding component classes. The compress method is not compressing any content, but
 * replacing the URLs with Wicket representatives.<br>
 * <br>
 * Usage:
 * 
 * <pre>
 * this.getResourceSettings().setCssCompressor(new CssUrlReplacer(this));
 * </pre>
 * 
 * @since 6.20.0
 * @author Tobias Soloschenko
 */
public class CssUrlReplacer implements IScopeAwareTextResourceProcessor, ICssCompressor
{
	// The pattern to find URLs in CSS resources
	private static final Pattern URL_PATTERN = Pattern.compile("url\\(['|\"]*(.*?)['|\"]*\\)");

	/**
	 * Replaces the URLs of CSS resources with Wicket representatives.
	 */
	@Override
	public String process(String input, Class<?> scope, String name)
	{
		RequestCycle cycle = RequestCycle.get();
		Url cssUrl = Url.parse(name);
		Matcher matcher = URL_PATTERN.matcher(input);
		StringBuffer output = new StringBuffer();

		while (matcher.find())
		{
			Url imageCandidateUrl = Url.parse(matcher.group(1));
			CharSequence processedUrl;
			if (imageCandidateUrl.isFull())
			{
				processedUrl = imageCandidateUrl.toString(Url.StringMode.FULL);
			}
			else if (imageCandidateUrl.isContextAbsolute())
			{
				processedUrl = imageCandidateUrl.toString();
			}
			else
			{
				// relativize against the url for the containing CSS file
				Url cssUrlCopy = new Url(cssUrl);
				cssUrlCopy.resolveRelative(imageCandidateUrl);
				PackageResourceReference imageReference = new PackageResourceReference(scope,
					cssUrlCopy.toString());
				processedUrl = cycle.urlFor(imageReference, null);

			}
			matcher.appendReplacement(output, "url('" + processedUrl + "')");
		}
		matcher.appendTail(output);
		return output.toString();
	}

	@Override
	public String compress(String original)
	{
		throw new UnsupportedOperationException(CssUrlReplacer.class.getSimpleName() +
			".process() should be used instead!");
	}
}
