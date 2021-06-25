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
package org.apache.wicket.bean.validation;

import java.util.Locale;

import javax.validation.MessageInterpolator;

import org.apache.wicket.Session;
import org.apache.wicket.util.lang.Args;

/**
 * {@link MessageInterpolator} that adapts another to a locale from Wicket's {@link Session}
 * 
 * @author igor
 */
public class SessionLocaleInterpolator implements MessageInterpolator
{
	private final MessageInterpolator delegate;

	/**
	 * Constructor
	 * 
	 * @param delegate
	 *            the MessageInterpolator to delegate to
	 */
	public SessionLocaleInterpolator(MessageInterpolator delegate)
	{
		Args.notNull(delegate, "delegate");
		this.delegate = delegate;
	}

	@Override
	public String interpolate(final String messageTemplate,
		final MessageInterpolator.Context context)
	{
		final Locale locale = getLocale();
		if (locale != null)
		{
			return interpolate(messageTemplate, context, locale);
		}
		else
		{
			return delegate.interpolate(messageTemplate, context);
		}
	}

	@Override
	public String interpolate(final String message, final MessageInterpolator.Context context,
		final Locale locale)
	{
		return delegate.interpolate(message, context, locale);
	}

	private Locale getLocale()
	{
		return Session.exists() ? Session.get().getLocale() : null;
	}
}
