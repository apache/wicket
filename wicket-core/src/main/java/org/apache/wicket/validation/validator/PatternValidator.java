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

import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.parse.metapattern.MetaPattern;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Validates an {@link IValidatable} by matching the value against a regular expression pattern. A
 * <code>PatternValidator</code> can be constructed with either a Java regular expression (compiled
 * or not) or a {@link MetaPattern}. If the pattern matches against the value then it is considered
 * valid. If the pattern does not match, the an {@link IValidationError} will be reported on the
 * {@link IValidatable}.
 * <p>
 * For example, to restrict a field to only digits, you might add a <code>PatternValidator</code>
 * constructed with the pattern "\d+". Another way to do the same thing would be to construct the
 * <code>PatternValidator</code> passing in {@link MetaPattern#DIGITS}. The advantages of using
 * {@link MetaPattern} over straight Java regular expressions are that the patterns are easier to
 * construct and easier to combine into complex patterns. They are also more readable and more
 * reusable. See {@link MetaPattern} for details.
 * <p>
 * The error message will be generated with the key "PatternValidator" and one additional message
 * key ${pattern} for the pattern which failed to match. See {@link FormComponent} for a list of
 * further messages keys.
 * 
 * @see java.util.regex.Pattern
 * 
 * @author Jonathan Locke
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public class PatternValidator implements IValidator<String>
{
	private static final long serialVersionUID = 1L;

	/** the pattern to match */
	private final Pattern pattern;

	/** whether to exclude matching input **/
	private boolean reverse = false;

	/** the message key used to build the error message **/
	private String errorKey;

	/**
	 * Constructor that accepts a <code>String</code> regular expression pattern.
	 * 
	 * @param pattern
	 *            a regular expression pattern
	 */
	public PatternValidator(final String pattern)
	{
		this(Pattern.compile(pattern));
	}

	/**
	 * Constructor that accepts a <code>String</code> pattern and Java <code>regex</code> compile
	 * flags as arguments.
	 * 
	 * @param pattern
	 *            a regular expression pattern
	 * @param flags
	 *            compile flags for <code>java.util.regex.Pattern</code>
	 */
	public PatternValidator(final String pattern, final int flags)
	{
		this(Pattern.compile(pattern, flags));
	}

	/**
	 * Constructor that accepts a compiled pattern.
	 * 
	 * @param pattern
	 *            a compiled pattern
	 */
	public PatternValidator(final Pattern pattern)
	{
		this.pattern = pattern;
	}

	/**
	 * Constructor that accepts a <code>MetaPattern</code> argument.
	 * 
	 * @param pattern
	 *            a <code>MetaPattern</code>
	 */
	public PatternValidator(final MetaPattern pattern)
	{
		this(pattern.pattern());
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
	 * If set to true then input that matches the pattern is considered invalid.
	 * 
	 * @param reverse
	 * @return itself
	 */
	public PatternValidator setReverse(boolean reverse)
	{
		this.reverse = reverse;
		return this;
	}

	/**
	 * Gets the error message key.
	 * 
	 * @return the error message key
	 */
	public String getErrorKey()
	{
		return errorKey;
	}

	/**
	 * If set, this message key is used to build the error message.
	 * 
	 * @param errorKey
	 * @return itself
	 */
	public PatternValidator setErrorKey(String errorKey)
	{
		this.errorKey = errorKey;
		return this;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[PatternValidator pattern = " + pattern + "]";
	}

	/**
	 * Checks a value against this <code>PatternValidator</code>'s {@link Pattern}.
	 * 
	 * @param validatable
	 *            the <code>IValidatable</code> to check
	 */
	@Override
	public void validate(IValidatable<String> validatable)
	{
		// Check value against pattern
		if (pattern.matcher(validatable.getValue()).matches() == reverse)
		{
			ValidationError error = new ValidationError(this);
			if (errorKey != null)
			{
				error.setKeys(Collections.singletonList(errorKey));
			}
			error.setVariable("pattern", pattern.pattern());
			validatable.error(decorate(error, validatable));
		}
	}

	/**
	 * Allows subclasses to decorate reported errors
	 * 
	 * @param error
	 * @param validatable
	 * @return decorated error
	 */
	protected IValidationError decorate(IValidationError error, IValidatable<String> validatable)
	{
		return error;
	}
}