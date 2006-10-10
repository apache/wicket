/*
 * $Id: CsvView.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
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

import org.apache.commons.lang.StringUtils;

/**
 * Export view for comma separated value exporting.
 * 
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CsvView extends BaseExportView
{
	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#BaseExportView(List,
	 *      boolean, boolean, boolean)
	 */
	public CsvView(final List tableModel, final boolean exportFullList,
			final boolean includeHeader, final boolean decorateValues)
	{
		super(tableModel, exportFullList, includeHeader, decorateValues);
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getRowStart()
	 */
	@Override
	protected String getRowStart()
	{
		return "";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getRowEnd()
	 */
	@Override
	protected String getRowEnd()
	{
		return "\n";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getCellStart()
	 */
	@Override
	protected String getCellStart()
	{
		return "";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getCellEnd()
	 */
	@Override
	protected String getCellEnd()
	{
		return ",";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getDocumentStart()
	 */
	@Override
	protected String getDocumentStart()
	{
		return "";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getDocumentEnd()
	 */
	@Override
	protected String getDocumentEnd()
	{
		return "";
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getAlwaysAppendCellEnd()
	 */
	@Override
	protected boolean getAlwaysAppendCellEnd()
	{
		return false;
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getAlwaysAppendRowEnd()
	 */
	@Override
	protected boolean getAlwaysAppendRowEnd()
	{
		return true;
	}

	/**
	 * @see wicket.examples.displaytag.export.BaseExportView#getMimeType()
	 */
	@Override
	public String getMimeType()
	{
		return "text/csv";
	}

	/**
	 * Escaping for csv format.
	 * <ul>
	 * <li>Quotes inside quoted strings are escaped with a /</li>
	 * <li>Fields containings newlines or , are surrounded by ""</li>
	 * </ul>
	 * Note this is the standard CVS format and it's not handled well by excel.
	 * 
	 * @see wicket.examples.displaytag.export.BaseExportView#escapeColumnValue(java.lang.Object)
	 */
	@Override
	protected Object escapeColumnValue(Object value)
	{
		if (value != null)
		{
			String stringValue = StringUtils.trim(value.toString());
			if (!StringUtils.containsNone(stringValue, new char[] { '\n', ',' }))
			{
				return "\"" + StringUtils.replace(stringValue, "\"", "\\\"") + "\"";
			}

			return stringValue;
		}

		return null;
	}

}
