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
package wicket.behavior;

import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestListenerInterface;
import wicket.request.RequestParameters;
import wicket.request.target.component.listener.BehaviorRequestTarget;

/**
 * Same as {@link wicket.behavior.IBehaviorListener}, but urls generated for
 * this interface will always be executed against the latest page version.
 * 
 * @author Igor Vaynberg
 */
public interface IUnversionedBehaviorListener extends IBehaviorListener
{
	/** Behavior listener interface */
	public static final RequestListenerInterface INTERFACE = new RequestListenerInterface(
			IUnversionedBehaviorListener.class, false)
	{
		public IRequestTarget newRequestTarget(Page page, Component component,
				RequestListenerInterface listener, RequestParameters requestParameters)
		{
			return new BehaviorRequestTarget(page, component, listener, requestParameters);
		}
	};

}
