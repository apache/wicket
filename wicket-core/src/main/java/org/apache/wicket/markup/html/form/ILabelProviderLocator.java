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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.Component;

/**
 * This interface can be used to mark some complex component, e.g. a Panel, as capable of providing a reference to a {@link ILabelProvider},
 * e.g a form component. The typical use case could be a user has a components factory of type "label" --&gt; "panel with some fields". Let's say the structure of
 * this panel varies but we still would like to use wicket:for="panel". In this case this panel could implement ILabelProviderLocator to point
 * to TextFiled that label should refer to.
 *
 * @author reiern70@gmail.com
 */
public interface ILabelProviderLocator
{

	/**
	 * @return The component the wicket:for attribute is referring to.
	 */
	Component getAutoLabelComponent();
}
