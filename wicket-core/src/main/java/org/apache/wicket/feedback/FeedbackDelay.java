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
package org.apache.wicket.feedback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Postpone calling {@link IFeedback#beforeRender()} after other components.
 * <p>
 * This gives other {@link Component#beforeRender()} the possibility to report feedbacks,
 * which can then be collected by {@link IFeedback}s afterwards.
 */
public class FeedbackDelay implements Serializable, AutoCloseable
{
	private static final MetaDataKey<FeedbackDelay> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};
	
	private List<IFeedback> feedbacks = new ArrayList<>();

	private RequestCycle cycle;
	
	/**
	 * Delay all feedbacks for the given cycle.
	 * <p>
	 * All postponed feedbacks will be prepared for render with {@link #beforeRender()}.
	 * 
	 * @param cycle
	 *            request cycle
	 */
	public FeedbackDelay(RequestCycle cycle) {
		if (get(cycle).isPresent()) {
			throw new WicketRuntimeException("feedbacks are already delayed");
		}
		
		cycle.setMetaData(KEY, this);
		
		this.cycle = cycle;
	}
	
	/**
	 * Get the current delay.
	 * 
	 * @param cycle
	 * @return optional delay
	 */
	public static Optional<FeedbackDelay> get(RequestCycle cycle) {
		return Optional.ofNullable(cycle.getMetaData(KEY));
	}

	/**
	 * Postpone {@link Component#beforeRender()} on the given feedback.
	 * 
	 * @param feedback
	 * @return
	 */
	public FeedbackDelay postpone(IFeedback feedback) {
		feedbacks.add(feedback);
		
		return this;
	}

	/**
	 * Prepares all postponed feedbacks for render.
	 * 
	 * @see IFeedback#beforeRender()
	 */
	public void beforeRender() {
		cycle.setMetaData(KEY, null);
		cycle = null;
		
		for (IFeedback feedback : feedbacks)
		{
			if (feedback instanceof Component) {
				Component component = (Component)feedback;
				
				// render only if it is still in the page hierarchy (WICKET-4895)
				if (component.findParent(Page.class) == null)
				{
					continue;
				}			
			}
		
			feedback.beforeRender();
		}
	}
	
	/**
	 * Close any delays.
	 * <p>
	 * This does not call {@link #beforeRender()} on the delayed feedbacks.
	 */
	@Override
	public void close() {
		if (cycle != null) {
			cycle.setMetaData(KEY, null);
			cycle = null;
			feedbacks.clear();
		}
	}
}
