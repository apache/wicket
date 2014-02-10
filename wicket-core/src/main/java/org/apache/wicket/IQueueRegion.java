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

/**
 * Demarcates components that can dequeue children. These are usually components with associated
 * markup since the markup is needed to dequeue.
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

}
