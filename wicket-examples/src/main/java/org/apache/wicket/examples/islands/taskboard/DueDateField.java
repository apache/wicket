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
package org.apache.wicket.examples.islands.taskboard;

import java.time.LocalDate;

import org.apache.wicket.islands.FormIsland;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.ValidationError;

/**
 * A date picker in the browser, as a form component whose value is a {@link LocalDate}. A due date
 * in the past is refused by a plain Wicket validator; its message shows in the picker and in the
 * form's feedback panel.
 */
public class DueDateField extends FormIsland<LocalDate, Void>
{
	private static final long serialVersionUID = 1L;

	public DueDateField(String id, IModel<LocalDate> model)
	{
		super(id, "date-picker", model, LocalDate.class);
		add(validatable -> {
			if (validatable.getValue().isBefore(LocalDate.now()))
			{
				validatable.error(new ValidationError("The due date cannot be in the past."));
			}
		});
	}
}
