package wicket.markup.html.form;

import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Component used to connect instances of Radio components into a group.
 * Instances of Radio have to be in the component hierarchy somewhere below the
 * group component. The model object of the gorup is set to the model object of
 * the selected radio component or null if none selected.
 * 
 * ie
 * 
 * <code>
 * <span wicket:id="radiochoicegroup">
 *   ...
 *   <input type="radio" wicket:id="singleradiochoice1">choice 1</input>
 *   ...
 *   <input type="radio" wicket:id="singleradiochoice2">choice 2</input>
 *   ...
 * </span>
 * </code>
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * 
 */
public class RadioGroup extends FormComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String)
	 */
	public RadioGroup(String id)
	{
		this(id, new Model());
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(String, IModel)
	 */
	public RadioGroup(String id, IModel model)
	{
		super(id, model);
		setRenderBodyOnly(true);
	}

	/**
	 * @see FormComponent#updateModel()
	 */
	public void updateModel()
	{
		/*
		 * the input value contains the full path of the radio unless no choice
		 * was selected in which case the input contains null
		 */
		String path = getInput();

		if (path != null)
		{
			/*
			 * single radio choice component path sans group path = relative
			 * path from group to choice since we know the choice is child of
			 * group
			 */
			path = path.substring(getPath().length() + 1);

			// retrieve the selected single radio choice component
			Radio choice = (Radio)get(path);

			// assign the value of the group's model
			setModelObject(choice.getModelObject());
		} else {
			// no choice selected - set model object to null
			setModelObject(null);
		}
	}

}
