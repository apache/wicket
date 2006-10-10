/*
 * $Id: EmailAddressPatternValidator.java 5791 2006-05-20 00:32:57 +0000 (Sat,
 * 20 May 2006) joco01 $ $Revision: 7269 $ $Date: 2006-05-20 00:32:57 +0000
 * (Sat, 20 May 2006) $
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
package wicket.validation.validator;

import java.util.regex.Pattern;

/**
 * Validator for checking the form/pattern of email addresses.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 * @author Martijn Dashorst
 * 
 */
public class EmailAddressPatternValidator extends PatternValidator
{
	private static final long serialVersionUID = 1L;

	/** Singleton instance */
	private static final EmailAddressPatternValidator INSTANCE = new EmailAddressPatternValidator();


	/**
	 * @return Instance of emailadress validator
	 */
	public static EmailAddressPatternValidator getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Protected constructor to force use of static singleton accessor method.
	 * Or override it to implement resourceKey(Component)
	 */
	protected EmailAddressPatternValidator()
	{
		super("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,3})$",
				Pattern.CASE_INSENSITIVE);
	}

	@Override
	protected String resourceKey()
	{
		return "EmailAddressPatternValidator";
	}
}
