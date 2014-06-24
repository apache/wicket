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
package org.apache.wicket.markup.head;

import static org.junit.Assert.assertEquals;

import org.apache.wicket.model.Model;
import org.junit.Test;


public class MetaDataHeaderItemTest
{
	@Test
	public void testMetaTag() throws Exception
	{
		String expectedString = "<meta name=\"robots\" content=\"index,nofollow\" />\n";
		MetaDataHeaderItem metaTag = MetaDataHeaderItem.forMetaTag("robots", "index,nofollow");
		
		assertEquals(expectedString, metaTag.generateString());
	}
	
	@Test
	public void testLinkTag() throws Exception
	{
		String expectedString = "<link rel=\"shortcut icon\" href=\"http://www.mysite.com/favicon.ico\" type=\"image/x-icon\" />\n";
		MetaDataHeaderItem metaTag = MetaDataHeaderItem.forLinkTag("shortcut icon", "http://www.mysite.com/favicon.ico");
		metaTag.addTagAttribute("type", Model.of("image/x-icon"));
		
		assertEquals(expectedString, metaTag.generateString());
	}
}
