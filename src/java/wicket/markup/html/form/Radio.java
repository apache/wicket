package wicket.markup.html.form;

import wicket.Component;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.util.lang.Objects;

/**
 * Component representing a single radio choice in a wicket.markup.html.form.RadioGroup.
 * 
 * Must be attached to an &lt;input type=&quot;radio&quot; ... &gt; component.
 * 
 * @see RadioGroup
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * 
 */
public class Radio extends WebMarkupContainer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public Radio(String id)
	{
		super(id);
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public Radio(String id, IModel model)
	{
		super(id, model);
	}


	/**
	 * @see Component#onComponentTag(ComponentTag)
	 * @param tag
	 *            the abstraction representing html tag of this component
	 */
	protected void onComponentTag(final ComponentTag tag)
	{

		// must be attached to <input type="radio" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "radio");

		RadioGroup group = (RadioGroup)findParent(RadioGroup.class);
		if (group == null)
		{
			throw new RuntimeException(
					"RadioChoice component ["
							+ getPath()
							+ "] cannot find a parent RadioGroup. All RadioChoice components must be a child of or below in the hierarchy of a RadioGroup component.");
		}

		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", getPath());

		// compare the model objects of the group and self, if the same add the
		// checked attribute
		if (Objects.equal(group.getModelObject(), getModelObject()))
		{
			tag.put("checked", "checked");
		}

		// Default handling for component tag
		super.onComponentTag(tag);
	}


}
