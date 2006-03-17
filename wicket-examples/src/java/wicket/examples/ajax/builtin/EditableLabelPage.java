package wicket.examples.ajax.builtin;

import wicket.Component;
import wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.Link;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.PropertyModel;

/**
 * Page to demo the inplace edit label {@link AjaxEditableLabel}
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class EditableLabelPage extends BasePage
{
	private String text1 = "fox";
	private String text2 = "dog";
	private int refreshCounter = 0;


	/**
	 * Constructor
	 */
	public EditableLabelPage()
	{
		add(new AjaxEditableLabel("text1", new PropertyModel(this, "text1")));
		add(new AjaxEditableLabel("text2", new PropertyModel(this, "text2")));
		add(new Label("refresh-counter", new AbstractReadOnlyModel()
		{

			public Object getObject(Component component)
			{
				return ""+refreshCounter;
			}

		}));

		add(new Link("refresh-link")
		{

			public void onClick()
			{
				refreshCounter++;
			}

		});
	}

	/**
	 * @return text1
	 */
	public String getText1()
	{
		return text1;
	}


	/**
	 * @return text2
	 */
	public String getText2()
	{
		return text2;
	}


	/**
	 * @param text1
	 */
	public void setText1(String text1)
	{
		this.text1 = text1;
	}


	/**
	 * @param text2
	 */
	public void setText2(String text2)
	{
		this.text2 = text2;
	}


}
