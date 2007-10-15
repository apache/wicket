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
package org.apache.wicket.protocol.http;

import org.apache.wicket.Session;

/**
 * This exception is thrown in {@link Session} class when an Ajax requests attempts to get a lock on
 * pagemap while a regular request is processing the same page.
 * <p>
 * 
 * @author Matej Knopp
 */
// TODO: Using an exception to control flow is not the nicest way
public class IgnoreAjaxRequestException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

}
