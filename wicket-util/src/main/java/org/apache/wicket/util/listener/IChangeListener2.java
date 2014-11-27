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

import org.apache.wicket.util.watch.IModifiable;

/**
 * Generic "something changed" listener interface that also provides the
 * changed object as an argument.
 */
// TODO Wicket 8: Rename to IChangeListener and remove the old one
public interface IChangeListener2<T extends IModifiable> extends IChangeListener
{
	/**
	 * Client method that is called to indicate that something changed.
	 *
	 * @param modifiable The object that has changed
	 */
	void onChange(T modifiable);
}
