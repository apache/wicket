/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.displaytag.export;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RenderException;
import wicket.RequestCycle;
import wicket.protocol.http.HttpResponse;


/**
 */
public class Export
{
    final private Log log = LogFactory.getLog(Export.class);

    /**
     * called when data are not displayed in a html page but should be exported.
     * @return int EVAL_PAGE or SKIP_PAGE
     * @throws JspException generic exception
     */
    public int doExport(final RequestCycle cycle, final BaseExportView exportView, final List data) 
    {
        final boolean exportFullList = true;
        final boolean exportHeader = true;
        final boolean exportDecorated = true;

        String mimeType = exportView.getMimeType();
        String exportString = exportView.doExport();

        String filename = null; // getExportFileName(MediaTypeEnum.XML);
        return writeExport(cycle, mimeType, exportString, filename);
    }

    /**
     * Will write the export. The default behavior is to write directly to the wicket.response. If the ResponseOverrideFilter
     * is configured for this request, will instead write the export content to a StringBuffer in the Request object.
     * @param mimeType mime type to set in the wicket.response
     * @param exportString String
     * @param filename name of the file to be saved. Can be null, if set the content-disposition header will be added.
     * @return int
     * @throws JspException for errors in resetting the wicket.response or in writing to out
     */
    protected int writeExport(final RequestCycle cycle, final String mimeType, final String exportString, final String filename)
    {
        HttpResponse response = (HttpResponse)cycle.getResponse();
        HttpServletResponse servletResponse = response.getServletResponse();

        // wicket.response can't be already committed at this time
        if (servletResponse.isCommitted())
        {
            throw new RenderException("HTTP wicket.response already committed. Can not change that any more");
        }

        // if cache is disabled using http header, export will not work.
        // Try to remove bad headers overwriting them, since there is no way to remove a single header and reset()
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
            // there is no "Cache-Control: public" equivalent, so just try to set it to an empty String (note
            // this is NOT a valid header)
            servletResponse.setHeader("Pragma", "");
        }

        try
        {
            servletResponse.resetBuffer();
        }
        catch (Exception e)
        {
            throw new RenderException("Unable to reset HTTP wicket.response", e);
        }

        response.setContentType(mimeType);

        if ((filename != null) && (filename.trim().length() > 0))
        {
            servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        }

        response.write(exportString);
        return 0;
    }

    /**
     * Returns the file name for the given media. Can be null
     * @param exportType instance of MediaTypeEnum
     * @return String filename
     */
    public String getExportFileName(MediaTypeEnum exportType)
    {
        return "Test" + "." + exportType;
    }
}
