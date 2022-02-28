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

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.visit.IVisitor;

/**
 * This listener updates the {@link Form}'s <em>enctype</em> whenever a multipart {@link FormComponent}
 * is added to the {@code AjaxRequestTarget}.
 * This is needed because the multipart form component may change its visibility/enablement and thus
 * change the multipart-ness of the whole form.
 */
public class MultipartFormComponentListener implements AjaxRequestTarget.IListener
{
	static final String ENCTYPE_URL_ENCODED = "application/x-www-form-urlencoded";

	@Override
	public void onAfterRespond(final Map<String, Component> map, final AjaxRequestTarget target)
	{
		target.getPage().visitChildren(Form.class, (IVisitor<Form<?>, Void>) (form, formVisitor) -> {
			if (form.isVisibleInHierarchy()) {
				form.visitFormComponents((formComponent, visit) -> {
					if (formComponent.isMultiPart()) {
						String enctype = form.isMultiPart() ? Form.ENCTYPE_MULTIPART_FORM_DATA : ENCTYPE_URL_ENCODED;
						target.appendJavaScript(String.format("Wicket.$('%s').enctype='%s'", form.getMarkupId(), enctype));
						visit.stop();
					}
				});
			} else {
				formVisitor.dontGoDeeper();
			}
		});
	}
}
