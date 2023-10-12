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
package org.apache.wicket.pageStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockSessionStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class DefaultPageContextTest extends WicketTestCase
{

	private static final String KEY = "TEST";

	private static final MetaDataKey<String> DATA_KEY = new MetaDataKey<String>()
	{
	};

	MockSessionStore sessionStore = new MockSessionStore() {
		public void setAttribute(Request request, String name, Serializable value) {
			super.setAttribute(request, name, value);
			
			attributeSet = true;
		}
		
		public void flushSession(Request request, Session session) {
			super.flushSession(request, session);
			
			sessionFlushed = true;
		}
	};
	
	private boolean attributeSet;
	
	private boolean sessionFlushed;
	
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication() {
			@Override
			protected void init()
			{
				setSessionStoreProvider(() -> {
					return sessionStore;
				});
			}
		};
	}
	
	@Test
	void testAttribute()
	{
		var context = new DefaultPageContext();
		
		assertEquals(null, context.<String>getSessionAttribute(KEY, null));
		assertFalse(attributeSet);
	}

	@Test
	void testAttributeWithDefault()
	{
		var context = new DefaultPageContext();
		
		assertEquals("FOO", context.<String>getSessionAttribute(KEY, () -> {
			return "FOO";
		}));
		assertTrue(attributeSet);
	}

	@Test
	void testAttributeExistsWithDefault()
	{
		tester.getSession().bind();
		tester.getSession().setAttribute(KEY, "EXISTS");
		
		var context = new DefaultPageContext();
		
		assertEquals("EXISTS", context.<String>getSessionAttribute(KEY, () -> {
			return "FOO";
		}));
		assertTrue(attributeSet);
	}

	@Test
	void testData()
	{
		var context = new DefaultPageContext();
		
		assertEquals(null, context.<String>getSessionData(DATA_KEY, null));
		
		tester.getSession().internalDetach();
		assertFalse(sessionFlushed);
	}

	@Test
	void testDataWithDefault()
	{
		var context = new DefaultPageContext();
		
		assertEquals("FOO", context.<String>getSessionData(DATA_KEY, () -> {
			return "FOO";
		}));
		tester.getSession().internalDetach();
		assertTrue(sessionFlushed);
	}

	@Test
	void testDataExistsWithDefault()
	{
		tester.getSession().setMetaData(DATA_KEY, "EXISTS");
		tester.getSession().bind();
		
		var context = new DefaultPageContext();
		
		assertEquals("EXISTS", context.<String>getSessionData(DATA_KEY, () -> {
			return "FOO";
		}));
		tester.getSession().internalDetach();
		assertTrue(sessionFlushed);
	}
}
