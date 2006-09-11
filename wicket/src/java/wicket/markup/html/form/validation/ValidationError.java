/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationError implements IValidationError
{
	private List<String> keys = new ArrayList<String>(1);
	private Map<String, Object> params;

	public ValidationError()
	{

	}

	public ValidationError(String key)
	{
		addKey(key);
	}

	public ValidationError(String key, Map<String, Object> params)
	{
		if (params == null)
		{
			throw new IllegalArgumentException("Argument [[params]] cannot be null");
		}
		addKey(key);
		this.params = params;
	}


	public ValidationError addKey(String key)
	{
		if (key == null || key.trim().length() == 0)
		{
			throw new IllegalArgumentException("Argument [[key]] cannot be null or an empty string");
		}
		keys.add(key);
		return this;
	}


	public List<String> getKeys()
	{
		return keys;
	}

	public ValidationError setParam(String name, Object value)
	{
		if (name == null || name.trim().length() == 0)
		{
			throw new IllegalArgumentException(
					"Argument [[name]] cannot be null or an empty string");
		}
		if (value == null)
		{
			throw new IllegalArgumentException(
					"Argument [[value]] cannot be null or an empty string");
		}

		getParams().put(name, value);

		return this;
	}

	public Map<String, Object> getParams()
	{
		if (params == null)
		{
			params = new HashMap<String, Object>(2);
		}
		return params;
	}

	@SuppressWarnings("unchecked")
	public String getMessage(IMessageSource messageSource)
	{
		final Map<String, Object> p = (params == null) ? Collections.EMPTY_MAP : params;

		String message = null;

		for (String key : keys)
		{
			message = messageSource.getMessage(key, p);
			if (message != null)
			{
				break;
			}
		}
		return message;
	}

	@Override
	public String toString()
	{
		// FIXME 2.0: ivaynberg: implement this - specifically show resource
		// keys
		return super.toString();
	}

}
