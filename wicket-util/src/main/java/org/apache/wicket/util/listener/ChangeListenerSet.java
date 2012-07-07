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
public final class ChangeListenerSet extends ListenerCollection<IChangeListener>
{
	private static final long serialVersionUID = 1L;

	protected void notifyListener(final IChangeListener listener)
	{
	}

	/**
	 * 
	 */
	public void notifyListeners()
	{
		notify(new INotifier<IChangeListener>()
		{
			@Override
			public void notify(final IChangeListener object)
			{
				object.onChange();
			}
		});
	}
}
