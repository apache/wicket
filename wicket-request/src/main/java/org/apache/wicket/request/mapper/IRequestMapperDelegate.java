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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.IRequestMapper;

/**
 * A interface to be implemented by {@link IRequestMapper}s that delegate to other {@link IRequestMapper}s.
 * This allows the application to traverse the tree of request mappers to find a mounted mapper to remove
 * when unmounting mounted mappers.
 *
 * @author Jesse Long
 */
public interface IRequestMapperDelegate extends IRequestMapper
{
	/**
	 * Returns the delegate {@link IRequestMapper}.
	 *
	 * @return The delegate {@link IRequestMapper}.
	 */
	IRequestMapper getDelegateMapper();
}
