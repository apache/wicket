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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;

/**
 * Works on a {@link java.time.ZonedDateTime} object. See {@link AbstractDateTimeField} for
 * further details.
 * <p>
 * If you want to Ajaxify this component with an {@link AjaxFormComponentUpdatingBehavior}, it be done in 2 ways:
 * </p>
 * <ul>
 *     <li>
 *         On <code>ZonedDateTimeField</code>: easy, less code, larger requests, and (unfortunately) excessive requests.
 *         <p>
 *         Create a subclass and:
 *         <ul>
 *             <li>
 *                 Set {@link #WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE} to <code>true</code>.
 *             </li>
 *             <li>
 *                 Add the <code>AjaxFormComponentUpdatingBehavior</code> with event <code>"input change"</code> to it.
 *             </li>
 *             <li>
 *                 Override {@link #newTimeField(String, IModel)} and also set
 *                 {@link #WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE} to <code>true</code> on the component created
 *                 by the superclass.
 *             </li>
 *         </ul>
 *     </li>
 *     <li>
 *         On the descendent form components: cumbersone, quite a bit of code, but few, smallest possible requests.
 *         <p>
 *         Create a subclass and:
 *         <ul>
 *             <li>
 *                 Override {@link AbstractDateTimeField#newDateField(String, IModel)} and add a
 *                 <code>AjaxFormComponentUpdatingBehavior</code> to the component created by the superclass.
 *                 <p>
 *                 {@link IModel#setObject(Object)} of the date field is a no-op. So use
 *                 {@link FormComponent#getConvertedInput()} to get the submitted date, and update the model object of
 *                 this field manually.
 *             </li>
 *             <li>
 *                 Override {@link Component#onInitialize()} and
 *                 {@link AbstractDateTimeField#getTimeField() get the timefield}. Use
 *                 {@link TimeField#getHoursField()}, {@link TimeField#getMinutesField()} and
 *                 {@link TimeField#getAmOrPmChoice()} to get the subfields, and add
 *                 <code>AjaxFormComponentUpdatingBehavior</code> with event <code>"change"</code> to them.
 *                 <p>
 *                 {@link IModel#setObject(Object)} of these fields is also a no-op. So again use
 *                 <code>getConvertedInput()</code> to get the submitted values, and update the model object of these
 *                 fields manually.
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author eelcohillenius
 */
public class ZonedDateTimeField extends AbstractDateTimeField<ZonedDateTime>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public ZonedDateTimeField(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public ZonedDateTimeField(final String id, final IModel<ZonedDateTime> model)
	{
		super(id, model);

		// Sets the type that will be used when updating the model for this component.
		setType(ZonedDateTime.class);
	}

	/**
	 * Creates a zoned date time in the systems default zone.
	 * 
	 * @see ZoneId#systemDefault()
	 */
	@Override
	protected ZonedDateTime createTemporal(LocalDate date, LocalTime time)
	{
		return ZonedDateTime.of(date, time, ZoneId.systemDefault());
	}

	@Override
	protected LocalDate getLocalDate(ZonedDateTime temporal)
	{
		return temporal.toLocalDate();
	}

	@Override
	protected LocalTime getLocalTime(ZonedDateTime temporal)
	{
		return temporal.toLocalTime();
	}
}
