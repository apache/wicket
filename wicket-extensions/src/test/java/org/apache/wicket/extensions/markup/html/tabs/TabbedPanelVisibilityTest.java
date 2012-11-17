package org.apache.wicket.extensions.markup.html.tabs;

import java.util.List;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class TabbedPanelVisibilityTest extends WicketTestCase
{
    @Test
    public void testLastTabCSSClass_2Tabs_FirstSelected_2Visible()
    {
        final TabbedPanelVisibilityTestPage visibilityTestPage = new TabbedPanelVisibilityTestPage(2, 2);
        tester.startPage(visibilityTestPage);
        final List<TagTester> tabsTags = tester.getTagsByWicketId("tabs");
        Boolean[] tagsFound = new Boolean[]{Boolean.FALSE, Boolean.FALSE};
        for (TagTester tabTags : tabsTags)
        {
            final String cssClass = tabTags.getAttribute("class");
            //we should have tab0 selected and tabs1 last
            if (cssClass.equals("tab0 selected"))
            {
                tagsFound[0] = Boolean.TRUE;
            }
            if (cssClass.equals("tab1 last"))
            {
                tagsFound[1] = Boolean.TRUE;
            }
        }
        Assert.assertArrayEquals(new Boolean[]{Boolean.TRUE, Boolean.TRUE}, tagsFound);
    }

    @Test
    public void testLastTabCSSClass_2Tabs_LastSelected_2Visible()
    {
        final TabbedPanelVisibilityTestPage visibilityTestPage = new TabbedPanelVisibilityTestPage(2, 2);
        //selecting the last tab
        visibilityTestPage.tabbedPanel.setSelectedTab(1);
        tester.startPage(visibilityTestPage);
        final List<TagTester> tabsTags = tester.getTagsByWicketId("tabs");
        Boolean[] tagsFound = new Boolean[]{Boolean.FALSE, Boolean.FALSE};
        for (TagTester tabTags : tabsTags)
        {
            final String cssClass = tabTags.getAttribute("class");
            //we should have tab0 and tab1 selected last
            if (cssClass.equals("tab0"))
            {
                tagsFound[0] = Boolean.TRUE;
            }
            if (cssClass.equals("tab1 selected last"))
            {
                tagsFound[1] = Boolean.TRUE;
            }
        }
        Assert.assertArrayEquals(new Boolean[]{Boolean.TRUE, Boolean.TRUE}, tagsFound);
    }

    @Test
    public void testLastTabCSSClass_2Tabs_FirstSelected_1Visible()
    {
        final TabbedPanelVisibilityTestPage visibilityTestPage = new TabbedPanelVisibilityTestPage(2, 1);
        tester.startPage(visibilityTestPage);
        final List<TagTester> tabsTags = tester.getTagsByWicketId("tabs");
        Boolean[] tagsFound = new Boolean[]{Boolean.FALSE};
        for (TagTester tabTags : tabsTags)
        {
            final String cssClass = tabTags.getAttribute("class");
            //we should have tab0 selected and last
            if (cssClass.equals("tab0 selected last"))
            {
                tagsFound[0] = Boolean.TRUE;
            }
        }
        Assert.assertArrayEquals(new Boolean[]{Boolean.TRUE}, tagsFound);
    }

}
