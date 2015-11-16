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
 * Manages stack of executions of {@link IRequestHandler}s.
 * 
 * @author Matej Knopp
 * @author igor.vaynberg
 * @deprecated Use the class with the same name from wicket-core
 */
@Deprecated
public abstract class RequestHandlerStack
{
	/**
	 * Exception to stop current request handler and execute a new one.
	 * 
	 * @author Matej Knopp
	 */
	public static class ReplaceHandlerException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		private final boolean removeAll;

		private final IRequestHandler replacementRequestHandler;

		/**
		 * Construct.
		 * 
		 * @param replacementRequestHandler
		 * @param removeAll
		 */
		public ReplaceHandlerException(final IRequestHandler replacementRequestHandler,
			final boolean removeAll)
		{
			this.replacementRequestHandler = replacementRequestHandler;
			this.removeAll = removeAll;
		}
		
		/**
		 * @return the RequestHandler that should be used to continue handling the request
		 */
		public IRequestHandler getReplacementRequestHandler()
		{
			return replacementRequestHandler;
		}

		public boolean isRemoveAll()
		{
			return removeAll;
		}

		/**
		 * @see java.lang.Throwable#fillInStackTrace()
		 */
		@Override
		public Throwable fillInStackTrace()
		{
			// don't do anything here
			return null;
		}
	}
}
