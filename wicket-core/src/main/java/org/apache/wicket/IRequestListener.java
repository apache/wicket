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


import org.apache.wicket.util.io.IClusterable;

/**
 * Base interface for all interfaces that listen for requests from the client browser. All
 * sub-interfaces of this interface must have a single method which takes no arguments.
 * <p>
 * New listener interfaces must be registered with Wicket by constructing a
 * {@link RequestListenerInterface} object for the given interface class. The best way to do this is
 * to create a public static final constant field in your request listener interface. Doing this
 * will cause the interface to automatically register whenever it is used. For example, see
 * {@link org.apache.wicket.IRedirectListener#INTERFACE}.
 * 
 * @author Jonathan Locke
 */
public interface IRequestListener extends IClusterable
{
}
