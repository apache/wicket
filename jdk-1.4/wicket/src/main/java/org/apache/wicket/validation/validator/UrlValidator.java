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

/**
 * <p>
 * Urls validator
 * </p>
 * <p>
 * Default alowed schemes accepted are <code>http://, https://, ftp://</code>.
 * </p>
 * Behavour of validation is modified by passing in options:
 * <li>ALLOW_2_SLASHES - [FALSE] Allows double '/' characters in the path
 * component.</li>
 * <li>NO_FRAGMENT- [FALSE] By default fragments are allowed, if this option is
 * included then fragments are flagged as illegal.</li>
 * <li>ALLOW_ALL_SCHEMES - [FALSE] By default only http, https, and ftp are
 * considered valid schemes. Enabling this option will let any scheme pass
 * validation.</li>
 * 
 * <p>
 * Originally based org.apache.commons.validator.UrlValidator. Remove dependency
 * on Oro and use java.util.regexp instead
 * </p>
 * <pre>
 * 	Usage : 
 * 	component.add(new UrlValidator({"http", "https"}));
 * </pre>
 * 
 * @see <a href='http://www.ietf.org/rfc/rfc2396.txt' > Uniform Resource
 *      Identifiers (URI): Generic Syntax </a>
 * 
 * @author Vincent Demay
 * 
 */
public class UrlValidator extends AbstractValidator
{
	private static final long serialVersionUID = 1L;

	/**
	 * Allows all validly formatted schemes to pass validation instead of
	 * supplying a set of valid schemes.
	 */
	public static final int ALLOW_ALL_SCHEMES = 1 << 0;

	/**
	 * Allow two slashes in the path component of the URL.
	 */
	public static final int ALLOW_2_SLASHES = 1 << 1;

	/**
	 * Enabling this options disallows any URL fragments.
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
	//                                          12            3  4          5       6   7        8  9

	/**
	 * Schema/Protocol (ie. http:, ftp:, file:, etc).
	 */
	private static final int PARSE_URL_SCHEME = 2;

	/**
	 * Includes hostname/ip and port number.
	 */
	private static final int PARSE_URL_AUTHORITY = 4;

	private static final int PARSE_URL_PATH = 5;

	private static final int PARSE_URL_QUERY = 7;

	private static final int PARSE_URL_FRAGMENT = 9;

	/**
	 * Protocol (ie. http:, ftp:,https:).
	 */
	private static final String SCHEME_PATTERN = "^[" + SCHEME_CHARS + "].*$";

	private static final String AUTHORITY_PATTERN = "^([" + AUTHORITY_CHARS + "]*)(:\\d*)?(.*)?";
	// 1 2 3 4

	private static final int PARSE_AUTHORITY_HOST_IP = 1;

	private static final int PARSE_AUTHORITY_PORT = 2;

	/**
	 * Should always be empty.
	 */
	private static final int PARSE_AUTHORITY_EXTRA = 3;

	private static final String PATH_PATTERN = "^(/[-\\w:@&?=+,.!/~*'%$_;]*)?$";

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
	private Set allowedSchemes = new HashSet();

	/**
	 * If no schemes are provided, default to this set.
	 */
	protected String[] defaultSchemes = { "http", "https", "ftp" };

	/**
	 * Create a UrlValidator with default properties.
	 */
	public UrlValidator()
	{
		this(null);
	}

	/**
	 * Behavior of validation is modified by passing in several strings options:
	 * 
	 * @param schemes
	 *            Pass in one or more url schemes to consider valid, passing in
	 *            a null will default to "http,https,ftp" being valid. If a
	 *            non-null schemes is specified then all valid schemes must be
	 *            specified. Setting the ALLOW_ALL_SCHEMES option will ignore
	 *            the contents of schemes.
	 */
	public UrlValidator(String[] schemes)
	{
		this(schemes, 0);
	}

	/**
	 * Initialize a UrlValidator with the given validation options.
	 * 
	 * @param options
	 *            The options should be set using the public constants declared
	 *            in this class. To set multiple options you simply add them
	 *            together. For example, ALLOW_2_SLASHES + NO_FRAGMENTS enables
	 *            both of those options.
	 */
	public UrlValidator(int options)
	{
		this(null, options);
	}

	/**
	 * Behavour of validation is modified by passing in options:
	 * 
	 * @param schemes
	 *            The set of valid schemes.
	 * @param options
	 *            The options should be set using the public constants declared
	 *            in this class. To set multiple options you simply add them
	 *            together. For example, ALLOW_2_SLASHES + NO_FRAGMENTS enables
	 *            both of those options.
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
			schemes = this.defaultSchemes;
		}

		this.allowedSchemes.addAll(Arrays.asList(schemes));
	}

	protected void onValidate(IValidatable validatable)
	{
		String url = (String)validatable.getValue();
		if(url != null && !isValid(url)){
			error(validatable);
		}
	}

	/**
	 * <p>
	 * Checks if a field has a valid url address.
	 * </p>
	 * <p>
	 * This method is public because it is directly used in tests.
	 * </p>
	 * 
	 * @param value
	 *            The value validation is being performed on. A
	 *            <code>null</code> value is considered invalid.
	 * @return true if the url is valid.
	 */
	public final boolean isValid(String value)
	{
		if (value == null)
		{
			return false;
		}

		Matcher matchAsciiPat = Pattern.compile(LEGAL_ASCII_PATTERN).matcher(value);
		Matcher matchUrlPat = Pattern.compile(URL_PATTERN).matcher(value);


		if (!matchAsciiPat.matches())
		{
			return false;
		}

		// Check the whole url address structure
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
	 * Validate scheme. If schemes[] was initialized to a non null, then only
	 * those scheme's are allowed. Note this is slightly different than for the
	 * constructor.
	 * 
	 * @param scheme
	 *            The scheme to validate. A <code>null</code> value is
	 *            considered invalid.
	 * @return true if valid.
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

			if (!this.allowedSchemes.contains(scheme))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true if the authority is properly formatted. An authority is the
	 * combination of hostname and port. A <code>null</code> authority value
	 * is considered invalid.
	 * 
	 * @param authority
	 *            Authority value to validate.
	 * @return true if authority (hostname and port) is valid.
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
			for (int i = 0; i < chars.length; i++)
			{
				if (chars[i] == '.')
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
					hostIP = (segmentLength >= hostIP.length()) ? "" : hostIP
							.substring(segmentLength);

					segmentCount++;
				}
			}
			String topLevel = domainSegment[segmentCount - 1];
			if (topLevel.length() < 2 || topLevel.length() > 4)
			{
				return false;
			}

			// First letter of top level must be a alpha
			Matcher alphaMatcher = Pattern.compile(ALPHA_PATTERN).matcher(topLevel.substring(0, 1));

			if (!alphaMatcher.matches())
			{
				return false;
			}

			// Make sure there's a host name preceding the authority.
			if (segmentCount < 2)
			{
				return false;
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
	 * Returns true if the path is valid. A <code>null</code> value is
	 * considered invalid.
	 * 
	 * @param path
	 *            Path value to validate.
	 * @return true if path is valid.
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
		int dot2Count = countToken("..", path);
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
	 * Returns true if the query is null or it's a properly formatted query
	 * string.
	 * 
	 * @param query
	 *            Query value to validate.
	 * @return true if query is valid.
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
	 * Returns true if the given fragment is null or fragments are allowed.
	 * 
	 * @param fragment
	 *            Fragment value to validate.
	 * @return true if fragment is valid.
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
	 *            Token value to be counted.
	 * @param target
	 *            Target value to count tokens in.
	 * @return the number of tokens.
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
	 * <p>
	 * Checks if the field isn't null and length of the field is greater than
	 * zero not including whitespace.
	 * </p>
	 * 
	 * @param value
	 *            The value validation is being performed on.
	 * @return true if blank or null.
	 */
	public static boolean isBlankOrNull(String value)
	{
		return ((value == null) || (value.trim().length() == 0));
	}


	// Flag Management
	/**
	 * Tests whether the given flag is on. If the flag is not a power of 2 (ie.
	 * 3) this tests whether the combination of flags is on.
	 * 
	 * @param flags
	 *            list
	 * @param flag
	 *            Flag value to check.
	 * 
	 * 
	 * @return whether the specified flag value is on.
	 */
	public boolean isOn(long flag)
	{
		return (this.options & flag) > 0;
	}

	/**
	 * Tests whether the given flag is off. If the flag is not a power of 2 (ie.
	 * 3) this tests whether the combination of flags is off.
	 * 
	 * @param flag
	 *            Flag value to check.
	 * 
	 * @return whether the specified flag value is off.
	 */
	public boolean isOff(long flag)
	{
		return (options & flag) == 0;
	}

}
