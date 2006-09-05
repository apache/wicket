/*
 * $Id: AbstractTextComponent.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25
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
package wicket.markup.html.form;

import wicket.MarkupContainer;
import wicket.model.IModel;
import wicket.util.convert.ConversionException;
import wicket.util.string.Strings;

/**
 * Abstract base class for TextArea and TextField.
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractTextComponent<T> extends FormComponent<T>
{
	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public AbstractTextComponent(MarkupContainer parent, String id)
	{
		super(parent, id);
		setConvertEmptyInputStringToNull(true);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	AbstractTextComponent(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);
		setConvertEmptyInputStringToNull(true);
	}

	/**
	 * Should the bound object become <code>null</code> when the input is
	 * empty?
	 * 
	 * @return <code>true</code> when the value will be set to
	 *         <code>null</code> when the input is empty.
	 */
	public final boolean getConvertEmptyInputStringToNull()
	{
		return getFlag(FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL);
	}

	/**
	 * TextFields return an empty string even if the user didn't type anything
	 * in them. To be able to work nicely with validation, this method returns
	 * false.
	 * 
	 * @see wicket.markup.html.form.FormComponent#isInputNullable()
	 */
	@Override
	public boolean isInputNullable()
	{
		return false;
	}

	/**
	 * Should the bound object become <code>null</code> when the input is
	 * empty?
	 * 
	 * @param flag
	 *            the value to set this flag.
	 * @return this
	 */
	public final FormComponent setConvertEmptyInputStringToNull(boolean flag)
	{
		setFlag(FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL, flag);
		return this;
	}

	/**
	 * @see FormComponent#supportsPersistence()
	 */
	@Override
	protected final boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#convertValue(String[])
	 */
	@Override
	protected T convertValue(String[] value) throws ConversionException
	{
		String tmp = value != null && value.length > 0 ? value[0] : null;
		if (getConvertEmptyInputStringToNull() && Strings.isEmpty(tmp))
		{
			return null;
		}
		return super.convertValue(value);
	}
}
