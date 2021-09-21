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
package org.apache.wicket.protocol.http;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

public class MultipartFormComponentListenerPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public MultipartFormComponentListenerPage() {
		CompoundPropertyModel<MultipartFormComponentListenerBean> model = new CompoundPropertyModel<>(new MultipartFormComponentListenerBean());
		Form<MultipartFormComponentListenerBean> form = new Form<>("form", model);
		add(form);

		RequiredTextField<String> textField = new RequiredTextField<>("textField");
		form.add(textField);

		ArrayList<String> list = new ArrayList<>();
		list.add("Option 1");
		list.add("Option 2");

		FileUploadField fileUpload = new FileUploadField("fileUpload", new Model<>(new ArrayList<>()));
		fileUpload.setOutputMarkupPlaceholderTag(true);
		form.add(fileUpload);

		DropDownChoice<String> dropDown = new DropDownChoice<>("dropDown", list);
		dropDown.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				fileUpload.setVisible(!fileUpload.isVisible());
				target.add(fileUpload);

			}
		});
		form.add(dropDown);

		final Label label = new Label("label");
		add(label);

		form.add(new Button("submitButton") {
			@Override
			public void onSubmit() {
				label.setDefaultModel(new Model<>(model.getObject().getTextField()));
			}

			@Override
			public void onError() {
				label.setDefaultModel(new Model<>("Validation Error..."));
			}
		});

		add(new AjaxLink<>("toggleVisibility") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				form.setVisible(!form.isVisible());
				target.add(form);
			}
		});
	}
}
