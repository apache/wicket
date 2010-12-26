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

import org.apache.wicket.Component;

/**
 * This is a marker interface for models that can be inherited from components higher in the
 * hierarchy.
 * 
 * If a model implements this interface then you can give the parent container this model and all
 * the child (recursively) components will also get and then set that model on their own if they are
 * created with a null model
 * 
 * <pre>
 * Form form = new Form(&quot;form&quot;, new ModelImplementingIInheritableModel());
 * new TextField(&quot;textfield&quot;); // notice textfield is created with a null model
 * </pre>
 * 
 * @author jcompagner
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            The model object type
 */
public interface IComponentInheritedModel<T> extends IModel<T>
{
	/**
	 * @param <W>
	 *            the model object type of the wrapped model
	 * @param component
	 * @return The WrapModel that wraps this model
	 */
	<W> IWrapModel<W> wrapOnInheritance(Component component);
}