package wicket.extensions.markup.html.repeater.data.table;

import wicket.model.IModel;

/**
 * This column will display the specified default value if the evaluation of the
 * provided ognl expression fails. Useful for cases where a part of ognl
 * expression can evaluate to null, ie foo.bar with foo being null.
 * 
 * @author igor
 * 
 */
public class LenientPropertyColumn extends PropertyColumn
{
	private static final long serialVersionUID = 1L;

	private Object defaultValue;

	/**
	 * Constructs a sortable column
	 * 
	 * @param displayModel
	 *            column caption model
	 * @param sortProperty
	 *            sort property
	 * @param ognlExpression
	 *            ognl expression
	 * @param defaultValue
	 *            default value
	 */
	public LenientPropertyColumn(IModel displayModel, String sortProperty, String ognlExpression,
			Object defaultValue)
	{
		super(displayModel, sortProperty, ognlExpression);
		this.defaultValue = defaultValue;
	}

	/**
	 * Constructs non sortable column
	 * 
	 * @param displayModel
	 *            column caption model
	 * @param ognlExpression
	 *            ognl expression
	 * @param defaultValue
	 *            default value
	 */
	public LenientPropertyColumn(IModel displayModel, String ognlExpression, Object defaultValue)
	{
		super(displayModel, ognlExpression);
		this.defaultValue = defaultValue;
	}

	protected IModel createLabelModel(IModel embeddedModel)
	{
		IModel model = super.createLabelModel(embeddedModel);
		return new LenientModelWrapper(model, defaultValue);
	}

}
