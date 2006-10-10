/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http.request;

import java.lang.reflect.Field;
import java.util.SortedMap;

import junit.framework.TestCase;
import wicket.WicketRuntimeException;
import wicket.request.target.coding.IRequestTargetUrlCodingStrategy;

/**
 * Test of WebRequestCodingStrategy
 * 
 * @author Frank Bille
 */
public class WebRequestCodingStrategyTest extends TestCase
{
	/**
	 * We expect an IllegalArgumentException if encoder is null.
	 */
	public void testMount_IRequestTargetUrlCodingStrategy_1()
	{
		try
		{
			WebRequestCodingStrategy strategy = new WebRequestCodingStrategy();
			strategy.mount(null);
		}
		catch (IllegalArgumentException e)
		{
			if ("Argument encoder must be not-null".equals(e.getMessage()) == false)
			{
				fail();
			}
		}
		catch (Exception e)
		{
			fail();
		}
	}

	/**
	 * Path must not be empty or /
	 */
	public void testMount_IRequestTargetUrlCodingStrategy_2()
	{
		// null
		try
		{
			WebRequestCodingStrategy strategy = new WebRequestCodingStrategy();
			strategy.mount(new MockRequestTargetUrlCodingStrategy()
			{
				@Override
				public String getMountPath()
				{
					return null;
				}
			});
		}
		catch (IllegalArgumentException e)
		{
			if ("Argument path must be not be empty".equals(e.getMessage()) == false)
			{
				fail();
			}
		}
		catch (Exception e)
		{
			fail();
		}

		// empty
		try
		{
			WebRequestCodingStrategy strategy = new WebRequestCodingStrategy();
			strategy.mount(new MockRequestTargetUrlCodingStrategy()
			{
				@Override
				public String getMountPath()
				{
					return "";
				}
			});
		}
		catch (IllegalArgumentException e)
		{
			if ("Argument path must be not be empty".equals(e.getMessage()) == false)
			{
				fail();
			}
		}
		catch (Exception e)
		{
			fail();
		}

		// slash /
		try
		{
			WebRequestCodingStrategy strategy = new WebRequestCodingStrategy();
			strategy.mount(new MockRequestTargetUrlCodingStrategy()
			{
				@Override
				public String getMountPath()
				{
					return "/";
				}
			});
		}
		catch (IllegalArgumentException e)
		{
			if ("The mount path '/' is reserved for the application home page".equals(e
					.getMessage()) == false)
			{
				fail();
			}
		}
		catch (Exception e)
		{
			fail();
		}
	}

	/**
	 * Test that path is prefixed with /.
	 * 
	 * @throws Exception
	 *             We don't care about exceptions in this test.
	 */
	@SuppressWarnings("unchecked")
	public void testMount_IRequestTargetUrlCodingStrategy_3() throws Exception
	{
		IRequestTargetUrlCodingStrategy codingStrategy = new MockRequestTargetUrlCodingStrategy()
		{
			@Override
			public String getMountPath()
			{
				return "mockpath";
			}
		};

		WebRequestCodingStrategy strategy = new WebRequestCodingStrategy();
		strategy.mount(codingStrategy);

		// Get the mountsOnPath field
		Field mountsOnPathField = WebRequestCodingStrategy.class.getDeclaredField("mountsOnPath");
		mountsOnPathField.setAccessible(true);
		SortedMap<String, IRequestTargetUrlCodingStrategy> mountsOnPath = (SortedMap<String, IRequestTargetUrlCodingStrategy>)mountsOnPathField
				.get(strategy);

		IRequestTargetUrlCodingStrategy foundStrategy = mountsOnPath.get("/mockpath");

		assertNotNull(foundStrategy);
		assertEquals(codingStrategy, foundStrategy);
	}

	/**
	 * If two IRequestTargetUrlCodingStrategy's is mounted with the same path an
	 * exception is thrown
	 */
	public void testMount_IRequestTargetUrlCodingStrategy_4()
	{
		try
		{
			WebRequestCodingStrategy strategy = new WebRequestCodingStrategy();
			strategy.mount(new MockRequestTargetUrlCodingStrategy()
			{
				@Override
				public String getMountPath()
				{
					return "/mockpath";
				}
			});
			// Add another one with the same path
			strategy.mount(new MockRequestTargetUrlCodingStrategy()
			{
				@Override
				public String getMountPath()
				{
					return "/mockpath";
				}
			});
		}
		catch (WicketRuntimeException e)
		{
			// Expected
		}
		catch (Exception e)
		{
			fail();
		}
	}
}
