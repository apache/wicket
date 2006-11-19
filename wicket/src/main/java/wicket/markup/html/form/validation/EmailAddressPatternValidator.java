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
package wicket.markup.html.form.validation;

import java.util.regex.Pattern;

import wicket.markup.html.form.FormComponent;

/**
 * Validator for checking the form/pattern of email addresses.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 * @author Martijn Dashorst
 */
public class EmailAddressPatternValidator extends PatternValidator
{
	private static final long serialVersionUID = 1L;
	
	/** Singleton instance */
	private static final EmailAddressPatternValidator instance = new EmailAddressPatternValidator();
	
	
	/**
	 * @return Instance of emailadress validator
	 */	
	public static  EmailAddressPatternValidator getInstance()
	{
		return instance;
	}
	
	/**
	 * Protected constructor to force use of static singleton accessor method.
	 * Or override it to implement resourceKey(Component)
	 */
	protected EmailAddressPatternValidator()
	{
		super("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)", Pattern.CASE_INSENSITIVE);
	}

	/**
	 * @see wicket.markup.html.form.validation.AbstractValidator#resourceKey(wicket.markup.html.form.FormComponent)
	 */
	protected String resourceKey(FormComponent formComponent)
	{
		return "EmailAddressPatternValidator";
	}
}
