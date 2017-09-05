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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

public class PanelEdit extends Panel {

    public PanelEdit(String id) {
        super(id);

        setOutputMarkupId(true);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Form<Void> form = new Form<>("form", null);
        add(form);

        form.add(createSubmitButton());
    }

    private AjaxButton createSubmitButton() {

        AjaxButton submit = new AjaxButton("submit") {

            protected void onSubmit(AjaxRequestTarget target) {
                Component currentcomponent = PanelEdit.this;
                Component newComponent = new Label(currentcomponent.getId())
                		.setOutputMarkupId(true);
				currentcomponent.replaceWith(newComponent);
                target.add(newComponent);
            }

        };

        return submit;
    }
}