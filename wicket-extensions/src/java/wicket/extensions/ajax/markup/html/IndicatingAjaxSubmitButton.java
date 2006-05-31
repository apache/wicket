package wicket.extensions.ajax.markup.html;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxIndicatorAware;
import wicket.ajax.markup.html.form.AjaxSubmitButton;
import wicket.markup.html.form.Form;

/**
 * A variant of the {@link AjaxSubmitButton} that displays a busy indicator while the
 * ajax request is in progress.
 *
 * @author evan
 *
 */
public abstract class IndicatingAjaxSubmitButton
       extends AjaxSubmitButton implements IAjaxIndicatorAware {

       private final WicketAjaxIndicatorAppender indicatorAppender = new WicketAjaxIndicatorAppender();

       /**
        *
        * @param id
        * @param form
        */
       public IndicatingAjaxSubmitButton(String id, Form form)
       {
               super(id, form);
               add(indicatorAppender);
       }

       protected abstract void onSubmit(AjaxRequestTarget target, Form form);

       /**
        * @see IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
        * @return the markup id of the ajax indicator
        *
        */
       public String getAjaxIndicatorMarkupId()
       {
               return indicatorAppender.getMarkupId();
       }

}