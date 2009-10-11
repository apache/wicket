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
package org.apache.wicket.ng.page;

import java.io.Serializable;

/**
 * Context object for {@link PageManager}. This decouples the {@link PageManager} from request cycle
 * and session.
 * 
 * @author Matej Knopp
 * 
 */
public interface PageManagerContext
{
	public void setRequestData(Object data);

	public Object getRequestData();

	public void setSessionAttribute(String key, Serializable value);

	public Serializable getSessionAttribute(String key);

	public void bind();

	public String getSessionId();
}