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
package org.apache.wicket.validation;

import java.io.Serializable;

import org.apache.wicket.util.lang.Args;

/**
 * An IValidationError implementation that just keeps a Serializable
 * error message. Use it with a custom FeedbackPanel implementation
 * that knows how to render such error message.
 *
 * @since 6.0
 * @see org.apache.wicket.Component#error(java.io.Serializable)
 * @see org.apache.wicket.Session#error(java.io.Serializable)
 * @see org.apache.wicket.markup.html.panel.FeedbackPanel
 */
public class RawValidationError implements IValidationError
{
	private final Serializable errorMessage;

	/**
	 * Constructor.
	 *
	 * @param errorMessage
	 *      the custom error message that will be rendered by a custom FeedbackPanel
	 */
	public RawValidationError(final Serializable errorMessage)
	{
		this.errorMessage = Args.notNull(errorMessage, "errorMessage");
	}

	@Override
	public Serializable getErrorMessage(@SuppressWarnings("unused") IErrorMessageSource messageSource)
	{
		return errorMessage;
	}
}
