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

import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Url;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class SessionCheckerTest extends WicketTestCase
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5634
	 *
	 * Tests that the serialization fails when a checking ObjectOutputStream is
	 * used with SessionChecker and there is a component in the object tree that
	 * keeps a reference to the Wicket Session.
	 */
	@Test
	void serializingTheSession()
	{
		JavaSerializer serializer = new JavaSerializer("JavaSerializerTest")
		{
			@Override
			protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws IOException
			{
				IObjectChecker checker = new SessionChecker();
				return new CheckingObjectOutputStream(out, checker);
			}
		};

		WebMarkupContainer container = new WebMarkupContainer("container");
		// WICKET-6196 force container#children to be an array
		container.add(new Label("id1"));
		container.add(new ComponentWithAReferenceToTheSession("id2"));
		
		byte[] serialized = serializer.serialize(container);
		assertNull(serialized, "The produced byte[] must be null if there was an error");
	}

	private static class ComponentWithAReferenceToTheSession extends WebComponent
	{
		private final Session member = new WebSession(new MockWebRequest(Url.parse("")));

		ComponentWithAReferenceToTheSession(final String id)
		{
			super(id);
		}
	}

}
