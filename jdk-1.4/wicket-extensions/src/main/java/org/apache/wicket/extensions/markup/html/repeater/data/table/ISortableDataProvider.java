package org.apache.wicket.extensions.markup.html.repeater.data.table;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.markup.repeater.data.IDataProvider;


/**
 * Data provider that can hold sort state
 * 
 * @author Igor Vaynberg (ivaynberg at apache dot org)
 * 
 */
public interface ISortableDataProvider extends IDataProvider, ISortStateLocator
{


}