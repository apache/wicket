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

		const TEXTS = {waiting: 'Waiting', paused: 'Paused', retrying: 'Retrying', offline: 'Offline',
			interrupted: 'Connection lost at', stopped: 'Stopped at', failed: 'Failed', pause: 'Pause',
			resume: 'Resume', cancel: 'Cancel', reconnected: 'Back. Resume?',
			recalled: 'Earlier uploads. Continue?', 'continue': 'Continue', discard: 'Discard'};

		// stands in for the XMLHttpRequest the field sends its requests with
		function FakeXhr() {
			FakeXhr.instances.push(this);
			this.status = 0;
			this.headers = {};
			this.responseHeaders = {};
			this.upload = {};
		}
		FakeXhr.prototype.open = function (method, url) {
			this.method = method;
			this.url = url;
		};
		FakeXhr.prototype.setRequestHeader = function (name, value) {
			this.headers[name] = value;
		};
		FakeXhr.prototype.getResponseHeader = function (name) {
			return name in this.responseHeaders ? this.responseHeaders[name] : null;
		};
		FakeXhr.prototype.send = function (body) {
			this.body = body;
			this.running = true;
		};
		FakeXhr.prototype.respond = function (status, headers) {
			this.status = status;
			this.responseHeaders = headers || {};
			this.running = false;
			this.onload();
		};
		FakeXhr.prototype.fail = function () {
			this.running = false;
			this.onerror();
		};
		FakeXhr.prototype.abort = function () {
			if (this.running) {
				this.running = false;
				this.aborted = true;
				this.onabort();
			}
		};

		let originalXhr, originalPost, reports, calls, assert_, fields, inputs = 0;

		// lets the promises of the field run, and timers of no delay
		function settle() {
			return new Promise(resolve => window.setTimeout(resolve, 0));
		}

		// lets the field go on until a condition holds, a while at most
		function until(condition) {
			let tries = 0;
			const loop = function () {
				return condition() || ++tries > 100 ? Promise.resolve() : settle().then(loop);
			};
			return loop();
		}

		function last() {
			return FakeXhr.instances[FakeXhr.instances.length - 1];
		}

		function running() {
			return FakeXhr.instances.filter(xhr => xhr.running);
		}

		// a URL made absolute, as the field keeps the address of an upload
		function abs(url) {
			return new window.URL(url, document.baseURI).href;
		}

		function newFile(name, size) {
			return new File([new Uint8Array(size)], name, {type: 'text/plain', lastModified: 1000});
		}

		function choose(field, files) {
			const dataTransfer = new DataTransfer();
			files.forEach(file => dataTransfer.items.add(file));
			field.input.files = dataTransfer.files;
			field.files = files;
		}

		// a field of an input of its own, with a list
		function newField(settings, inputId, resumeQuestionCallBack) {
			const id = inputId || 'resumable' + (++inputs);
			if (!document.getElementById(id)) {
				const input = document.createElement('input');
				input.type = 'file';
				input.multiple = true;
				input.id = id;
				const list = document.createElement('ul');
				list.id = id + '-list';
				document.getElementById('qunit-fixture').append(input, list);
			}
			const field = new Wicket.ResumableUploadToResourceField(Object.assign({
					inputName: id,
					resourceUrl: '/uploads',
					ajaxCallBackUrl: 'callback',
					maxSize: 100,
					fileMaxSize: 50,
					fileCountMax: -1,
					uploadToken: 'token',
					grantExpires: 1767225600000,
					chunkSize: 4,
					parallelUploads: 1,
					remembering: false,
					// the timings ResumableUploadToResourceField sends by default
					retryDelays: [0, 1000, 3000, 5000, 10000, 20000, 1000],
					probeDelays: [5000, 10000, 20000, 30000],
					stallMillis: 30000,
					inUseMillis: 20000,
					texts: TEXTS
				}, settings),
				function () { return true; },
				function () { calls.push('success'); },
				function () { calls.push('cancel'); },
				function (res) { calls.push('error:' + res.errorMessage); },
				resumeQuestionCallBack);
			Wicket.Timer[id] = field;
			fields.push(field);
			Wicket.FileUploadToResourceField.addList(id, id + '-list', 'Remove');
			return field;
		}

		function items(field) {
			return Array.from(document.getElementById(field.inputName + '-list').children);
		}

		// what an earlier page of a field remembered of an upload in the local storage, for the
		// place of the field or for a context
		function rememberUpload(fieldId, url, name, size, alive, context) {
			const key = 'wicket-resumable-upload::' + abs('/uploads/') + '::' +
				(context ? 'context:' + context : 'place:' + fieldId) + '::' +
				[name, size, 1000, 'text/plain'].join('::');
			window.localStorage.setItem(key, JSON.stringify({'url': url, 'field': fieldId,
				'context': context || null, 'name': name, 'size': size, 'lastModified': 1000,
				'type': 'text/plain', 'alive': alive || 0}));
			return key;
		}

		function remembered() {
			return Object.keys(window.localStorage).filter(key => key.indexOf('wicket-resumable-upload::') === 0);
		}

		// creates the upload of the next file, answering its POST
		function created(location) {
			assert_.equal(last().method, 'POST');
			last().respond(201, {'Location': location, 'Upload-Offset': '0'});
			return settle();
		}

		// loses the connection of the upload a field just created for longer than its tries,
		// until the server answers again
		function loseConnection() {
			return created('/uploads/u1').then(function () {
				last().fail();
				return until(() => last().method === 'HEAD' && last().running);
			}).then(function () {
				last().fail();
				return until(() => last().running);
			}).then(function () {
				last().respond(200, {'Upload-Offset': '4'});
				return settle();
			});
		}

		module('Wicket.ResumableUploadToResourceField', {
			beforeEach: function (assert) {
				assert_ = assert;
				originalXhr = window.XMLHttpRequest;
				originalPost = Wicket.Ajax.post;
				FakeXhr.instances = [];
				window.XMLHttpRequest = FakeXhr;
				reports = [];
				calls = [];
				fields = [];
				Wicket.Ajax.post = function (attrs) {
					reports.push(attrs);
				};
				Object.keys(window.localStorage).filter(key => key.indexOf('wicket-resumable-upload::') === 0)
					.forEach(key => window.localStorage.removeItem(key));
			},
			afterEach: function () {
				// the uploads would go on beyond the test that started them
				fields.forEach(field => field.pause());
				FakeXhr.instances.forEach(xhr => xhr.abort());
				window.XMLHttpRequest = originalXhr;
				Wicket.Ajax.post = originalPost;
			}
		});

		test('sends a file in chunks and reports it once the server stored it', assert => {
			const field = newField();
			choose(field, [newFile('a ü.txt', 10)]);

			assert.true(field.upload());

			const create = last();
			assert.equal(create.method, 'POST');
			assert.equal(create.url, '/uploads?uploadId=' + field.inputName + '&fileMaxSize=50&uploadToken=token&grantExpires=1767225600000',
				'the grant of the field, its only limit the server holds a request to');
			assert.equal(create.headers['Tus-Resumable'], '1.0.0');
			assert.equal(create.headers['Upload-Length'], '10');
			assert.equal(create.headers['Upload-Metadata'], 'filename YSDDvC50eHQ=,filetype dGV4dC9wbGFpbg==' +
				',lastmodified MTAwMA==,place ' + btoa(field.rememberedAs), 'with the date of the file and the place of the field');
			assert.equal(field.input.files.length, 0, 'the files left the input');
			assert.equal(items(field)[0].getAttribute('data-state'), 'uploading');
			return created('/uploads/u1').then(function () {
				const first = last();
				assert.equal(first.method, 'PATCH');
				assert.equal(first.url, abs('/uploads/u1') + '?uploadId=' + field.inputName + '&fileMaxSize=50&uploadToken=token&grantExpires=1767225600000');
				assert.equal(first.headers['Upload-Offset'], '0');
				assert.equal(first.headers['Content-Type'], 'application/offset+octet-stream');
				assert.equal(first.body.size, 4);
				first.upload.onprogress({loaded: 2});
				assert.equal(items(field)[0].querySelector('progress').value, 2);
				assert.equal(items(field)[0].querySelector('.wicket-resumable-upload-status').textContent, '20%');
				first.respond(204, {'Upload-Offset': '4'});
				return settle();
			}).then(function () {
				assert.equal(last().headers['Upload-Offset'], '4');
				last().respond(204, {'Upload-Offset': '8'});
				return settle();
			}).then(function () {
				assert.equal(last().body.size, 2);
				last().respond(204, {'Upload-Offset': '10', 'Wicket-Upload-File-Name': 'stored'});
				return settle();
			}).then(function () {
				assert.deepEqual(calls, ['success']);
				assert.equal(reports.length, 1);
				assert.equal(reports[0].u, 'callback');
				assert.deepEqual(reports[0].ep, {'error': false, 'filesInfo': JSON.stringify([
					{'fileName': 'stored', 'clientFileName': 'a ü.txt', 'contentType': 'text/plain'}])});
				assert.equal(items(field).length, 0, 'the upload left the list');
				assert.false(field.isUploading());
			});
		});

		test('pauses an upload and resumes it where the server got', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				const chunk = last();
				items(field)[0].querySelector('.wicket-resumable-upload-pause').click();
				assert.true(chunk.aborted);
				const item = items(field)[0];
				assert.equal(item.getAttribute('data-state'), 'paused');
				assert.equal(item.querySelector('.wicket-resumable-upload-resume').textContent, 'Resume');
				assert.false(field.isUploading());
				assert.equal(reports.length, 0, 'a pause is no end');

				item.querySelector('.wicket-resumable-upload-resume').click();
				assert.equal(last().method, 'HEAD');
				assert.equal(last().url.split('?')[0], abs('/uploads/u1'));
				last().respond(200, {'Upload-Offset': '3', 'Upload-Length': '10'});
				return settle();
			}).then(function () {
				assert.equal(last().method, 'PATCH');
				assert.equal(last().headers['Upload-Offset'], '3');
			});
		});

		test('goes on after a broken connection, from where the server got', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				last().fail();
				return settle();
			}).then(function () {
				return settle();
			}).then(function () {
				assert.equal(last().method, 'HEAD', 'asks after the first, immediate retry');
				last().respond(200, {'Upload-Offset': '2'});
				return settle();
			}).then(function () {
				assert.equal(last().headers['Upload-Offset'], '2');
				assert.equal(reports.length, 0);
			});
		});

		test('waits for the browser to be online again', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			Object.defineProperty(navigator, 'onLine', {configurable: true, get: () => false});
			return created('/uploads/u1').then(function () {
				last().fail();
				return settle();
			}).then(function () {
				assert.equal(items(field)[0].getAttribute('data-state'), 'offline');
				assert.equal(items(field)[0].querySelector('.wicket-resumable-upload-status').textContent, 'Offline');
				const requests = FakeXhr.instances.length;
				delete navigator.onLine;
				window.dispatchEvent(new Event('online'));
				assert.equal(FakeXhr.instances.length, requests + 1);
				assert.equal(last().method, 'HEAD');
			}).finally(function () {
				delete navigator.onLine;
			});
		});

		test('resumes an upload remembered from an earlier page', assert => {
			const field = newField({remembering: true, rememberedAs: 'files'});
			const file = newFile('a.txt', 10);
			choose(field, [file]);
			field.upload();
			return created('/uploads/u1').then(function () {
				field.pause();

				const again = newField({remembering: true, rememberedAs: 'files'});
				choose(again, [newFile('a.txt', 10)]);
				again.upload();
				assert.equal(last().method, 'HEAD');
				assert.equal(last().url.split('?')[0], abs('/uploads/u1'));
				last().respond(200, {'Upload-Offset': '8'});
				return settle();
			}).then(function () {
				assert.equal(last().method, 'PATCH');
				assert.equal(last().headers['Upload-Offset'], '8');
				last().respond(204, {'Upload-Offset': '10', 'Wicket-Upload-File-Name': 'stored'});
				return settle();
			}).then(function () {
				assert.equal(remembered().length, 0, 'forgotten once stored');
			});
		});

		test('starts anew when the server lost a remembered upload', assert => {
			const field = newField({remembering: true, rememberedAs: 'files'});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				field.pause();
				const again = newField({remembering: true, rememberedAs: 'files'});
				choose(again, [newFile('a.txt', 10)]);
				again.upload();
				last().respond(404);
				return settle();
			}).then(function () {
				assert.equal(last().method, 'POST');
			});
		});

		test('reports the refusal of the server with its key, and keeps the upload to resume it', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			last().respond(403, {'Wicket-Upload-Error': 'resumableUploadTooMany'});
			return settle().then(function () {
				assert.deepEqual(calls, ['error:resumableUploadTooMany']);
				assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'resumableUploadTooMany'});
				const item = items(field)[0];
				assert.equal(item.getAttribute('data-state'), 'failed');
				assert.equal(item.querySelector('.wicket-resumable-upload-status').textContent, 'Failed');

				item.querySelector('.wicket-resumable-upload-resume').click();
				assert.equal(last().method, 'POST', 'tried again');
			});
		});

		test('fails a remembered upload whose grant the server refuses, and keeps it for a page loaded again', assert => {
			const field = newField({remembering: true, rememberedAs: 'files'});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			let again;
			return created('/uploads/u1').then(function () {
				field.pause();

				again = newField({remembering: true, rememberedAs: 'files'});
				choose(again, [newFile('a.txt', 10)]);
				again.upload();
				assert.equal(last().method, 'HEAD');
				assert.ok(last().url.indexOf('&grantExpires=1767225600000') > 0, 'with the expiry of the grant');
				last().respond(403, {'Wicket-Upload-Error': 'resumableUploadGrantRefused'});
				return settle();
			}).then(function () {
				assert.equal(last().method, 'HEAD', 'no new upload');
				assert.deepEqual(calls, ['error:resumableUploadGrantRefused']);
				const item = items(again)[0];
				assert.equal(item.getAttribute('data-state'), 'failed');
				assert.equal(item.querySelector('.wicket-resumable-upload-status').textContent, 'Failed');
				assert.equal(remembered().length, 1, 'still remembered');
				assert.equal(JSON.parse(window.localStorage.getItem(remembered()[0])).url, abs('/uploads/u1'));
			});
		});

		test('reports an upload the server no longer has as lost', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				last().respond(404);
				return settle();
			}).then(function () {
				assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'resumableUploadLost'});
			});
		});

		test('cancels an upload, deleting it on the server', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				const chunk = last();
				items(field)[0].querySelector('.wicket-resumable-upload-cancel').click();
				assert.true(chunk.aborted);
				assert.equal(last().method, 'DELETE');
				assert.equal(last().url.split('?')[0], abs('/uploads/u1'));
				assert.deepEqual(calls, ['cancel']);
				assert.deepEqual(reports.map(report => report.ep), [{'error': true, 'errorMessage': 'upload.canceled'}]);
				assert.equal(items(field).length, 0);
				last().respond(204);
			});
		});

		test('refuses files beyond the limits of the field without sending them', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 51)]);

			assert.false(field.upload());

			assert.equal(FakeXhr.instances.length, 0);
			assert.deepEqual(reports[0].ep, {'error': true, 'errorMessage': 'uploadSingleFileTooLarge'});
		});

		test('sends as many files at once as allowed, the others waiting', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 2), newFile('b.txt', 2)]);
			field.upload();

			assert.equal(running().length, 1);
			assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['uploading', 'waiting']);
			assert.equal(items(field)[1].querySelector('.wicket-resumable-upload-status').textContent, 'Waiting');
			return created('/uploads/u1').then(function () {
				last().respond(204, {'Upload-Offset': '2', 'Wicket-Upload-File-Name': 'a'});
				return settle();
			}).then(function () {
				assert.equal(last().method, 'POST');
				assert.equal(last().headers['Upload-Metadata'].split(',')[0], 'filename Yi50eHQ=');
			});
		});

		test('stores an empty file as the server creates its upload', assert => {
			const field = newField();
			choose(field, [newFile('empty.txt', 0)]);
			field.upload();
			last().respond(201, {'Location': '/uploads/u1', 'Upload-Offset': '0', 'Wicket-Upload-File-Name': 'stored'});
			return settle().then(function () {
				assert.deepEqual(calls, ['success']);
				assert.equal(FakeXhr.instances.length, 1, 'no chunk to send');
			});
		});

		test('a new rendering of the field takes over its uploads', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 2)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				const input = field.input;
				const copy = input.cloneNode(false);
				input.replaceWith(copy);
				const again = newField({}, field.inputName);
				assert.equal(items(again).length, 1, 'listed by the new rendering');
				assert.true(again.isUploading());
				last().respond(204, {'Upload-Offset': '2', 'Wicket-Upload-File-Name': 'a'});
				return settle();
			}).then(function () {
				assert.deepEqual(calls, ['success']);
			});
		});

		test('pauses and resumes all its uploads', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 2), newFile('b.txt', 2)]);
			field.upload();
			field.pause();
			assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['paused', 'paused']);
			assert.equal(running().length, 0);
			field.resume();
			assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['uploading', 'waiting']);
		});

		test('asks whether to resume once the server answers again after the connection was lost', assert => {
			const field = newField({retryDelays: [0], probeDelays: [0]});
			choose(field, [newFile('a.txt', 10), newFile('b.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				last().fail();
				return until(() => last().method === 'HEAD' && last().running);
			}).then(function () {
				assert.equal(last().url.split('?')[0], abs('/uploads/u1'), 'tries once more');
				last().fail();
				return until(() => last().running);
			}).then(function () {
				assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['interrupted', 'interrupted'],
					'the upload waiting for its turn waits for the connection too');
				assert.equal(items(field)[0].querySelector('.wicket-resumable-upload-status').textContent, 'Connection lost at 0%');
				assert.equal(last().method, 'HEAD', 'looks whether the server answers');
				last().fail();
				return until(() => last().running);
			}).then(function () {
				assert.equal(last().method, 'HEAD', 'looks again');
				last().respond(404);
				return until(() => items(field)[0].classList.contains('wicket-resumable-upload-question'));
			}).then(function () {
				const question = items(field)[0];
				assert.equal(question.getAttribute('data-reason'), 'reconnected');
				assert.equal(question.querySelector('.wicket-resumable-upload-question-text').textContent, 'Back. Resume?');
				assert.equal(running().length, 0, 'nothing sent before the user answers');
				assert.equal(reports.length, 0);

				question.querySelector('.wicket-resumable-upload-resume').click();
				assert.equal(last().method, 'HEAD');
				assert.equal(last().url.split('?')[0], abs('/uploads/u1'));
				last().respond(200, {'Upload-Offset': '4'});
				return settle();
			}).then(function () {
				assert.equal(last().method, 'PATCH');
				assert.equal(last().headers['Upload-Offset'], '4');
				assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['uploading', 'waiting']);
			});
		});

		test('cancels the uploads instead of resuming them when the user says so', assert => {
			const field = newField({retryDelays: [0], probeDelays: [0]});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return loseConnection().then(function () {
				items(field)[0].querySelector('.wicket-resumable-upload-cancel').click();
				assert.equal(last().method, 'DELETE');
				assert.equal(last().url.split('?')[0], abs('/uploads/u1'));
				assert.deepEqual(reports.map(report => report.ep), [{'error': true, 'errorMessage': 'upload.canceled'}]);
				assert.equal(items(field).length, 0);
				last().respond(204);
			});
		});

		test('goes on by itself once the server answers again when it does not ask', assert => {
			const field = newField({retryDelays: [0], probeDelays: [0], askingToResume: false});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return loseConnection().then(function () {
				assert.equal(last().method, 'HEAD');
				assert.true(last().running);
				assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['uploading']);
			});
		});

		test('lets the page ask the user instead of its lists', assert => {
			const questions = [];
			const field = newField({retryDelays: [0], probeDelays: [0]}, undefined, function (question) {
				questions.push(question);
				return true;
			});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return loseConnection().then(function () {
				assert.equal(questions.length, 1);
				assert.equal(questions[0].reason, 'reconnected');
				assert.deepEqual(questions[0].files, [{'name': 'a.txt', 'size': 10, 'sent': 0}]);
				assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['interrupted'], 'no question in the list');

				questions[0].resume();
				assert.equal(last().method, 'HEAD');
				assert.true(last().running);
			});
		});

		test('offers to go on with the uploads of an earlier page, once the user chooses their files again', assert => {
			const id = 'resumable' + (++inputs);
			rememberUpload(id, '/uploads/u1', 'a.txt', 10);
			rememberUpload('another-field', '/uploads/u2', 'b.txt', 10);
			const field = newField({remembering: true}, id);
			assert.equal(FakeXhr.instances.length, 1, 'only the uploads of the field');
			assert.equal(last().method, 'HEAD');
			assert.equal(last().url.split('?')[0], '/uploads/u1');
			last().respond(200, {'Upload-Offset': '4', 'Upload-Length': '10'});
			return until(() => items(field).length === 2).then(function () {
				const question = items(field)[0];
				const item = items(field)[1];
				assert.equal(question.getAttribute('data-reason'), 'recalled');
				assert.equal(question.querySelector('.wicket-resumable-upload-question-text').textContent, 'Earlier uploads. Continue?');
				assert.equal(question.querySelector('.wicket-resumable-upload-resume').textContent, 'Continue');
				assert.equal(item.getAttribute('data-state'), 'recalled');
				assert.equal(item.querySelector('.wicket-resumable-upload-status').textContent, 'Stopped at 40%');
				assert.equal(item.querySelector('progress').value, 4);

				let opened = 0;
				field.input.click = function () {
					opened++;
				};
				question.querySelector('.wicket-resumable-upload-resume').click();
				assert.equal(opened, 1, 'opens the dialog to choose the files again');

				field.choose([newFile('a.txt', 10)]);
				assert.true(field.upload(), 'nothing more to send');
				assert.equal(reports.length, 0);
				assert.equal(last().method, 'HEAD');
				last().respond(200, {'Upload-Offset': '4'});
				return settle();
			}).then(function () {
				assert.equal(last().method, 'PATCH');
				assert.equal(last().headers['Upload-Offset'], '4');
				assert.deepEqual(items(field).map(item => item.getAttribute('data-state')), ['uploading'], 'answered');
			});
		});

		test('offers the uploads the server keeps for the field, and goes on with one once its file is chosen again', assert => {
			const url = new window.URL('/uploads/u7', document.baseURI).href;
			const field = newField({remembering: true, recalled: [
				{id: 'u7', name: 'a.txt', size: 10, lastModified: 1000, type: 'text/plain', sent: 4}]});
			assert.equal(FakeXhr.instances.length, 1);
			assert.equal(last().method, 'HEAD');
			assert.equal(last().url.split('?')[0], url);
			last().respond(200, {'Upload-Offset': '4', 'Upload-Length': '10'});
			return until(() => items(field).length === 2).then(function () {
				assert.equal(items(field)[0].getAttribute('data-reason'), 'recalled');
				assert.equal(items(field)[1].getAttribute('data-state'), 'recalled');
				field.choose([newFile('a.txt', 10)]);
				assert.equal(last().method, 'HEAD');
				last().respond(200, {'Upload-Offset': '4'});
				return settle();
			}).then(function () {
				assert.equal(last().method, 'PATCH');
				assert.equal(last().url.split('?')[0], url);
				assert.equal(last().headers['Upload-Offset'], '4');
			});
		});

		test('asks once of an upload the browser remembers and the server offers, and takes a date the server does not know', assert => {
			const id = 'resumable' + (++inputs);
			rememberUpload(id, '/uploads/u1', 'a.txt', 10);
			const field = newField({remembering: true, recalled: [
				{id: 'u1', name: 'a.txt', size: 10, lastModified: 1000, type: 'text/plain', sent: 4},
				{id: 'u2', name: 'b.txt', size: 10, lastModified: 0, type: 'text/plain', sent: 2}]}, id);
			assert.deepEqual(FakeXhr.instances.map(xhr => xhr.url.split('?')[0]),
				['/uploads/u1', abs('/uploads/u2')], 'one question for each upload');
			FakeXhr.instances[0].respond(200, {'Upload-Offset': '4'});
			FakeXhr.instances[1].respond(200, {'Upload-Offset': '2'});
			return until(() => items(field).length === 3).then(function () {
				field.choose([new File([new Uint8Array(10)], 'b.txt', {type: 'text/plain', lastModified: 5000})]);
				assert.equal(last().method, 'HEAD', 'the file of any date, the server not knowing it');
				assert.equal(last().url.split('?')[0], new window.URL('/uploads/u2', document.baseURI).href);
			});
		});

		test('sends the context of the field, and the id of the session along with an upload the server offers', assert => {
			newField({remembering: true, uploadContext: 'videos', resourceUrl: '/uploads;jsessionid=abc', recalled: [
				{id: 'u7', name: 'a.txt', size: 10, lastModified: 1000, type: 'text/plain', sent: 4}]});
			assert.equal(last().method, 'HEAD');
			assert.equal(last().url.split('?')[0], abs('/uploads/u7') + ';jsessionid=abc');
			assert.true(last().url.endsWith('&grantExpires=1767225600000&uploadContext=videos'), last().url);
		});

		test('keeps the address of an upload without the id of the session, and adds it as it sends a request', assert => {
			const field = newField({remembering: true, resourceUrl: '/uploads;jsessionid=abc'});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			assert.equal(last().url.split('?')[0], '/uploads;jsessionid=abc');
			last().respond(201, {'Location': 'uploads/u1;jsessionid=abc', 'Upload-Offset': '0'});
			return settle().then(function () {
				assert.equal(last().method, 'PATCH');
				assert.equal(last().url.split('?')[0], abs('/uploads/u1') + ';jsessionid=abc',
					'the address of the endpoint, whatever path the server sees');
				assert.equal(JSON.parse(window.localStorage.getItem(remembered()[0])).url, abs('/uploads/u1'),
					'no session remembered');
			});
		});

		test('takes a refusal naming its error as final, though its status is one tried again', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				last().respond(500, {'Wicket-Upload-Error': 'uploadFailed'});
				return settle();
			}).then(function () {
				const requests = FakeXhr.instances.length;
				assert.deepEqual(calls, ['error:uploadFailed']);
				assert.equal(items(field)[0].getAttribute('data-state'), 'failed');
				return settle().then(function () {
					assert.equal(FakeXhr.instances.length, requests, 'not sent again');
				});
			});
		});

		test('starts an upload over at most twice after the server lost it, until the user resumes it', assert => {
			const field = newField({retryDelays: [0, 0, 0, 0, 0, 0, 0, 0, 0]});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			let posts = 1;
			const loseAgain = function () {
				// the chunk gets no answer, and the server no longer has the upload
				last().fail();
				return until(() => last().method === 'HEAD' && last().running).then(function () {
					last().respond(404);
					return settle();
				});
			};
			return created('/uploads/u1').then(loseAgain).then(function () {
				assert.equal(last().method, 'POST', 'started over');
				posts++;
				return created('/uploads/u2');
			}).then(loseAgain).then(function () {
				assert.equal(last().method, 'POST', 'started over again');
				posts++;
				return created('/uploads/u3');
			}).then(loseAgain).then(function () {
				assert.equal(posts, 3);
				assert.equal(last().method, 'HEAD', 'not started over a third time');
				assert.deepEqual(calls, ['error:resumableUploadLost']);
				assert.equal(items(field)[0].getAttribute('data-state'), 'failed');

				items(field)[0].querySelector('.wicket-resumable-upload-resume').click();
				assert.equal(last().method, 'POST', 'the user resumed it');
			});
		});

		test('deletes a canceled upload again while the server is busy with it', assert => {
			const field = newField();
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			return created('/uploads/u1').then(function () {
				field.cancel();
				assert.equal(last().method, 'DELETE');
				last().respond(423);
				return until(() => last().running);
			}).then(function () {
				assert.equal(last().method, 'DELETE', 'tried again');
				assert.equal(last().url.split('?')[0], abs('/uploads/u1'));
				const requests = FakeXhr.instances.length;
				last().respond(204);
				return settle().then(function () {
					assert.equal(FakeXhr.instances.length, requests, 'done');
				});
			});
		});

		test('goes on with an upload remembered for its context, and not with that of another context or place', assert => {
			rememberUpload('another-place', '/uploads/u1', 'a.txt', 10, 0, 'videos');
			rememberUpload('another-place', '/uploads/u2', 'b.txt', 10);
			const videos = newField({remembering: true, recalling: false, uploadContext: 'videos'});
			choose(videos, [newFile('a.txt', 10)]);
			videos.upload();
			assert.equal(last().method, 'HEAD', 'of its context, from another place');
			assert.equal(last().url.split('?')[0], '/uploads/u1');

			const attachments = newField({remembering: true, recalling: false, uploadContext: 'attachments'});
			choose(attachments, [newFile('a.txt', 10)]);
			attachments.upload();
			assert.equal(last().method, 'POST', 'of another context');

			const plain = newField({remembering: true, recalling: false});
			choose(plain, [newFile('b.txt', 10)]);
			plain.upload();
			assert.equal(last().method, 'POST', 'of another place');
		});

		test('overrides the method of its chunks and deletions when told to', assert => {
			const field = newField({overridingMethod: true});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			assert.equal(last().method, 'POST');
			assert.equal(last().headers['X-HTTP-Method-Override'], undefined, 'a creation is a POST');
			return created('/uploads/u1').then(function () {
				assert.equal(last().method, 'POST');
				assert.equal(last().headers['X-HTTP-Method-Override'], 'PATCH');
				assert.equal(last().headers['Upload-Offset'], '0');
				field.cancel();
				assert.equal(last().method, 'POST');
				assert.equal(last().headers['X-HTTP-Method-Override'], 'DELETE');
				last().respond(204);
			});
		});

		test('forgets an upload the server offered once it no longer has it', assert => {
			const field = newField({remembering: true, recalled: [
				{id: 'u7', name: 'a.txt', size: 10, lastModified: 1000, type: 'text/plain', sent: 4}]});
			last().respond(404);
			return settle().then(function () {
				assert.equal(items(field).length, 0);
			});
		});

		test('discards the uploads of an earlier page when the user says so', assert => {
			const id = 'resumable' + (++inputs);
			const key = rememberUpload(id, '/uploads/u1', 'a.txt', 10);
			const field = newField({remembering: true}, id);
			last().respond(200, {'Upload-Offset': '4'});
			return until(() => items(field).length === 2).then(function () {
				items(field)[0].querySelector('.wicket-resumable-upload-cancel').click();
				assert.equal(last().method, 'DELETE');
				assert.equal(last().url.split('?')[0], '/uploads/u1');
				assert.equal(window.localStorage.getItem(key), null);
				assert.equal(items(field).length, 0);
				last().respond(204);
			});
		});

		test('forgets the uploads of an earlier page the server no longer has, and leaves those another page sends', assert => {
			const id = 'resumable' + (++inputs);
			const lost = rememberUpload(id, '/uploads/u1', 'a.txt', 10);
			const sending = rememberUpload(id, '/uploads/u2', 'b.txt', 10, Date.now());
			const field = newField({remembering: true}, id);
			assert.equal(FakeXhr.instances.length, 1);
			last().respond(404);
			return settle().then(function () {
				assert.equal(window.localStorage.getItem(lost), null);
				assert.notEqual(window.localStorage.getItem(sending), null);
				assert.equal(items(field).length, 0);
			});
		});

		test('lists no uploads of an earlier page when not recalling, and goes on with a file chosen again', assert => {
			const id = 'resumable' + (++inputs);
			rememberUpload(id, '/uploads/u1', 'a.txt', 10);
			const field = newField({remembering: true, recalling: false}, id);
			assert.equal(FakeXhr.instances.length, 0, 'nothing asked');
			assert.equal(items(field).length, 0);
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			assert.equal(last().method, 'HEAD');
			assert.equal(last().url.split('?')[0], '/uploads/u1', 'remembered all the same');
		});

		test('takes an upload sent a while ago for in use by another page for as long as told', assert => {
			const id = 'resumable' + (++inputs);
			rememberUpload(id, '/uploads/u1', 'a.txt', 10, Date.now() - 30000);
			newField({remembering: true}, id);
			assert.equal(FakeXhr.instances.length, 1, 'no longer in use after 20 seconds');
			const other = 'resumable' + (++inputs);
			rememberUpload(other, '/uploads/u2', 'b.txt', 10, Date.now() - 30000);
			newField({remembering: true, inUseMillis: 60000}, other);
			assert.equal(FakeXhr.instances.length, 1, 'still in use within a minute');
		});

		test('gives a request without an answer up after the stall timeout', assert => {
			const field = newField({stallMillis: 20, retryDelays: [0]});
			choose(field, [newFile('a.txt', 10)]);
			field.upload();
			const create = last();
			return until(() => create.aborted).then(function () {
				assert.true(create.aborted, 'given up');
				return until(() => last() !== create);
			}).then(function () {
				assert.equal(last().method, 'POST', 'tried again');
			});
		});
	});
})();
