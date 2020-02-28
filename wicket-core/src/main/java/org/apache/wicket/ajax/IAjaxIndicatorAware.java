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


import org.apache.wicket.util.io.IClusterable;

/**
 * This interface makes it trivial to use busy indicators for ajax requests. This interface can be
 * implemented by a component that has an ajax behavior attached to it, or any parent of the
 * component, or by the ajax behavior itself. If this is the case javascript will be added
 * automatically that will show a markup element pointed to by the
 * {@link #getAjaxIndicatorMarkupId()} markup id attribute when the ajax request begins, and hide it
 * when the ajax requests succeeds or fails.
 * <p>
 * If both a component and a behavior implement this interface, the component will take precedence.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
@FunctionalInterface
public interface IAjaxIndicatorAware extends IClusterable
{
	/**
	 * @return the value of the markup id attribute of the indicating element
	 */
	String getAjaxIndicatorMarkupId();
}
