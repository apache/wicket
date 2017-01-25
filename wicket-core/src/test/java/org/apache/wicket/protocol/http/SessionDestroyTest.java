package org.apache.wicket.protocol.http;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Locale;

import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

public class SessionDestroyTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-6310
	 */
	@Test
	public void whenSessionIsDestroyed_thenItShouldResetItState()
	{
		final Locale locale = Locale.ENGLISH;
		MockWebRequest request = new MockWebRequest(Url.parse("/"))
		{
			@Override
			public Locale getLocale()
			{
				return locale;
			}
		};

		final WebSession session = spy(new WebSession(request));

		// initially #invalidateNow() (and destroy()) are not called
		verify(session, never()).invalidateNow();
		assertThat(session.isSessionInvalidated(), is(false));

		// schedule invalidation
		session.invalidate();

		// the invalidation will happen on #detach(), so #destroy() is still not called
		verify(session, never()).invalidateNow();
		assertThat(session.isSessionInvalidated(), is(true));

		session.detach();

		// the session has been detached so #destroy() has been called and 'sessionInvalidated' is reset
		verify(session, times(1)).invalidateNow();
		assertThat(session.isSessionInvalidated(), is(false));

		// no matter how many times #detach() is called #destroy() should not be called
		session.detach();
		verify(session, times(1)).invalidateNow();
		session.detach();
		session.detach();
		verify(session, times(1)).invalidateNow();
		assertThat(session.isSessionInvalidated(), is(false));

	}
}
