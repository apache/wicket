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
package response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.protocol.http.MockWebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.settings.IExceptionSettings;

public class StringResponseTest extends WicketTestCase
{
	public void testBrokenPage() {
		tester.setupRequestAndResponse();
		tester.getApplication().getRequestCycleSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_EXCEPTION_PAGE);

		WebRequestCycle cycle = tester.createRequestCycle();
		cycle.setResponse(new StringResponse());
		try
		{
			// Decompose processRequestCycle() as error pages are not rendered in WicketTester, the exception is thrown instead
			cycle.request(new BookmarkablePageRequestTarget(BrokenPage.class));
			Method method = MockWebApplication.class.getDeclaredMethod("generateLastRenderedPage", new Class[]{WebRequestCycle.class});
			method.setAccessible(true);
			Page page = (Page)method.invoke(tester, new Object[]{cycle});
			assertTrue("Page is not an ExceptionErrorPage", page instanceof ExceptionErrorPage);
			WebRequestCycle cycle2 = tester.createRequestCycle();
			cycle2.setResponse(new StringResponse());
			// Render the error page to exercise configureResponse()
			page.render();
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			cycle.getResponse().close();
		}
	}
}
