/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.validation;

import java.util.regex.Pattern;

import wicket.markup.html.form.FormComponent;
import wicket.util.parse.metapattern.MetaPattern;

/**
 * Validates component by matching the component's value against a regular
 * expression pattern. A PatternValidator can be constructed with either a Java
 * regular expression (compiled or not) or a MetaPattern. If the pattern matches
 * against the value of the component it is attached to when validate() is
 * called by the framework, then that input value is considered valid. If the
 * pattern does not match, the errorMessage() method will be called.
 * <p>
 * For example, to restrict a field to only digits, you might add a
 * PatternValidator constructed with the pattern "\d+". Another way to do the
 * same thing would be to construct the PatternValidator passing in
 * MetaPattern.DIGITS. The advantages of using MetaPattern over straight Java
 * regular expressions are that the patterns are easier to construct and easier
 * to combine into complex patterns. They are also more readable and more
 * reusable. See {@link wicket.util.parse.metapattern.MetaPattern}for details.
 * 
 * @see java.util.regex.Pattern
 * @see wicket.util.parse.metapattern.MetaPattern
 * @author Jonathan Locke
 */
public class PatternValidator extends AbstractValidator
{
	/** The regexp pattern. */
	private final Pattern pattern;

	/**
	 * Constructor.
	 * 
	 * @param pattern
	 *            Regular expression pattern
	 */
	public PatternValidator(final String pattern)
	{
		this(Pattern.compile(pattern));
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern
	 *            Regular expression pattern
	 * @param flags
	 *            Compile flags for java.util.regex.Pattern
	 */
	public PatternValidator(final String pattern, final int flags)
	{
		this(Pattern.compile(pattern, flags));
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern
	 *            Java regex pattern
	 */
	public PatternValidator(final Pattern pattern)
	{
		this.pattern = pattern;
	}

	/**
	 * Constructor.
	 * 
	 * @param pattern
	 *            MetaPattern pattern
	 */
	public PatternValidator(final MetaPattern pattern)
	{
		this(pattern.pattern());
	}

	/**
	 * Validates the given form component.
	 * 
	 * @param component
	 *            The component to validate
	 */
	public final void validate(final FormComponent component)
	{
		// Get component value
		final String value = component.getRequestString();

		// Check value against pattern
		if (!pattern.matcher(value).matches())
		{
			error(component, value);
		}
	}

	/**
	 * Gets the regexp pattern.
	 * 
	 * @return the regexp pattern
	 */
	public final Pattern getPattern()
	{
		return pattern;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[PatternValidator pattern = " + pattern + "]";
	}
}
