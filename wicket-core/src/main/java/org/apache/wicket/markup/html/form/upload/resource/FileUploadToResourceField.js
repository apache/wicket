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

;(function (undefined) {

    'use strict';

    if (typeof(Wicket.FileUploadToResourceField) === 'object') {
        return;
    }

    Wicket.FileUploadToResourceField = function (settings, clientBeforeSendCallBack, clientSideSuccessCallBack, clientSideCancelCallBack, uploadErrorCallBack)
    {
        this.settings = settings;
        this.inputName = settings.inputName;
        this.input = document.getElementById(this.inputName);
        this.resourceUrl = settings.resourceUrl + "?uploadId=" + encodeURIComponent(this.inputName) + "&maxSize=" + encodeURIComponent(this.settings.maxSize);
        if (this.settings.fileMaxSize != null) {
            this.resourceUrl = this.resourceUrl + "&fileMaxSize=" + encodeURIComponent(this.settings.fileMaxSize);
        }
        if (this.settings.fileCountMax != null) {
            this.resourceUrl = this.resourceUrl + "&fileCountMax=" + encodeURIComponent(this.settings.fileCountMax);
        }
        this.resourceUrl = this.resourceUrl + "&uploadToken=" + encodeURIComponent(this.settings.uploadToken);
        this.ajaxCallBackUrl = settings.ajaxCallBackUrl;
        this.clientBeforeSendCallBack = clientBeforeSendCallBack;
        this.clientSideSuccessCallBack = clientSideSuccessCallBack;
        this.clientSideCancelCallBack = clientSideCancelCallBack;
        this.uploadErrorCallBack = uploadErrorCallBack;
    };

    // the keys of the errors, as the field knows them
    const UPLOAD_TOO_LARGE = 'uploadTooLarge';
    const UPLOAD_SINGLE_FILE_TOO_LARGE = 'uploadSingleFileTooLarge';
    const UPLOAD_TOO_MANY_FILES = 'uploadTooManyFiles';
    const UPLOAD_FAILED = 'uploadFailed';
    const CONNECTION_LOST = 'uploadConnectionLost';
    const UPLOAD_CANCELED = 'upload.canceled';

    /**
     * Uploads the files of the input, unless an upload of the field is running already, or the
     * files break a limit of the field: the server is told of those as of an upload it refused,
     * without the files being sent.
     *
     * @returns {boolean} whether the upload was sent
     */
    Wicket.FileUploadToResourceField.prototype.upload = function()
    {
        if (this.xhr) {
            Wicket.Log.log("An upload of field '" + this.inputName + "' is running: it has to end before another one starts.");
            return false;
        }
        // get a fresh reference to input
        this.input = document.getElementById(this.inputName);
        const files = this.input.files;
        const exceededLimit = this.exceededLimit(files);
        if (exceededLimit) {
            this.failed(exceededLimit);
            return false;
        }
        // we add the files to a FormData object.
        const formData = new FormData();
        for (let index = 0; index < files.length; index++) {
            formData.append("WICKET-FILE-UPLOAD", files[index]);
        }
        const self = this;
        // we post the files to the resource (this.resourceUrl) with a plain XMLHttpRequest
        // and keep a reference to it in order to be able to cancel the upload
        const xhr = new XMLHttpRequest();

        xhr.open("POST", this.resourceUrl);

        xhr.onload = function () {
            if (xhr.status < 200 || xhr.status >= 300) {
                self.failed(UPLOAD_FAILED);
                return;
            }
            let res;
            try {
                res = xhr.responseText ? JSON.parse(xhr.responseText) : {};
            } catch (parseError) {
                Wicket.Log.error("The answer to the upload of field '" + self.inputName + "' is not JSON: " + xhr.responseText);
                self.failed(UPLOAD_FAILED);
                return;
            }
            if (res.error) {
                self.failed(res.errorMessage);
            } else {
                delete self.xhr;
                self.clientSideSuccessCallBack();
                self.report({'error': false, 'filesInfo': JSON.stringify(res)});
            }
        };

        xhr.onerror = function () {
            self.failed(CONNECTION_LOST);
        };

        xhr.onabort = function () {
            // user aborted the upload.
            delete self.xhr;
            self.report({'error': true, 'errorMessage': UPLOAD_CANCELED});
        };

        if (this.clientBeforeSendCallBack(xhr) === false) {
            Wicket.Log.log("The upload of field '" + this.inputName + "' was not sent: the before-send callback returned false.");
            this.report({'error': true, 'errorMessage': UPLOAD_CANCELED});
            return false;
        }

        this.xhr = xhr;
        xhr.send(formData);
        return true;
    };

    /**
     * @param files the files to upload
     * @returns {string|null} the key of the first limit of the field the files break, if any
     */
    Wicket.FileUploadToResourceField.prototype.exceededLimit = function (files)
    {
        const settings = this.settings;
        let totalSize = 0;
        let largestSize = 0;
        for (let index = 0; index < files.length; index++) {
            totalSize += files[index].size;
            largestSize = Math.max(largestSize, files[index].size);
        }
        if (totalSize > settings.maxSize) {
            return UPLOAD_TOO_LARGE;
        }
        if (settings.fileCountMax >= 0 && files.length > settings.fileCountMax) {
            return UPLOAD_TOO_MANY_FILES;
        }
        if (settings.fileMaxSize != null && largestSize > settings.fileMaxSize) {
            return UPLOAD_SINGLE_FILE_TOO_LARGE;
        }
        return null;
    };

    /**
     * Ends an upload that failed, and tells the page and the server why.
     *
     * @param errorMessage the key of the error
     */
    Wicket.FileUploadToResourceField.prototype.failed = function (errorMessage)
    {
        delete this.xhr;
        const ep = {'error': true, 'errorMessage': errorMessage};
        this.uploadErrorCallBack(ep);
        this.report(ep);
    };

    /**
     * Tells the server how an upload ended. A POST carries the information about the files in its
     * body, which would make the URL of a GET too long for the server with many files.
     *
     * @param ep the parameters of the request
     */
    Wicket.FileUploadToResourceField.prototype.report = function (ep)
    {
        Wicket.Ajax.post({"u": this.ajaxCallBackUrl, "ep": ep});
    };

    // cancel the upload
    Wicket.FileUploadToResourceField.prototype.cancel = function () {
        // we have a reference to the request we can cancel it.
        if (this.xhr) {
            this.xhr.abort();
            delete (this.xhr);
            this.clientSideCancelCallBack();
            Wicket.Log.log("The upload associated with field '" + this.inputName + "' has been canceled!");
        } else {
            Wicket.Log.log("No upload of field '"  + this.inputName +  "' to cancel: it has already ended, or never started.");
        }
    };
})();
