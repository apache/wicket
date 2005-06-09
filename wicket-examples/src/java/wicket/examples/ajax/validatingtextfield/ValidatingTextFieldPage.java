package wicket.examples.ajax.validatingtextfield;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ajax.AjaxValidationTextField;
import wicket.markup.html.form.validation.IntegerValidator;
import wicket.markup.html.form.validation.LengthValidator;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.Model;

/**
 * @author jcompagner
 * @version $Id$
 */
public class ValidatingTextFieldPage extends WebPage
{
	/**
	 * Simple pages with a form/feedback panel and a AjaxValidation textfield 
	 */
	public ValidatingTextFieldPage()
	{
		
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		Form form = new Form("form",feedback) {};
		AjaxValidationTextField tf = new AjaxValidationTextField("validating",feedback,new Model());
		tf.add(LengthValidator.range(2, 5));
		tf.add(IntegerValidator.INT);
		form.add(tf);
		add(form);
		
	}
}
