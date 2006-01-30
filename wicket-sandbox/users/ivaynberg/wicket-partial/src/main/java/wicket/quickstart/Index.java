package wicket.quickstart;

import wicket.PageParameters;
import wicket.markup.html.basic.Label;
import wicket.model.PropertyModel;
import wicket.quickstart.partial.AjaxIdSetter;
import wicket.quickstart.partial.AjaxLink;
import wicket.quickstart.partial.AjaxRequestTarget;

/**
 * Basic bookmarkable index page.
 * 
 * NOTE: You can get session properties from QuickStartSession via
 * getQuickStartSession()
 */
public class Index extends QuickStartPage
{
	int counter = 0;

	int counter2 = 100;

	public int getCounter()
	{
		return counter;
	}

	public int getCounter2()
	{
		return counter2;
	}

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public Index(final PageParameters parameters)
	{
		final Label counterLabel;
		final Label counterLabel2;

		counterLabel = new Label("counter-label", new PropertyModel(this, "counter"));
		counterLabel.add(AjaxIdSetter.INSTANCE);

		counterLabel2 = new Label("counter-label2", new PropertyModel(this, "counter2"));
		counterLabel2.add(AjaxIdSetter.INSTANCE);

		add(counterLabel);
		add(counterLabel2);

		add(new AjaxLink("ajax-link")
		{
			public void onAjax()
			{
				System.out.println("AJAX");
				counter++;
				counter2++;
				AjaxRequestTarget target = new AjaxRequestTarget();
				target.addComponent(counterLabel);
				target.addComponent(counterLabel2);
				target.addJavascript("alert('counters " + counter + " " + counter2 + "')");
				getRequestCycle().setRequestTarget(target);
			}
		});
	}
}
