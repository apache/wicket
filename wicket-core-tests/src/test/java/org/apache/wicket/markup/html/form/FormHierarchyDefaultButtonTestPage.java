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

    /**
     * Construct.
     */
    public FormHierarchyDefaultButtonTestPage() {
        parentForm = new Form<>("parentForm");
        add(parentForm);

        parentInput = new TextField<>("parentInput");
        parentForm.add(parentInput);

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

        childSubmit = new Button("childSubmit");
        childForm.setDefaultButton(childSubmit);
        childForm.add(childSubmit);
    }
}
