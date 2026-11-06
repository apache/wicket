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

import org.apache.wicket.islands.FormIsland;
import org.apache.wicket.model.IModel;

/**
 * A rich-text editor in the browser, as a form component whose value is HTML. That HTML comes
 * from the browser, so it is sanitized before it is validated and reaches the model; see
 * {@link Descriptions}.
 */
public class RichTextField extends FormIsland<String, Void>
{
	private static final long serialVersionUID = 1L;

	public RichTextField(String id, IModel<String> model)
	{
		super(id, "rich-text", model, String.class);
	}

	@Override
	protected String sanitize(String value)
	{
		return Descriptions.sanitize(value);
	}
}
