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
package wicket.spring.injection.annot.test;

import wicket.injection.web.InjectorHolder;
import wicket.spring.injection.annot.AnnotSpringInjector;
import wicket.spring.test.ApplicationContextMock;
import wicket.spring.test.SpringContextLocatorMock;

/**
 * Spring application context mock that does all the initialization required to
 * setup an {@link AnnotSpringInjector} that will use this mock context as its
 * source of beans.
 * <p>
 * Example
 * 
 * <pre>
 *  AnnotApplicationContextMock appctx = new AnnotApplicationContextMock();
 *  appctx.putBean(&quot;contactDao&quot;, dao);
 *  
 *  WicketTester app = new WicketTester();
 *  
 *  Page deletePage=new DeleteContactPage(new DummyHomePage(), 10));
 * </pre>
 * 
 * DeleteContactPage will have its dependencies initialized by the
 * {@link AnnotSpringInjector}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AnnotApplicationContextMock extends ApplicationContextMock
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * <p>
	 * Sets up an {@link AnnotSpringInjector} that will use this mock context as
	 * its source of beans
	 * 
	 * 
	 */
	public AnnotApplicationContextMock()
	{
		SpringContextLocatorMock ctxLocator = new SpringContextLocatorMock(this);

		AnnotSpringInjector injector = new AnnotSpringInjector(ctxLocator);

		InjectorHolder.setInjector(injector);
	}
}
