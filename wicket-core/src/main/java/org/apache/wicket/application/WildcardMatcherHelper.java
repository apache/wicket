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
package org.apache.wicket.application;

import java.util.HashMap;
import java.util.Map;


/**
 * This class is an utility class that perform wildcard-patterns matching and isolation.
 * 
 * @version $Id: WildcardMatcherHelper.java 420832 2006-07-11 13:11:02Z cziegeler $
 */
public class WildcardMatcherHelper
{
	// ~ Static fields/initializers
	// -----------------------------------------------------------------

	/** Escape character */
	public static final char ESC = '\\';

	/** Default path separator: "." */
	public static final char PATHSEP = '.';

	/** any char */
	public static final char STAR = '*';

	// ~ Methods
	// ------------------------------------------------------------------------------------

	/**
	 * Match a pattern against a string and isolates wildcard replacement into a <code>Map</code>. <br>
	 * Here is how the matching algorithm works:
	 * 
	 * <ul>
	 * <li>The '*' character, meaning that zero or more characters (excluding the path separator
	 * '/') are to be matched.</li>
	 * <li>The '**' sequence, meaning that zero or more characters (including the path separator
	 * '/') are to be matched.</li>
	 * <li>The '\*' sequence is honored as a literal '*' character, not a wildcard</li>
	 * </ul>
	 * <br>
	 * When more than two '*' characters, not separated by another character, are found their value
	 * is considered as '**' and immediate succeeding '*' are skipped. <br>
	 * The '**' wildcard is greedy and thus the following sample cannot match:
	 * <dl>
	 * <dt>pattern</dt>
	 * <dd>STAR,STAR,PATHSEP,STAR,PATHSEP,STAR,STAR (why can't I express it literally?)</dt>
	 * <dt>string</dt>
	 * <dd>foo/bar/baz/bug</dt>
	 * </dl>
	 * The first '**' in the pattern will suck up until the last '/' in string and thus will not
	 * match. <br>
	 * A more advance algorithm could certainly check whether there is an other literal later in the
	 * pattern to ev. match in string but I'll leave this exercise to someone else ATM if one needs
	 * such.
	 * 
	 * @param pat
	 *            The pattern string.
	 * @param str
	 *            The string to math against the pattern
	 * 
	 * @return a <code>Map</code> containing the representation of the extracted pattern. The
	 *         extracted patterns are keys in the <code>Map</code> from left to right beginning with
	 *         "1" for the left most, "2" for the next, a.s.o. The key "0" is the string itself. If
	 *         the return value is null, string does not match to the pattern .
	 */
	public static Map<String, String> match(final String pat, final String str)
	{
		final Matcher map = new Matcher(pat, str);

		if (map.isMatch())
		{
			return map.getMap();
		}

		return null;
	}

	// ~ Inner Classes
	// ------------------------------------------------------------------------------

	/**
	 * The private matcher class
	 */
	private static class Matcher
	{
		// ~ Instance fields
		// ------------------------------------------------------------------------

		/** The character array of the pattern */
		private final char[] apat;

		/** The length of the character array of the pattern */
		private final int lpat;

		/** The character array of the string */
		private final char[] astr;

		/** The length of the character array of the string */
		private final int lstr;

		/** The <code>Map</code> to be filled */
		private final Map<String, String> map = new HashMap<String, String>();

		/** Whether string matched to pattern */
		private final boolean matched;

		/** map index */
		private int idx = 0;

		/** index into pattern */
		private int ipat = 0;

		/** index into string */
		private int istr = 0;

		// ~ Constructors
		// ---------------------------------------------------------------------------

		/**
		 * Creates a new Matcher object.
		 * 
		 * @param pat
		 *            The pattern
		 * @param str
		 *            The string
		 */
		public Matcher(final String pat, final String str)
		{
			apat = pat.toCharArray();
			lpat = apat.length;
			astr = str.toCharArray();
			lstr = astr.length;
			add(str);
			matched = match();
		}

		// ~ Methods
		// --------------------------------------------------------------------------------

		/**
		 * DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 */
		public Map<String, String> getMap()
		{
			return map;
		}

		/**
		 * Has it matched?
		 * 
		 * @return whether it has matched
		 */
		public boolean isMatch()
		{
			return matched;
		}

		/**
		 * Add a extracted substring to the map
		 * 
		 * @param aStr
		 *            The extracted substring
		 */
		private void add(final String aStr)
		{
			String key = String.valueOf(idx++).intern();
			map.put(key, aStr);
		}

		/**
		 * Scans the pattern and the search string from the start toward the end
		 * 
		 * @return whether the string matches the pattern
		 */
		private boolean match()
		{
			// scan a common literal prefix
			scanLiteralPrefix();

			// if we are already at the end of both strings
			// than the pattern matched
			if (ipat >= lpat && istr >= lstr)
			{
				return true;
			}

			// if hole string has matched the pattern so far and the rest of the pattern only has
			// wildcard(s)
			// we match too otherwise we clearly don't match
			if (ipat < lpat && istr >= lstr)
			{
				while (ipat < lpat && apat[ipat] == STAR)
				{
					ipat++;
				}

				if (ipat >= lpat)
				{
					add("");

					return true;
				}
				else
				{
					return false;
				}
			}

			// if hole pattern has matched the string so far but the string has more characters left
			// we don't match
			if (ipat >= lpat && istr < lstr)
			{
				return false;
			}

			// if we have not stopped at a wildcard character
			// a character doesn't match and thus we do not match at all
			if (apat[ipat] != STAR)
			{
				return false;
			}

			// if it is a double (or more) wildcard pattern
			if (ipat < lpat - 1 && apat[ipat + 1] == STAR)
			{
				// skip to first non star character in the pattern
				while (++ipat < lpat && apat[ipat] == STAR)
				{
					; // noop
				}

				// if we are at the end of the pattern we've matched and are finish scanning
				if (ipat >= lpat)
				{
					add(new String(astr, istr, lstr - istr));

					return true;
				}

				// Now we need to scan for the end of the literal characters in the pattern
				final int sipat = ipat; // start position of a literal character used for substring
				// operations

				while (ipat < lpat && (apat[ipat] != STAR || (ipat > 0 && apat[ipat - 1] == ESC)))
				{
					ipat++;
				}

				// if we reached the end of the pattern just do a string compare with the
				// corresponding part from
				// the end of the string
				if (ipat >= lpat)
				{
					return checkEnds(sipat, false);
				}

				// Now we need to check whether the literal substring of the pattern
				// is contained in the string somewhere
				final int l = ipat - sipat;
				int eistr = lstr - l;

				// because the '**' wildcard need to be greedy we scan from the end of the string
				// for a match
				while (istr < eistr && !strncmp(apat, sipat, astr, eistr, l))
				{
					eistr--;
				}

				if (istr >= eistr)
				{
					return false;
				}

				add(new String(astr, istr, eistr - istr));
				istr = eistr + l;
			}
			else
			{// if it is a single star pattern
				// skip the star
				++ipat;

				// if we are at the beginning of the pattern we have to check there is no PATH_SEP
				// in string
				if (ipat >= lpat)
				{
					final int sistr = istr;

					while (istr < lstr && (astr[istr] != PATHSEP))
					{
						istr++;
					}

					if (istr >= lstr)
					{
						add(new String(astr, sistr, lstr - sistr));

						return true;
					}

					// otherwise we do not match
					return false;
				}

				// Now we need to search for the start of either a path separator or another
				// wildcard characters
				// in the pattern
				final int sipat = ipat;

				while (ipat < lpat && apat[ipat] != STAR &&
					(apat[ipat] != ESC || ipat < lpat - 1 && apat[ipat + 1] != STAR) &&
					apat[ipat] != PATHSEP)
				{
					ipat++;
				}

				// if we reached the end of the pattern just do a string compare with the
				// corresponding part from
				// the end of the string
				if (ipat >= lpat)
				{
					return checkEnds(sipat, true);
				}

				// If we stopped at an other wildcard
				// we exclude it from being compared
				if (apat[ipat] == STAR)
				{
					ipat--;
				}

				// Now we need to check whether the literal substring of the pattern
				// is contained in the string somewhere
				final int l = ipat - sipat + 1;
				final int sistr = istr;

				while (istr < lstr && !strncmp(apat, sipat, astr, istr, l))
				{
					istr++;
				}

				if (istr >= lstr)
				{
					return false;
				}

				add(new String(astr, sistr, istr - sistr));
				ipat++;
				istr += l;
			}

			return match();
		}

		/**
		 * Scan a possible common suffix
		 */
		private final void scanLiteralPrefix()
		{
			// scan a common literal suffix
			while (ipat < lpat &&
				istr < lstr &&
				(apat[ipat] == ESC && ipat < lpat - 1 && apat[ipat + 1] == STAR &&
					apat[++ipat] == astr[istr] || apat[ipat] != STAR && apat[ipat] == astr[istr]))
			{
				ipat++;
				istr++;
			}
		}

		/**
		 * Compare two character array from individual offsets
		 * 
		 * @param a1
		 *            The first character array
		 * @param o1
		 *            The offset into the first character array
		 * @param a2
		 *            The second character array
		 * @param o2
		 *            The offset into the second character array
		 * @param l
		 *            The length to compare
		 * 
		 * @return Whether the all the mentioned characters match each other
		 */
		private final boolean strncmp(final char[] a1, final int o1, final char[] a2, final int o2,
			final int l)
		{
			int i = 0;

			while (i < l && o1 + i < a1.length && o2 + i < a2.length && a1[o1 + i] == a2[o2 + i])
			{
				i++;
			}

			return i == l;
		}

		private final boolean checkEnds(final int sipat, final boolean isSingleStart)
		{
			// if the remaining length of the string isn't the same as that found in the pattern
			// we do not match
			final int l = lpat - sipat; // calculate length of comparison
			final int ostr = lstr - l; // calculate offset into string
			if (ostr >= 0 && strncmp(apat, sipat, astr, ostr, l))
			{
				if (isSingleStart)
				{
					// if the ends matches make sure there isn't a PATHSEP in the candidate string
					// part
					int i = ostr - istr;
					while (i > istr)
					{
						if (astr[--i] == PATHSEP)
						{
							return false; // we cannot match because of a PATHSEP in the matched
							// part
						}
					}
				}
				add(new String(astr, istr, ostr - istr));

				return true;
			}

			// otherwise we do not match
			return false;
		}
	}
}
