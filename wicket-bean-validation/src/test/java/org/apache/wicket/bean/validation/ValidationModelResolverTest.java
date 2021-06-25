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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IPropertyReflectionAwareModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.tester.WicketTesterExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author alexander.v.morozov
 */
public class ValidationModelResolverTest
{

    @RegisterExtension
    public WicketTesterExtension scope = new WicketTesterExtension();

    @Test
    public void noModelBoundToComponent()
    {
        TextField<String> textField = new TextField<String>("field");
        assertNull(ValidationModelResolver.resolvePropertyModelFrom(textField));
    }

    @Test
    public void simpleModelBoundToComponent()
    {
        TextField<String> textField = new TextField<String>("text", new Model<String>());
        assertNull(ValidationModelResolver.resolvePropertyModelFrom(textField));
    }

    @Test
    public void propertyModelBoundToComponent()
    {
        TextField<String> textField = new TextField<String>("text", new PropertyModel<String>(new TestValidatableBean(), "text"));
        IPropertyReflectionAwareModel<?> model = ValidationModelResolver.resolvePropertyModelFrom(textField);
        assertNotNull(model);
        assertEquals("text", model.getPropertyField().getName());
    }

}
