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
        this.resourceUrl = settings.resourceUrl + "?uploadId=" + this.inputName + "&maxSize=" + this.settings.maxSize;
        if (this.settings.fileMaxSize != null) {
            this.resourceUrl = this.resourceUrl + "&fileMaxSize=" + this.settings.fileMaxSize;
        }
        if (this.settings.fileCountMax != null) {
            this.resourceUrl = this.resourceUrl + "&fileCountMax=" + this.settings.fileCountMax;
        }
        this.ajaxCallBackUrl = settings.ajaxCallBackUrl;
        this.clientBeforeSendCallBack = clientBeforeSendCallBack;
        this.clientSideSuccessCallBack = clientSideSuccessCallBack;
        this.clientSideCancelCallBack = clientSideCancelCallBack;
        this.uploadErrorCallBack = uploadErrorCallBack;
    }

    Wicket.FileUploadToResourceField.prototype.upload = function()
    {
        // get a fresh reference to input
        this.input = document.getElementById(this.inputName);
        // we add the files to a FormData object.
        var formData = new FormData();
        var totalfiles = this.input.files.length;
        for (var index = 0; index < totalfiles; index++) {
            formData.append("WICKET-FILE-UPLOAD",this.input.files[index]);
        }
        var self = this;
        // we use jQuery to post the files to the resource (this.resourceUrl)
        // and we keep a reference to the request in order to be able
        // to cancel the upload
        this.xhr = $.ajax({
            url: this.resourceUrl,
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (res) {
                // do clean up on success
                if (res.error) {
                    self.uploadErrorCallBack(res);
                    Wicket.Ajax.get({"u": self.ajaxCallBackUrl, "ep": res});
                } else {
                    self.clientSideSuccessCallBack();
                    var ep = {'error': false, 'filesInfo': JSON.stringify(res)};
                    Wicket.Ajax.get({"u": self.ajaxCallBackUrl, "ep": ep});
                }
            },
            beforeSend: function (xhr) {
                self.clientBeforeSendCallBack(xhr);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (textStatus === "abort") {
                    // user aborted the upload.
                    var ep = {'error': true, 'errorMessage': 'upload.canceled'};
                    Wicket.Ajax.get({"u": self.ajaxCallBackUrl, "ep": ep});
                } else if (textStatus === "error"){
                    var ep = {'error': true, "errorMessage": errorThrown};
                    self.uploadErrorCallBack(ep);
                    Wicket.Ajax.get({"u": self.ajaxCallBackUrl, "ep": ep});
                } else if (textStatus === "parsererror"){
                    // this error will only happen is generated JSON at server side is faulty
                    var data = jqXHR.responseText;
                    Wicket.Log.log(data);
                }
            }
        });
    }

    // cancel the upload
    Wicket.FileUploadToResourceField.prototype.cancel = function () {
        // we have a reference to the request we can cancel it.
        if (this.xhr) {
            this.xhr.abort();
            this.clientSideCancelCallBack();
            Wicket.Log.log("The upload associated with field '" + this.inputName + "' has been canceled!");
            delete (this.xhr);
        } else {
            Wicket.Log.log("Too late to cancel upload for field '"  + this.inputName +  "': the upload has already finished.");
        }
    }
})();