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
package org.apache.wicket.ajax.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AjaxFormComponentUpdatingBehaviorTest extends WicketTestCase
{
    @Test
    void enablesRecursiveSerializationIfFormComponentPanelAndInnerComponentsMustBeUpdated()
    {
        FormComponentPanel<Object> formComponentPanel = new FormComponentPanel<>("some_id") {};
        var behavior = new AjaxFormComponentUpdatingBehavior("some_event")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
            }
        };
        formComponentPanel.add(behavior);
        AjaxRequestAttributes attributes = new AjaxRequestAttributes();

        behavior.updateAjaxAttributes(attributes);

        assertTrue(attributes.isSerializeRecursively());
    }

    @Test
    void doesNotModifyRecursiveSerializationIfNotFormComponentPanel()
    {
        TextField textField = new TextField<>("some_id");
        var behavior = new AjaxFormComponentUpdatingBehavior("some_event")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
            }
        };
        textField.add(behavior);
        AjaxRequestAttributes attributes = new AjaxRequestAttributes();
        attributes.setSerializeRecursively(true);

        behavior.updateAjaxAttributes(attributes);

        assertTrue(attributes.isSerializeRecursively());
    }

    @Test
    void doesNotModifyRecursiveSerializationIfInnerComponentsMustNotBeUpdated()
    {
        FormComponentPanel<Object> formComponentPanel = new FormComponentPanel<>("some_id") {};
        var behavior = new AjaxFormComponentUpdatingBehavior("some_event")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
            }

            @Override
            protected boolean updateInnerComponents()
            {
                return false;
            }
        };
        formComponentPanel.add(behavior);
        AjaxRequestAttributes attributes = new AjaxRequestAttributes();
        attributes.setSerializeRecursively(true);

        behavior.updateAjaxAttributes(attributes);

        assertTrue(attributes.isSerializeRecursively());
    }
}
