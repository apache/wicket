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
package org.apache.wicket.examples.atmosphere;

import org.apache.wicket.Session;

import com.google.common.base.Predicate;

public class ReceiverFilter implements Predicate<Object>
{
	public ReceiverFilter()
	{
	}

	@Override
	public boolean apply(Object input)
	{
		if (input instanceof ChatMessage)
		{
			ChatMessage msg = (ChatMessage)input;
			return msg.getReceiver() == null || msg.getReceiver().isEmpty() ||
				msg.getReceiver().equals(Session.get().getId());
		}
		return false;
	}
}
