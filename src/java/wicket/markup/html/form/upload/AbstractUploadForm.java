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
package wicket.markup.html.form.upload;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.validation.IValidationErrorHandler;
import wicket.protocol.http.HttpRequest;


/**
 * Base class for upload components.
 *
 * @author Eelco Hillenius
 */
public abstract class AbstractUploadForm extends Form
{
    /** Code broadcaster for reporting. */
    private static Log log = LogFactory.getLog(AbstractUploadForm.class);

    /**
     * Construct.
     * @param name component name
     * @param validationErrorHandler validation error handler
     */
    public AbstractUploadForm(String name, IValidationErrorHandler validationErrorHandler)
    {
        super(name, validationErrorHandler);
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected final void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        super.handleComponentTag(cycle, tag);
        tag.put("enctype", "multipart/form-data");
    }

    /**
     * Handles an upload.
     * @param cycle the request cycle
     * @see wicket.markup.html.form.Form#handleSubmit(wicket.RequestCycle)
     */
    public void handleSubmit(RequestCycle cycle)
    {
		try
        {
			HttpServletRequest request = ((HttpRequest)cycle.getRequest()).getServletRequest();
			boolean isMultipart = FileUpload.isMultipartContent(request);
			if(!isMultipart)
			{
			    throw new IllegalStateException("request is not a multipart request");
			}
			prepareUpload();
			FileUploadBase upload = createUpload();
            List items = parseRequest(request, upload);
			processFileItems(items);
			finishUpload();
        }
        catch (FileUploadException e)
        {
            throw new RuntimeException(e); // for the time being, we throw
        }
    }

    /**
     * Process the list of file items.
     * @param items List of {@link FileItem}s
     */
    protected void processFileItems(List items)
    {
        for (Iterator i = items.iterator(); i.hasNext();)
        {
            FileItem item = (FileItem) i.next();

            if (item.isFormField())
            {
                processFormField(item);
            }
            else
            {
                processUploadedFile(item);
            }
        }
    }

    /**
     * Template method that is called before the handling of the upload form starts.
     * Use for initialization of directories etc.
     */
    protected void prepareUpload()
    {
        
    }

    /**
     * Template method that is called after the handling of the upload form finishes.
     * Use for things like re-rendering the UI etc.
     */
    protected void finishUpload()
    {
        
    }

    /**
     * Process a form field.
     * @param item form field item (item.isFormField() == true)
     */
    protected abstract void processFormField(FileItem item);

    /**
     * Process an upload item.
     * @param item upload item (item.isFormField() == false)
     */
    protected abstract void processUploadedFile(FileItem item);

    /**
     * parse the request and return a List of {@link FileItem}s.
     * @param request http servlet request
     * @param upload upload object
     * @return List with {@link FileItem}s
     * @throws FileUploadException
     */
    protected List parseRequest(HttpServletRequest request, FileUploadBase upload)
    	throws FileUploadException
    {
        List items = upload.parseRequest(request);
        return items;
    }

    /**
     * Create an upload object. Override this to
     * use anything else than {@link DiskFileUpload} or to parameterize the upload object
     * (e.g. set the max size, temp dir, etc).
     * @return upload object
     */
    protected FileUploadBase createUpload()
    {
        FileUploadBase upload = new DiskFileUpload();
        return upload;
    }
}
