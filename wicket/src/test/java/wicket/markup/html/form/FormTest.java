/*
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
package wicket.markup.html.form;

import junit.framework.TestCase;
import wicket.Component;

/**
 * @author Pekka Enberg
 */
public class FormTest extends TestCase
{
	private FormComponent.IVisitor visitor;

	@Override
	protected void setUp() throws Exception
	{
		visitor = new Form.ValidationVisitor()
		{
			@Override
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
}
