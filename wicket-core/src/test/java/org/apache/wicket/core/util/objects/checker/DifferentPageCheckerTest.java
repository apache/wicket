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
package org.apache.wicket.core.util.objects.checker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.wicket.Component;
import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.form.login.MockHomePage;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

public class DifferentPageCheckerTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5634
	 *
	 * Tests that the serialization fails when a checking ObjectOutputStream is
	 * used with DifferentPageChecker and there is a component in the object tree that
	 * keeps a reference to a page which is not component.getPage()..
	 */
	@Test
	public void serializingAnotherPage()
	{
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest")
		{
			@Override
			protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
			{
				IObjectChecker checker = new DifferentPageChecker();
				return new CheckingObjectOutputStream(out, checker);
			}
		};

		WebComponent component = new ComponentThatKeepsAReferenceToAnotherPage(MockPageWithLink.LINK_ID);
		MockPageWithLink rootPage = new MockPageWithLink();
		rootPage.add(component);
		byte[] serialized = serializer.serialize(rootPage);
		assertNull("The produced byte[] must be null if there was an error", serialized);
	}

	private static class ComponentThatKeepsAReferenceToAnotherPage extends WebComponent
	{
		private final Component member = new WebComponent("inner") {
			private final Page anotherPage = new MockHomePage();
		};

		public ComponentThatKeepsAReferenceToAnotherPage(final String id)
		{
			super(id);
		}
	}
}
