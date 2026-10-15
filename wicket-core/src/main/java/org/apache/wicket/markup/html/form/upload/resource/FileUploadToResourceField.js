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

    if (typeof(Wicket.FileUploadToResourceField) === 'function') {
        return;
    }

    // the keys of the errors, as the field knows them
    const UPLOAD_TOO_LARGE = 'uploadTooLarge';
    const UPLOAD_SINGLE_FILE_TOO_LARGE = 'uploadSingleFileTooLarge';
    const UPLOAD_TOO_MANY_FILES = 'uploadTooManyFiles';
    const UPLOAD_FAILED = 'uploadFailed';
    const CONNECTION_LOST = 'uploadConnectionLost';
    const UPLOAD_CANCELED = 'upload.canceled';

    // the file lists of the fields, by field id: the text of the remove buttons, by list id
    const lists = {};

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
        // the files chosen for the next upload, which the input holds as well
        this.files = Array.from(this.input.files);
        const self = this;
        // in the capture phase, so the other listeners of the input see all the chosen files
        this.input.addEventListener('change', function () {
            if (!self.dispatching) {
                self.choose(Array.from(self.input.files));
            }
        }, true);
        this.renderLists();
    };

    /**
     * Makes a file list of a field show the files chosen for its next upload. With a list, the
     * files chosen are added to those chosen before if the input is multiple, and leave the list
     * once they are uploaded.
     *
     * @param fieldId the id of the field
     * @param listId the id of the list, a ul or an ol
     * @param removeText the text of the buttons removing a file
     */
    Wicket.FileUploadToResourceField.addList = function (fieldId, listId, removeText)
    {
        lists[fieldId] = lists[fieldId] || {};
        lists[fieldId][listId] = removeText;
        const field = Wicket.Timer[fieldId];
        if (field) {
            field.renderLists();
        }
    };

    /**
     * Makes an element a zone files are dropped on to choose them for the upload of a field.
     *
     * @param zoneId the id of the element
     * @param fieldId the id of the field
     * @param dragOverClass the CSS class of the element while files are dragged over it
     */
    Wicket.FileUploadToResourceField.addDropZone = function (zoneId, fieldId, dragOverClass)
    {
        const zone = document.getElementById(zoneId);
        if (!zone || zone.wicketUploadDropZone) {
            return;
        }
        zone.wicketUploadDropZone = true;
        // dragenter and dragleave fire for every element within the zone the files pass over
        let depth = 0;
        const draggingFiles = function (event) {
            return event.dataTransfer && Array.prototype.indexOf.call(event.dataTransfer.types, 'Files') >= 0;
        };
        const leave = function () {
            depth = 0;
            zone.classList.remove(dragOverClass);
        };
        zone.addEventListener('dragenter', function (event) {
            if (draggingFiles(event)) {
                event.preventDefault();
                depth++;
                zone.classList.add(dragOverClass);
            }
        });
        zone.addEventListener('dragover', function (event) {
            if (draggingFiles(event)) {
                event.preventDefault();
                event.dataTransfer.dropEffect = 'copy';
            }
        });
        zone.addEventListener('dragleave', function (event) {
            if (draggingFiles(event) && --depth <= 0) {
                leave();
            }
        });
        zone.addEventListener('drop', function (event) {
            if (draggingFiles(event)) {
                event.preventDefault();
                leave();
                const field = Wicket.Timer[fieldId];
                if (field) {
                    field.drop(event.dataTransfer);
                }
            }
        });
    };

    /**
     * @param dataTransfer the data of a drop
     * @returns {File[]} the files dropped, without folders
     */
    function droppedFiles(dataTransfer)
    {
        if (!dataTransfer.items) {
            return Array.from(dataTransfer.files);
        }
        const files = [];
        for (let index = 0; index < dataTransfer.items.length; index++) {
            const item = dataTransfer.items[index];
            const entry = item.kind === 'file' && item.webkitGetAsEntry ? item.webkitGetAsEntry() : null;
            const file = item.kind === 'file' && !(entry && entry.isDirectory) ? item.getAsFile() : null;
            if (file) {
                files.push(file);
            }
        }
        return files;
    }

    /**
     * @param accept the accept attribute of an input
     * @param file a file
     * @returns {boolean} whether the input accepts the file, by its extension or its type
     */
    function accepts(accept, file)
    {
        if (!accept) {
            return true;
        }
        const name = file.name.toLowerCase();
        const type = (file.type || '').toLowerCase();
        return accept.split(',').some(function (token) {
            token = token.trim().toLowerCase();
            if (token.charAt(0) === '.') {
                return name.endsWith(token);
            }
            if (token.endsWith('/*')) {
                return type.startsWith(token.slice(0, -1));
            }
            return token !== '' && type === token;
        });
    }

    /**
     * Chooses files for the next upload: in addition to those chosen before if the field lists its
     * files and its input is multiple, else instead of them.
     *
     * @param files the files
     */
    Wicket.FileUploadToResourceField.prototype.choose = function (files)
    {
        const input = document.getElementById(this.inputName);
        this.files = this.hasLists() && input.multiple ? this.files.concat(files) : files;
        this.updateInput();
        this.renderLists();
    };

    /**
     * Chooses the files dropped on a drop zone of the field as if they were chosen in the
     * browser's dialog, and fires the input and change events of the input: those the input
     * accepts, and only the first of them unless the input is multiple.
     *
     * @param dataTransfer the data of the drop
     */
    Wicket.FileUploadToResourceField.prototype.drop = function (dataTransfer)
    {
        const input = document.getElementById(this.inputName);
        let files = droppedFiles(dataTransfer).filter(function (file) {
            return accepts(input.accept, file);
        });
        if (!input.multiple) {
            files = files.slice(0, 1);
        }
        if (files.length === 0) {
            return;
        }
        this.choose(files);
        this.dispatching = true;
        try {
            input.dispatchEvent(new Event('input', {'bubbles': true}));
            input.dispatchEvent(new Event('change', {'bubbles': true}));
        } finally {
            this.dispatching = false;
        }
    };

    /**
     * Takes a file out of those chosen for the next upload.
     *
     * @param file the file
     */
    Wicket.FileUploadToResourceField.prototype.remove = function (file)
    {
        this.files = this.files.filter(function (chosen) {
            return chosen !== file;
        });
        this.updateInput();
        this.renderLists();
    };

    Wicket.FileUploadToResourceField.prototype.updateInput = function ()
    {
        const dataTransfer = new window.DataTransfer();
        this.files.forEach(function (file) {
            dataTransfer.items.add(file);
        });
        document.getElementById(this.inputName).files = dataTransfer.files;
    };

    /**
     * @returns {boolean} whether a file list of the field is in the page
     */
    Wicket.FileUploadToResourceField.prototype.hasLists = function ()
    {
        return Object.keys(lists[this.inputName] || {}).some(function (listId) {
            return document.getElementById(listId) !== null;
        });
    };

    Wicket.FileUploadToResourceField.prototype.renderLists = function ()
    {
        const fieldLists = lists[this.inputName] || {};
        Object.keys(fieldLists).forEach(function (listId) {
            const list = document.getElementById(listId);
            if (list) {
                this.renderList(list, fieldLists[listId]);
            }
        }, this);
    };

    /**
     * Renders an item for each chosen file into a list: its name, as text, and a button removing
     * it, which is disabled while an upload runs.
     *
     * @param list the ul or ol
     * @param removeText the text of the buttons
     */
    Wicket.FileUploadToResourceField.prototype.renderList = function (list, removeText)
    {
        const self = this;
        list.replaceChildren();
        this.files.forEach(function (file, index) {
            const name = document.createElement('span');
            name.className = 'wicket-upload-file-name';
            name.id = list.id + '-' + index + '-name';
            name.textContent = file.name;
            const remove = document.createElement('button');
            remove.type = 'button';
            remove.className = 'wicket-upload-file-remove';
            remove.id = list.id + '-' + index + '-remove';
            remove.textContent = removeText;
            remove.setAttribute('aria-labelledby', remove.id + ' ' + name.id);
            remove.disabled = !!self.xhr;
            remove.addEventListener('click', function () {
                self.remove(file);
            });
            const item = document.createElement('li');
            item.className = 'wicket-upload-file';
            item.append(name, ' ', remove);
            list.appendChild(item);
        });
    };

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
                self.uploaded();
                self.clientSideSuccessCallBack();
                self.report({'error': false, 'filesInfo': JSON.stringify(res)});
            }
        };

        xhr.onerror = function () {
            self.failed(CONNECTION_LOST);
        };

        xhr.onabort = function () {
            // user aborted the upload.
            self.ended();
            self.report({'error': true, 'errorMessage': UPLOAD_CANCELED});
        };

        if (this.clientBeforeSendCallBack(xhr) === false) {
            Wicket.Log.log("The upload of field '" + this.inputName + "' was not sent: the before-send callback returned false.");
            this.report({'error': true, 'errorMessage': UPLOAD_CANCELED});
            return false;
        }

        this.xhr = xhr;
        this.sending = this.files.slice();
        this.renderLists();
        xhr.send(formData);
        return true;
    };

    Wicket.FileUploadToResourceField.prototype.ended = function ()
    {
        delete this.xhr;
        this.renderLists();
    };

    /**
     * Ends an upload that succeeded: the files sent leave the lists of the field.
     */
    Wicket.FileUploadToResourceField.prototype.uploaded = function ()
    {
        if (this.hasLists()) {
            const sent = this.sending;
            this.files = this.files.filter(function (file) {
                return sent.indexOf(file) < 0;
            });
            this.updateInput();
        }
        this.ended();
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
        this.ended();
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
