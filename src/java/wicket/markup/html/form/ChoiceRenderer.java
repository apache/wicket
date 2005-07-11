/**
 * 
 */
package wicket.markup.html.form;

import ognl.Ognl;
import ognl.OgnlException;
import wicket.WicketRuntimeException;

/**
 * @author jcompagner
 *
 */
public class ChoiceRenderer implements IChoiceRenderer
{

	private final String displayExpression;
	private final String idExpression;

	/**
	 * Default contructor of the ChoiceRenderer. 
	 * Both display and id propert will be null.
	 * The displayvalue will be the toString() of the object and
	 * the id value will be the index
	 */
	public ChoiceRenderer()
	{
		super();
		this.displayExpression = null;
		this.idExpression = null;
	}

	/**
	 * Id expression will be null so the id value will base itself on the index
	 * The display value will be calculated by the given ognl expression
	 * 
	 * @param displayExpression An ognl expression to get the display value
	 */
	public ChoiceRenderer(String displayExpression)
	{
		super();
		this.displayExpression = displayExpression;
		this.idExpression = null;
	}
	
	/**
	 * The display and id value will be calculated by the given ognl expressions
	 * 
	 * @param displayExpression An ognl expression to get the display value
	 * @param idExpression An ognl expression to get the id value
	 */
	public ChoiceRenderer(String displayExpression, String idExpression)
	{
		super();
		this.displayExpression = displayExpression;
		this.idExpression = idExpression;
	}

	/**
	 * @see wicket.markup.html.form.IChoiceRenderer#getDisplayValue(java.lang.Object)
	 */
	public String getDisplayValue(Object object)
	{
		Object returnValue = object;
		if(displayExpression != null && object != null)
		{
			try
			{
				returnValue = Ognl.getValue(displayExpression, object);
			}
			catch (OgnlException ex)
			{
				throw new WicketRuntimeException("Error getting display value of: " + object+ " for property: " + displayExpression,ex);
			}
		}
		if(returnValue == null) return "";
		else return returnValue.toString();
	}

	/**
	 * @see wicket.markup.html.form.IChoiceRenderer#getIdValue(java.lang.Object, int)
	 */
	public String getIdValue(Object object, int index)
	{
		if(idExpression == null) return Integer.toString(index);
		if(object == null) return "";
		try
		{
			Object returnValue = Ognl.getValue(idExpression, object);
			if(returnValue == null) return "";
			else return returnValue.toString();
		}
		catch (OgnlException ex)
		{
			throw new WicketRuntimeException("Error getting id value of: " + object + " for property: " + idExpression,ex);
		}
	}

}
