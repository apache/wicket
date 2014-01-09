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
package org.apache.wicket;

/**
 * ListenerCollection for ISessionListener
 * @author igor
 */
import org.apache.wicket.util.listener.ListenerCollection;

/**
 * {@link ListenerCollection} for {@link ISessionListener}s
 * 
 * @author igor
 */
public class SessionListenerCollection extends ListenerCollection<ISessionListener>
	implements
		ISessionListener
{

	@Override
	public void onCreated(final Session session)
	{
		notify(new INotifier<ISessionListener>()
		{
			@Override
			public void notify(ISessionListener listener)
			{
				listener.onCreated(session);
			}
		});
	}

	@Override
	public void onUnbound(final String sessionId)
	{
		notify(new INotifier<ISessionListener>()
		{
			@Override
			public void notify(ISessionListener listener)
			{
				listener.onUnbound(sessionId);
			}
		});
	}

}
