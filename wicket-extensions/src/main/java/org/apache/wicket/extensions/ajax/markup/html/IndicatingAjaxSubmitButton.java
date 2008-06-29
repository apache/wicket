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

import org.apache.wicket.ajax.markup.html.form.AjaxSubmitButton;
import org.apache.wicket.markup.html.form.Form;

/**
 * A variant of the {@link AjaxSubmitButton} that displays a busy indicator while the ajax request
 * is in progress.
 * 
 * @author evan
 * @deprecated Use {@link IndicatingAjaxButton} instead.
 */
@Deprecated
public abstract class IndicatingAjaxSubmitButton extends IndicatingAjaxButton
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param id
	 * @param form
	 */
	public IndicatingAjaxSubmitButton(String id, Form<?> form)
	{
		super(id, form);
	}
}