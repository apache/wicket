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
package org.apache.wicket.validation;

import org.apache.wicket.markup.html.form.FormComponent;

/**
 * Marker interface for validators that will accept a <code>null</code> value. Without implementing
 * this interface Wicket will never pass <code>null</code> values to
 * {@link IValidator#validate(IValidatable)}.
 * <p>
 * Keep in mind that the {@link FormComponent} must have set the required property to
 * <code>false</code>, otherwise Wicket will not permit the validator to process the
 * <code>null</code> value.
 * 
 * @author Matej Knopp
 * @param <T>
 *            type of validatable
 * @since 1.2.6
 * @see IValidator
 * @see FormComponent#setRequired(boolean)
 */
public interface INullAcceptingValidator<T> extends IValidator<T>
{

}
