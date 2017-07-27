package org.apache.wicket.util.tester;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Created by dpoliakas on 27/07/2017.
 */
public class MockPageWithLabelInEnclosure extends WebPage {

    public MockPageWithLabelInEnclosure() {
        // Clicking this link re-renders the link itself
        this.add(new AjaxLink<Void>("testLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(this);
            }
        });
    }

    public AjaxLink<Void> getSelfRefreshingAjaxLink(){
        return (AjaxLink<Void>) get("testLink");
    }
}
