/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.displaytag.export;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * Export view for xml exporting.
 * 
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class XmlView extends BaseExportView
{
	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#BaseExportView(List,
	 *      boolean, boolean, boolean)
	 */
	public XmlView(final List tableModel, final boolean exportFullList,
			final boolean includeHeader, final boolean decorateValues)
	{
		super(tableModel, exportFullList, includeHeader, decorateValues);
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getRowStart()
	 */
	protected String getRowStart()
	{
		return "<row>\n";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getRowEnd()
	 */
	protected String getRowEnd()
	{
		return "</row>\n";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getCellStart()
	 */
	protected String getCellStart()
	{
		return "<column>";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getCellEnd()
	 */
	protected String getCellEnd()
	{
		return "</column>\n";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getDocumentStart()
	 */
	protected String getDocumentStart()
	{
		return "<?xml version=\"1.0\"?>\n<table>\n";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getDocumentEnd()
	 */
	protected String getDocumentEnd()
	{
		return "</table>\n";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getAlwaysAppendCellEnd()
	 */
	protected boolean getAlwaysAppendCellEnd()
	{
		return true;
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getAlwaysAppendRowEnd()
	 */
	protected boolean getAlwaysAppendRowEnd()
	{
		return true;
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getMimeType()
	 */
	public String getMimeType()
	{
		return "text/xml";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#escapeColumnValue(java.lang.Object)
	 */
	protected Object escapeColumnValue(Object value)
	{
		if (value != null)
		{
			return StringEscapeUtils.escapeXml(value.toString());
		}
		return null;
	}

}