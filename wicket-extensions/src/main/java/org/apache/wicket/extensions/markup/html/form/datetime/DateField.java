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
package org.apache.wicket.extensions.markup.html.form.datetime;

import java.time.ZonedDateTime;

import org.apache.wicket.model.IModel;

/**
 * Works on a {@link java.util.Date} object. Displays a {@link DateTextField} and a
 * {@link DatePicker calendar popup}.<br/>
 * <p>
 * Note: {@link DateField} must <strong>not</strong> be associated with an
 * <code>&lt;input&gt;</code> tag, as opposed to {@link DateTextField}! The corresponding tag is
 * typically either a <code>&lt;div&gt;</code> or a <code>&lt;span&gt;</code> tag.
 * </p>
 * 
 * Example:
 * <p>
 * <u>Java:</u>
 * 
 * <pre>
 * DateField dateField = new DateField(&quot;birthday&quot;);
 * </pre>
 * 
 * </p>
 * <p>
 * <u>Markup:</u>
 * 
 * <pre>
 * &lt;div wicket:id=&quot;birthday&quot;&gt;&lt;/div&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author eelcohillenius
 */
public class DateField extends DateTimeField
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public DateField(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public DateField(String id, IModel<ZonedDateTime> model)
	{
		super(id, model);

		get(HOURS).setVisibilityAllowed(false);
		get(MINUTES).setVisibilityAllowed(false);
		get(AM_OR_PM_CHOICE).setVisibilityAllowed(false);
	}
}
