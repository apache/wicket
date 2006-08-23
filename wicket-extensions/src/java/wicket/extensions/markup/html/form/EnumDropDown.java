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
package wicket.extensions.markup.html.form;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import wicket.MarkupContainer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.model.IModel;

public class EnumDropDown<T extends Enum> extends DropDownChoice<T>
{
	private static final long serialVersionUID = 1L;

	public EnumDropDown(MarkupContainer parent, String id, IModel<T> model, Class enumType)
	{
		super(parent, id);

		if (enumType == null)
		{
			throw new IllegalArgumentException("Argument [[enumType]] cannot be null");
		}
		else if (enumType.isEnum() == false)
		{
			throw new IllegalArgumentException("Argument [[enumType]] must be an enum");
		}


		setModel(model);
		setChoices((List<T>)Arrays.asList(enumType.getEnumConstants()));
		setChoiceRenderer(new EnumRenderer<T>());

	}

	private static class EnumRenderer<T extends Enum> implements IChoiceRenderer<T>
	{

		private static final long serialVersionUID = 1L;

		public Object getDisplayValue(T object)
		{
			return object.toString();
		}

		public String getIdValue(T object, int index)
		{
			return object.name();
		}

	}


}
