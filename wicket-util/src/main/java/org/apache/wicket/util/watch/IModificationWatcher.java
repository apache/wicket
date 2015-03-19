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
package org.apache.wicket.util.watch;

import java.util.Set;

import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.time.Duration;


/**
 * Monitors one or more <code>IModifiable</code> objects, calling a {@link IChangeListener
 * IChangeListener} when a given object's modification time changes.
 * 
 * @author Juergen Donnerstag
 */
public interface IModificationWatcher
{
	/**
	 * Adds an <code>IModifiable</code> object and an <code>IChangeListener</code> object to call
	 * when the modifiable object is modified.
	 * 
	 * @param modifiable
	 *            an <code>IModifiable</code> object to monitor
	 * @param listener
	 *            an <code>IChangeListener</code> to call if the <code>IModifiable</code> object is
	 *            modified
	 * @return <code>true</code> if the set did not already contain the specified element
	 */
	boolean add(final IModifiable modifiable, final IChangeListener listener);

	/**
	 * Removes all entries associated with an <code>IModifiable</code> object.
	 * 
	 * @param modifiable
	 *            an <code>IModifiable</code> object
	 * @return the <code>IModifiable</code> object that was removed, else <code>null</code>
	 */
	IModifiable remove(final IModifiable modifiable);

	/**
	 * Starts watching at a given <code>Duration</code> polling rate.
	 * 
	 * @param pollFrequency
	 *            the polling rate <code>Duration</code>
	 */
	void start(final Duration pollFrequency);

	/**
	 * Stops this <code>ModificationWatcher</code>.
	 */
	void destroy();

	/**
	 * Retrieves a key set of all <code>IModifiable</code> objects currently being monitored.
	 * 
	 * @return a <code>Set</code> of all <code>IModifiable</code> entries currently maintained
	 */
	Set<IModifiable> getEntries();
}