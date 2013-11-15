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