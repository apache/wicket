package wicket.examples.ajax.builtin;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.ajax.markup.html.AjaxLink;
import wicket.behavior.MarkupIdSetter;
import wicket.markup.html.basic.Label;
import wicket.model.PropertyModel;

public class LinksPage extends BasePage
{
	private int counter1 = 0;
	private int counter2 = 0;


	public int getCounter1()
	{
		return counter1;
	}


	public void setCounter1(int counter1)
	{
		this.counter1 = counter1;
	}


	public int getCounter2()
	{
		return counter2;
	}


	public void setCounter2(int counter2)
	{
		this.counter2 = counter2;
	}

	public LinksPage()
	{

		final Label c1 = new Label("c1", new PropertyModel(this, "counter1"));
		add(c1.add(MarkupIdSetter.INSTANCE));

		final Label c2 = new Label("c2", new PropertyModel(this, "counter2"));
		add(c2.add(MarkupIdSetter.INSTANCE));

		add(new AjaxLink("c1-link")
		{

			protected void onClick(AjaxRequestTarget target)
			{
				counter1++;
				target.addComponent(c1);
			}

		});

		add(new AjaxFallbackLink("c2-link")
		{

			protected void onClick(AjaxRequestTarget target)
			{
				counter2++;
				if (target != null)
				{
					target.addComponent(c2);
				}
			}

		});

	}

}
