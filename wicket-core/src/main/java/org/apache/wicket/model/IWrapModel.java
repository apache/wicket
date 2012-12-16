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
package org.apache.wicket.model;

/**
 * A marker interface that represents a model that serves as a wrapper for another. Typically these
 * models are produced by the following methods:
 * {@link IComponentAssignedModel#wrapOnAssignment(org.apache.wicket.Component)} and
 * {@link IComponentInheritedModel#wrapOnInheritance(org.apache.wicket.Component)}
 * 
 * The wrapped model will be called detach on when the component is detached when the wrap model is
 * created by an {@link IComponentAssignedModel}
 * 
 * @author jcompagner
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            The model object type
 */
@Refactor("kommt da ein model oder ein writeable model raus?")
public interface IWrapModel<T> extends IModel<T>
{
	/**
	 * Gets the wrapped model.
	 * 
	 * @return The wrapped model
	 */
	IModel<?> getWrappedModel();
}
