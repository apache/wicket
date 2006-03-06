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
public class MaximumValidator extends AbstractValidator
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Ensures the number is smaller then zero.
	 */
	public static final MaximumValidator NEGATIVE = new MaximumValidator(0);
	
	/**
	 * Creates a MaximumValidator for the given long minium value.
	 * 
	 * @param minimum long minimum value.
	 * @return The MaximumValidator
	 */
	public static MaximumValidator getInstance(long minimum)
	{
		return new MaximumValidator(minimum);
	}
	
	/**
	 * Creates a MaximumValidator for the given double minium value.
	 * 
	 * @param minimum double minimum value.
	 * @return The MaximumValidator
	 */
	public static MaximumValidator getInstance(double minimum)
	{
		return new MaximumValidator(minimum);
	}
	
	
	private final double maximum;
	
	/**
	 * Construct.
	 * @param maximum
	 */
	protected MaximumValidator(long maximum)
	{
		this.maximum = maximum;
	}

	/**
	 * Construct.
	 * @param maximum
	 */
	protected MaximumValidator(double maximum)
	{
		this.maximum = maximum;
	}
	
	/**
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public void validate(FormComponent component)
	{
		Number number = (Number)component.getConvertedInput();
		if(number != null)
		{
			if(number.doubleValue() > maximum)
			{
				error(component);
			}
		}
	}
	
	/**
	 * @see wicket.markup.html.form.validation.AbstractValidator#messageModel(wicket.markup.html.form.FormComponent)
	 */
	protected Map messageModel(FormComponent formComponent)
	{
		Map map = super.messageModel(formComponent);
		Number number = (Number)formComponent.getConvertedInput();
		if(number instanceof Double || number instanceof Float)
		{
			map.put("maximum", new Double(maximum));
		}
		else
		{
			map.put("maximum", new Long((long)maximum));
		}
		return map;
	}

}
