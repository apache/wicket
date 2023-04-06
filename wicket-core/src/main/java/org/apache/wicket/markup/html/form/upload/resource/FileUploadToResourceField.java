/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.form.upload.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxUtils;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * Implementation of FileUploadField capable to uploading a file into a wicket mounted resource.
 * This field does not require a {@link org.apache.wicket.markup.html.form.Form}!
 * The upload of the file id done via the {@link #startUpload(IPartialPageRequestHandler)} method.
 */
public abstract class FileUploadToResourceField extends FileUploadField
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadToResourceField.class);

    /**
     * Info regarding an upload.
     */
    public static final class UploadInfo
    {
        private File file;
        private final String clientFileName;

        private final long size;

        private final String contentType;

        public UploadInfo(String clientFileName, long size, String contentType)
        {
            this.clientFileName = clientFileName;
            this.size = size;
            this.contentType = contentType;
        }

        public File getFile()
        {
            return file;
        }

        public void setFile(File file)
        {
            this.file = file;
        }

        public String getClientFileName()
        {
            return clientFileName;
        }

        public long getSize()
        {
            return size;
        }

        public String getContentType()
        {
            return contentType;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UploadInfo fileInfo = (UploadInfo) o;
            return Objects.equals(clientFileName, fileInfo.clientFileName);
        }

        /**
         * @return the bytes associated with the upload. Return null in case no file is present
         * or some other error condition happens.
         */
        public byte[] get()
        {
            if (file == null)
            {
               return null;
            }

            byte[] fileData = new byte[(int) getSize()];
            InputStream fis = null;

            try
            {
                fis = new FileInputStream(file);
                IOUtils.readFully(fis, fileData);
            }
            catch (IOException e)
            {
                LOGGER.debug("IOException at get", e);
                fileData = null;
            }
            finally
            {
                IOUtils.closeQuietly(fis);
            }

            return fileData;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(clientFileName);
        }

        public static List<UploadInfo> fromJson(String json)
        {
            List<UploadInfo> infos = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                infos.add(new UploadInfo(jsonObject.getString("clientFileName"), jsonObject.getLong("size"), jsonObject.getString("contentType")));
            }
            return infos;
        }
    }

    private static final JavaScriptResourceReference JS = new JavaScriptResourceReference(FileUploadToResourceField.class, "FileUploadToResourceField.js");

    public static String UPLOAD_CANCELED = "upload.canceled";


    private static abstract class FileModel implements IModel<List<UploadInfo>>
    {

        @Override
        public List<UploadInfo> getObject()
        {
            List<UploadInfo> fileInfos = getFileUploadInfos();
            for (UploadInfo uploadInfo : fileInfos)
            {
                // at this point files were stored at the server side by resource
                // UploadFieldId acts as a discriminator at application level
                // so that uploaded files are isolated.
                uploadInfo.setFile(fileManager().getFile(getUploadFieldId(), uploadInfo.clientFileName));
            }
            return fileInfos;
        }

        @Override
        public void setObject(List<UploadInfo> object)
        {
            throw new UnsupportedOperationException("setObject not supported");
        }

        protected abstract IUploadsFileManager fileManager();

        /*
            This is an application unique ID assigned to upload field.
         */
        protected abstract String getUploadFieldId();

        protected abstract List<UploadInfo> getFileUploadInfos();
    }

    private final AbstractDefaultAjaxBehavior ajaxBehavior;

    private transient List<UploadInfo> fileUploadInfos;

    /**
     * Maximum size of file of upload in bytes (if there are more than one) in request.
     */
    private Bytes fileMaxSize;

    /**
     * Maximum amount of files in request.
     * A value of -1 indicates no maximum.
     */
    private long fileCountMax = -1L;

    /**
     * Maximum size of an upload in bytes. If null, the setting
     * {@link org.apache.wicket.settings.ApplicationSettings#getDefaultMaximumUploadSize()} is used.
     */
    private Bytes maxSize = null;

    public FileUploadToResourceField(String id)
    {
        super(id);
        setOutputMarkupId(true);
        // generate a unique ID
        setMarkupId(generateAUniqueApplicationWiseId());
        setDefaultModel(new FileModel() {
            @Override
            protected IUploadsFileManager fileManager() {
                return FileUploadToResourceField.this.fileManager();
            }

            @Override
            protected String getUploadFieldId()
            {
                return FileUploadToResourceField.this.getMarkupId();
            }

            @Override
            protected List<UploadInfo> getFileUploadInfos()
            {
                return fileUploadInfos;
            }
        });
        ajaxBehavior = new AbstractDefaultAjaxBehavior()
        {
            @Override
            protected void respond(AjaxRequestTarget target)
            {
                Request request = RequestCycle.get().getRequest();
                boolean error = request.getRequestParameters().getParameterValue("error").toBoolean(true);
                if (!error) {
                    String filesIfo = request.getRequestParameters().getParameterValue("filesInfo").toString();
                    fileUploadInfos = UploadInfo.fromJson(filesIfo);
                    onUploadSuccess(target, getFileUploadInfos());
                }
                else
                {
                    String errorMessage = request.getRequestParameters().getParameterValue("errorMessage").toString(null);
                    if (UPLOAD_CANCELED.equals(errorMessage))
                    {
                        onUploadCanceled(target);
                    }
                    else
                    {
                        if (AbstractFileUploadResource.NO_FILE_SELECTED.equals(errorMessage))
                        {
                            errorMessage = getString(AbstractFileUploadResource.NO_FILE_SELECTED);
                        }
                        else
                        {
                            final Map<String, Object> model = new HashMap<>();
                            if (Strings.isEmpty(errorMessage)) {
                                errorMessage = "uploadTooLarge";
                            }
                            model.put("exception", errorMessage);
                            model.put("maxSize", getMaxSize());
                            model.put("fileMaxSize", getFileMaxSize());
                            model.put("fileCountMax", getFileCountMax());
                            errorMessage = getString(errorMessage, Model.ofMap(model));
                        }
                        error(errorMessage);
                        onUploadFailure(target, errorMessage);
                    }
                }
            }
        };
        add(ajaxBehavior);
    }

    private List<UploadInfo> getFileUploadInfos()
    {
        // mind that this makes files to be added.
        return (List<UploadInfo>)getDefaultModel().getObject();
    }

    @Override
    public FileUpload getFileUpload()
    {
        throw new UnsupportedOperationException("FileUploadToResourceField does not support working with FileUpload");
    }

    @Override
    public List<FileUpload> getFileUploads()
    {
        throw new UnsupportedOperationException("FileUploadToResourceField does not support working with FileUpload");
    }

    @Override
    protected void onRemove()
    {
        super.onRemove();
        // we clean any client side mess if component is removed via "partial" page replacement
        AjaxUtils.executeIfAjaxOrWebSockets(target -> target.appendJavaScript("delete Wicket.Timer." + getMarkupId() + ";"));

    }

    /**
     * Override to do something on a successful upload.
     *
     * @param target         The {@link AjaxRequestTarget}
     * @param fileInfos      The List<FileInfo
     */
    protected abstract void onUploadSuccess(AjaxRequestTarget target, List<UploadInfo> fileInfos);


    /**
     *  Override to do something on a non successful upload
     *
     * @param target The {@link AjaxRequestTarget}
     * @param errorInfo The cause of the failure
     */
    protected void onUploadFailure(AjaxRequestTarget target, String errorInfo)
    {
        // nothing by default
    }

    /**
     *  Override to do something in case user canceled upload
     *
     * @param target The {@link AjaxRequestTarget}
     */
    protected void onUploadCanceled(AjaxRequestTarget target)
    {
        // nothing by default
    }

    /**
     * @return  a unique application wise ID (it should be a valid HTML id).
     */
    protected String generateAUniqueApplicationWiseId()
    {
        return "WRFUF_" + UUID.randomUUID().toString().replace("-", "_");
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        CoreLibrariesContributor.contributeAjax(getApplication(), response);
        response.render(JavaScriptHeaderItem.forReference(JS));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("inputName", getMarkupId());
        jsonObject.put("resourceUrl", urlFor(getFileUploadResourceReference(), new PageParameters()).toString());
        jsonObject.put("ajaxCallBackUrl", ajaxBehavior.getCallbackUrl());
        jsonObject.put("maxSize", getMaxSize().bytes());
        Bytes fileMaxSize = getFileMaxSize();
        if (fileMaxSize != null)
        {
            jsonObject.put("fileMaxSize", fileMaxSize.bytes());
        }
        jsonObject.put("fileCountMax", getFileCountMax());
        response.render(OnDomReadyHeaderItem.forScript("Wicket.Timer."
                + getMarkupId() + " = new Wicket.FileUploadToResourceField("
                + jsonObject + ","
                + getClientBeforeSendCallBack() + ","
                + getClientSideSuccessCallBack() + ","
                + getClientSideCancelCallBack() + ","
                + getClientSideUploadErrorCallBack() + ");"));
    }

    /**
     * Sets maximum size of each file in upload request.
     *
     * @param fileMaxSize
     */
    public void setFileMaxSize(Bytes fileMaxSize)
    {
        this.fileMaxSize = fileMaxSize;
    }

    /**
     * Sets maximum amount of files in upload request.
     *
     * @param fileCountMax
     */
    public void setFileCountMax(long fileCountMax)
    {
        this.fileCountMax = fileCountMax;
    }

    /**
     * Sets the maximum size for uploads. If null, the setting
     * {@link org.apache.wicket.settings.ApplicationSettings#getDefaultMaximumUploadSize()} is used.
     *
     * @param maxSize
     *            The maximum size
     */
    public void setMaxSize(final Bytes maxSize)
    {
        this.maxSize = maxSize;
    }


    /**
     * Gets the maximum size for uploads. If null, the setting
     * {@link org.apache.wicket.settings.ApplicationSettings#getDefaultMaximumUploadSize()} is used.
     *
     * @return the maximum size
     */
    public final Bytes getMaxSize()
    {
        if (maxSize == null)
        {
            return getApplication().getApplicationSettings().getDefaultMaximumUploadSize();
        }
        return maxSize;
    }

    /**
     *
     * @return Gets maximum size for each file of an upload.
     */
    public Bytes getFileMaxSize()
    {
        return fileMaxSize;
    }

    /**
     *
     *
     * @return Gets maximum count of files in the form
     */
    public long getFileCountMax()
    {
        return fileCountMax;
    }

    /**
     * Override if you need to return a different instance of FileUploadResourceReference
     * @return FileUploadResourceReference
     */
    protected FileUploadResourceReference getFileUploadResourceReference()
    {
        return FileUploadResourceReference.getInstance();
    }

    /**
     * @return The JavaScript expression starting the upload.
     */
    public String getTriggerUploadScript()
    {
        return "Wicket.Timer." + getMarkupId() + ".upload();";
    }

    /**
     * Starts the upload via an AJAX request.
     *
     * @param target The {@link AjaxRequestTarget}
     */
    public void startUpload(IPartialPageRequestHandler target)
    {
        target.appendJavaScript(getTriggerUploadScript());
    }

    /**
     * @return The JavaScript expression canceling the upload.
     */
    public String getTriggerCancelUploadScript()
    {
        return "Wicket.Timer." + getMarkupId() + ".cancel();";
    }
    /**
     * Cancels the upload via an AJAX request.
     *
     * @param target The {@link AjaxRequestTarget}
     */
    public void cancelUpload(IPartialPageRequestHandler target)
    {
        target.appendJavaScript(getTriggerCancelUploadScript());
    }

    /**
     * See jQuery.ajax documentation.
     *
     * @return A JavaScript function to be executed on beforeSend the upload request.
     */
    protected CharSequence getClientBeforeSendCallBack()
    {
        return "function (xhr, settings) { return true; }";
    }

    /**
     * @return A JavaScript function to be executed on successful upload. This is, besides the normal wicket
     * AJAX request (see {@link #onUploadSuccess(AjaxRequestTarget, List)}).
     */
    protected CharSequence getClientSideSuccessCallBack()
    {
        return "function () {}";
    }

    /**
     * @return A JavaScript function to be executed on upload canceled. This is, besides the normal wicket
     * AJAX request (see {@link #onUploadFailure(AjaxRequestTarget, String)}).
     */
    protected CharSequence getClientSideCancelCallBack()
    {
        return "function () {}";
    }

    /**
     * @return A JavaScript function to be executed on upload canceled. This is, besides the normal wicket
     * AJAX request (see {@link #onUploadFailure(AjaxRequestTarget, String)}). It receives as parameter a JSON object
     * like <code>{'error': true, errorMessage: 'xxx'}</code>.
     */
    protected CharSequence getClientSideUploadErrorCallBack()
    {
        return "function (res) {}";
    }

    /**
     * @return The IUploadsFileManager
     */
    protected IUploadsFileManager fileManager()
    {
        return getFileUploadResourceReference().getUploadFileManager();
    }
}
