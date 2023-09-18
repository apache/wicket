package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ErrorRenderingPage extends BasePage
{
    private static final JavaScriptResourceReference JS = new JavaScriptResourceReference(ErrorRenderingPage.class, "ErrorRenderingPage.js");
    public static class WeirdException extends UnsupportedOperationException {

    }
    private boolean produceError = false;

    private final Label error;
    private final Label error1;

    public ErrorRenderingPage()
    {
        add(new AjaxLink<>("click")
        {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                produceError = true;
                target.add(error1);
                target.add(error);
            }
        });

        add(error = new Label("error", "Click above to trigger dynamic error")
        {
            @Override
            protected void onBeforeRender()
            {
                super.onBeforeRender();
                if (produceError)
                {
                    produceError = false;
                    throw new WeirdException();
                }
            }
        });
        error.setOutputMarkupId(true);

        add(error1 = new Label("error1", "No error!"));
        error1.setOutputMarkupId(true);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JS));
    }
}
