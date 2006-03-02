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

import java.util.HashMap;
import java.util.Map;

import wicket.markup.html.form.FormComponent;
import wicket.model.IModel;

/**
 * Base class for form validators
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractFormValidator implements IFormValidator
{

	protected Map messageModel()
	{
		FormComponent[] fcs = getDependentFormComponents();

		if (fcs != null && fcs.length > 0)
		{
			Map args = new HashMap(fcs.length * 2);
			for (int i = 0; i < fcs.length; i++)
			{
				final FormComponent fc = fcs[i];

				String arg = "label" + i;

				IModel label = fc.getLabel();

				if (label != null)
				{
					args.put(arg, label.getObject(fc));
				}
				else
				{
					args.put(arg, fc.getLocalizer().getString(fc.getId(), fc.getParent(),
							fc.getId()));
				}

				arg = "input" + i;
				args.put(arg, fc.getInput());
			}
			return args;
		}
		else
		{
			return new HashMap(2);
		}
	}
	

}
