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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

class FormMethodMismatchTest {

    static class PlainFormPage extends WebPage {
        PlainFormPage(Form<Void> underTest) {
            add(underTest);
        }
    }

    @Test
    void formSubmittedContinuesWithCorrectMethod() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }
        };
        tester.startPage(new PlainFormPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        formTester.submit();
        assertTrue(onSubmitCalled[0]);
    }

    @Test
    void formSubmittedContinuesByDefaultWithMismatchingMethod() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }
        };
        tester.startPage(new PlainFormPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        tester.getRequest().setMethod("GET");
        formTester.submit();
        assertTrue(onSubmitCalled[0]);
    }

    @Test
    void formSubmittedAbortsByWithMismatchingMethodWhenDesired() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }

            @Override
            protected MethodMismatchResponse onMethodMismatch() {
                return MethodMismatchResponse.ABORT;
            }
        };
        tester.startPage(new PlainFormPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        tester.getRequest().setMethod("GET");
        formTester.submit();
        assertFalse(onSubmitCalled[0]);
    }

    @Test
    void formSubmittedContinuesByWithCorrectMethodWhenDesired() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }

            @Override
            protected MethodMismatchResponse onMethodMismatch() {
                return MethodMismatchResponse.ABORT;
            }
        };
        tester.startPage(new PlainFormPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        formTester.submit();
        assertTrue(onSubmitCalled[0]);
    }

    static class FormWithButtonPage extends WebPage {
        FormWithButtonPage(Form<Void> underTest) {
            add(underTest);
            underTest.add(new Button("button"));
        }
    }

    @Test
    void withButtonFormSubmittedContinuesWithCorrectMethod() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }
        };
        tester.startPage(new FormWithButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        formTester.submit("button");
        assertTrue(onSubmitCalled[0]);
    }

    @Test
    void withButtonFormSubmittedContinuesByDefaultWithMismatchingMethod() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }
        };
        tester.startPage(new FormWithButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        tester.getRequest().setMethod("GET");
        formTester.submit("button");
        assertTrue(onSubmitCalled[0]);
    }

    @Test
    void withButtonFormSubmittedAbortsByWithMismatchingMethodWhenDesired() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }

            @Override
            protected MethodMismatchResponse onMethodMismatch() {
                return MethodMismatchResponse.ABORT;
            }
        };
        tester.startPage(new FormWithButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        tester.getRequest().setMethod("GET");
        formTester.submit("button");
        assertFalse(onSubmitCalled[0]);
    }

    @Test
    void withButtonFormSubmittedContinuesByWithCorrectMethodWhenDesired() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }

            @Override
            protected MethodMismatchResponse onMethodMismatch() {
                return MethodMismatchResponse.ABORT;
            }
        };
        tester.startPage(new FormWithButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        formTester.submit("button");
        assertTrue(onSubmitCalled[0]);
    }

    static class FormWithAjaxButtonPage extends WebPage {
        FormWithAjaxButtonPage(Form<Void> underTest) {
            add(underTest);
            underTest.add(new AjaxButton("button") {

            });
        }
    }
    @Test
    void withAjaxButtonFormSubmittedContinuesWithCorrectMethod() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }
        };
        tester.startPage(new FormWithAjaxButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        formTester.submit("button");
        assertTrue(onSubmitCalled[0]);
    }

    @Test
    void withAjaxButtonFormSubmittedContinuesByDefaultWithMismatchingMethod() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }
        };
        tester.startPage(new FormWithAjaxButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        tester.getRequest().setMethod("GET");
        formTester.submit("button");
        assertTrue(onSubmitCalled[0]);
    }

    @Test
    void withAjaxButtonFormSubmittedAbortsByWithMismatchingMethodWhenDesired() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }

            @Override
            protected MethodMismatchResponse onMethodMismatch() {
                return MethodMismatchResponse.ABORT;
            }
        };
        tester.startPage(new FormWithAjaxButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        tester.getRequest().setMethod("GET");
        formTester.submit("button");
        assertFalse(onSubmitCalled[0]);
    }

    @Test
    void withAjaxButtonFormSubmittedContinuesByWithCorrectMethodWhenDesired() {
        final WicketTester tester = new WicketTester();
        final boolean[] onSubmitCalled = new boolean[1];
        final Form<Void> underTest = new Form<Void>("underTest") {
            @Override
            protected void onSubmit() {
                onSubmitCalled[0] = true;
            }

            @Override
            protected MethodMismatchResponse onMethodMismatch() {
                return MethodMismatchResponse.ABORT;
            }
        };
        tester.startPage(new FormWithAjaxButtonPage(underTest));
        final FormTester formTester = tester.newFormTester("underTest");
        formTester.submit("button");
        assertTrue(onSubmitCalled[0]);
    }
}

