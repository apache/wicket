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
package org.apache.wicket.validation.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Validator for checking URLs. The default schemes allowed are <code>http://</code>,
 * <code>https://</code>, and <code>ftp://</code>.
 * <p>
 * The behavior of validation is modified by passing in one of these options:
 * <p>
 * <ul>
 * <li><code>ALLOW_2_SLASHES - [FALSE]</code>: Allows double '/' characters in the path component.</li>
 * <li><code>NO_FRAGMENT- [FALSE]</code>: By default fragments are allowed. If this option is
 * included then fragments are flagged as illegal.</li>
 * <li><code>ALLOW_ALL_SCHEMES - [FALSE]</code>: By default only http, https, and ftp are considered
 * valid schemes. Enabling this option will let any scheme pass validation.</li>
 * </ul>
 * <p>
 * This was originally based <code>org.apache.commons.validator.UrlValidator</code>, but the
 * dependency on Jakarta-ORO was removed and it now uses java.util.regexp instead. Usage example:
 * <p>
 * 
 * <pre>
 * &lt;code&gt;
 * Component.add(new UrlValidator({&quot;http&quot;, &quot;https&quot;}));
 * &lt;/code&gt;
 * </pre>
 * 
 * @author Vincent Demay
 * @since 1.2.6
 * @see "http://www.ietf.org/rfc/rfc2396.txt"
 */
public class UrlValidator implements IValidator<String>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Allows all validly-formatted schemes to pass validation instead of supplying a set of valid
	 * schemes.
	 */
	public static final int ALLOW_ALL_SCHEMES = 1 << 0;

	/**
	 * Allow two slashes in the path component of the <code>URL</code>.
	 */
	public static final int ALLOW_2_SLASHES = 1 << 1;

	/**
	 * Enabling this option disallows any <code>URL</code> fragments.
	 */
	public static final int NO_FRAGMENTS = 1 << 2;

	private static final String ALPHA_CHARS = "a-zA-Z";

	private static final String ALPHA_NUMERIC_CHARS = ALPHA_CHARS + "\\d";

	private static final String SPECIAL_CHARS = ";/@&=,.?:+$";

	private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";

	private static final String SCHEME_CHARS = ALPHA_CHARS;

	// Drop numeric, and "+-." for now
	private static final String AUTHORITY_CHARS = ALPHA_NUMERIC_CHARS + "\\-\\.";

	private static final String ATOM = VALID_CHARS + '+';

	/**
	 * This expression derived/taken from the BNF for URI (RFC2396).
	 */
	private static final String URL_PATTERN = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";

	/**
	 * Schema / Protocol (<code>http:</code>, <code>ftp:</code>, <code>file:</code>, etc).
	 */
	private static final int PARSE_URL_SCHEME = 2;
	private static final int PARSE_URL_AUTHORITY = 4; // Includes hostname / ip and port number.
	private static final int PARSE_URL_PATH = 5;
	private static final int PARSE_URL_QUERY = 7;
	private static final int PARSE_URL_FRAGMENT = 9;

	/**
	 * Protocol (<code>http:</code>, <code>ftp:</code>, or <code>https:</code>).
	 */
	private static final String SCHEME_PATTERN = "^[" + SCHEME_CHARS + "].*$";

	private static final String AUTHORITY_PATTERN = "^(.+(:.*)?@)?([" + AUTHORITY_CHARS +
		"]*)(:\\d*)?(.*)?";

	private static final int PARSE_AUTHORITY_HOST_IP = 3;
	private static final int PARSE_AUTHORITY_PORT = 4;
	private static final int PARSE_AUTHORITY_EXTRA = 5; // Should always be empty.

	private static final String PATH_PATTERN = "^(/[-\\w:@&?=+,.!/~*'%$_;\\(\\)]*)?$";

	private static final String QUERY_PATTERN = "^(.*)$";

	private static final String LEGAL_ASCII_PATTERN = "^[\\x00-\\x7F]+$";

	private static final String IP_V4_DOMAIN_PATTERN = "^(\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})$";

	private static final String DOMAIN_PATTERN = "^" + ATOM + "(\\." + ATOM + ")*$";

	private static final String PORT_PATTERN = "^:(\\d{1,5})$";

	private static final String ATOM_PATTERN = "(" + ATOM + ")";

	private static final String ALPHA_PATTERN = "^[" + ALPHA_CHARS + "]";

	/**
	 * Holds the set of current validation options.
	 */
	private long options = 0;

	/**
	 * The set of schemes that are allowed to be in a URL.
	 */
	private final Set<String> allowedSchemes = new HashSet<String>();

	/**
	 * If no schemes are provided, default to this set of protocols.
	 */
	protected String[] defaultSchemes = { "http", "https", "ftp" };

	/**
	 * Constructs a <code>UrlValidator</code> with default properties.
	 */
	public UrlValidator()
	{
		this(null);
	}

	/**
	 * Constructs a <code>UrlValidator</code> with the given <code>String</code> array of scheme
	 * options. The validation is modified by passing in options in the <code>schemes</code>
	 * argument.
	 * 
	 * @param schemes
	 *            Pass in one or more <code>URL</code> schemes to consider valid. Passing in a
	 *            <code>null</code> will default to "<code>http,https,ftp</code>" being used. If a
	 *            non-<code>null</code> scheme is specified, then all valid schemes must be
	 *            specified. Setting the <code>ALLOW_ALL_SCHEMES</code> option will ignore the
	 *            contents of <code>schemes</code>.
	 */
	public UrlValidator(String[] schemes)
	{
		this(schemes, 0);
	}

	/**
	 * Constructs a <code>UrlValidator</code> with the given validation options.
	 * 
	 * @param options
	 *            The options should be set using the public constants declared in this class. To
	 *            set multiple options you simply add them together. For example,
	 *            <code>ALLOW_2_SLASHES</code> + <code>NO_FRAGMENTS</code> enables both of those
	 *            options.
	 */
	public UrlValidator(int options)
	{
		this(null, options);
	}

	/**
	 * Constructs a <code>UrlValidator</code> with the given scheme and validation options (see
	 * class description).
	 * 
	 * @param schemes
	 *            Pass in one or more <code>URL</code> schemes to consider valid. Passing in a
	 *            <code>null</code> will default to "<code>http,https,ftp</code>" being used. If a
	 *            non-<code>null</code> scheme is specified, then all valid schemes must be
	 *            specified. Setting the <code>ALLOW_ALL_SCHEMES</code> option will ignore the
	 *            contents of <code>schemes</code>.
	 * @param options
	 *            The options should be set using the public constants declared in this class. To
	 *            set multiple options you simply add them together. For example,
	 *            <code>ALLOW_2_SLASHES</code> + <code>NO_FRAGMENTS</code> enables both of those
	 *            options.
	 * 
	 */
	public UrlValidator(String[] schemes, int options)
	{
		this.options = options;

		if (isOn(ALLOW_ALL_SCHEMES))
		{
			return;
		}

		if (schemes == null)
		{
			schemes = defaultSchemes;
		}

		allowedSchemes.addAll(Arrays.asList(schemes));
	}


	@Override
	public void validate(IValidatable<String> validatable)
	{
		String url = validatable.getValue();
		if (!isValid(url))
		{
			validatable.error(decorate(new ValidationError(this), validatable));
		}
	}

	/**
	 * Allows subclasses to decorate reported errors
	 * 
	 * @param error
	 * @return decorated error
	 */
	protected IValidationError decorate(IValidationError error, IValidatable<String> validatable)
	{
		return error;
	}

	/**
	 * Checks if a field has a valid <code>URL</code>. This method is public because it is directly
	 * used in tests.
	 * 
	 * @param value
	 *            The value validation is being performed on. A <code>null</code> value is
	 *            considered invalid.
	 * @return <code>true</code> if the <code>URL</code> is valid
	 */
	public final boolean isValid(String value)
	{
		if (value == null)
		{
			return false;
		}

		Matcher matchAsciiPat = Pattern.compile(LEGAL_ASCII_PATTERN).matcher(value);
		if (!matchAsciiPat.matches())
		{
			return false;
		}

		// Check the whole url address structure
		Matcher matchUrlPat = Pattern.compile(URL_PATTERN).matcher(value);
		if (!matchUrlPat.matches())
		{
			return false;
		}

		if (!isValidScheme(matchUrlPat.group(PARSE_URL_SCHEME)))
		{
			return false;
		}

		if (!isValidAuthority(matchUrlPat.group(PARSE_URL_AUTHORITY)))
		{
			return false;
		}

		if (!isValidPath(matchUrlPat.group(PARSE_URL_PATH)))
		{
			return false;
		}

		if (!isValidQuery(matchUrlPat.group(PARSE_URL_QUERY)))
		{
			return false;
		}

		if (!isValidFragment(matchUrlPat.group(PARSE_URL_FRAGMENT)))
		{
			return false;
		}

		return true;
	}

	/**
	 * Validates a scheme. If schemes[] was initialized to non-<code>null</code>, then only those
	 * schemes are allowed. Note that this is slightly different than for the constructor.
	 * 
	 * @param scheme
	 *            The scheme to validate. A <code>null</code> value is considered invalid.
	 * @return <code>true</code> if the <code>URL</code> is valid
	 */
	protected boolean isValidScheme(String scheme)
	{
		if (scheme == null)
		{
			return false;
		}

		if (!Pattern.compile(SCHEME_PATTERN).matcher(scheme).matches())
		{
			return false;
		}

		if (isOff(ALLOW_ALL_SCHEMES))
		{

			if (!allowedSchemes.contains(scheme))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns <code>true</code> if the authority is properly formatted. An authority is the
	 * combination of host name and port. A <code>null</code> authority value is considered invalid.
	 * 
	 * @param authority
	 *            an authority value to validate
	 * @return true if authority (host name and port) is valid.
	 */
	protected boolean isValidAuthority(String authority)
	{
		if (authority == null)
		{
			return false;
		}

		Matcher authorityMatcher = Pattern.compile(AUTHORITY_PATTERN).matcher(authority);
		if (!authorityMatcher.matches())
		{
			return false;
		}

		boolean ipV4Address = false;
		boolean hostname = false;
		// check if authority is IP address or hostname
		String hostIP = authorityMatcher.group(PARSE_AUTHORITY_HOST_IP);
		Matcher matchIPV4Pat = Pattern.compile(IP_V4_DOMAIN_PATTERN).matcher(hostIP);
		ipV4Address = matchIPV4Pat.matches();

		if (ipV4Address)
		{
			// this is an IP address so check components
			for (int i = 1; i <= 4; i++)
			{
				String ipSegment = matchIPV4Pat.group(i);
				if (ipSegment == null || ipSegment.length() <= 0)
				{
					return false;
				}

				try
				{
					if (Integer.parseInt(ipSegment) > 255)
					{
						return false;
					}
				}
				catch (NumberFormatException e)
				{
					return false;
				}

			}
		}
		else
		{
			// Domain is hostname name
			hostname = Pattern.compile(DOMAIN_PATTERN).matcher(hostIP).matches();
		}

		// rightmost hostname will never start with a digit.
		if (hostname)
		{
			// LOW-TECH FIX FOR VALIDATOR-202
			// TODO: Rewrite to use ArrayList and .add semantics: see
			// VALIDATOR-203
			char[] chars = hostIP.toCharArray();
			int size = 1;
			for (char ch : chars)
			{
				if (ch == '.')
				{
					size++;
				}
			}
			String[] domainSegment = new String[size];
			boolean match = true;
			int segmentCount = 0;
			int segmentLength = 0;

			while (match)
			{
				Matcher atomMatcher = Pattern.compile(ATOM_PATTERN).matcher(hostIP);
				match = atomMatcher.find();
				if (match)
				{
					domainSegment[segmentCount] = atomMatcher.group(1);
					segmentLength = domainSegment[segmentCount].length() + 1;
					hostIP = (segmentLength >= hostIP.length()) ? ""
						: hostIP.substring(segmentLength);

					segmentCount++;
				}
			}

			if (segmentCount > 1)
			{
				String topLevel = domainSegment[segmentCount - 1];
				if (topLevel.length() < 2)
				{
					return false;
				}

				// First letter of top level must be a alpha
				Matcher alphaMatcher = Pattern.compile(ALPHA_PATTERN).matcher(
					topLevel.substring(0, 1));
				if (!alphaMatcher.matches())
				{
					return false;
				}
			}
		}

		if (!hostname && !ipV4Address)
		{
			return false;
		}

		String port = authorityMatcher.group(PARSE_AUTHORITY_PORT);
		if (port != null)
		{
			Matcher portMatcher = Pattern.compile(PORT_PATTERN).matcher(port);
			if (!portMatcher.matches())
			{
				return false;
			}
		}

		String extra = authorityMatcher.group(PARSE_AUTHORITY_EXTRA);
		if (!isBlankOrNull(extra))
		{
			return false;
		}

		return true;
	}

	/**
	 * Returns <code>true</code> if the path is valid. A <code>null</code> value is considered
	 * invalid.
	 * 
	 * @param path
	 *            a path value to validate.
	 * @return <code>true</code> if path is valid.
	 */
	protected boolean isValidPath(String path)
	{
		if (path == null)
		{
			return false;
		}

		Matcher pathMatcher = Pattern.compile(PATH_PATTERN).matcher(path);

		if (!pathMatcher.matches())
		{
			return false;
		}

		int slash2Count = countToken("//", path);
		if (isOff(ALLOW_2_SLASHES) && (slash2Count > 0))
		{
			return false;
		}

		int slashCount = countToken("/", path);
		int dot2Count = countToken("/..", path);
		if (dot2Count > 0)
		{
			if ((slashCount - slash2Count - 1) <= dot2Count)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns <code>true</code> if the query is <code>null</code> or if it's a properly-formatted
	 * query string.
	 * 
	 * @param query
	 *            a query value to validate
	 * @return <code>true</code> if the query is valid
	 */
	protected boolean isValidQuery(String query)
	{
		if (query == null)
		{
			return true;
		}

		Matcher queryMatcher = Pattern.compile(QUERY_PATTERN).matcher(query);
		return queryMatcher.matches();
	}

	/**
	 * Returns <code>true</code> if the given fragment is <code>null</code> or fragments are
	 * allowed.
	 * 
	 * @param fragment
	 *            a fragment value to validate
	 * @return <code>true</code> if the fragment is valid
	 */
	protected boolean isValidFragment(String fragment)
	{
		if (fragment == null)
		{
			return true;
		}

		return isOff(NO_FRAGMENTS);
	}

	/**
	 * Returns the number of times the token appears in the target.
	 * 
	 * @param token
	 *            a token value to be counted
	 * @param target
	 *            a target <code>String</code> to count tokens in
	 * @return the number of tokens
	 */
	protected int countToken(String token, String target)
	{
		int tokenIndex = 0;
		int count = 0;
		while (tokenIndex != -1)
		{
			tokenIndex = target.indexOf(token, tokenIndex);
			if (tokenIndex > -1)
			{
				tokenIndex++;
				count++;
			}
		}
		return count;
	}

	/**
	 * Checks if the field isn't <code>null</code> and if length of the field is greater than zero,
	 * not including whitespace.
	 * 
	 * @param value
	 *            the value validation is being performed on
	 * @return <code>true</code> if blank or <code>null</code>
	 */
	public static boolean isBlankOrNull(String value)
	{
		return ((value == null) || (value.trim().length() == 0));
	}

	// Flag Management
	/**
	 * Tests whether the given flag is on. If the flag is not a power of 2 (ie. 3) this tests
	 * whether the combination of flags is on.
	 * 
	 * @param flag
	 *            flag value to check
	 * @return whether the specified flag value is on
	 */
	public boolean isOn(long flag)
	{
		return (options & flag) > 0;
	}

	/**
	 * Tests whether the given flag is off. If the flag is not a power of 2 (ie. 3) this tests
	 * whether the combination of flags is off.
	 * 
	 * @param flag
	 *            flag value to check.
	 * @return whether the specified flag value is off
	 */
	public boolean isOff(long flag)
	{
		return (options & flag) == 0;
	}

}
