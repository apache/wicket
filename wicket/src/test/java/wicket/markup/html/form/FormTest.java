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
package wicket.markup.html.form;

import wicket.Component;
import wicket.WicketTestCase;

/**
 * @author Pekka Enberg
 * @author Martijn Dashorst
 */
public class FormTest extends WicketTestCase
{
	private FormComponent.IVisitor visitor;

	/**
	 * Construct.
	 * @param name
	 */
	public FormTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		visitor = new Form.ValidationVisitor()
		{
			public void validate(FormComponent formComponent)
			{
			}
		};
	}

	/**
	 * 
	 */
	public void testShouldContinueTraversalIfListenerAllowsChildProcessing()
	{
		assertTraversalStatus(Component.IVisitor.CONTINUE_TRAVERSAL, true);
	}

	/**
	 * 
	 */
	public void testShouldContinueTraversalButDontGoDeeperIfListenerDisallowsChildProcessing()
	{
		assertTraversalStatus(Component.IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER, false);
	}

	private void assertTraversalStatus(Object expected, final boolean processChildren)
	{
		assertEquals(expected, visitor.formComponent(new IFormProcessingListener()
		{
			public boolean processChildren()
			{
				return processChildren;
			}
		}));
	}

	/**
	 * @throws Exception
	 */
	public void testFormMethodGet() throws Exception 
	{
		executeTest(FormMethodTestPage.class, "FormMethodTestPage_expected.html");
	}
}
