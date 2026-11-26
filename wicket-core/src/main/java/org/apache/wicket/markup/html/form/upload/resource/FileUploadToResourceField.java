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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxUtils;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * Implementation of FileUploadField capable to uploading a file into a wicket mounted resource.
 * This field does not require a {@link org.apache.wicket.markup.html.form.Form}!
 * The upload of the file id done via the {@link #startUpload(IPartialPageRequestHandler)} method.
 * <p>
 * A {@link FileDropZoneBehavior} makes a component a zone the files to upload can be dropped on,
 * and a {@link FileListBehavior} lists the files chosen, which then add up over several choices.
 */
public abstract class FileUploadToResourceField extends FileUploadField
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadToResourceField.class);

    /**
     * Info regarding an upload.
     * <p>
     * The stored name, the client file name and the content type are reported by the browser and
     * must not be trusted. The size and the bytes are taken from the stored file.
     */
    public static final class UploadInfo
    {
        private File file;
        private final String fileName;
        private final String clientFileName;

        private final String contentType;

        /**
         * @param fileName
         *            the name the {@link IUploadsFileManager} stored the file under
         * @param clientFileName
         *            the file name reported by the browser
         * @param contentType
         *            the content type reported by the browser
         * @since 11.0.0
         */
        public UploadInfo(String fileName, String clientFileName, String contentType)
        {
            this.fileName = fileName;
            this.clientFileName = clientFileName;
            this.contentType = contentType;
        }

        /**
         * @param clientFileName
         *            the file name reported by the browser
         * @param size
         *            ignored, the size is taken from the stored file
         * @param contentType
         *            the content type reported by the browser
         * @deprecated the file is then looked up by its client file name, which several files can
         *             share. Use {@link #UploadInfo(String, String, String)} instead.
         */
        @Deprecated(since = "11.0.0", forRemoval = true)
        public UploadInfo(String clientFileName, long size, String contentType)
        {
            this(clientFileName, clientFileName, contentType);
        }

        public File getFile()
        {
            return file;
        }

        public void setFile(File file)
        {
            this.file = file;
        }

        /**
         * @return the name the {@link IUploadsFileManager} stored the file under
         * @since 11.0.0
         */
        public String getFileName()
        {
            return fileName;
        }

        public String getClientFileName()
        {
            return clientFileName;
        }

        /**
         * @return the length of the stored file, or 0 if no stored file is present
         */
        public long getSize()
        {
            if (isStored())
            {
                return file.length();
            }
            return 0;
        }

        private boolean isStored()
        {
            return file != null && file.isFile();
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
            return Objects.equals(fileName, fileInfo.fileName);
        }

        /**
         * Reads the stored file into memory.
         *
         * @return the bytes of the stored file. Return null in case no file is present
         * or some other error condition happens.
         */
        public byte[] get()
        {
            if (!isStored())
            {
               return null;
            }

            try
            {
                return Files.readAllBytes(file.toPath());
            }
            catch (IOException e)
            {
                LOGGER.debug("IOException at get", e);
                return null;
            }
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(fileName);
        }

        public static List<UploadInfo> fromJson(String json)
        {
            List<UploadInfo> infos = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String clientFileName = jsonObject.getString("clientFileName");
                // a resource written before files had names of their own stores them by client name
                infos.add(new UploadInfo(jsonObject.optString("fileName", clientFileName),
                    clientFileName, jsonObject.getString("contentType")));
            }
            return infos;
        }
    }

    static final JavaScriptResourceReference JS = new JavaScriptResourceReference(FileUploadToResourceField.class, "FileUploadToResourceField.js");

    public static String UPLOAD_CANCELED = "upload.canceled";

    /**
     * The resource key of the text of an upload that failed for a reason other than a limit.
     *
     * @since 11.0.0
     */
    public static final String UPLOAD_FAILED_RESOURCE_KEY = "uploadToResourceFailed";

    /**
     * The resource key of the text of an upload whose connection broke off before the server
     * answered, which the browser reports under this key.
     *
     * @since 11.0.0
     */
    public static final String CONNECTION_LOST_RESOURCE_KEY = "uploadConnectionLost";

    private static final Set<String> ERROR_RESOURCE_KEYS = Set.of(
        AbstractFileUploadResource.NO_FILE_SELECTED, CONNECTION_LOST_RESOURCE_KEY,
        Form.UPLOAD_TOO_LARGE_RESOURCE_KEY, Form.UPLOAD_SINGLE_FILE_TOO_LARGE_RESOURCE_KEY,
        Form.UPLOAD_TOO_MANY_FILES_RESOURCE_KEY);


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
                uploadInfo.setFile(fileManager().getFile(getUploadFieldId(), uploadInfo.getFileName()));
            }
            fileInfos.removeIf(uploadInfo -> !uploadInfo.isStored());
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
                IRequestParameters parameters = RequestCycle.get().getRequest().getRequestParameters();
                boolean error = parameters.getParameterValue("error").toBoolean(true);
                if (!error) {
                    String filesIfo = parameters.getParameterValue("filesInfo").toString();
                    List<UploadInfo> reportedInfos = UploadInfo.fromJson(filesIfo);
                    if (getFileCountMax() > -1 && reportedInfos.size() > getFileCountMax())
                    {
                        reportFailure(target, Form.UPLOAD_TOO_MANY_FILES_RESOURCE_KEY);
                        return;
                    }
                    fileUploadInfos = new ArrayList<>(new LinkedHashSet<>(reportedInfos));
                    List<UploadInfo> storedInfos = getFileUploadInfos();
                    if (storedInfos.isEmpty())
                    {
                        reportFailure(target, AbstractFileUploadResource.NO_FILE_SELECTED);
                        return;
                    }
                    onUploadSuccess(target, storedInfos);
                }
                else
                {
                    String errorKey = parameters.getParameterValue("errorMessage").toString(null);
                    if (UPLOAD_CANCELED.equals(errorKey))
                    {
                        onUploadCanceled(target);
                    }
                    else
                    {
                        reportFailure(target, errorKey);
                    }
                }
            }
        };
        add(ajaxBehavior);
    }

    private void reportFailure(AjaxRequestTarget target, String errorKey)
    {
        fileUploadInfos = new ArrayList<>();
        String errorMessage = getUploadErrorMessage(errorKey);
        error(errorMessage);
        onUploadFailure(target, errorMessage);
    }

    /**
     * Words the error of a failed upload, which {@link #onUploadFailure(AjaxRequestTarget, String)}
     * then receives. The key comes from the browser, which can send any, so it is never looked up
     * as given: a key this method does not know gets the text of
     * {@link #UPLOAD_FAILED_RESOURCE_KEY}. An override words the keys it knows, such as those its
     * own script reports, and leaves the others to this method.
     *
     * @param errorKey
     *            the key of the error the browser reported, or {@code null}
     * @return the text of a limit, of no files selected or of
     *         {@link #CONNECTION_LOST_RESOURCE_KEY}, else that of {@link #UPLOAD_FAILED_RESOURCE_KEY}
     * @since 11.0.0
     */
    protected String getUploadErrorMessage(String errorKey)
    {
        final Map<String, Object> model = new HashMap<>();
        model.put("maxSize", getMaxSize());
        model.put("fileMaxSize", getFileMaxSize());
        model.put("fileCountMax", getFileCountMax());
        String key = errorKey != null && ERROR_RESOURCE_KEYS.contains(errorKey) ? errorKey
            : UPLOAD_FAILED_RESOURCE_KEY;
        return getString(key, Model.ofMap(model));
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
     * <p>
     * Only uploads whose file is present in the {@link IUploadsFileManager} are passed, each once.
     * An upload result listing more files than {@link #getFileCountMax()}, or none that are
     * present, is reported to {@link #onUploadFailure(AjaxRequestTarget, String)} instead.
     *
     * @param target         The {@link AjaxRequestTarget}
     * @param fileInfos      The List<FileInfo
     */
    protected abstract void onUploadSuccess(AjaxRequestTarget target, List<UploadInfo> fileInfos);


    /**
     *  Override to do something on a non successful upload
     *
     * @param target The {@link AjaxRequestTarget}
     * @param errorInfo The cause of the failure, as {@link #getUploadErrorMessage(String)} words it
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
        jsonObject.put("uploadToken", AbstractFileUploadResource.createUploadToken(getMarkupId(),
                getMaxSize(), fileMaxSize, getFileCountMax()));
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
     * Starts the upload via an AJAX request. Only one upload of the field runs at a time: while one
     * runs, starting another one does nothing. Files beyond the limits of the field are not sent:
     * {@link #onUploadFailure(AjaxRequestTarget, String)} is called with the text of the limit
     * instead.
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
     * @return A JavaScript function called with the {@code XMLHttpRequest} of the upload before it
     *         is sent. If it returns {@code false}, the upload is not sent, and
     *         {@link #onUploadCanceled(AjaxRequestTarget)} is called.
     */
    protected CharSequence getClientBeforeSendCallBack()
    {
        return "function (xhr) { return true; }";
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
     * AJAX request (see {@link #onUploadCanceled(AjaxRequestTarget)}).
     */
    protected CharSequence getClientSideCancelCallBack()
    {
        return "function () {}";
    }

    /**
     * @return A JavaScript function to be executed on upload failed. This is, besides the normal wicket
     * AJAX request (see {@link #onUploadFailure(AjaxRequestTarget, String)}). It receives as parameter a JSON object
     * like <code>{'error': true, errorMessage: 'uploadTooLarge'}</code>, where {@code errorMessage} is the
     * resource key of the error.
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
