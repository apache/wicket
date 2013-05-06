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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Tests for {@link FeedbackPanel}
 * 
 * @author cgatay
 */
public class FeedbackPanelTest extends WicketTestCase
{
    
    @Test
    public void testCssClassesOnFeedbackPanel() throws Exception{
        TestPage testPage = new TestPage();
        testPage.label.error("Error message");
        testPage.label.info("Info message");
        testPage.label.warn("Warn message");
        executeTest(testPage, "FeedbackPanelTest_cssClasses_expected.html");
    }


    public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
    {
        FeedbackPanel feedbackPanel;
        Component label;

        public TestPage()
        {
            feedbackPanel = new FeedbackPanel("feedback");
            label = new Label("label");
            add(feedbackPanel, label);
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container,
                Class<?> containerClass)
        {
            return new StringResourceStream(
                    "<body>\n" + 
                    "<div wicket:id='feedback'/>\n" + 
                    "<div wicket:id='label'/>\n" + 
                    "</body>");
        }
    }
    
}
