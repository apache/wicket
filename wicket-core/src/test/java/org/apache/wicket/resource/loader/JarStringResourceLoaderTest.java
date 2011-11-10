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
package org.apache.wicket.resource.loader;

import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.basic.Label;
import org.junit.Test;

/**
 * 
 */
public class JarStringResourceLoaderTest extends WicketTestCase
{

	/**
	 * 
	 */
	@Test
	public void testLoader_default()
	{
		JarStringResourceLoader loader = new JarStringResourceLoader();
		MyLabel label = new MyLabel("id");

		// must specify locale explitely or it will fail on French system
		assertEquals("english", loader.loadStringResource(label,
			"JarStringResourceLoaderTest_text", Locale.ENGLISH, null, null));
	}

	/**
	 * 
	 */
	@Test
	public void testLoader_fr()
	{
		JarStringResourceLoader loader = new JarStringResourceLoader();
		MyLabel label = new MyLabel("id");
		assertEquals("french", loader.loadStringResource(label, "JarStringResourceLoaderTest_text",
			Locale.FRENCH, null, null));
	}

	/**
	 * 
	 */
	@Test
	public void testLoader_notFound()
	{
		JarStringResourceLoader loader = new JarStringResourceLoader();
		MyLabel label = new MyLabel("id");
		assertNull(loader.loadStringResource(label, "fhadfjksd", null, null, null));
	}

	/**
	 * 
	 */
	private static class MyLabel extends Label
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public MyLabel(String id)
		{
			super(id);
		}
	}
}
