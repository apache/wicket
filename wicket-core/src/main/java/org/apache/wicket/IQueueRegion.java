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
package org.apache.wicket;

import org.apache.wicket.markup.IMarkupFragment;


/**
 * Demarcates components that act as a root can dequeue children. These are usually components with
 * associated markup since the markup is needed to dequeue.
 * 
 * It is also important to note that components queued outside of a region cannot be dequeued into
 * it since regions act as roots for the dequeue process because they contain the markup. As such,
 * for example, a component queued in a page cannot be dequeued into a page child that is a panel
 * because a panel is an {@link IQueueRegion}.
 * 
 * @author igor
 * 
 */
public interface IQueueRegion
{
	/** 
	 * TODO Wicket 8: this interface might be a perfect candidate for Java 8 interface default methods.
	 * Now methods implementation is in MarkupContainer while it should simply be in those
	 * classes which implement this interface.
	 * 
	 * */
	
	/**
	 * Creates a new {@link DequeueContext} that will be used to dequeue children of this region.
	 * 
	 * Usually containers will create a context with their associated markup by getting it via
	 * {@link MarkupContainer#getAssociatedMarkup()}, but components that do not render markup in a
	 * standard way (such as repeaters and borders) may choose to override this method to implement
	 * custom behavior for the dequeueing process.
	 */
	DequeueContext newDequeueContext();

	/**
	 * Starts component dequeueing on this {@link IQueueRegion}. This is the entry point into the
	 * dequeuing process, it creates the {@link DequeueContext} and delegates the operation to the
	 * {@link org.apache.wicket.MarkupContainer#dequeue(DequeueContext)} method which performs the
	 * actual dequeueing. The context's markup is retrieved using the {@link MarkupContainer#getAssociatedMarkup()}.
	 */
	void dequeue();
	
	/**
	 * Returns the markup to use for queuing. Normally, this is the markup of the component 
	 * implementing this interface.
	 * 
	 * @return the markup to use for queuing
	 */
	IMarkupFragment getRegionMarkup();
}
