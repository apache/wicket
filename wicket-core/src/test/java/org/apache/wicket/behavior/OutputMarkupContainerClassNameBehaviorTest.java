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
package org.apache.wicket.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.MockPanelWithLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class OutputMarkupContainerClassNameBehaviorTest extends WicketTestCase {

    @Test
    void whenDebugIsEnabled_thenRenderAttribute()
    {
        tester.getApplication().getDebugSettings().setOutputMarkupContainerClassName(true);

        MockPanelWithLink component = new MockPanelWithLink("test") {
            @Override
            protected void onLinkClick(AjaxRequestTarget target) {

            }
        };
        tester.startComponentInPage(component);

        assertTrue(tester.getLastResponseAsString().contains("<wicket:panel wicket:className=\"org.apache.wicket.MockPanelWithLink\">"));
    }

    @Test
    void whenDebugIsDisabled_thenDontRenderAttribute()
    {
        tester.getApplication().getDebugSettings().setOutputMarkupContainerClassName(false);

        MockPanelWithLink component = new MockPanelWithLink("test") {
            @Override
            protected void onLinkClick(AjaxRequestTarget target) {

            }
        };
        tester.startComponentInPage(component);

        assertFalse(tester.getLastResponseAsString().contains("<wicket:panel wicket:className=\"org.apache.wicket.MockPanelWithLink\">"));
    }
}
