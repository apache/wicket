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
package org.apache.wicket.markup;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * 
 */
public class MarkupFragmentTest extends WicketTestCase
{
	/** */
	@Test
	public void iteratorSameAsSizeForMarkup()
	{
		Markup markup = Markup.of("<body wicket:id='body'><div wicket:id='label'> text </div></body>");

		assertEquals(5, markup.size());

		int count = 0;
		String xml = "";
		for (MarkupElement elem : markup)
		{
			count += 1;
			xml += elem.toString();
		}
		assertEquals(5, count);
		assertEquals("<body wicket:id=\"body\"><div wicket:id=\"label\"> text </div></body>", xml);

		count = 0;
		xml = "";
		for (int i = 0; i < markup.size(); i++)
		{
			count += 1;
			xml += markup.get(i).toString();
		}
		assertEquals(5, count);
		assertEquals("<body wicket:id=\"body\"><div wicket:id=\"label\"> text </div></body>", xml);
	}

	/** */
	@Test
	public void iteratorSameAsSizeForMarkupFragment()
	{
		Markup markup = Markup.of("<body wicket:id='body'><div wicket:id='label'> text </div></body>");
		MarkupFragment fragment = new MarkupFragment(markup, 1);

		assertEquals(3, fragment.size());

		int count = 0;
		String xml = "";
		for (MarkupElement elem : fragment)
		{
			count += 1;
			xml += elem.toString();
		}
		assertEquals(3, count);
		assertEquals("<div wicket:id=\"label\"> text </div>", xml);

		count = 0;
		xml = "";
		for (int i = 0; i < fragment.size(); i++)
		{
			count += 1;
			xml += fragment.get(i).toString();
		}
		assertEquals(3, count);
		assertEquals("<div wicket:id=\"label\"> text </div>", xml);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4136
	 */
	@Test
	public void createMarkupFragmentOnOpenTag()
	{
		Markup markup = Markup.of("<body><img wicket:id='photo'><span wicket:id='label'/></body>");
		MarkupFragment fragment = new MarkupFragment(markup, 1);

		assertEquals(1, fragment.size());
	}
}
