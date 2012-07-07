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
package org.apache.wicket.util.visit;

/**
 * Implementation of {@link IVisit} used by traversal algorithms
 * 
 * @author igor.vaynberg
 * 
 * @param <R>
 *            type of object that should be returned by the visit/traversal
 */
public class Visit<R> implements IVisit<R>
{
	private static enum Action {
		CONTINUE, CONTINUE_BUT_DONT_GO_DEEPER, STOP
	}

	private R result;
	private Action action = Action.CONTINUE;

	/** {@inheritDoc} */
	@Override
	public void stop()
	{
		stop(null);
	}

	/** {@inheritDoc} */
	@Override
	public void stop(final R result)
	{
		action = Action.STOP;
		this.result = result;
	}

	/** {@inheritDoc} */
	@Override
	public void dontGoDeeper()
	{
		action = Action.CONTINUE_BUT_DONT_GO_DEEPER;
	}

	/**
	 * Checks if the visit/traversal has been stopped
	 * 
	 * @return {@code true} if the visit/traversal has been stopped
	 */
	public boolean isStopped()
	{
		return action == Action.STOP;
	}

	/**
	 * Checks if the visit/traversal should continue
	 * 
	 * @return {@code true} if the visit/traversal should continue
	 */
	public boolean isContinue()
	{
		return action == Action.CONTINUE;
	}

	/**
	 * Checks if the visit/traversal has been stopped from visiting children of the currently
	 * visited object
	 * 
	 * @return {@code true} if the visit/traversal should not visit children of the currently
	 *         visited object
	 */
	public boolean isDontGoDeeper()
	{
		return action == Action.CONTINUE_BUT_DONT_GO_DEEPER;
	}

	/**
	 * Gets the result of the visit/traversal. This value is set using {@link #stop(Object)} or
	 * remains {@code null} if visit/traversal has ended in any other way
	 * 
	 * @return value that should be returned to the method that initiated the visit/traversal
	 */
	public R getResult()
	{
		return result;
	}
}
