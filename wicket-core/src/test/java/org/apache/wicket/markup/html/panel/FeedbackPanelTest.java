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
