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
 * Listens to {@link Component#detach()} calls.
 * 
 * Detach listeners are called as the last step in the process of detaching a component; this means
 * the component is in a fully detached state when the listener is invoked.
 * 
 * @author igor.vaynberg
 * 
 * @see org.apache.wicket.settings.FrameworkSettings#setDetachListener(IDetachListener)
 */
public interface IDetachListener
{
	/**
	 * Called when component is being detached via a call to {@link Component#detach()}.
	 * 
	 * NOTICE: The component is in a fully detached state when this method is invoked; It is the
	 * listener's responsibility to maintain the detached state after this method is finished -
	 * which means if the listener causes any part of the component (eg model) to become reattached
	 * it is the listener's responsibility to detach it before this method exits.
	 * 
	 * @param component
	 *            component being detached
	 */
	void onDetach(Component component);

	/**
	 * Called when the application is being destroyed. Useful for cleaning up listener caches, etc.
	 */
	void onDestroyListener();
}
