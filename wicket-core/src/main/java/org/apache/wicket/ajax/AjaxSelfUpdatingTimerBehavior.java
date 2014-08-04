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
package org.apache.wicket.ajax;

import org.apache.wicket.util.time.Duration;

/**
 * Automatically re-renders the component it is attached to via AJAX at a regular interval.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @see #onTimer(AjaxRequestTarget)
 * @see #onPostProcessTarget(AjaxRequestTarget)
 */
public class AjaxSelfUpdatingTimerBehavior extends AbstractAjaxTimerBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 */
	public AjaxSelfUpdatingTimerBehavior(final Duration updateInterval)
	{
		super(updateInterval);
	}

	/**
	 * @see org.apache.wicket.ajax.AbstractAjaxTimerBehavior#onTimer(AjaxRequestTarget)
	 */
	@Override
	protected final void onTimer(final AjaxRequestTarget target)
	{
		target.add(getComponent());
		onPostProcessTarget(target);
	}

	/**
	 * Give the subclass a chance to add something to the target, like a javascript effect call.
	 * Called after the hosting component has been added to the target.
	 * 
	 * @param target
	 *            The AJAX target
	 */
	protected void onPostProcessTarget(final AjaxRequestTarget target)
	{
	}
}
