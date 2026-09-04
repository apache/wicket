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
package org.apache.wicket.queueing.bodyisachild;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

public class LoginPanel extends Panel
{
    public final Pojo pojo = new Pojo();

    public LoginPanel(String id)
    {
        super(id);
        add(new LoginForm("loginForm"));
    }

    public static class Pojo implements Serializable
    {
        public String username;
        public String password;
    }

    class LoginForm extends StatelessForm {

        LoginForm(String id) {
            super(id);
            setModel(new CompoundPropertyModel<>(pojo));
            add(createValidatingFormField(new TextField("username").setRequired(true), "yourUsernameHeadline"));
            add(createValidatingFormField(new PasswordTextField("password").setResetPassword(false), "yourPasswordHeadline"));
        }

        private FormGroup createValidatingFormField(final FormComponent formComponent, String labelKey) {
            final String fieldId = formComponent.getId();
            final FormGroup formGroup = new FormGroup(fieldId + "FormGroup", Model.of(labelKey));
            formGroup.setOutputMarkupId(true);
            formGroup.add(formComponent);
            return formGroup;
        }
    }
}
