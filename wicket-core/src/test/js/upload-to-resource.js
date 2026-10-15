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

/*global QUnit: true, DataTransfer: true, DragEvent: true */

;(function (undefined) {
	"use strict";

	Wicket.Event.add(window, 'domready', function() {
		const { module, test } = QUnit;

		const INPUT_ID = 'uploadToResourceInput';
		// an id every object inherits as a property
		const INHERITED_ID = 'constructor';

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
			this.running = true;
		};
		FakeXhr.prototype.end = function (handler) {
			this.running = false;
			this[handler]();
		};
		FakeXhr.prototype.abort = function () {
			if (this.running) {
				this.aborted = true;
				this.end('onabort');
			}
		};
		FakeXhr.prototype.respond = function (status, responseText) {
			this.status = status;
			this.responseText = responseText;
			this.end('onload');
		};
		FakeXhr.prototype.fail = function () {
			this.end('onerror');
		};
		FakeXhr.prototype.timeOut = function () {
			this.end('ontimeout');
		};

		// the field keeps a running upload beyond the test that started it
		function abortUploads() {
			FakeXhr.instances.forEach(xhr => xhr.abort());
		}

		let originalXhr, originalPost, reports, calls;

		function chooseFiles(files) {
			const dataTransfer = new DataTransfer();
			files.forEach(file => dataTransfer.items.add(file));
			document.getElementById(INPUT_ID).files = dataTransfer.files;
		}

		// replaces an input with an empty copy, as an Ajax rendering of its field does
		function rerender(inputId) {
			const input = document.getElementById(inputId);
			const copy = input.cloneNode(false);
			copy.files = new DataTransfer().files;
			input.replaceWith(copy);
		}

		function newFile(name, size) {
			return new File([new Uint8Array(size)], name, {type: 'text/plain'});
		}

		function addInput(id, files) {
			const input = document.createElement('input');
			input.type = 'file';
			input.multiple = true;
			input.id = id;
			const dataTransfer = new DataTransfer();
			files.forEach(file => dataTransfer.items.add(file));
			input.files = dataTransfer.files;
			document.body.appendChild(input);
			return input;
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
				abortUploads();
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

			FakeXhr.instances[0].fail();

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

		test('a field rendered anew refuses a second upload while the first one runs', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			rerender(INPUT_ID);
			const field = newField();
			chooseFiles([newFile('b.txt', 3)]);

			assert.false(field.upload(), 'refused while the first one runs');
			assert.equal(FakeXhr.instances.length, 1);
			FakeXhr.instances[0].respond(200, '[]');
			assert.true(field.upload(), 'sent once the first one ended');
		});

		test('a field rendered anew cancels the running upload', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			rerender(INPUT_ID);
			newField({ajaxCallBackUrl: 'newCallback'}).cancel();

			assert.true(FakeXhr.instances[0].aborted);
			assert.deepEqual(calls, ['cancel']);
			assert.deepEqual(reports.map(report => [report.u, report.ep.errorMessage]),
				[['newCallback', 'upload.canceled']]);
		});

		test('a field rendered anew reports the end of the running upload', assert => {
			chooseFiles([newFile('a.txt', 3)]);
			newField().upload();

			rerender(INPUT_ID);
			newField({ajaxCallBackUrl: 'newCallback'});
			FakeXhr.instances[0].respond(200, '[]');

			assert.deepEqual(calls, ['success']);
			assert.deepEqual(reports.map(report => [report.u, report.ep.error]), [['newCallback', false]]);
		});

		test('a field whose id an object inherits starts its first upload', assert => {
			const input = addInput(INHERITED_ID, [newFile('a.txt', 3)]);
			try {
				assert.true(newField({inputName: INHERITED_ID}).upload());
				assert.equal(FakeXhr.instances.length, 1);
			} finally {
				input.remove();
			}
		});

		const LISTED_ID = 'uploadToResourceListedInput';
		const SINGLE_ID = 'uploadToResourceSingleInput';
		const LIST_ID = 'uploadToResourceList';
		const ZONE_ID = 'uploadToResourceDropZone';

		function newFieldOf(inputId) {
			const field = newField({inputName: inputId});
			Wicket.Timer[inputId] = field;
			return field;
		}

		function pick(inputId, files) {
			const dataTransfer = new DataTransfer();
			files.forEach(file => dataTransfer.items.add(file));
			const input = document.getElementById(inputId);
			input.files = dataTransfer.files;
			input.dispatchEvent(new Event('change', {bubbles: true}));
		}

		function drop(files) {
			const dataTransfer = new DataTransfer();
			files.forEach(file => dataTransfer.items.add(file));
			document.getElementById(ZONE_ID).dispatchEvent(new DragEvent('drop',
				{dataTransfer: dataTransfer, bubbles: true, cancelable: true}));
		}

		function listed() {
			return Array.from(document.querySelectorAll('#' + LIST_ID + ' .wicket-upload-file-name'))
				.map(name => name.textContent);
		}

		function inputFiles(inputId) {
			return Array.from(document.getElementById(inputId).files).map(file => file.name);
		}

		function removeButtons() {
			return Array.from(document.querySelectorAll('#' + LIST_ID + ' .wicket-upload-file-remove'));
		}

		module('Wicket.FileUploadToResourceField file lists and drop zones', {
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
				abortUploads();
				window.XMLHttpRequest = originalXhr;
				Wicket.Ajax.post = originalPost;
				delete Wicket.Timer[LISTED_ID];
				delete Wicket.Timer[SINGLE_ID];
				delete Wicket.Timer[INHERITED_ID];
			}
		});

		test('without a list, a choice replaces the files chosen before', assert => {
			newFieldOf(LISTED_ID);

			pick(LISTED_ID, [newFile('a.txt', 1)]);
			pick(LISTED_ID, [newFile('b.txt', 1)]);

			assert.deepEqual(inputFiles(LISTED_ID), ['b.txt']);
		});

		test('a list shows the files of several choices, which add up', assert => {
			newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');

			pick(LISTED_ID, [newFile('a.txt', 1)]);
			pick(LISTED_ID, [newFile('b.txt', 1), newFile('c.txt', 1)]);

			assert.deepEqual(listed(), ['a.txt', 'b.txt', 'c.txt']);
			assert.deepEqual(inputFiles(LISTED_ID), ['a.txt', 'b.txt', 'c.txt']);
		});

		test('a list added after the field shows the files chosen before', assert => {
			newFieldOf(LISTED_ID);
			pick(LISTED_ID, [newFile('a.txt', 1)]);

			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');

			assert.deepEqual(listed(), ['a.txt']);
		});

		test('a list added before its field, whose id an object inherits, shows its files', assert => {
			const input = addInput(INHERITED_ID, []);
			try {
				Wicket.FileUploadToResourceField.addList(INHERITED_ID, LIST_ID, 'Remove');
				newFieldOf(INHERITED_ID);

				pick(INHERITED_ID, [newFile('a.txt', 1)]);
				pick(INHERITED_ID, [newFile('b.txt', 1)]);

				assert.deepEqual(listed(), ['a.txt', 'b.txt']);
			} finally {
				input.remove();
			}
		});

		test('the name of a file is listed as text', assert => {
			newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');

			pick(LISTED_ID, [newFile('<b>bold</b>.txt', 1)]);

			assert.deepEqual(listed(), ['<b>bold</b>.txt']);
			assert.equal(document.querySelectorAll('#' + LIST_ID + ' b').length, 0);
		});

		test('a button removes its file', assert => {
			newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			pick(LISTED_ID, [newFile('a.txt', 1), newFile('b.txt', 1)]);

			const button = removeButtons()[1];
			assert.equal(button.textContent, 'Remove');
			assert.equal(button.getAttribute('aria-labelledby'),
				button.id + ' ' + document.querySelectorAll('#' + LIST_ID + ' .wicket-upload-file-name')[1].id);
			button.click();

			assert.deepEqual(listed(), ['a.txt']);
			assert.deepEqual(inputFiles(LISTED_ID), ['a.txt']);
		});

		test('the files sent leave the list once uploaded, those chosen meanwhile stay', assert => {
			const field = newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			pick(LISTED_ID, [newFile('a.txt', 1)]);

			field.upload();
			assert.true(removeButtons().every(button => button.disabled), 'no removing while uploading');
			pick(LISTED_ID, [newFile('b.txt', 1)]);
			FakeXhr.instances[0].respond(200, '[]');

			assert.deepEqual(listed(), ['b.txt']);
			assert.deepEqual(inputFiles(LISTED_ID), ['b.txt']);
			assert.true(removeButtons().every(button => !button.disabled), 'removing once the upload ended');
		});

		test('a failed upload keeps its files in the list', assert => {
			const field = newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			pick(LISTED_ID, [newFile('a.txt', 1)]);

			field.upload();
			FakeXhr.instances[0].respond(500, 'Internal Server Error');

			assert.deepEqual(listed(), ['a.txt']);
		});

		test('a timed-out upload keeps its files in the list, and they can be removed again', assert => {
			const field = newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			pick(LISTED_ID, [newFile('a.txt', 1)]);
			field.upload();

			FakeXhr.instances[0].timeOut();

			assert.deepEqual(listed(), ['a.txt']);
			assert.true(removeButtons().every(button => !button.disabled));
		});

		test('a field rendered anew during an upload keeps its own files when the upload ends', assert => {
			const field = newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			pick(LISTED_ID, [newFile('a.txt', 1)]);
			field.upload();

			rerender(LISTED_ID);
			newFieldOf(LISTED_ID);
			pick(LISTED_ID, [newFile('b.txt', 1)]);
			assert.true(removeButtons().every(button => button.disabled), 'no removing while the upload runs');
			FakeXhr.instances[0].respond(200, '[]');

			assert.deepEqual(inputFiles(LISTED_ID), ['b.txt']);
			assert.deepEqual(listed(), ['b.txt']);
			assert.true(removeButtons().every(button => !button.disabled), 'removing once it ended');
			assert.deepEqual(calls, ['success'], 'the upload is reported');
			assert.deepEqual(reports.map(report => report.ep.error), [false]);
		});

		test('an upload ending after its field was removed still reports', assert => {
			const field = newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			pick(LISTED_ID, [newFile('a.txt', 1)]);
			field.upload();

			document.getElementById(LISTED_ID).remove();
			FakeXhr.instances[0].respond(200, '[]');

			assert.deepEqual(calls, ['success']);
			assert.deepEqual(reports.map(report => report.ep.error), [false]);
		});

		test('dropped files are chosen, and the input fires its events', assert => {
			newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			Wicket.FileUploadToResourceField.addDropZone(ZONE_ID, LISTED_ID, 'dragging');
			pick(LISTED_ID, [newFile('a.txt', 1)]);
			const events = [];
			const input = document.getElementById(LISTED_ID);
			input.addEventListener('input', () => events.push('input:' + input.files.length));
			input.addEventListener('change', () => events.push('change:' + input.files.length));

			drop([newFile('b.txt', 1), newFile('c.txt', 1)]);

			assert.deepEqual(listed(), ['a.txt', 'b.txt', 'c.txt']);
			assert.deepEqual(events, ['input:3', 'change:3']);
		});

		test('a drop zone chooses only the files the input accepts, one for a single input', assert => {
			newFieldOf(SINGLE_ID);
			Wicket.FileUploadToResourceField.addDropZone(ZONE_ID, SINGLE_ID, 'dragging');

			drop([new File(['x'], 'a.pdf', {type: 'application/pdf'}),
				new File(['x'], 'b.png', {type: 'image/png'}),
				new File(['x'], 'c.txt', {type: 'text/plain'})]);

			assert.deepEqual(inputFiles(SINGLE_ID), ['b.png']);
		});

		test('a drop zone is marked while files are dragged over it', assert => {
			newFieldOf(LISTED_ID);
			Wicket.FileUploadToResourceField.addDropZone(ZONE_ID, LISTED_ID, 'dragging');
			const zone = document.getElementById(ZONE_ID);
			const dataTransfer = new DataTransfer();
			dataTransfer.items.add(newFile('a.txt', 1));

			zone.dispatchEvent(new DragEvent('dragenter', {dataTransfer: dataTransfer, bubbles: true, cancelable: true}));
			assert.true(zone.classList.contains('dragging'), 'marked');
			zone.dispatchEvent(new DragEvent('dragleave', {dataTransfer: dataTransfer, bubbles: true, cancelable: true}));
			assert.false(zone.classList.contains('dragging'), 'unmarked');
		});

		test('a second run of the script keeps the field and its lists', assert => {
			const done = assert.async();
			const constructor = Wicket.FileUploadToResourceField;
			Wicket.FileUploadToResourceField.addList(LISTED_ID, LIST_ID, 'Remove');
			const script = document.createElement('script');
			script.src = document.querySelector('script[src$="FileUploadToResourceField.js"]').src;
			script.onload = function () {
				assert.strictEqual(Wicket.FileUploadToResourceField, constructor, 'not replaced');

				newFieldOf(LISTED_ID);
				pick(LISTED_ID, [newFile('a.txt', 1)]);
				pick(LISTED_ID, [newFile('b.txt', 1)]);

				assert.deepEqual(inputFiles(LISTED_ID), ['a.txt', 'b.txt'], 'the files add up');
				assert.deepEqual(listed(), ['a.txt', 'b.txt'], 'the list shows them');
				script.remove();
				done();
			};
			document.head.appendChild(script);
		});
	});
})();
