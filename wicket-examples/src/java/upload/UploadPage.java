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
package upload;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.markup.html.form.TextField;
import com.voicetribe.wicket.markup.html.form.upload.FileUploadForm;
import com.voicetribe.wicket.markup.html.form.validation.IValidationErrorHandler;
import com.voicetribe.wicket.markup.html.table.Cell;
import com.voicetribe.wicket.markup.html.table.Table;

/**
 * Upload example.
 *
 * @author Eelco Hillenius
 */
public class UploadPage extends HtmlPage
{
    /** Log. */
    private static Log log = LogFactory.getLog(UploadPage.class);

    /** directory we are working with. */
    private File tempDir;

    /** list of files, model for file table. */
    private final List files = new ArrayList();

    /**
     * Constructor.
     * @param parameters Page parameters
     */
    public UploadPage(final PageParameters parameters)
    {
        super();
        tempDir = new File(System.getProperty("java.io.tmpdir"), "wicket-upload-test");
        if(!tempDir.isDirectory())
        {
            tempDir.mkdir();
        }
        add(new UploadForm("upload", null, tempDir));
        add(new Label("dir", tempDir.getAbsolutePath()));
        files.addAll(Arrays.asList(tempDir.list()));
        add(new FileTable("fileList", files));
    }

    /**
     * form for uploads.
     */
    private class UploadForm extends FileUploadForm
    {
        /**
         * Construct.
         * @param name
         * @param validationErrorHandler
         * @param targetDirectory
         */
        public UploadForm(String name, IValidationErrorHandler validationErrorHandler, File targetDirectory)
        {
            super(name, validationErrorHandler, targetDirectory);
            add(new TextField("fileName", ""));
        }

        /**
         * @see com.voicetribe.wicket.markup.html.form.upload.AbstractUploadForm#finishUpload()
         */
        protected void finishUpload()
        {
            files.clear();
            files.addAll(Arrays.asList(tempDir.list()));
        }
    }

    /**
     * table for files.
     */
    private static class FileTable extends Table
    {
        /**
         * Construct.
         * @param name
         * @param object
         */
        public FileTable(String name, List object)
        {
            super(name, object);
        }

        /**
         * @see com.voicetribe.wicket.markup.html.table.Table#populateCell(com.voicetribe.wicket.markup.html.table.Cell)
         */
        protected void populateCell(Cell cell)
        {
            String fileName = (String)cell.getModelObject();
            cell.add(new Label("file", fileName));
        }
        
    }
}
