/*
 * $Id: Export.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.markup.MarkupStream;
import wicket.protocol.http.WebResponse;

/**
 * 
 */
public class Export extends Page
{
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Export.class);

	private final BaseExportView exportView;

	/**
	 * 
	 * @param exportView
	 * @param data
	 */
	public Export(final BaseExportView exportView, final List data)
	{
		this.exportView = exportView;
	}

	/**
	 * 
	 * @param markupStream
	 */
	@Override
	protected void onRender(MarkupStream markupStream)
	{
		doExport();
	}

	/**
	 * called when data are not displayed in a html page but should be exported.
	 * 
	 * @return int EVAL_PAGE or SKIP_PAGE
	 */
	public int doExport()
	{
		String mimeType = exportView.getMimeType();
		String exportString = exportView.doExport();

		String filename = null; // getExportFileName(MediaTypeEnum.XML);
		return writeExport(mimeType, exportString, filename);
	}

	/**
	 * Will write the export. The default behavior is to write directly to the
	 * response. If the ResponseOverrideFilter is configured for this request,
	 * will instead write the export content to a StringBuffer in the Request
	 * object.
	 * 
	 * @param mimeType
	 *            mime type to set in the response
	 * @param exportString
	 *            String
	 * @param filename
	 *            name of the file to be saved. Can be null, if set the
	 *            content-disposition header will be added.
	 * @return int
	 */
	protected int writeExport(final String mimeType, final String exportString,
			final String filename)
	{
		WebResponse response = (WebResponse)getResponse();
		HttpServletResponse servletResponse = response.getHttpServletResponse();

		// response can't be already committed at this time
		if (servletResponse.isCommitted())
		{
			throw new WicketRuntimeException(
					"HTTP response already committed. Can not change that any more");
		}

		// if cache is disabled using http header, export will not work.
		// Try to remove bad headers overwriting them, since there is no way to
		// remove a single header and reset()
		// could remove other "useful" headers like content encoding
		if (servletResponse.containsHeader("Cache-Control"))
		{
			servletResponse.setHeader("Cache-Control", "public");
		}
		if (servletResponse.containsHeader("Expires"))
		{
			servletResponse.setHeader("Expires", "Thu, 01 Dec 2069 16:00:00 GMT");
		}
		if (servletResponse.containsHeader("Pragma"))
		{
			// Pragma: no-cache
			// http 1.0 equivalent of Cache-Control: no-cache
			// there is no "Cache-Control: public" equivalent, so just try to
			// set it to an empty String (note
			// this is NOT a valid header)
			servletResponse.setHeader("Pragma", "");
		}

		try
		{
			servletResponse.resetBuffer();
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Unable to reset HTTP response", e);
		}

		response.setContentType(mimeType);

		if ((filename != null) && (filename.trim().length() > 0))
		{
			servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + filename
					+ "\"");
		}

		response.write(exportString);
		return 0;
	}

	/**
	 * Returns the file name for the given media. Can be null
	 * 
	 * @param exportType
	 *            instance of MediaTypeEnum
	 * @return String filename
	 */
	public String getExportFileName(MediaTypeEnum exportType)
	{
		return "Test" + "." + exportType;
	}
}
