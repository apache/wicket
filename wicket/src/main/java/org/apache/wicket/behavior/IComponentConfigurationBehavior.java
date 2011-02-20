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
package org.apache.wicket.behavior;

import org.apache.wicket.Component;

/**
 * An add-on interface for IBehavior instances that want to contribute to a component's
 * configuration. Any IBehavior that also implements this method will receive notifications when the
 * component onConfigure method is being called.
 * 
 * @author jrthomerson
 * @since 1.4.16
 */
public interface IComponentConfigurationBehavior extends IBehavior
{

	/**
	 * Called immediately after the onConfigure method in a component. Since this is before the
	 * rendering cycle has begun, the behavior can modify the configuration of the component (i.e.
	 * setVisible(false))
	 * 
	 * @param component
	 *            the component being configured
	 */
	void onConfigure(Component component);
}
