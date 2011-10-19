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
package org.apache.wicket.request.handler.logger;

import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.IRequestHandlerDelegate;

/**
 * Contains logging data for {@link IRequestHandlerDelegate} implementations.
 * 
 * @author Emond Papegaaij
 */
public class DelegateLogData implements ILogData
{
	private static final long serialVersionUID = 1L;

	private final ILogData delegateData;

	/**
	 * Construct.
	 * 
	 * @param delegateData
	 */
	public DelegateLogData(ILogData delegateData)
	{
		this.delegateData = delegateData;
	}

	/**
	 * @return The logging data from the delegate
	 */
	public final ILogData getDelegateData()
	{
		return delegateData;
	}

	@Override
	public String toString()
	{
		return "{delegate=" + delegateData + "}";
	}
}
