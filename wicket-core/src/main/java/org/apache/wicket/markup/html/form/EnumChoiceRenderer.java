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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.util.lang.Classes;

/**
 * {@link IChoiceRenderer} implementation that makes it easy to work with java 5 enums. This
 * renderer will attempt to lookup strings used for the display value using a localizer of a given
 * component. If the component is not specified, the global instance of localizer will be used for
 * lookups.
 * <p>
 * display value resource key format: {@code <enum.getSimpleClassName()>.<enum.name()>}
 * </p>
 * <p>
 * id value format: {@code <enum.name()>}
 * </p>
 * 
 * @author igor.vaynberg
 * 
 * @param <T>
 */
public class EnumChoiceRenderer<T extends Enum<T>> implements IChoiceRenderer<T>
{

	private static final long serialVersionUID = 1L;

	/**
	 * component used to resolve i18n resources for this renderer.
	 */
	private final Component resourceSource;


	/**
	 * Constructor that creates the choice renderer that will use global instance of localizer to
	 * resolve resource keys.
	 */
	public EnumChoiceRenderer()
	{
		resourceSource = null;
	}

	/**
	 * Constructor
	 * 
	 * @param resourceSource
	 */
	public EnumChoiceRenderer(Component resourceSource)
	{
		this.resourceSource = resourceSource;
	}

	/** {@inheritDoc} */
	@Override
	public final Object getDisplayValue(T object)
	{
		final String value;

		String key = resourceKey(object);

		if (resourceSource != null)
		{
			value = resourceSource.getString(key);
		}
		else
		{
			value = Application.get().getResourceSettings().getLocalizer().getString(key, null);
		}

		return postprocess(value);
	}

	/**
	 * Translates the {@code object} into resource key that will be used to lookup the value shown
	 * to the user
	 * 
	 * @param object
	 * @return resource key
	 */
	protected String resourceKey(T object)
	{
		return Classes.simpleName(object.getDeclaringClass()) + '.' + object.name();
	}

	/**
	 * Postprocesses the {@code value} after it is retrieved from the localizer. Default
	 * implementation escapes any markup found in the {@code value}.
	 * 
	 * @param value
	 * @return postprocessed value
	 */
	protected CharSequence postprocess(String value)
	{
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String getIdValue(T object, int index)
	{
		return object.name();
	}

}
