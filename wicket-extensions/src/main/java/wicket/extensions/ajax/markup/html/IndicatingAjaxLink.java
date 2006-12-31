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
package wicket.extensions.ajax.markup.html;

import wicket.MarkupContainer;
import wicket.ajax.IAjaxIndicatorAware;
import wicket.ajax.markup.html.AjaxLink;
import wicket.model.IModel;

/**
 * A variant of the {@link AjaxLink} that displays a busy indicator while the
 * ajax request is in progress.
 * 
 * @param <T>
 *            The type
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class IndicatingAjaxLink<T> extends AjaxLink<T> implements IAjaxIndicatorAware
{
	private final WicketAjaxIndicatorAppender indicatorAppender = new WicketAjaxIndicatorAppender();

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public IndicatingAjaxLink(MarkupContainer parent, final String id)
	{
		this(parent, id, null);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String,IModel)
	 */
	public IndicatingAjaxLink(MarkupContainer parent, final String id, IModel<T> model)
	{
		super(parent, id, model);
		add(indicatorAppender);
	}

	/**
	 * @see wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
	 */
	public String getAjaxIndicatorMarkupId()
	{
		return indicatorAppender.getMarkupId();
	}
}
