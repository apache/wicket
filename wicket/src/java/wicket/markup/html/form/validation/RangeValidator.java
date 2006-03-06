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
public class RangeValidator extends AbstractValidator
{
	private static final long serialVersionUID = 1L;
	private final double max;
	private final double min;

	/**
	 * Creates a RangeValidator for a range between 2 long/integer values.
	 * 
	 * @param min Minimum long value
	 * @param max Maximum long value
	 * @return The RangeValidator
	 */
	public static RangeValidator range(long min, long max)
	{
		return new RangeValidator(min,max);
	}
	
	/**
	 * Creates a RangeValidator for a range between 2 double/float values.
	 * 
	 * @param min Minimum double value
	 * @param max Maximum double value
	 * @return The RangeValidator
	 */
	public static RangeValidator range(double min, double max)
	{
		return new RangeValidator(min,max);
	}
	
	protected RangeValidator(long min, long max)
	{
		this.min = min;
		this.max = max;
	}

	protected RangeValidator(double min, double max)
	{
		this.min = min;
		this.max = max;
	}
	
	/**
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public void validate(FormComponent component)
	{
		Number number = (Number)component.getConvertedInput();
		if(number != null)
		{
			if(number.doubleValue() < min || number.doubleValue() > max) 
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
			map.put("min", new Double(min));
			map.put("max", new Double(max));
		}
		else
		{
			map.put("min", new Long((long)min));
			map.put("max", new Long((long)max));
		}
		return map;
	}
}
