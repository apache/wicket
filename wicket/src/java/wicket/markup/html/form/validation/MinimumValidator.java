/*
 * $Id$
 * $Revision$
 * $Date$
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
/*
 * $Id$
 * $Revision$
 * $Date$
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

import wicket.markup.html.form.FormComponent;

/**
 * @author jcompagner
 */
public class MinimumValidator extends AbstractValidator
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Ensures the number is greater then zero.
	 */
	public static final MinimumValidator POSITIVE = new MinimumValidator(0);
	
	
	/**
	 * Creates a MinimumValidator for the given long minium value.
	 * 
	 * @param minimum long minimum value.
	 * @return The MinimumValidator
	 */
	public static MinimumValidator getInstance(long minimum)
	{
		return new MinimumValidator(minimum);
	}
	
	/**
	 * Creates a MinimumValidator for the given double minium value.
	 * 
	 * @param minimum double minimum value.
	 * @return The MinimumValidator
	 */
	public static MinimumValidator getInstance(double minimum)
	{
		return new MinimumValidator(minimum);
	}
	
	
	private final double minimum;
	
	/**
	 * Construct.
	 * @param minimum
	 */
	protected MinimumValidator(long minimum)
	{
		this.minimum = minimum;
	}

	/**
	 * Construct.
	 * @param minimum
	 */
	protected MinimumValidator(double minimum)
	{
		this.minimum = minimum;
	}
	
	/**
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public void validate(FormComponent component)
	{
		Number number = (Number)component.getConvertedInput();
		if(number != null)
		{
			if(number.doubleValue() < minimum)
			{
				error(component);
			}
		}
	}
	
	/**
	 * 'minimum' is the value that is given to the message. So that can be used in the properties file like
	 * MinimumValdiator= {$input} must be greater then {$min}
	 * 
	 * @see wicket.markup.html.form.validation.AbstractValidator#messageModel(wicket.markup.html.form.FormComponent)
	 */
	protected Map messageModel(FormComponent formComponent)
	{
		Map map = super.messageModel(formComponent);
		Number number = (Number)formComponent.getConvertedInput();
		if(number instanceof Double || number instanceof Float)
		{
			map.put("minimum", new Double(minimum));
		}
		else
		{
			map.put("minimum", new Long((long)minimum));
		}
		return map;
	}

}
