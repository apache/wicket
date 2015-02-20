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
package org.apache.wicket.util.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.net.URL;

import org.apache.wicket.util.time.Time;
import org.junit.Assert;
import org.junit.Test;

public class ConnectionsTest extends Assert
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5838
	 * @throws Exception
	 */
	@Test
	public void getLastModified() throws Exception
	{
		URL url = new URL("http://wicket.apache.org/learn/books/wia.png");
		Time lastModified = Connections.getLastModified(url);
		assertThat(lastModified, is(notNullValue()));
		assertThat(lastModified.getMilliseconds(), is(not(0L)));
	}
}
