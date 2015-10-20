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
package org.apache.wicket.util.listener;

/**
 * Holds a set of IChangeListeners.
 * 
 * Note that these classes are not meant to be serializable or for you to hold them in session (see
 * WICKET-2697)
 * 
 * @author Jonathan Locke
 */
public final class ChangeListenerSet<T> extends ListenerCollection<IChangeListener<T>>
{
	private static final long serialVersionUID = 1L;

	public void notifyListeners(final T t)
	{
		notify(new INotifier<IChangeListener<T>>()
		{
			@Override
			public void notify(final IChangeListener<T> listener)
			{
				listener.onChange(t);
			}
		});
	}
}
