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
package org.apache.wicket.bean.validation;

import jakarta.validation.constraints.Size;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * A tag modifier that adds the {@code maxlength} and  {@code minlength} attributes to the {@code input}
 * and {@code textarea} tag with the max/min value from the {@link Size} constraint annotation.
 * 
 * @author igor
 * 
 */
public class SizeTagModifier implements ITagModifier<Size>
{
	@Override
	public void modify(FormComponent<?> component, ComponentTag tag, Size annotation)
	{
		if (hasLengthAttribute(tag.getName()))
		{
			tag.put("maxlength", annotation.max());

			if (annotation.min() > 0)
			{
				tag.put("minlength", annotation.min());
			}
		}
	}

	protected boolean hasLengthAttribute(String tagName)
	{
		return "input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName);
	}
}
