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

/**
 * Marker interface for validators that will accept <code>null</code> value.
 * Without implementing this interface wicket will never pass <code>null</code>
 * value to {@link IValidator#validate(IValidatable)}.
 * <p>
 * Keep in mind that the form component must have set the required property to
 * <code>false</code>, otherwise wicket will not permit the validator to
 * process the <code>null</code> value.
 * 
 * @see IValidator
 * @see org.apache.wicket.markup.html.form.FormComponent#setRequired(boolean)
 * 
 * @author Matej Knopp
 */
public interface INullAcceptingValidator extends IValidator
{

}
