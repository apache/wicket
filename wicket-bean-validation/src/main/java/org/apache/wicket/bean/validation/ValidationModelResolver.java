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
package org.apache.wicket.bean.validation;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IPropertyReflectionAwareModel;
import org.apache.wicket.model.IWrapModel;

/**
 * @author alexander.v.morozov
 */
final class ValidationModelResolver
{

    /**
     * Lookup for property-aware model, attached to certain form component.
     *
     * @param component
     *              form component
     *
     * @return property-aware model, extracted from supplied component or <code>null</code>
     */
    public static IPropertyReflectionAwareModel<?> resolvePropertyModelFrom(FormComponent<?> component)
    {
        IModel<?> model = component.getModel();
        while (model != null) {
            if (model instanceof IPropertyReflectionAwareModel)
            {
                return (IPropertyReflectionAwareModel<?>) model;
            }
            if (model instanceof IWrapModel<?>)
            {
                model = ((IWrapModel<?>)model).getWrappedModel();
                continue;
            }
            break; // not model found
        }
        return null;
    }

    private ValidationModelResolver()
    {
        // nop
    }

}
