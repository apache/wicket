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
package org.apache.wicket.spring.injection.annot;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.proxy.ILazyInitProxy;
import org.apache.wicket.spring.Bean;
import org.apache.wicket.spring.SpringBeanLocator;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for SpringBean.
 *
 * @author Andrea Del Bene
 */
public class SpringBeanTest extends Assert
{
	private WicketTester tester;
	private ApplicationContextMock ctx;

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		tester = new WicketTester();
		ctx = new ApplicationContextMock();

		SpringComponentInjector springInjector = new SpringComponentInjector(
			tester.getApplication(), ctx);

		tester.getApplication().getComponentInstantiationListeners().add(springInjector);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void beanExists() throws Exception
	{
		// add dependency bean
		ctx.putBean("bean", new Bean());
		AnnotatedBeanRequired page;

		// first test with standard behavior (required = true)
		tester.startPage(page = new AnnotatedBeanRequired());
		assertNotNull(page.getBean());

		// now test with required = false
		AnnotatedBeanNotRequired notRequiredpage;
		tester.startPage(notRequiredpage = new AnnotatedBeanNotRequired());
		assertNotNull(notRequiredpage.getBean());

		// both page must have the same bean instance
		assertTrue(page.getBean() == notRequiredpage.getBean());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void beanNotExists() throws Exception
	{
		// with required = true we get IllegalStateException
		try
		{
			tester.startPage(new AnnotatedBeanRequired());
			fail();
		}
		catch (IllegalStateException e)
		{
		}

		// with required = false everything is fine
		AnnotatedBeanNotRequired page;
		tester.startPage(page = new AnnotatedBeanNotRequired());
		assertNull(page.getBean());

		// with name = something, required = false everything is fine
		AnnotatedBeanWithSameNameRequired page2;
		tester.startPage(page2 = new AnnotatedBeanWithSameNameRequired());
		assertNull(page2.getBean());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void beanExistsDifferentName() throws Exception
	{
		// add dependency beans of the same type
		ctx.putBean("mrBean", new Bean());
		ctx.putBean("theBean", new Bean());

		// with no name specified we get IllegalStateException
		try
		{
			tester.startPage(new AnnotatedBeanRequired());
			fail();
		}
		catch (IllegalStateException e)
		{
		}

		// we must inject bean with name "mrBean"
		AnnotatedBeanNotRequiredDifferentName page;
		tester.startPage(page = new AnnotatedBeanNotRequiredDifferentName());
		SpringBeanLocator locator = (SpringBeanLocator)((ILazyInitProxy)page.getBean()).getObjectLocator();

		assertTrue(locator.getBeanName().equals("mrBean"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4149
	 */
	@Test
	public void beanInjectedInBehavior()
	{
		ctx.putBean("mrBean", new Bean());

		// with no name specified we get IllegalStateException
		Page page = tester.startPage(new AnnotatedFieldInBehaviorPage());
		TestBehavior behavior = page.getBehaviors(TestBehavior.class).get(0);
		assertNotNull(behavior.getBean());
	}
}

class AnnotatedBeanRequired extends DummyHomePage
{
	@SpringBean
	private Bean bean;

	public Bean getBean()
	{
		return bean;
	}
}

class AnnotatedBeanWithSameNameRequired extends DummyHomePage
{
	@SpringBean(name = "bean", required = false)
	private Bean bean;

	public Bean getBean()
	{
		return bean;
	}
}

class AnnotatedBeanNotRequired extends DummyHomePage
{
	@SpringBean(required = false)
	private Bean bean;

	public Bean getBean()
	{
		return bean;
	}
}

class AnnotatedBeanNotRequiredDifferentName extends DummyHomePage
{
	@SpringBean(required = false, name = "mrBean")
	private Bean bean;

	public Bean getBean()
	{
		return bean;
	}
}

/**
 * A behavior which will be automatically processed for @SpringBean annotation
 */
class TestBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	@SpringBean()
	private Bean bean;

	public Bean getBean()
	{
		return bean;
	}
}

/**
 * A test page with a behavior which will be processed for @SpringBean annotations
 */
class AnnotatedFieldInBehaviorPage extends DummyHomePage
{
	private static final long serialVersionUID = 1L;

	public AnnotatedFieldInBehaviorPage()
	{
		add(new TestBehavior());
	}
}
