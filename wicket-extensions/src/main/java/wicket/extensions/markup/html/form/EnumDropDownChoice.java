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
package wicket.extensions.markup.html.form;

import java.util.Arrays;

import wicket.MarkupContainer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.model.IModel;

/**
 * Dropdown choice that makes it trivial to work with enums. Enum's
 * <code>name()</code> is used as the choice id and enum's
 * <code>toString()</code> is used for text.
 * 
 * <pre>
 *     public enum Color {
 *       WHITE(&quot;White&quot;),BLACK(&quot;Black&quot;);
 *       
 *       private String text;
 *       private Color(String text) { this.text=text; }
 *       
 *       public String toString() { return text; }
 *     }
 *     
 *     new EnumDropDownChoice&lt;Color&gt;(this, &quot;color&quot;, new PropertyModel(...), Color.class);
 * </pre>
 * 
 * 
 * 
 * 
 * @author ivaynberg
 * @param <T>
 */
public class EnumDropDownChoice<T extends Enum> extends DropDownChoice<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param enumType
	 *            class of enum used to provide choices
	 */
	public EnumDropDownChoice(MarkupContainer parent, String id, Class<T> enumType)
	{
		this(parent, id, null, enumType);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 * @param enumType
	 *            class of enum used to provide choices
	 */
	@SuppressWarnings("unchecked")
	public EnumDropDownChoice(MarkupContainer parent, String id, IModel<T> model, Class<T> enumType)
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
		setChoices(Arrays.asList(enumType.getEnumConstants()));
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
