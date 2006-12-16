/**
 * 
 */
package wicket.examples.forminput;

import wicket.MarkupContainer;
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
public class Multiply extends FormComponentPanel<Integer>
{
	private TextField left;

	private int lhs = 0;

	private int rhs = 0;

	private TextField right;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 *            The component id
	 */
	public Multiply(final MarkupContainer parent, String id)
	{
		super(parent, id);
		init();
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 *            The component id
	 * @param model
	 *            The model
	 */
	public Multiply(final MarkupContainer parent, String id, IModel<Integer> model)
	{
		super(parent, id, model);
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
		left = new TextField<Integer>(this, "left", new PropertyModel<Integer>(this, "lhs"), Integer.class);
		right = new TextField<Integer>(this, "right", new PropertyModel<Integer>(this, "rhs"), Integer.class);
		left.setRequired(true);
		right.setRequired(true);
	}
}
