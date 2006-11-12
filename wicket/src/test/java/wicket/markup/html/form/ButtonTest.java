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

import wicket.MockPageWithOneComponent;
import wicket.Page;
import wicket.WicketTestCase;
import wicket.util.tester.ITestPageSource;

/**
 * Test of the form Button.
 * 
 * @author Frank Bille (billen)
 */
public class ButtonTest extends WicketTestCase
{
	/**
	 * Construct.
	 */
	public ButtonTest()
	{
		super("Button test");
	}

	/**
	 * Test that the button can be rendered in it's simple form.
	 */
	public void testRender()
	{
		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				Page page = new MockPageWithOneComponent();
				
				new Button(page, MockPageWithOneComponent.COMPONENT_ID)
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit()
					{
					}
				};
				
				return page;
			}
			
		});
	
		tester.assertComponent(MockPageWithOneComponent.COMPONENT_ID, Button.class);
	}
	
	/**
	 * Test that the button has no onclick when inside a form and that it has the onclick javascript
	 * when outside of the form.
	 */
	public void testRender_2()
	{
		tester.startPage(MockFormAndButtonPage.class);

		tester.assertContains("<input name=\"button\" type=\"submit\" wicket:id=\"button\"/>");
		String doc = tester.getServletResponse().getDocument();
		boolean contains = doc.contains("<input onclick=\"var e=document.getElementById('form:hf:fs'); e.name='button2'; e.value='x';var f=document.getElementById('form');var ff=f;if (ff.onsubmit != undefined) { if (ff.onsubmit()==false) return false; }f.submit();e.value='';e.name='';return false;\" type=\"submit\" name=\"button2\" wicket:id=\"button2\"/>");		
		assertTrue(contains);
	}
}
