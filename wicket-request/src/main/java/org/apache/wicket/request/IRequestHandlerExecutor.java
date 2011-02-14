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
 * API responsible to schedule, execute, detach and access {@link IRequestHandler}s.
 * 
 * @author Pedro Santos
 */
public interface IRequestHandlerExecutor
{
	/**
	 * Executes the specified {@link IRequestHandler} an keep its reference to be detached at the
	 * end of request cycle. When the specified {@link IRequestHandler} finishes, the
	 * {@link IRequestHandler} that invoked this method continues (unless the new
	 * {@link IRequestHandler} called {@link #replaceAll(IRequestHandler)}.
	 * 
	 * @param handler
	 *            Executes the handler
	 */
	public abstract void execute(final IRequestHandler handler);

	/**
	 * Returns the request handler scheduled after current request handler.
	 * 
	 * @see #schedule(IRequestHandler)
	 * @return request handler scheduled to be executed next or <code>null</code>
	 */
	public abstract IRequestHandler next();

	/**
	 * Schedules the request handler to be executed after current request handler finishes. If there
	 * is already another request handler scheduled it will be discarded and overwritten by the new
	 * one. If {@link #replaceAll(IRequestHandler)} is invoked during current request handler
	 * execution the scheduled handler will be also discarded.
	 * 
	 * @param handler
	 *            handler to be executed after current request handler finishes
	 */
	public abstract void schedule(final IRequestHandler handler);

	/**
	 * Returns currently active {@link IRequestHandler}.
	 * 
	 * @return Active RequestHandler or <code>null</code> if no handler is active.
	 */
	public abstract IRequestHandler getActive();

	/**
	 * Removes the whole {@link IRequestHandler} stack, terminates currently running
	 * {@link IRequestHandler} and executes the new {@link IRequestHandler}.
	 * 
	 * @param handler
	 */
	public abstract void replaceAll(final IRequestHandler handler);

	/**
	 * Detaches all executed {@link IRequestHandler}s.
	 */
	public abstract void detach();

}
