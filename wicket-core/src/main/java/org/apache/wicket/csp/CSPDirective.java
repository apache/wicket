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
package org.apache.wicket.csp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.wicket.util.string.Strings;

/**
 * An enum holding the possible CSP Directives. Via the
 * {@link #checkValueForDirective(CSPRenderable, List)}-method, new values are verified before being
 * added to the list of values for a directive.
 *
 * @see <a href="https://www.w3.org/TR/CSP2/">https://www.w3.org/TR/CSP2</a>
 * @see <a href=
 *      "https://developer.mozilla.org/en-US/docs/Web/Security/CSP">https://developer.mozilla.org/en-US/docs/Web/Security/CSP</a>
 */
public enum CSPDirective
{
	DEFAULT_SRC("default-src"),
	SCRIPT_SRC("script-src"),
	STYLE_SRC("style-src"),
	IMG_SRC("img-src"),
	CONNECT_SRC("connect-src"),
	FONT_SRC("font-src"),
	OBJECT_SRC("object-src"),
	MANIFEST_SRC("manifest-src"),
	MEDIA_SRC("media-src"),
	CHILD_SRC("child-src"),
	FRAME_ANCESTORS("frame-ancestors"),
	/**
	 * This directive was deprecated in CSP 2, but no longer in 3. Wicket will automatically add a
	 * {@code frame-src} directive when {@code child-src} is added.
	 */
	FRAME_SRC("frame-src"),
	SANDBOX("sandbox")
	{
		/**
		 * Only allow {@link CSPDirectiveSandboxValue} for the {@code 'sandbox'} directive and block
		 * conflicting options.
		 */
		@Override
		public void checkValueForDirective(CSPRenderable value,
				List<CSPRenderable> existingDirectiveValues)
		{
			if (!existingDirectiveValues.isEmpty())
			{
				if (CSPDirectiveSandboxValue.EMPTY.equals(value))
				{
					throw new IllegalArgumentException(
						"A sandbox directive can't contain an empty string if it already contains "
							+ "other values: " + existingDirectiveValues);
				}
				if (existingDirectiveValues.contains(CSPDirectiveSandboxValue.EMPTY))
				{
					throw new IllegalArgumentException(
						"A sandbox directive can't contain other values if it already contains an "
							+ "empty string, can't add " + value);
				}
			}

			if (!(value instanceof CSPDirectiveSandboxValue))
			{
				throw new IllegalArgumentException(
					"A sandbox directive can only contain values from CSPDirectiveSandboxValue or "
						+ "be empty, can't add " + value);
			}
		}
	},
	REPORT_URI("report-uri")
	{
		/**
		 * Only allow URI, and only one.
		 */
		@Override
		public void checkValueForDirective(CSPRenderable value,
				List<CSPRenderable> existingDirectiveValues)
		{
			if (!existingDirectiveValues.isEmpty())
			{
				throw new IllegalArgumentException(
					"A report-uri directive can only contain one URI, it already contains "
						+ existingDirectiveValues);
			}
			if (!(value instanceof FixedCSPDirective))
			{
				throw new IllegalArgumentException(
					"A report-uri directive can only contain an URI, not " + value);
			}
			try
			{
				new URI(value.render(null, null));
			}
			catch (URISyntaxException urise)
			{
				throw new IllegalArgumentException("Illegal URI for report-uri directive", urise);
			}
		}
	};

	private String value;

	private CSPDirective(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	/**
	 * Check if {@code value} can be added to the list of other values. By default, it checks for
	 * conflicts with wildcards and none and it checks if values are valid uris.
	 * 
	 * @param value
	 *            The value to add.
	 * @param existingDirectiveValues
	 *            The other values.
	 * @throws IllegalArgumentException
	 *             if the given value is invalid.
	 */
	public void checkValueForDirective(CSPRenderable value,
			List<CSPRenderable> existingDirectiveValues)
	{
		if (!existingDirectiveValues.isEmpty())
		{
			if (CSPDirectiveSrcValue.WILDCARD.equals(value)
				|| CSPDirectiveSrcValue.NONE.equals(value))
			{
				throw new IllegalArgumentException(
					"A -src directive can't contain an * or a 'none' if it already contains other "
						+ "values: " + existingDirectiveValues);
			}
			if (existingDirectiveValues.contains(CSPDirectiveSrcValue.WILDCARD)
				|| existingDirectiveValues.contains(CSPDirectiveSrcValue.NONE))
			{
				throw new IllegalArgumentException(
					"A -src directive can't contain other values if it already contains an * or "
						+ "a 'none', can't add " + value);
			}
		}
		
		if (value instanceof CSPDirectiveSandboxValue)
		{
			throw new IllegalArgumentException(
				"A -src directive can't contain any of the sandbox directive values, like "
						+ value);
		}
		
		value.checkValidityForSrc();
	}

	/**
	 * @return The CSPDirective constant whose value-parameter equals the input-parameter or
	 *         {@code null} if none can be found.
	 */
	public static CSPDirective fromValue(String value)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}
		for (int i = 0; i < values().length; i++)
		{
			if (value.equals(values()[i].getValue()))
			{
				return values()[i];
			}
		}
		return null;
	}
}
