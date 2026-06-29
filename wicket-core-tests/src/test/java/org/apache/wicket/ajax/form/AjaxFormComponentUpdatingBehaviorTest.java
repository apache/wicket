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

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.ajax.AjaxRequestHandler;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.MultiFileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class AjaxFormComponentUpdatingBehaviorTest extends WicketTestCase
{
    @Test
    void enablesRecursiveSerializationIfComponentMarkedToWantToProcessInputOfChildrenInAjaxUpdate()
    {
        var behavior = new AjaxFormComponentUpdatingBehavior("some event")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
            }
        };
        var formComponentPanel = new MultiFileUploadField("someId");
        formComponentPanel.setMetaData(FormComponentPanel.WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE, TRUE);
        formComponentPanel.add(behavior);
        var attributes = new AjaxRequestAttributes();

        behavior.updateAjaxAttributes(attributes);

        assertTrue(attributes.isSerializeRecursively());
    }

    @Test
    void invokesProcessChildrenOnEventIfComponentMarkedToWantToProcessInputOfChildrenInAjaxUpdate()
    {
        var behavior = new AjaxFormComponentUpdatingBehavior("some event")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
            }
        };
        var form = new Form<Void>("form");
        var hasProcessInputOfChildreenBeenCalled = new AtomicBoolean(false);
        var formComponentPanel = new MultiFileUploadField("upload", Model.of(List.of()))
        {
            @Override
            public void processInputOfChildren()
            {
                hasProcessInputOfChildreenBeenCalled.set(true);
            }
        };
        formComponentPanel.setMetaData(FormComponentPanel.WANT_CHILDREN_TO_PROCESS_INPUT_IN_AJAX_UPDATE, TRUE);
        formComponentPanel.add(behavior);
        form.add(formComponentPanel);
        tester.startComponentInPage(form, Markup.of("<form wicket:id=\"form\"><input type=\"file\" wicket:id=\"upload\"></input></form>"));

        behavior.onEvent(new AjaxRequestHandler(form.getPage()));

        assertTrue(hasProcessInputOfChildreenBeenCalled.get());
    }
}
