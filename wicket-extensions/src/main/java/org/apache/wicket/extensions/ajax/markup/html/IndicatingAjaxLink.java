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
package org.apache.wicket.extensions.ajax.markup.html;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Args;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;

/**
 * A variant of the {@link AjaxLink} that displays a busy indicator while the ajax request is in
 * progress.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 * 
 */
public abstract class IndicatingAjaxLink<T> extends AjaxLink<T> implements IAjaxIndicatorAware
{
	private static final long serialVersionUID = 1L;
	private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public IndicatingAjaxLink(final String id)
	{
		this(id, null);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public IndicatingAjaxLink(final String id, final IModel<T> model)
	{
		super(id, model);
		add(indicatorAppender);
	}

	/**
	 * @see org.apache.wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
	 */
	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return indicatorAppender.getMarkupId();
	}


	public static <T> IndicatingAjaxLink<T> onClick(String id,
		SerializableBiConsumer<AjaxLink<T>, AjaxRequestTarget> onClick)
	{
		Args.notNull(onClick, "onClick");

		return new IndicatingAjaxLink<T>(id)
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onClick.accept(this, target);
			}
		};
	}
}
