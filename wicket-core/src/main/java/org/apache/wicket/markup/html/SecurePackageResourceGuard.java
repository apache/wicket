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
package org.apache.wicket.markup.html;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.apache.wicket.util.collections.ReverseListIterator;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a resource guard which by default denies access to all resources and thus is more secure.
 * <p/>
 * All pattern are executed in the order they were provided. All pattern are executed to determine
 * if access can be granted or not.
 * <p/>
 * Note that access to the config data such as get/setPattern() and acceptXXX() is not synchronized.
 * It is assumed that configuration has finished before the first request gets executed.
 * <p/>
 * The rules are fairly simple. Each pattern must start with either "+" (include) or "-" (exclude).
 * "*" is a placeholder for zero, one or more characters within a file or directory name. "**" is a
 * placeholder for zero, one or more sub-directories.
 * <p/>
 * Examples:
 * <table border="0">
 * <tr>
 * <td>+*.gif</td>
 * <td>All gif files in all directories</td>
 * </tr>
 * <tr>
 * <td>+test*.*</td>
 * <td>All files in all directories starting with "test"</td>
 * </tr>
 * <tr>
 * <td>+mydir&#47;*&#47;*.gif</td>
 * <td>All gif files two levels below the mydir directory. E.g. mydir&#47;dir2&#47;test.gif</td>
 * </tr>
 * <tr>
 * <td>+mydir&#47;**&#47;*.gif</td>
 * <td>All gif files in all directories below mydir. E.g. mydir&#47;test.gif or
 * mydir&#47;dir2&#47;dir3&#47;test.gif</td>
 * </tr>
 * </table>
 * 
 * @see IPackageResourceGuard
 * @see org.apache.wicket.settings.ResourceSettings#getPackageResourceGuard
 * @see PackageResourceGuard
 * 
 * @author Juergen Donnerstag
 */
public class SecurePackageResourceGuard extends PackageResourceGuard
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(SecurePackageResourceGuard.class);

	/** The path separator used */
	private static final char PATH_SEPARATOR = '/';

	/** The list of pattern. Note that the order is important, hence a list */
	private List<SearchPattern> pattern = new ArrayList<>();

	/** A cache to speed up the checks */
	private final ConcurrentMap<String, Boolean> cache;

	/**
	 * Constructor.
	 */
	public SecurePackageResourceGuard()
	{
		this(new SimpleCache(100));
	}

	/**
	 * Constructor.
	 * 
	 * @param cache
	 *            the internal cache that will hold the results for all already checked resources.
	 *            Use {@code null} to disable caching.
	 */
	public SecurePackageResourceGuard(final ConcurrentMap<String, Boolean> cache)
	{
		this.cache = cache;

		// the order is important for better performance
		// first add the most commonly used
		addPattern("+*.js");
		addPattern("+*.css");
		addPattern("+*.png");
		addPattern("+*.jpg");
		addPattern("+*.jpeg");
		addPattern("+*.gif");
		addPattern("+*.ico");
		addPattern("+*.cur");

		// WICKET-208 non page templates may be served
		addPattern("+*.html");

		addPattern("+*.txt");
		addPattern("+*.swf");
		addPattern("+*.bmp");
		addPattern("+*.svg");

		// allow web fonts
		addPattern("+*.eot");
		addPattern("+*.ttf");
		addPattern("+*.woff");

	}

	/**
	 * 
	 */
	public void clearCache()
	{
		if (cache != null)
		{
			cache.clear();
		}
	}

	/**
	 * Whether the provided absolute path is accepted.
	 * 
	 * @param path
	 *            The absolute path, starting from the class root (packages are separated with
	 *            forward slashes instead of dots).
	 * @return True if accepted, false otherwise.
	 */
	@Override
	public boolean accept(String path)
	{
		// First check the cache
		if (cache != null)
		{
			Boolean rtn = cache.get(path);
			if (rtn != null)
			{
				return rtn;
			}
		}

		// Check typical files such as log4j.xml etc.
		if (super.accept(path) == false)
		{
			return false;
		}

		// Check against the pattern
		boolean hit = false;
		for (SearchPattern pattern : new ReverseListIterator<>(this.pattern))
		{
			if ((pattern != null) && pattern.isActive())
			{
				if (pattern.matches(path))
				{
					hit = pattern.isInclude();
					break;
				}
			}
		}

		if (cache != null)
		{
			// Do not use putIfAbsent(). See newCache()
			cache.put(path, (hit ? Boolean.TRUE : Boolean.FALSE));
		}

		if (hit == false)
		{
			log.warn("Access denied to shared (static) resource: " + path);
		}

		return hit;
	}

	/**
	 * Gets the current list of pattern. Please invoke clearCache() or setPattern(List) when
	 * finished in order to clear the cache of previous checks.
	 * 
	 * @return pattern
	 */
	public List<SearchPattern> getPattern()
	{
		clearCache();
		return pattern;
	}

	/**
	 * Sets pattern.
	 * 
	 * @param pattern
	 *            pattern
	 */
	public void setPattern(List<SearchPattern> pattern)
	{
		this.pattern = pattern;
		clearCache();
	}

	/**
	 * @param pattern
	 */
	public void addPattern(String pattern)
	{
		this.pattern.add(new SearchPattern(pattern));
		clearCache();
	}

	/**
	 * 
	 */
	public static class SearchPattern
	{
		private String pattern;

		private Pattern regex;

		private boolean include;

		private boolean active = true;

		private boolean fileOnly;

		/**
		 * Construct.
		 * 
		 * @param pattern
		 */
		public SearchPattern(final String pattern)
		{
			setPattern(pattern);
		}

		/**
		 * 
		 * @param pattern
		 * @return Regex pattern
		 */
		private Pattern convertToRegex(final String pattern)
		{
			String regex = Strings.replaceAll(pattern, ".", "#dot#").toString();

			// If path starts with "*/" or "**/"
			regex = regex.replaceAll("^\\*" + PATH_SEPARATOR, "[^" + PATH_SEPARATOR + "]+" +
				PATH_SEPARATOR);
			regex = regex.replaceAll("^[\\*]{2,}" + PATH_SEPARATOR, "([^" + PATH_SEPARATOR +
				"].#star#" + PATH_SEPARATOR + ")?");

			// Handle "/*/" and "/**/"
			regex = regex.replaceAll(PATH_SEPARATOR + "\\*" + PATH_SEPARATOR, PATH_SEPARATOR +
				"[^" + PATH_SEPARATOR + "]+" + PATH_SEPARATOR);
			regex = regex.replaceAll(PATH_SEPARATOR + "[\\*]{2,}" + PATH_SEPARATOR, "(" +
				PATH_SEPARATOR + "|" + PATH_SEPARATOR + ".+" + PATH_SEPARATOR + ")");

			// Handle "*" within dir or file names
			regex = regex.replaceAll("\\*+", "[^" + PATH_SEPARATOR + "]*");

			// replace placeholder
			regex = Strings.replaceAll(regex, "#dot#", "\\.").toString();
			regex = Strings.replaceAll(regex, "#star#", "*").toString();

			return Pattern.compile(regex);
		}

		/**
		 * Gets pattern.
		 * 
		 * @return pattern
		 */
		public String getPattern()
		{
			return pattern;
		}

		/**
		 * Gets regex.
		 * 
		 * @return regex
		 */
		public Pattern getRegex()
		{
			return regex;
		}

		/**
		 * Sets pattern.
		 * 
		 * @param pattern
		 *            pattern
		 */
		public void setPattern(String pattern)
		{
			if (Strings.isEmpty(pattern))
			{
				throw new IllegalArgumentException(
					"Parameter 'pattern' can not be null or an empty string");
			}

			if (pattern.charAt(0) == '+')
			{
				include = true;
			}
			else if (pattern.charAt(0) == '-')
			{
				include = false;
			}
			else
			{
				throw new IllegalArgumentException(
					"Parameter 'pattern' must start with either '+' or '-'. pattern='" + pattern +
						"'");
			}

			this.pattern = pattern;
			regex = convertToRegex(pattern.substring(1));

			fileOnly = (pattern.indexOf(PATH_SEPARATOR) == -1);
		}

		/**
		 * 
		 * @param path
		 * @return True if 'path' matches the pattern
		 */
		public boolean matches(String path)
		{
			if (fileOnly)
			{
				path = Strings.lastPathComponent(path, PATH_SEPARATOR);
			}
			return regex.matcher(path).matches();
		}

		/**
		 * Gets include.
		 * 
		 * @return include
		 */
		public boolean isInclude()
		{
			return include;
		}

		/**
		 * Sets include.
		 * 
		 * @param include
		 *            include
		 */
		public void setInclude(boolean include)
		{
			this.include = include;
		}

		/**
		 * Gets active.
		 * 
		 * @return active
		 */
		public boolean isActive()
		{
			return active;
		}

		/**
		 * Sets active.
		 * 
		 * @param active
		 *            active
		 */
		public void setActive(boolean active)
		{
			this.active = active;
		}

		@Override
		public String toString()
		{
			return "Pattern: " + pattern + ", Regex: " + regex + ", include:" + include +
				", fileOnly:" + fileOnly + ", active:" + active;
		}
	}

	/**
	 * A very simple cache
	 */
	public static class SimpleCache extends ConcurrentHashMap<String, Boolean>
	{
		private static final long serialVersionUID = 1L;

		private final ConcurrentLinkedQueue<String> fifo = new ConcurrentLinkedQueue<>();

		private final int maxSize;

		/**
		 * Construct.
		 * 
		 * @param maxSize
		 */
		public SimpleCache(int maxSize)
		{
			this.maxSize = maxSize;
		}

		/**
		 * @see java.util.concurrent.ConcurrentHashMap#put(java.lang.Object, java.lang.Object)
		 */
		@Override
		public Boolean put(String key, Boolean value)
		{
			// add the key to the hash map. Do not replace existing once
			Boolean rtn = super.putIfAbsent(key, value);

			// If found, than remove it from the fifo list and ...
			if (rtn != null)
			{
				fifo.remove(key);
			}

			// append it at the end of the list
			fifo.add(key);

			// remove all "outdated" cache entries
			while (fifo.size() > maxSize)
			{
				remove(fifo.poll());
			}
			return rtn;
		}
	}
}
