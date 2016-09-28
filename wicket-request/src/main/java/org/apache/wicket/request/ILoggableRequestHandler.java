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
package org.apache.wicket.request;

/**
 * A loggable request handler is a {@link IRequestHandler} that is capable of delivering log data.
 * Implementations of this interface should collect data before or from
 * {@link #detach(IRequestCycle)}. {@link #getLogData()} will never be called before the handler is
 * detached.
 * 
 * @author Emond Papegaaij
 */
public interface ILoggableRequestHandler extends IRequestHandler
{
	/**
	 * Returns the collected log data for this request handler and should never throw an exception.
	 * This method is never called before the request handler is detached.
	 * 
	 * @return The collected log data.
	 */
	ILogData getLogData();
}
