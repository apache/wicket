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
package org.apache.wicket.request;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UrlDecoderTest
{
	@Test
	public void mustNotEmitNullByteForPath() throws Exception
	{
		String evil = "http://www.devil.com/highway/to%00hell";
		String decoded = UrlDecoder.PATH_INSTANCE.decode(evil, "UTF-8");
		assertEquals(-1, decoded.indexOf('\0'));
		assertEquals("http://www.devil.com/highway/toNULLhell", decoded);
	}

	@Test
	public void mustNotEmitNullByteForQuery() throws Exception
	{
		String evil = "http://www.devil.com/highway?destination=%00hell";
		String decoded = UrlDecoder.QUERY_INSTANCE.decode(evil, "UTF-8");
		assertEquals(-1, decoded.indexOf('\0'));
		assertEquals("http://www.devil.com/highway?destination=NULLhell", decoded);
	}
}
