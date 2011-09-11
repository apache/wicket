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
package org.apache.wicket.behavior;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.lang.Args;

/**
 * A lightweight version of the attribute modifier. This is convenient for simpler situations where
 * you know the value upfront and you do not need a pull-based model.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @deprecated use {@link AttributeModifier#replace(String, java.io.Serializable)} instead
 */
@Deprecated
public class SimpleAttributeModifier extends Behavior
{
	private static final long serialVersionUID = 1L;

	/** The attribute */
	private final String attribute;

	/** The value to set */
	private final CharSequence value;

	/**
	 * Construct.
	 * 
	 * @param attribute
	 *            The attribute
	 * @param value
	 *            The value
	 */
	public SimpleAttributeModifier(final String attribute, final CharSequence value)
	{
		Args.notNull(attribute, "attribute");
		Args.notNull(value, "value");
		this.attribute = attribute;
		this.value = value;
	}

	/**
	 * @return the attribute
	 */
	public final String getAttribute()
	{
		return attribute;
	}

	/**
	 * @return the value to set
	 */
	public final CharSequence getValue()
	{
		return value;
	}

	/**
	 * @see org.apache.wicket.behavior.Behavior#onComponentTag(org.apache.wicket.Component,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		if (isEnabled(component))
		{
			tag.getAttributes().put(attribute, value);
		}
	}
}
