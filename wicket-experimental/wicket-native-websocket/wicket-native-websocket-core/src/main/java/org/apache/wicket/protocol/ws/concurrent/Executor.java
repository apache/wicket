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
package org.apache.wicket.protocol.ws.concurrent;

import java.util.concurrent.Callable;

/**
 * An abstraction over all available executor services.
 * The application may use {@link java.util.concurrent.Executor} or
 * Akka/Scala 2.10 ExecutionContext, or anything that serves the same purpose.
 */
public interface Executor
{
	/**
	 * Runs a simple task that doesn't return a result
	 *
	 * @see     java.lang.Thread#run()
	 */
	void run(Runnable command);

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 * @throws Exception if unable to compute a result
	 */
	<T> T call(Callable<T> callable) throws Exception;
}
