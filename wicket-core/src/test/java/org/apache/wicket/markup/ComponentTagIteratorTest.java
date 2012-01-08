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

import static org.hamcrest.Matchers.*;

import java.util.NoSuchElementException;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

public class ComponentTagIteratorTest extends WicketTestCase
{
	@Test
	public void test()
	{
		Markup markup = Markup.of("<body wicket:id='body'><div wicket:id='label'> text </div></body>");
		MarkupFragment fragment = new MarkupFragment(markup, 0);
		MarkupStream stream = new MarkupStream(fragment);

		ComponentTagIterator it = new ComponentTagIterator(stream);

		// <body wicket:id='body'>

		assertThat(it.hasNext(), is(true));
		assertThat(it.hasNext(), is(true));

		ComponentTag body = it.next();
		assertThat(body, is(not(nullValue())));
		assertThat(body.getId(), is("body"));

		assertThat(it.stack().size(), is(1));
		assertThat(it.stack(), contains(body));

		// <div wicket:id='label'>

		assertThat(it.hasNext(), is(true));
		assertThat(it.hasNext(), is(true));

		ComponentTag label = it.next();
		assertThat(label, is(not(nullValue())));
		assertThat(label.getId(), is("label"));

		assertThat(it.stack().size(), is(2));
		assertThat(it.stack(), contains(body, label));

		// </div>

		assertThat(it.hasNext(), is(true));

		ComponentTag label2 = it.next();
		assertThat(label2, is(not(nullValue())));

		assertThat(it.stack().size(), is(1));
		assertThat(it.stack(), contains(body));

		// </body>

		assertThat(it.hasNext(), is(true));

		ComponentTag body2 = it.next();
		assertThat(body2, is(not(nullValue())));

		assertThat(it.stack().size(), is(0));

		// end

		assertThat(it.hasNext(), is(false));

		try
		{
			it.next();
			fail();
		}
		catch (NoSuchElementException e)
		{
			// expected
		}

	}
}
