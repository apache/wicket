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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.junit.jupiter.api.Assertions;

public class FormHierarchyDefaultButtonTestPage extends WebPage {
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 1L;

    public final Form<Void> parentForm;
    public final Form<Void> childForm;
    public final Button parentSubmit;
    public final Button childSubmit;
    public final TextField<?> parentInput;
    public final TextField<?> childInput;
	public final TextArea<?> parentTextarea;
	public final TextArea<?> childTextarea;

    /**
     * Construct.
     */
    public FormHierarchyDefaultButtonTestPage() {
        parentForm = new Form<>("parentForm");
        add(parentForm);

        parentInput = new TextField<>("parentInput");
        parentForm.add(parentInput);

		parentTextarea = new TextArea<>("parentTextarea");
		parentForm.add(parentTextarea);

        parentSubmit = new Button("parentSubmit");
        parentSubmit.add(new AjaxFormSubmitBehavior(parentForm, "click") {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                Assertions.fail("Shouldn't be called");
            }

        });
        parentForm.setDefaultButton(parentSubmit);
        parentForm.add(parentSubmit);

        childForm = new Form<>("childForm");
        parentForm.add(childForm);

        childInput = new TextField<>("childInput");
        childForm.add(childInput);

		childTextarea = new TextArea<>("childTextarea");
		childForm.add(childTextarea);

        childSubmit = new Button("childSubmit");
        childForm.setDefaultButton(childSubmit);
        childForm.add(childSubmit);
    }
}
