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

/*
 * The script of ResumableUploadToResourceField: a Wicket.FileUploadToResourceField that sends each
 * file in chunks over the tus protocol 1.0 (https://tus.io/protocols/resumable-upload).
 */
;(function (undefined) {

    'use strict';

    if (typeof(Wicket.ResumableUploadToResourceField) === 'function') {
        return;
    }

    const TUS_RESUMABLE = '1.0.0';
    const CHUNK_TYPE = 'application/offset+octet-stream';
    const FILE_NAME_HEADER = 'Wicket-Upload-File-Name';
    const ERROR_HEADER = 'Wicket-Upload-Error';
    const STORAGE_PREFIX = 'wicket-resumable-upload::';

    // the keys of the errors, as the field knows them
    const NO_FILE_SELECTED = 'wicket.no.files.selected';
    const UPLOAD_FAILED = 'uploadFailed';
    const UPLOAD_CANCELED = 'upload.canceled';
    const UPLOAD_LOST = 'resumableUploadLost';

    // how often an upload starts over at most, after the server lost it, until the user resumes it
    const MAX_RESTARTS = 2;

    // the state of the fields, by field id, which a new rendering of a field takes over: the
    // current field, its uploads, the question it asks and its look whether the server answers
    const statesOf = Object.create(null);

    let lastKey = 0;

    const Base = Wicket.FileUploadToResourceField;

    /**
     * @param settings the settings of Wicket.FileUploadToResourceField, with grantExpires,
     *        uploadContext, chunkSize, parallelUploads, remembering, rememberedAs, recalling,
     *        askingToResume, retryDelays, probeDelays, stallMillis, inUseMillis, overridingMethod
     *        and texts, as
     *        ResumableUploadToResourceField#newClientSettings() renders them: the timings have no
     *        defaults here, the field sets them
     * @param resumeQuestionCallBack a function asking the user whether to go on with uploads,
     *        returning true when it did, see ResumableUploadToResourceField#getClientSideResumeQuestionCallBack()
     */
    const Field = Wicket.ResumableUploadToResourceField = function (settings, clientBeforeSendCallBack, clientSideSuccessCallBack, clientSideCancelCallBack, uploadErrorCallBack, resumeQuestionCallBack)
    {
        // what rendering the lists needs, before the parent renders them
        this.texts = settings.texts || {};
        this.state = statesOf[settings.inputName] = statesOf[settings.inputName] ||
            {'field': null, 'uploads': [], 'question': null, 'probe': null, 'recalled': false};
        this.state.field = this;
        this.uploads = this.state.uploads;
        Base.call(this, settings, clientBeforeSendCallBack, clientSideSuccessCallBack, clientSideCancelCallBack, uploadErrorCallBack);
        this.endpoint = settings.resourceUrl;
        // the path parameters of the endpoint, the id of a session without cookies: added to the
        // address of an upload as it is sent, never kept with it
        const session = /;[^\/]*$/.exec(this.endpoint);
        this.session = session ? session[0] : '';
        this.uploadContext = settings.uploadContext || null;
        // the grant of the field, the only limit of the field the server holds a request to
        this.query = 'uploadId=' + encodeURIComponent(this.inputName) +
            '&fileMaxSize=' + encodeURIComponent(settings.fileMaxSize) +
            '&uploadToken=' + encodeURIComponent(settings.uploadToken) +
            '&grantExpires=' + encodeURIComponent(settings.grantExpires) +
            (this.uploadContext ? '&uploadContext=' + encodeURIComponent(this.uploadContext) : '');
        this.overridingMethod = settings.overridingMethod === true;
        this.chunkSize = settings.chunkSize;
        this.parallelUploads = settings.parallelUploads || 1;
        this.remembering = settings.remembering !== false;
        // the id of the field is a new one on every page
        this.rememberedAs = settings.rememberedAs || this.inputName;
        this.askingToResume = settings.askingToResume !== false;
        // the waits before the tries again: once they are spent without an answer, the connection
        // counts as lost and the user is asked before going on
        this.retryDelays = settings.retryDelays;
        // the waits before each look whether the server answers again, once the connection is
        // lost, the last one again and again
        this.probeDelays = settings.probeDelays;
        this.recalling = settings.recalling !== false;
        // the uploads the server keeps for the field to offer, besides those the browser remembers
        this.recalledByServer = Array.isArray(settings.recalled) ? settings.recalled : [];
        // how long a request may send or get nothing before it is given up as stalled, 0 never
        this.stallMillis = settings.stallMillis;
        // how long after it last sent something another page takes an upload for in use: a page
        // sending it tells so four times as often
        this.inUseMillis = settings.inUseMillis;
        this.resumeQuestionCallBack = typeof resumeQuestionCallBack === 'function' ? resumeQuestionCallBack : function () {
            return false;
        };
        const self = this;
        this.uploads.forEach(function (upload) {
            upload.field = self;
        });
        this.recall();
    };

    Field.prototype = Object.create(Base.prototype);
    Field.prototype.constructor = Field;

    /**
     * @param url the URL of the endpoint or of an upload
     * @param query the query string of the field
     * @returns {string} the URL with the query
     */
    function withQuery(url, query)
    {
        return url + (url.indexOf('?') < 0 ? '?' : '&') + query;
    }

    /**
     * @param text a text
     * @returns {string} the text, encoded in UTF-8 and then in base64
     */
    function base64(text)
    {
        const bytes = new window.TextEncoder().encode(text);
        let binary = '';
        for (let index = 0; index < bytes.length; index++) {
            binary += String.fromCharCode(bytes[index]);
        }
        return window.btoa(binary);
    }

    /**
     * @returns {string} a URL made absolute, to compare it with another
     */
    function absolute(url)
    {
        return new window.URL(url, document.baseURI).href;
    }

    /**
     * @param field a field
     * @param id the id of an upload
     * @returns {string} the absolute URL of the upload, without the id of a session
     */
    function uploadUrl(field, id)
    {
        return absolute(field.endpoint.substring(0, field.endpoint.length - field.session.length) +
            '/' + encodeURIComponent(id));
    }

    /**
     * @param location the Location of an upload the server created
     * @returns {string} the id of the upload, the last segment of its path
     */
    function idOf(location)
    {
        const path = location.split('?')[0].replace(/;[^\/]*$/, '');
        return decodeURIComponent(path.substring(path.lastIndexOf('/') + 1));
    }

    /**
     * @param field a field
     * @param url a URL to send a request to
     * @returns {string} the URL with the path parameters of the endpoint, unless it has some
     */
    function withSession(field, url)
    {
        return /;[^\/]*$/.test(url) ? url : url + field.session;
    }

    /**
     * @param field a field
     * @returns {string} the start of the keys the uploads of the endpoint of the field are
     *          remembered under in the local storage
     */
    function storagePrefix(field)
    {
        return STORAGE_PREFIX + uploadUrl(field, '') + '::';
    }

    /**
     * @param field a field
     * @returns {string} where the uploads of the field may go on: in its context, else in its place
     */
    function scopeOf(field)
    {
        return field.uploadContext !== null ? 'context:' + field.uploadContext : 'place:' + field.rememberedAs;
    }

    /**
     * @param field a field
     * @param value what is remembered of an upload
     * @returns {boolean} whether the field may go on with the upload: one of its context, else of
     *          its place
     */
    function belongs(field, value)
    {
        return field.uploadContext !== null ? value.context === field.uploadContext
            : !value.context && value.field === field.rememberedAs;
    }

    /**
     * @param upload an upload
     * @returns {string} the key the upload is remembered under in the local storage
     */
    function storageKey(upload)
    {
        const file = upload.file;
        return storagePrefix(upload.field) + scopeOf(upload.field) + '::' +
            [file.name, file.size, file.lastModified, file.type].join('::');
    }

    /**
     * @param key a key of the local storage
     * @returns {object} what is remembered under it: the URL of an upload, what its field is
     *          remembered as, the name, size, date and type of its file and when it last sent
     *          something; null for nothing
     */
    function rememberedAt(key)
    {
        try {
            const value = JSON.parse(window.localStorage.getItem(key));
            return value && typeof value.url === 'string' ? value : null;
        } catch (e) {
            return null;
        }
    }

    /**
     * @returns {string} the URL of the upload of the same file the field remembers, in its context
     *          or place, or null
     */
    function remembered(upload)
    {
        if (!upload.field.remembering) {
            return null;
        }
        const value = rememberedAt(storageKey(upload));
        return value && belongs(upload.field, value) ? value.url : null;
    }

    /**
     * Remembers the URL of an upload, with what its field is remembered as, the name, size, date
     * and type of its file, and when it last sent something.
     *
     * @param upload the upload
     * @param alive when it last sent something, now if undefined, 0 once the page is left
     */
    function remember(upload, alive)
    {
        if (upload.field.remembering && upload.url) {
            const file = upload.file;
            upload.alive = alive === undefined ? Date.now() : alive;
            try {
                window.localStorage.setItem(storageKey(upload), JSON.stringify({
                    'url': upload.url, 'field': upload.field.rememberedAs,
                    'context': upload.field.uploadContext, 'name': file.name,
                    'size': file.size, 'lastModified': file.lastModified, 'type': file.type,
                    'alive': upload.alive
                }));
            } catch (e) {
                // a browser without storage, or with a full one, cannot resume after a reload
            }
        }
    }

    /**
     * Remembers, now and then, that an upload is sending, so that another page of the same field
     * does not offer to go on with it.
     */
    function alive(upload)
    {
        if (upload.url && Date.now() - (upload.alive || 0) >= upload.field.inUseMillis / 4) {
            remember(upload);
        }
    }

    // once the page is left, the next one offers to go on with the uploads that did not end
    window.addEventListener('pagehide', function () {
        Object.keys(statesOf).forEach(function (fieldId) {
            statesOf[fieldId].uploads.forEach(function (upload) {
                if (upload.state !== 'recalled') {
                    remember(upload, 0);
                }
            });
        });
    });

    function forget(upload)
    {
        try {
            window.localStorage.removeItem(storageKey(upload));
        } catch (e) {
            // nothing remembered
        }
    }

    /**
     * @param xhr an answer of the server
     * @returns {boolean} whether the request is worth sending again: not when the server refused
     *          it, naming the error
     */
    function isTransient(xhr)
    {
        const status = xhr.status;
        return xhr.getResponseHeader(ERROR_HEADER) === null &&
            (status === 0 || status === 409 || status === 423 || status === 429 || status >= 500);
    }

    /**
     * Sends a request of tus for an upload.
     *
     * @param upload the upload, or an object with its field
     * @param method the method
     * @param url the URL, without the query of the field
     * @param headers the headers
     * @param body the body, a Blob, or null
     * @returns {Promise<XMLHttpRequest>} the request once answered; rejected with the request
     *          when it got no answer, which is aborted when it stalled: when it sent nothing,
     *          or got no answer, for a while; once all its body left the browser, when the server
     *          no longer answers either
     */
    function send(upload, method, url, headers, body)
    {
        return new Promise(function (resolve, reject) {
            const xhr = new XMLHttpRequest();
            let stall = null;
            let sentAll = false;
            let ended = false;
            const unstall = function () {
                window.clearTimeout(stall);
            };
            const restall = function () {
                unstall();
                if (ended || upload.field.stallMillis <= 0) {
                    return;
                }
                stall = window.setTimeout(function () {
                    if (!sentAll) {
                        xhr.abort();
                        return;
                    }
                    // all of a chunk can be in the buffers of the network long before the answer,
                    // which may still come as long as the server answers
                    send({'field': upload.field}, 'HEAD', url, {}, null).then(restall, function () {
                        xhr.abort();
                    });
                }, upload.field.stallMillis);
            };
            const overridden = upload.field.overridingMethod && (method === 'PATCH' || method === 'DELETE');
            xhr.open(overridden ? 'POST' : method, withQuery(withSession(upload.field, url), upload.field.query));
            xhr.setRequestHeader('Tus-Resumable', TUS_RESUMABLE);
            if (overridden) {
                xhr.setRequestHeader('X-HTTP-Method-Override', method);
            }
            Object.keys(headers).forEach(function (name) {
                xhr.setRequestHeader(name, headers[name]);
            });
            if (body) {
                xhr.upload.onprogress = function (event) {
                    upload.sent = event.loaded;
                    sentAll = event.loaded >= body.size;
                    upload.field.progress(upload);
                    alive(upload);
                    restall();
                };
            }
            // a request whose answer never comes, through a connection that went away without a
            // word, is given up as well
            restall();
            xhr.onload = function () {
                ended = true;
                unstall();
                resolve(xhr);
            };
            xhr.onerror = xhr.ontimeout = xhr.onabort = function () {
                ended = true;
                unstall();
                reject(xhr);
            };
            upload.xhr = xhr;
            xhr.send(body || null);
        });
    }

    /**
     * @param field the field
     * @param file the file, or what an earlier page remembered of it: its name, size, date and type
     * @param state the state of the upload
     * @returns {object} a new upload
     */
    function newUpload(field, file, state)
    {
        return {
            'key': ++lastKey, 'file': file, 'field': field, 'state': state, 'url': null,
            'offset': 0, 'sent': 0, 'attempt': 0, 'restarts': 0, 'run': 0, 'xhr': null, 'timer': null,
            'error': null
        };
    }

    /**
     * @returns {string} the content type of a file, as the server is told it
     */
    function typeOf(file)
    {
        return file.type || 'application/octet-stream';
    }

    /**
     * @returns {boolean} whether a file is the one an upload was created for: the same name, size,
     *          date and type
     */
    function isFileOf(upload, file)
    {
        const known = upload.file;
        // the server may not know the date, and takes a file without a type for an octet stream
        return known.name === file.name && known.size === file.size &&
            (!known.lastModified || known.lastModified === file.lastModified) &&
            typeOf(known) === typeOf(file);
    }

    /**
     * Ends an upload whose file the server stored, and tells the server of it.
     *
     * @param upload the upload
     * @param fileName the name the file was stored under
     */
    function completed(upload, fileName)
    {
        const field = upload.field;
        upload.state = 'done';
        forget(upload);
        field.discard(upload);
        field.clientSideSuccessCallBack();
        field.report({'error': false, 'filesInfo': JSON.stringify([{
            'fileName': fileName,
            'clientFileName': upload.file.name,
            'contentType': upload.file.type || 'application/octet-stream'
        }])});
        field.pump();
    }

    /**
     * Ends an upload that failed, keeping it in the lists to be resumed or removed, and tells the
     * page and the server why.
     *
     * @param upload the upload
     * @param errorMessage the key of the error
     */
    function failed(upload, errorMessage)
    {
        const field = upload.field;
        upload.state = 'failed';
        upload.error = errorMessage;
        upload.xhr = null;
        field.renderLists();
        const ep = {'error': true, 'errorMessage': errorMessage};
        field.uploadErrorCallBack(ep);
        field.report(ep);
        field.pump();
    }

    /**
     * Sends the chunks of an upload from the offset the server holds, until the file is whole.
     *
     * @returns {Promise} done when the server stored the file
     */
    function sendChunks(upload, run)
    {
        if (upload.run !== run) {
            return Promise.resolve();
        }
        const end = Math.min(upload.file.size, upload.offset + upload.field.chunkSize);
        const headers = {'Upload-Offset': String(upload.offset), 'Content-Type': CHUNK_TYPE};
        upload.sent = 0;
        return send(upload, 'PATCH', upload.url, headers, upload.file.slice(upload.offset, end)).then(function (xhr) {
            if (upload.run !== run) {
                return;
            }
            const offset = xhr.getResponseHeader('Upload-Offset');
            if (offset !== null) {
                upload.offset = Number(offset);
            }
            upload.sent = 0;
            if (xhr.status === 204) {
                upload.attempt = 0;
                const fileName = xhr.getResponseHeader(FILE_NAME_HEADER);
                if (fileName !== null) {
                    completed(upload, fileName);
                    return;
                }
                upload.field.progress(upload);
                return sendChunks(upload, run);
            }
            throw xhr;
        });
    }

    /**
     * Asks the server how many bytes of an upload it holds. A refusal, of a grant the server no
     * longer takes for example, fails the upload, which stays remembered for a page loaded again.
     *
     * @returns {Promise<boolean>} whether the server still has the upload
     */
    function ask(upload, run)
    {
        return send(upload, 'HEAD', upload.url, {}, null).then(function (xhr) {
            if (upload.run !== run || xhr.status === 404 || xhr.status === 410) {
                return false;
            }
            if (xhr.status !== 200) {
                throw xhr;
            }
            upload.offset = Number(xhr.getResponseHeader('Upload-Offset')) || 0;
            const fileName = xhr.getResponseHeader(FILE_NAME_HEADER);
            if (fileName !== null) {
                completed(upload, fileName);
                upload.run++;
            } else if (upload.offset >= upload.file.size) {
                // stored for another field, a rendering before a reload of the page
                throw UPLOAD_LOST;
            }
            return true;
        });
    }

    /**
     * Creates an upload on the server.
     *
     * @returns {Promise} done when the server created it, or stored an empty file
     */
    function create(upload, run)
    {
        const file = upload.file;
        const headers = {
            'Upload-Length': String(file.size),
            'Upload-Metadata': 'filename ' + base64(file.name) + ',filetype ' + base64(typeOf(file)) +
                ',lastmodified ' + base64(String(file.lastModified || 0)) +
                ',place ' + base64(upload.field.rememberedAs)
        };
        return send(upload, 'POST', upload.field.endpoint, headers, null).then(function (xhr) {
            if (upload.run !== run) {
                return;
            }
            if (xhr.status !== 201) {
                throw xhr;
            }
            // behind a proxy the path the server sees may not be the browser's: the endpoint is
            upload.url = uploadUrl(upload.field, idOf(xhr.getResponseHeader('Location') || ''));
            upload.offset = 0;
            const fileName = xhr.getResponseHeader(FILE_NAME_HEADER);
            if (fileName !== null) {
                completed(upload, fileName);
                upload.run++;
                return;
            }
            remember(upload);
        });
    }

    /**
     * Stops looking whether the server answers again.
     */
    function unwatch(state)
    {
        const probe = state.probe;
        if (!probe) {
            return;
        }
        state.probe = null;
        window.clearTimeout(probe.timer);
        window.removeEventListener('online', probe.check);
        window.removeEventListener('focus', probe.check);
        document.removeEventListener('visibilitychange', probe.check);
        if (probe.request && probe.request.xhr) {
            probe.request.xhr.abort();
        }
    }

    /**
     * Looks whether the server answers again while uploads of a field wait for the connection:
     * after a while, longer each time, and whenever the browser is online again or the page is
     * looked at again. Once it answers, the field goes on with them, or asks the user first.
     */
    function watch(state)
    {
        if (state.probe) {
            return;
        }
        const probe = state.probe = {'attempt': 0, 'timer': null, 'request': null};
        const later = function () {
            const delays = state.field.probeDelays;
            probe.timer = window.setTimeout(probe.check, delays[Math.min(probe.attempt++, delays.length - 1)]);
        };
        probe.check = function () {
            const field = state.field;
            const lost = field.uploads.filter(function (upload) {
                return upload.state === 'interrupted';
            });
            if (state.probe !== probe || probe.request) {
                return;
            }
            if (lost.length === 0) {
                unwatch(state);
                return;
            }
            window.clearTimeout(probe.timer);
            probe.request = {'field': field};
            send(probe.request, 'HEAD', lost[0].url || field.endpoint, {}, null).then(function () {
                if (state.probe === probe) {
                    unwatch(state);
                    state.field.reconnected();
                }
            }, function () {
                probe.request = null;
                if (state.probe === probe) {
                    later();
                }
            });
        };
        window.addEventListener('online', probe.check);
        window.addEventListener('focus', probe.check);
        document.addEventListener('visibilitychange', probe.check);
        later();
    }

    /**
     * Stops an upload whose connection is lost, and those waiting for their turn, until the
     * server answers again.
     */
    function interrupt(upload)
    {
        const field = upload.field;
        upload.state = 'interrupted';
        upload.xhr = null;
        field.uploads.forEach(function (each) {
            if (each.state === 'waiting') {
                each.state = 'interrupted';
            }
        });
        field.renderLists();
        watch(field.state);
    }

    /**
     * Goes on with an upload after a request failed: once more after a while, or once the browser
     * is online again, unless the failure is final. Once the tries are spent without an answer,
     * the connection counts as lost.
     */
    function retry(upload, run, problem)
    {
        if (upload.run !== run) {
            return;
        }
        upload.xhr = null;
        if (typeof problem === 'string') {
            failed(upload, problem);
            return;
        }
        if (!(problem instanceof XMLHttpRequest)) {
            Wicket.Log.error('The upload of ' + upload.file.name + ' failed', problem);
            failed(upload, UPLOAD_FAILED);
            return;
        }
        const status = problem.status;
        if (!isTransient(problem)) {
            if ((status === 404 || status === 410) && problem.getResponseHeader(ERROR_HEADER) === null) {
                forget(upload);
                failed(upload, UPLOAD_LOST);
            } else {
                failed(upload, problem.getResponseHeader(ERROR_HEADER) || UPLOAD_FAILED);
            }
            return;
        }
        const delays = upload.field.retryDelays;
        if (navigator.onLine === false) {
            upload.state = 'offline';
            upload.field.renderLists();
            const online = function () {
                window.removeEventListener('online', online);
                window.clearTimeout(upload.timer);
                if (upload.run === run) {
                    upload.field.begin(upload);
                }
            };
            window.addEventListener('online', online);
            // offline for longer than the tries take: lost
            upload.timer = window.setTimeout(function () {
                window.removeEventListener('online', online);
                if (upload.run === run) {
                    interrupt(upload);
                }
            }, delays.reduce(function (sum, delay) {
                return sum + delay;
            }, 0));
            return;
        }
        if (upload.attempt >= delays.length) {
            if (status === 0) {
                interrupt(upload);
            } else {
                failed(upload, problem.getResponseHeader(ERROR_HEADER) || UPLOAD_FAILED);
            }
            return;
        }
        upload.state = 'retrying';
        upload.field.renderLists();
        upload.timer = window.setTimeout(function () {
            if (upload.run === run) {
                upload.field.begin(upload);
            }
        }, delays[upload.attempt++]);
    }

    /**
     * Starts an upload, or goes on with it: from where the server got, if it has the upload,
     * whose URL may be remembered from an earlier page; else from the start.
     *
     * @param upload the upload
     */
    Field.prototype.begin = function (upload)
    {
        const run = ++upload.run;
        upload.state = 'uploading';
        upload.error = null;
        upload.field.renderLists();
        const url = upload.url || remembered(upload);
        let begun;
        if (url) {
            upload.url = url;
            begun = ask(upload, run).then(function (known) {
                if (!known && upload.run === run) {
                    forget(upload);
                    upload.url = null;
                    if (++upload.restarts > MAX_RESTARTS) {
                        throw UPLOAD_LOST;
                    }
                    return create(upload, run);
                }
            });
        } else {
            begun = create(upload, run);
        }
        begun.then(function () {
            return sendChunks(upload, run);
        }).catch(function (problem) {
            retry(upload, run, problem);
        });
    };

    /**
     * @returns {boolean} whether the upload is sending, or about to
     */
    function isActive(upload)
    {
        return upload.state === 'uploading' || upload.state === 'retrying' || upload.state === 'offline';
    }

    /**
     * Stops the requests of an upload, if any.
     */
    function halt(upload)
    {
        upload.run++;
        window.clearTimeout(upload.timer);
        if (upload.xhr) {
            upload.xhr.abort();
            upload.xhr = null;
        }
    }

    /**
     * Sends each file chosen in the input as an upload of its own, unless they break a limit of
     * the field: the server is told of that as of an upload it refused, without the files being
     * sent. The uploads wait until fewer than allowed are sending.
     *
     * @returns {boolean} whether the files are sent
     */
    Field.prototype.upload = function ()
    {
        const files = Array.from(this.input.files);
        if (files.length === 0) {
            if (this.resumedChosen) {
                // the files just chosen go on with uploads of an earlier page
                return true;
            }
            this.failed(NO_FILE_SELECTED);
            return false;
        }
        const exceededLimit = this.exceededLimit(files);
        if (exceededLimit) {
            this.failed(exceededLimit);
            return false;
        }
        if (this.clientBeforeSendCallBack(null) === false) {
            Wicket.Log.log("The upload of field '" + this.inputName + "' was not sent: the before-send callback returned false.");
            this.report({'error': true, 'errorMessage': UPLOAD_CANCELED});
            return false;
        }
        files.forEach(function (file) {
            this.uploads.push(newUpload(this, file, 'waiting'));
        }, this);
        this.files = [];
        this.updateInput();
        this.pump();
        this.renderLists();
        return true;
    };

    /**
     * Starts as many waiting uploads as may run at the same time.
     */
    Field.prototype.pump = function ()
    {
        let running = this.uploads.filter(isActive).length;
        this.uploads.forEach(function (upload) {
            if (upload.state === 'waiting' && running < this.parallelUploads) {
                running++;
                this.begin(upload);
            }
        }, this);
    };

    /**
     * @returns {boolean} whether an upload of the field is sending, or about to
     */
    Field.prototype.isUploading = function ()
    {
        return this.uploads.some(isActive);
    };

    /**
     * Renders the lists once files the field did not send failed.
     */
    Field.prototype.ended = function ()
    {
        this.renderLists();
    };

    /**
     * Takes an upload that ended out of the lists.
     */
    Field.prototype.discard = function (upload)
    {
        const index = this.uploads.indexOf(upload);
        if (index >= 0) {
            this.uploads.splice(index, 1);
        }
        this.renderLists();
    };

    /**
     * @param field a field
     * @param upload an upload, several of them in an array, or undefined
     * @param filter which uploads of the field undefined stands for
     * @returns {object[]} the uploads still in the lists of the field
     */
    function uploadsOf(field, upload, filter)
    {
        const chosen = upload === undefined ? field.uploads.filter(filter) : [].concat(upload);
        return chosen.filter(function (each) {
            return field.uploads.indexOf(each) >= 0;
        });
    }

    /**
     * Pauses an upload, or all the uploads of the field that send or wait.
     *
     * @param upload the upload, several of them in an array, or undefined for all
     */
    Field.prototype.pause = function (upload)
    {
        const paused = uploadsOf(this, upload, function (each) {
            return isActive(each) || each.state === 'waiting' || each.state === 'interrupted';
        }).filter(function (each) {
            return each.state !== 'recalled';
        });
        paused.forEach(function (each) {
            halt(each);
            each.state = 'paused';
            // no longer sending: another page may go on with it at once
            remember(each, 0);
        });
        this.renderLists();
        this.pump();
    };

    /**
     * Resumes an upload, or all the paused, failed and interrupted uploads of the field: they wait
     * until fewer uploads than allowed are sending. An upload of an earlier page goes on once the
     * user chooses its file again: resuming it opens the browser's dialog to choose it, which only
     * works while the user clicks.
     *
     * @param upload the upload, several of them in an array, or undefined for all
     */
    Field.prototype.resume = function (upload)
    {
        const resumed = uploadsOf(this, upload, function (each) {
            return each.state === 'paused' || each.state === 'failed' || each.state === 'interrupted';
        });
        let choosing = false;
        resumed.forEach(function (each) {
            if (each.state === 'recalled') {
                choosing = true;
            } else if (!isActive(each)) {
                halt(each);
                each.state = 'waiting';
                each.attempt = 0;
                each.restarts = 0;
            }
        });
        this.pump();
        this.renderLists();
        if (choosing) {
            this.input.click();
        }
    };

    /**
     * Deletes an upload on the server, trying again while the server is busy with it, a request
     * just aborted still writing it for example, as long as the tries of the field take.
     *
     * @param field the field
     * @param url the URL of the upload
     * @param attempt the number of tries before
     */
    function deleteOnServer(field, url, attempt)
    {
        const again = function (problem) {
            const delays = field.retryDelays;
            if (attempt < delays.length && isTransient(problem)) {
                window.setTimeout(function () {
                    deleteOnServer(field, url, attempt + 1);
                }, delays[attempt]);
            }
            // else the server deletes it once it expires
        };
        send({'field': field}, 'DELETE', url, {}, null).then(function (xhr) {
            if (xhr.status !== 204 && xhr.status !== 404 && xhr.status !== 410) {
                again(xhr);
            }
        }, again);
    }

    /**
     * Cancels an upload, or all the uploads of the field, deleting them on the server, and tells
     * the server, once.
     *
     * @param upload the upload, several of them in an array, or undefined for all
     */
    Field.prototype.cancel = function (upload)
    {
        const canceled = uploadsOf(this, upload, function () {
            return true;
        });
        if (canceled.length === 0) {
            Wicket.Log.log("No upload of field '" + this.inputName + "' to cancel: it has already ended, or never started.");
            return;
        }
        canceled.forEach(function (each) {
            halt(each);
            each.state = 'canceled';
            forget(each);
            if (each.url) {
                deleteOnServer(this, each.url, 0);
            }
            this.discard(each);
        }, this);
        this.clientSideCancelCallBack();
        this.report({'error': true, 'errorMessage': UPLOAD_CANCELED});
        this.pump();
    };

    /**
     * Chooses files for the next upload, as the parent does, except those of uploads of an
     * earlier page: they go on with them, from where the server got.
     *
     * @param files the files
     */
    Field.prototype.choose = function (files)
    {
        const recalled = this.uploads.filter(function (upload) {
            return upload.state === 'recalled';
        });
        const resumed = [];
        const others = files.filter(function (file) {
            const upload = recalled.find(function (each) {
                return resumed.indexOf(each) < 0 && isFileOf(each, file);
            });
            if (upload) {
                upload.file = file;
                upload.state = 'waiting';
                resumed.push(upload);
            }
            return !upload;
        });
        if (resumed.length > 0) {
            // the listeners of the change of the input, that start the upload, may find no file
            this.resumedChosen = true;
            const self = this;
            window.setTimeout(function () {
                self.resumedChosen = false;
            }, 0);
            this.pump();
        }
        Base.prototype.choose.call(this, others);
    };

    /**
     * Lists the uploads an earlier page of the field left unfinished, as remembered in the local
     * storage or as the server offers them, {@code recalled}, which the server still has, and asks
     * whether to go on with them; forgets those the server no longer has. Once per page.
     */
    Field.prototype.recall = function ()
    {
        const state = this.state;
        if (state.recalled || !this.remembering || !this.recalling) {
            return;
        }
        state.recalled = true;
        const self = this;
        const prefix = storagePrefix(this);
        let keys;
        try {
            keys = Object.keys(window.localStorage).filter(function (key) {
                return key.indexOf(prefix) === 0;
            });
        } catch (e) {
            // a browser without storage still gets those of the server
            keys = [];
        }
        const entries = keys.map(function (key) {
            return {'key': key, 'value': rememberedAt(key)};
        });
        this.recalledByServer.forEach(function (upload) {
            const url = uploadUrl(self, upload.id);
            const remembered = entries.some(function (entry) {
                return entry.value && belongs(self, entry.value) && absolute(entry.value.url) === url;
            });
            if (!remembered) {
                entries.push({'key': null, 'value': {
                    'url': url, 'field': self.rememberedAs, 'context': self.uploadContext,
                    'name': upload.name, 'size': upload.size, 'lastModified': upload.lastModified,
                    'type': upload.type, 'alive': 0
                }});
            }
        });
        const found = [];
        const asked = entries.map(function (entry) {
            const key = entry.key;
            const value = entry.value;
            const known = value && state.uploads.some(function (upload) {
                return upload.url && absolute(upload.url) === absolute(value.url);
            });
            const sending = value && Date.now() - (Number(value.alive) || 0) < self.inUseMillis;
            if (!value || !belongs(self, value) || known || sending) {
                return null;
            }
            const upload = newUpload(self, {
                'name': String(value.name), 'size': Number(value.size),
                'lastModified': Number(value.lastModified), 'type': String(value.type || '')
            }, 'recalled');
            upload.url = value.url;
            return send(upload, 'HEAD', upload.url, {}, null).then(function (xhr) {
                upload.xhr = null;
                if (xhr.status === 404 || xhr.status === 410 || xhr.getResponseHeader(FILE_NAME_HEADER) !== null) {
                    if (key !== null) {
                        window.localStorage.removeItem(key);
                    }
                    return;
                }
                upload.offset = Number(xhr.getResponseHeader('Upload-Offset')) || 0;
                if (xhr.status === 200 && upload.offset < upload.file.size) {
                    found.push(upload);
                }
            }, function () {
                // asked again on the next page
            });
        });
        Promise.all(asked).then(function () {
            const field = state.field;
            const recalled = found.filter(function (upload) {
                return !field.uploads.some(function (each) {
                    return each.url === upload.url;
                });
            });
            if (recalled.length > 0) {
                recalled.forEach(function (upload) {
                    upload.field = field;
                    field.uploads.push(upload);
                });
                field.ask('recalled', recalled);
            }
        });
    };

    /**
     * Goes on with the uploads that waited for the connection, now that the server answers
     * again, or asks the user first.
     */
    Field.prototype.reconnected = function ()
    {
        const lost = this.uploads.filter(function (upload) {
            return upload.state === 'interrupted';
        });
        if (lost.length === 0) {
            return;
        }
        if (this.askingToResume) {
            this.ask('reconnected', lost);
        } else {
            this.resume(lost);
        }
    };

    /**
     * Asks the user whether to go on with uploads: through the resume question callback, else in
     * the lists of the field until the user answers.
     *
     * @param reason 'reconnected' or 'recalled'
     * @param uploads the uploads
     */
    Field.prototype.ask = function (reason, uploads)
    {
        const state = this.state;
        const question = {
            'reason': reason,
            'files': uploads.map(function (upload) {
                return {'name': upload.file.name, 'size': upload.file.size, 'sent': upload.offset};
            }),
            'resume': function () {
                state.field.resume(uploads);
            },
            'cancel': function () {
                state.field.cancel(uploads);
            },
            'waitsFor': reason === 'recalled' ? 'recalled' : 'interrupted',
            'inLists': false
        };
        state.question = question;
        question.inLists = this.resumeQuestionCallBack(question) !== true;
        this.renderLists();
    };

    /**
     * @returns {object} the question the field asks, while an upload it is about waits for the
     *          answer, else null
     */
    Field.prototype.question = function ()
    {
        const question = this.state.question;
        if (question && !this.uploads.some(function (upload) {
            return upload.state === question.waitsFor;
        })) {
            this.state.question = null;
        }
        return this.state.question;
    };

    /**
     * Shows how far an upload got, without rendering the lists again.
     */
    Field.prototype.progress = function (upload)
    {
        const sent = Math.min(upload.file.size, upload.offset + upload.sent);
        (upload.views || []).forEach(function (view) {
            view.progress.value = sent;
            view.status.textContent = this.statusOf(upload, sent);
        }, this);
    };

    /**
     * @returns {string} the state of an upload in words
     */
    Field.prototype.statusOf = function (upload, sent)
    {
        const percent = upload.file.size > 0 ? Math.floor(100 * sent / upload.file.size) + '%' : '';
        const texts = this.texts;
        switch (upload.state) {
            case 'uploading':
                return percent;
            case 'failed':
                return texts.failed || '';
            case 'paused':
                return (texts.paused || '') + ' ' + percent;
            case 'interrupted':
                return (texts.interrupted || '') + ' ' + percent;
            case 'recalled':
                return (texts.stopped || '') + ' ' + percent;
            default:
                return texts[upload.state] || '';
        }
    };

    /**
     * @returns {HTMLButtonElement} a button labelled by its text and the name of a file
     */
    function button(id, className, text, name, onClick)
    {
        const result = document.createElement('button');
        result.type = 'button';
        result.id = id;
        result.className = className;
        result.textContent = text;
        result.setAttribute('aria-labelledby', id + ' ' + name.id);
        result.addEventListener('click', onClick);
        return result;
    }

    /**
     * @returns {HTMLLIElement} the item asking the question of the field in a list
     */
    Field.prototype.renderQuestion = function (list, question)
    {
        const texts = this.texts;
        const recalled = question.reason === 'recalled';
        const text = document.createElement('span');
        text.className = 'wicket-resumable-upload-question-text';
        text.id = list.id + '-question';
        text.textContent = texts[question.reason] || '';
        const resume = button(text.id + '-resume', 'wicket-resumable-upload-resume',
            recalled ? texts['continue'] : texts.resume, text, question.resume);
        const cancel = button(text.id + '-cancel', 'wicket-resumable-upload-cancel',
            recalled ? texts.discard : texts.cancel, text, question.cancel);
        const item = document.createElement('li');
        item.className = 'wicket-resumable-upload-question';
        item.setAttribute('data-reason', question.reason);
        text.setAttribute('role', 'status');
        item.append(text, ' ', resume, ' ', cancel);
        return item;
    };

    /**
     * Renders the uploads of the field into a list, each with its progress and buttons to pause
     * or resume it and to cancel it, followed by the files chosen for the next upload; above them
     * the question the field asks, if any.
     *
     * @param list the ul or ol
     * @param removeText the text of the buttons removing a file
     */
    Field.prototype.renderList = function (list, removeText)
    {
        const self = this;
        const focused = document.activeElement && list.contains(document.activeElement) ? document.activeElement.id : null;
        list.replaceChildren();
        const question = this.question();
        if (question && question.inLists) {
            list.appendChild(this.renderQuestion(list, question));
        }
        this.uploads.forEach(function (upload) {
            list.appendChild(self.renderUpload(list, upload));
        });
        this.files.forEach(function (file, index) {
            const name = document.createElement('span');
            name.className = 'wicket-upload-file-name';
            name.id = list.id + '-' + index + '-name';
            name.textContent = file.name;
            const remove = button(list.id + '-' + index + '-remove', 'wicket-upload-file-remove', removeText, name, function () {
                self.remove(file);
            });
            const item = document.createElement('li');
            item.className = 'wicket-upload-file';
            item.append(name, ' ', remove);
            list.appendChild(item);
        });
        const again = focused ? document.getElementById(focused) : null;
        if (again) {
            again.focus();
        }
    };

    /**
     * @returns {HTMLLIElement} the item of an upload in a list
     */
    Field.prototype.renderUpload = function (list, upload)
    {
        const self = this;
        const texts = this.texts;
        const id = list.id + '-upload-' + upload.key;
        const name = document.createElement('span');
        name.className = 'wicket-upload-file-name';
        name.id = id + '-name';
        name.textContent = upload.file.name;
        const progress = document.createElement('progress');
        progress.className = 'wicket-resumable-upload-progress';
        progress.max = upload.file.size || 1;
        progress.setAttribute('aria-labelledby', name.id);
        const status = document.createElement('span');
        status.className = 'wicket-resumable-upload-status';
        const pausing = isActive(upload) || upload.state === 'waiting';
        const recalled = upload.state === 'recalled';
        const toggle = button(id + '-toggle', 'wicket-resumable-upload-' + (pausing ? 'pause' : 'resume'),
            pausing ? texts.pause : (recalled ? texts['continue'] : texts.resume), name, function () {
                if (pausing) {
                    self.pause(upload);
                } else {
                    self.resume(upload);
                }
            });
        const cancel = button(id + '-cancel', 'wicket-resumable-upload-cancel',
            recalled ? texts.discard : texts.cancel, name, function () {
                self.cancel(upload);
            });
        const item = document.createElement('li');
        item.className = 'wicket-upload-file wicket-resumable-upload';
        item.setAttribute('data-state', upload.state);
        item.append(name, ' ', progress, ' ', status, ' ', toggle, ' ', cancel);
        upload.views = (upload.views || []).filter(function (view) {
            return view.list !== list && document.body.contains(view.progress);
        });
        upload.views.push({'list': list, 'progress': progress, 'status': status});
        this.progress(upload);
        return item;
    };
})();
