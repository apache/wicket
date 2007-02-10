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

import java.util.Map;
import java.util.regex.Pattern;

import wicket.markup.html.form.FormComponent;
import wicket.util.parse.metapattern.MetaPattern;
import wicket.util.string.Strings;

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
 * <p>
 * The error message will be generated with the
 * key "PatternValidator" and the messages keys that can be used are:
 * <ul>
 * <li>${pattern}: the pattern which failed to match</li>
 * <li>${input}: the input the user did give</li>
 * <li>${name}: the name of the component that failed</li>
 * <li>${label}: the label of the component - either comes from
 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
 * that order</li>
 * </ul>
 * 
 * @see java.util.regex.Pattern
 * @see wicket.util.parse.metapattern.MetaPattern
 * @author Jonathan Locke
 */
public class PatternValidator extends StringValidator
{
	private static final long serialVersionUID = 1L;
	
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
	 * Validates the set pattern.
	 *
	 * @see StringValidator#onValidate(wicket.markup.html.form.FormComponent,String)
	 */
	public void onValidate(FormComponent formComponent, String value)
	{
		if (!Strings.isEmpty(value))
		{
			// Check value against pattern
			if (!pattern.matcher(value).matches())
			{
				error(formComponent);
			}
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

	
	protected Map messageModel(FormComponent formComponent)
	{
		final Map map = super.messageModel(formComponent);
		map.put("pattern", pattern);
		return map;
	}
	
	/**
	 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
	 */
	protected String resourceKey(FormComponent formComponent)
	{
		return "PatternValidator";
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[PatternValidator pattern = " + pattern + "]";
	}
}
