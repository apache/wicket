/**
 * 
 */
package wicket.examples.forminput;

import wicket.markup.html.form.FormComponentPanel;
import wicket.markup.html.form.TextField;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * Displays how a {@link FormComponentPanel} can be used. Needs a model that
 * resolves to an Integer object.
 * 
 * @author eelcohillenius
 */
public class Multiply extends FormComponentPanel
{
	private TextField left;

	private int lhs = 0;

	private int rhs = 0;

	private TextField right;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 */
	public Multiply(String id)
	{
		super(id);
		init();
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 */
	public Multiply(String id, IModel model)
	{
		super(id, model);
		init();
	}

	/**
	 * @return gets lhs
	 */
	public int getLhs()
	{
		return lhs;
	}

	/**
	 * @return gets rhs
	 */
	public int getRhs()
	{
		return rhs;
	}

	/**
	 * @param lhs
	 *            the lhs to set
	 */
	public void setLhs(int lhs)
	{
		this.lhs = lhs;
	}

	/**
	 * @param rhs
	 *            the rhs to set
	 */
	public void setRhs(int rhs)
	{
		this.rhs = rhs;
	}

	/**
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
		// childs are currently updated *after* this component,
		// so if we want to use the updated models of these
		// components, we have to trigger the update manually
		left.updateModel();
		right.updateModel();
		setModelObject(new Integer(lhs * rhs));
	}

	private void init()
	{
		add(left = new TextField("left", new PropertyModel(this, "lhs"), Integer.class));
		add(right = new TextField("right", new PropertyModel(this, "rhs"), Integer.class));
		left.setRequired(true);
		right.setRequired(true);
	}
}
