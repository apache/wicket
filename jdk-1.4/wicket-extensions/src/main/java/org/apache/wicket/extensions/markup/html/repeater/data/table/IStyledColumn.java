package org.apache.wicket.extensions.markup.html.repeater.data.table;

/**
 * Interface that allows styling individuals DataTable columns 
 * 
 * @author Matej Knopp
 */
public interface IStyledColumn extends IColumn
{
	/**
	 * Returns the css class for this column.
	 * @return CSS class name
	 */
	public String getCssClass();
}
