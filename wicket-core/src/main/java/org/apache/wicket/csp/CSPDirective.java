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
import java.util.regex.Pattern;

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
	SCRIPT_SRC_ATTR("script-src-attr"),
	SCRIPT_SRC_ELEM("script-src-elem"),
	STYLE_SRC("style-src"),
	STYLE_SRC_ATTR("style-src-attr"),
	STYLE_SRC_ELEM("style-src-elem"),
	IMG_SRC("img-src"),
	CONNECT_SRC("connect-src"),
	FONT_SRC("font-src"),
	OBJECT_SRC("object-src"),
	MANIFEST_SRC("manifest-src"),
	MEDIA_SRC("media-src"),
	CHILD_SRC("child-src"),
	WORKER_SRC("worker-src"),
	FRAME_ANCESTORS("frame-ancestors"),
	BASE_URI("base-uri"),
	/**
	 * This directive was deprecated in CSP 2, but no longer in 3. Wicket will automatically add a
	 * {@code frame-src} directive when {@code child-src} is added.
	 */
	FRAME_SRC("frame-src"),
	FORM_ACTION("form-action"),
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
			if (value instanceof RelativeURICSPValue)
			{
				return;
			}
			if (!(value instanceof FixedCSPValue))
			{
				throw new IllegalArgumentException(
					"A report-uri directive can only contain an URI, not " + value);
			}
			try
			{
				new URI(value.toString());
			}
			catch (URISyntaxException urise)
			{
				throw new IllegalArgumentException("Illegal URI for report-uri directive", urise);
			}
		}
	},
	/**
	 * Makes the browser reject strings at script injection sinks such as {@code innerHTML}. The
	 * only value is {@code 'script'}.
	 *
	 * @since 12.0.0
	 * @see <a href="https://www.w3.org/TR/trusted-types/">https://www.w3.org/TR/trusted-types/</a>
	 */
	REQUIRE_TRUSTED_TYPES_FOR("require-trusted-types-for")
	{
		/**
		 * Only allow {@code 'script'}, and only once.
		 */
		@Override
		public void checkValueForDirective(CSPRenderable value,
				List<CSPRenderable> existingDirectiveValues)
		{
			if (!existingDirectiveValues.isEmpty())
			{
				throw new IllegalArgumentException(
					"A require-trusted-types-for directive can only contain one value, it already "
						+ "contains " + existingDirectiveValues);
			}
			if (!(value instanceof FixedCSPValue) || !"'script'".equals(value.toString()))
			{
				throw new IllegalArgumentException(
					"A require-trusted-types-for directive can only contain 'script', not "
						+ value);
			}
		}
	},
	/**
	 * Restricts the names of the Trusted Types policies a page may create. The values are policy
	 * names, {@code 'allow-duplicates'}, {@code *} or a single {@code 'none'}.
	 *
	 * @since 12.0.0
	 * @see <a href="https://www.w3.org/TR/trusted-types/">https://www.w3.org/TR/trusted-types/</a>
	 */
	TRUSTED_TYPES("trusted-types")
	{
		/**
		 * Only allow policy names and the keywords of the directive, and block {@code 'none'}
		 * next to other values.
		 */
		@Override
		public void checkValueForDirective(CSPRenderable value,
				List<CSPRenderable> existingDirectiveValues)
		{
			boolean none = isNone(value);
			if (!none && (!(value instanceof FixedCSPValue)
				|| !TRUSTED_TYPES_EXPRESSION.matcher(value.toString()).matches()))
			{
				throw new IllegalArgumentException(
					"A trusted-types directive can only contain policy names, 'allow-duplicates', "
						+ "* or 'none', not " + value);
			}
			if (!existingDirectiveValues.isEmpty())
			{
				if (none)
				{
					throw new IllegalArgumentException(
						"A trusted-types directive can't contain 'none' if it already contains "
							+ "other values: " + existingDirectiveValues);
				}
				if (isNone(existingDirectiveValues.get(0)))
				{
					throw new IllegalArgumentException(
						"A trusted-types directive can't contain other values if it already "
							+ "contains 'none', can't add " + value);
				}
			}
		}
	};

	private static final Pattern TRUSTED_TYPES_EXPRESSION = Pattern
		.compile("[A-Za-z0-9\\-#=_/@.%]+|'allow-duplicates'|'none'|\\*");

	private static boolean isNone(CSPRenderable value)
	{
		return CSPDirectiveSrcValue.NONE.equals(value)
			|| CSPDirectiveSrcValue.NONE.getValue().equals(value.toString());
	}

	private final String value;

	CSPDirective(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	/**
	 * Check if {@code value} can be added to the list of other values. By default, it checks for
	 * conflicts with wildcards and none, and it checks if values are valid uris.
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
		for (CSPDirective directive : values())
		{
			if (value.equals(directive.getValue()))
			{
				return directive;
			}
		}
		return null;
	}
}
