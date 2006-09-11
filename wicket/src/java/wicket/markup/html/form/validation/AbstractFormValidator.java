/*
 * $Id: AbstractFormValidator.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19
 * May 2006) $
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

import java.util.HashMap;
import java.util.Map;

import wicket.util.lang.Classes;

/**
 * Base class for {@link wicket.markup.html.form.validation.IFormValidator}s.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractFormValidator implements IFormValidator
{
	/**
	 * Gets the default variables for interpolation.
	 * 
	 * @return a map with the variables for interpolation
	 */
	protected Map<String, Object> messageModel()
	{
		return new HashMap<String, Object>(2);
	}

	/**
	 * Gets the resource key for validator's error message from the
	 * ApplicationSettings class.
	 * 
	 * @return the resource key based on the form component
	 */
	protected String resourceKey()
	{
		return Classes.simpleName(getClass());
	}
}