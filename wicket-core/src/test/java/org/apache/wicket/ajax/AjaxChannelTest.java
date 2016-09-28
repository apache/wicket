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
package org.apache.wicket.ajax;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author Martin Dilger
 */
public class AjaxChannelTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4024
	 */
	@Test
	public void getChannelNameRetursCorrectStringForStackableChannels()
	{
		String channelName = "channelName";
		String expectedForStackables = channelName + "|s";
		String expectedForDrops = channelName + "|d";

		AjaxChannel stackableChannel = new AjaxChannel(channelName, AjaxChannel.Type.QUEUE);
		AjaxChannel dropChannel = new AjaxChannel(channelName, AjaxChannel.Type.DROP);

		assertEquals(expectedForStackables, stackableChannel.getChannelName());
		assertEquals(expectedForDrops, dropChannel.getChannelName());
	}


}
