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

/*global QUnit: true, DataTransfer: true */

;(function (undefined) {
	"use strict";

	Wicket.Event.add(window, 'domready', function() {
		const { module, test } = QUnit;

		const INPUT_ID = 'uploadToResourceInput';

		// stands in for the XMLHttpRequest the field uploads with
		function FakeXhr() {
			FakeXhr.instances.push(this);
			this.status = 0;
			this.responseText = '';
		}
		FakeXhr.prototype.open = function (method, url) {
			this.method = method;
			this.url = url;
		};
		FakeXhr.prototype.send = function (body) {
			this.body = body;
		};
		FakeXhr.prototype.abort = function () {
			this.aborted = true;
			this.onabort();
		};
		FakeXhr.prototype.respond = function (status, responseText) {
			this.status = status;
			this.responseText = responseText;
			this.onload();
		};
		FakeXhr.prototype.timeOut = function () {
			this.ontimeout();
		};

		let originalXhr, originalPost, reports, calls;

		function chooseFiles(files) {
			const dataTransfer = new DataTransfer();
			files.forEach(file => dataTransfer.items.add(file));
			document.getElementById(INPUT_ID).files = dataTransfer.files;
		}

		function newFile(name, size) {
			return new File([new Uint8Array(size)], name, {type: 'text/plain'});
		}

		function newField(settings, beforeSend) {
			return new Wicket.FileUploadToResourceField(Object.assign({
					inputName: INPUT_ID,
					resourceUrl: 'resource',
					ajaxCallBackUrl: 'callback',
					maxSize: 100,
					fileCountMax: -1,
					uploadToken: 'token'
				}, settings),
				beforeSend || function () { return true; },
				function () { calls.push('success'); },
				function () { calls.push('cancel'); },
				function (res) { calls.push('error:' + res.errorMessage); });
		}

		module('Wicket.FileUploadToResourceField', {
			beforeEach: function () {
				originalXhr = window.XMLHttpRequest;
				originalPost = Wicket.Ajax.post;
				FakeXhr.instances = [];
				window.XMLHttpRequest = FakeXhr;
				reports = [];
				calls = [];
				Wicket.Ajax.post = function (attrs) {
					reports.push(attrs);
				};
			},
			afterEach: function () {
				window.XMLHttpRequest = originalXhr;
				Wicket.Ajax.post = originalPost;
			}
		});

		test('sends all the files of the input in one request', assert => {
			chooseFiles([newFile('a.txt', 3), newFile('b.txt', 4)]);

			assert.true(newField().upload());

			assert.equal(FakeXhr.instances.length, 1);
			const xhr = FakeXhr.instances[0];
			assert.equal(xhr.method, 'POST');
			assert.equal(xhr.body.getAll('WICKET-FILE-UPLOAD').length, 2);
		});

		test('reports the files received by POST', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			const filesInfo = '[{"fileName":"f1","clientFileName":"a.txt","size":3,"contentType":"text/plain"}]';
			FakeXhr.instances[0].respond(200, filesInfo);

			assert.deepEqual(calls, ['success']);
			assert.equal(reports.length, 1);
			assert.equal(reports[0].u, 'callback');
			assert.deepEqual(reports[0].ep, {'error': false, 'filesInfo': JSON.stringify(JSON.parse(filesInfo))});
		});

		test('reports the error the resource answered', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			FakeXhr.instances[0].respond(200, '{"error":true,"errorMessage":"uploadTooManyFiles"}');

			assert.deepEqual(calls, ['error:uploadTooManyFiles']);
			assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'uploadTooManyFiles'});
		});

		test('reports an HTTP error as a failed upload', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			FakeXhr.instances[0].respond(500, 'Internal Server Error');

			assert.deepEqual(calls, ['error:uploadFailed']);
			assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'uploadFailed'});
		});

		test('reports a network failure as a lost connection', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			FakeXhr.instances[0].onerror();

			assert.deepEqual(calls, ['error:uploadConnectionLost']);
			assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'uploadConnectionLost'});
		});

		test('reports a timed-out upload as a failed upload, and another one can start', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			const field = newField();
			field.upload();

			FakeXhr.instances[0].timeOut();

			assert.deepEqual(calls, ['error:uploadFailed']);
			assert.deepEqual(reports.map(report => report.ep), [{'error': true, 'errorMessage': 'uploadFailed'}]);
			assert.true(field.upload(), 'sent again');
			assert.equal(FakeXhr.instances.length, 2);
		});

		test('reports an answer that is not JSON as a failed upload', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			FakeXhr.instances[0].respond(200, '<html>not JSON</html>');

			assert.deepEqual(calls, ['error:uploadFailed']);
			assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'uploadFailed'});
		});

		test('does not send files beyond the limits of the field', assert => {
			const field = newField({maxSize: 10, fileMaxSize: 4, fileCountMax: 2});

			chooseFiles([newFile('a.txt', 6), newFile('b.txt', 6)]);
			assert.false(field.upload(), 'beyond maxSize');
			chooseFiles([newFile('a.txt', 1), newFile('b.txt', 1), newFile('c.txt', 1)]);
			assert.false(field.upload(), 'beyond fileCountMax');
			chooseFiles([newFile('a.txt', 5)]);
			assert.false(field.upload(), 'beyond fileMaxSize');

			assert.equal(FakeXhr.instances.length, 0);
			assert.deepEqual(reports.map(report => report.ep.errorMessage),
				['uploadTooLarge', 'uploadTooManyFiles', 'uploadSingleFileTooLarge']);
		});

		test('refuses a second upload while one is running', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			const field = newField();

			assert.true(field.upload());
			assert.false(field.upload(), 'refused while the first one runs');
			assert.equal(FakeXhr.instances.length, 1);

			FakeXhr.instances[0].respond(200, '[]');
			assert.true(field.upload(), 'sent once the first one ended');
			assert.equal(FakeXhr.instances.length, 2);
		});

		test('cancels the running upload', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			const field = newField();
			field.upload();

			field.cancel();

			assert.true(FakeXhr.instances[0].aborted);
			assert.deepEqual(calls, ['cancel']);
			assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'upload.canceled'});
		});

		test('does not cancel an upload that has ended', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			const field = newField();
			field.upload();
			FakeXhr.instances[0].respond(200, '[]');

			field.cancel();

			assert.notOk(FakeXhr.instances[0].aborted);
			assert.deepEqual(calls, ['success']);
		});

		test('does not send the upload if the before-send callback returns false', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			const field = newField({}, function () { return false; });

			assert.false(field.upload());

			assert.notOk(FakeXhr.instances[0].body, 'not sent');
			assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'upload.canceled'});
			field.upload();
			assert.equal(FakeXhr.instances.length, 2, 'the field is not left busy');
		});
	});
})();
