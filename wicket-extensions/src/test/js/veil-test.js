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

/*global QUnit: true */

Wicket.Event.add(window, 'domready', function() {
	"use strict";

	const { module, test } = QUnit;

	const OPTIONS = { delay: 300, minimum: 500 };

	let realClock;
	let clock;

	function fakeClock() {
		let now = 0;
		let nextId = 1;
		let timers = [];

		return {
			now: function() {
				return now;
			},
			setTimeout: function(fn, delay) {
				const id = nextId++;
				timers.push({ id: id, at: now + delay, fn: fn });
				return id;
			},
			clearTimeout: function(id) {
				timers = timers.filter(function(timer) {
					return timer.id !== id;
				});
			},
			tick: function(ms) {
				const until = now + ms;
				for (;;) {
					const due = timers.filter(function(timer) {
						return timer.at <= until;
					}).sort(function(a, b) {
						return a.at - b.at;
					})[0];
					if (!due) {
						break;
					}
					timers.splice(timers.indexOf(due), 1);
					now = due.at;
					due.fn();
				}
				now = until;
			}
		};
	}

	function send(attrs) {
		Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_BEFORE_SEND, attrs, {}, {});
		return attrs;
	}

	function done(attrs) {
		Wicket.Event.publish(Wicket.Event.Topic.AJAX_CALL_DONE, attrs);
	}

	function push(message) {
		Wicket.Event.publish('/websocket/message', message);
	}

	function veils() {
		return document.querySelectorAll('.wicket-veil');
	}

	function veilOf(element) {
		return Array.prototype.filter.call(element.children, function(child) {
			return child.classList.contains('wicket-veil');
		})[0];
	}

	function isBusy(veil) {
		return veil.classList.contains('wicket-veil-busy');
	}

	module("Wicket.Veil", {
		beforeEach: function() {
			Wicket.Veil._reset();
			realClock = Wicket.Veil._clock;
			clock = fakeClock();
			Wicket.Veil._clock = clock;
		},
		afterEach: function() {
			Wicket.Veil._reset();
			Wicket.Veil._clock = realClock;
		}
	});

	test("without a registered veil a request is not veiled", assert => {
		const attrs = send({ c: 'veilPageLink' });

		assert.equal(veils().length, 0, "a veil appeared although none is registered");
		done(attrs);
	});

	test("the page veil appears at once, transparent, and gets the spinner after the delay", assert => {
		Wicket.Veil.page(OPTIONS);

		const attrs = send({ c: 'veilPageLink' });
		const veil = veilOf(document.body);
		assert.ok(veil, "the page veil was not appended to the body");
		assert.notOk(isBusy(veil), "the spinner showed before the delay");

		clock.tick(299);
		assert.notOk(isBusy(veil), "the spinner showed before the delay");

		clock.tick(1);
		assert.ok(isBusy(veil), "the spinner did not show after the delay");

		done(attrs);
	});

	test("a request finishing before the delay removes the veil at once, without a spinner", assert => {
		Wicket.Veil.page(OPTIONS);

		const attrs = send({ c: 'veilPageLink' });
		const veil = veilOf(document.body);
		clock.tick(100);
		done(attrs);

		assert.equal(veils().length, 0, "the veil stayed after the request finished");
		clock.tick(1000);
		assert.notOk(isBusy(veil), "the spinner showed after the request finished");
	});

	test("once shown, the spinner stays for the minimum time", assert => {
		Wicket.Veil.page(OPTIONS);

		const attrs = send({ c: 'veilPageLink' });
		clock.tick(350);
		done(attrs);

		assert.equal(veils().length, 1, "the spinner was removed before its minimum time");
		clock.tick(449);
		assert.equal(veils().length, 1, "the spinner was removed before its minimum time");
		clock.tick(1);
		assert.equal(veils().length, 0, "the spinner stayed beyond its minimum time");
	});

	test("a spinner shown longer than the minimum time is removed at once", assert => {
		Wicket.Veil.page(OPTIONS);

		const attrs = send({ c: 'veilPageLink' });
		clock.tick(900);
		done(attrs);

		assert.equal(veils().length, 0, "the veil stayed after the request finished");
	});

	test("the configured timings are honoured", assert => {
		Wicket.Veil.page({ delay: 50, minimum: 1000 });

		const attrs = send({ c: 'veilPageLink' });
		clock.tick(50);
		assert.ok(isBusy(veilOf(document.body)), "the spinner did not show after the delay");

		done(attrs);
		clock.tick(999);
		assert.equal(veils().length, 1, "the spinner was removed before its minimum time");
		clock.tick(1);
		assert.equal(veils().length, 0, "the spinner stayed beyond its minimum time");
	});

	test("concurrent requests share one veil, removed when the last one finishes", assert => {
		Wicket.Veil.page(OPTIONS);

		const first = send({ c: 'veilPageLink' });
		const second = send({ c: 'veilPageLink' });
		assert.equal(veils().length, 1, "each request got its own veil");

		done(first);
		assert.equal(veils().length, 1, "the veil was removed while a request was still running");

		done(second);
		assert.equal(veils().length, 0, "the veil stayed after the last request finished");
	});

	test("a request during the minimum time keeps the spinner up", assert => {
		Wicket.Veil.page(OPTIONS);

		const first = send({ c: 'veilPageLink' });
		clock.tick(400);
		done(first);
		const veil = veilOf(document.body);

		const second = send({ c: 'veilPageLink' });
		clock.tick(1000);
		assert.equal(veilOf(document.body), veil, "the running spinner was replaced");
		assert.ok(isBusy(veil), "the running spinner was hidden");

		done(second);
		assert.equal(veils().length, 0, "the veil stayed after the last request finished");
	});

	test("a request with the wicket_nb extra parameter is not veiled", assert => {
		Wicket.Veil.page(OPTIONS);
		Wicket.Veil.local('veilOuter', OPTIONS);

		const pageRequest = send({ c: 'veilPageLink', ep: [{ name: 'wicket_nb', value: 'true' }] });
		const localRequest = send({ c: 'veilOuterLink', ep: { wicket_nb: 'true' } });
		assert.equal(veils().length, 0, "an opted-out request was veiled");

		done(pageRequest);
		done(localRequest);
		assert.equal(veils().length, 0, "finishing an opted-out request added a veil");
	});

	test("a request from inside a local veil veils only that component", assert => {
		Wicket.Veil.page(OPTIONS);
		Wicket.Veil.local('veilOuter', OPTIONS);
		const outer = document.getElementById('veilOuter');

		const attrs = send({ c: 'veilOuterLink' });
		assert.ok(veilOf(outer), "the local veil was not appended to its component");
		assert.ok(outer.classList.contains('wicket-veil-host'), "the component is not marked as host");
		assert.notOk(veilOf(document.body), "the page was veiled too");
		assert.equal(veils().length, 1, "more than one veil appeared");

		done(attrs);
		assert.equal(veils().length, 0, "the local veil stayed after the request finished");
		assert.notOk(outer.classList.contains('wicket-veil-host'), "the host class stayed");
	});

	test("a local veil shows the spinner after its delay and keeps it for its minimum time", assert => {
		Wicket.Veil.local('veilOuter', OPTIONS);
		const outer = document.getElementById('veilOuter');

		const attrs = send({ c: 'veilOuterLink' });
		const veil = veilOf(outer);
		clock.tick(299);
		assert.notOk(isBusy(veil), "the spinner showed before the delay");
		clock.tick(1);
		assert.ok(isBusy(veil), "the spinner did not show after the delay");

		done(attrs);
		clock.tick(499);
		assert.ok(veilOf(outer), "the spinner was removed before its minimum time");
		clock.tick(1);
		assert.notOk(veilOf(outer), "the spinner stayed beyond its minimum time");
	});

	test("nested local veils keep their own timings", assert => {
		Wicket.Veil.local('veilOuter', OPTIONS);
		Wicket.Veil.local('veilInner', { delay: 0, minimum: 1000 });
		const outer = document.getElementById('veilOuter');
		const inner = document.getElementById('veilInner');

		const innerRequest = send({ c: 'veilInnerLink' });
		clock.tick(0);
		assert.ok(isBusy(veilOf(inner)), "the inner spinner did not show at once");
		done(innerRequest);
		clock.tick(999);
		assert.ok(veilOf(inner), "the inner spinner was removed before its minimum time");
		clock.tick(1);
		assert.notOk(veilOf(inner), "the inner spinner stayed beyond its minimum time");

		const outerRequest = send({ c: 'veilOuterLink' });
		clock.tick(299);
		assert.notOk(isBusy(veilOf(outer)), "the outer spinner used the inner delay");
		clock.tick(1);
		assert.ok(isBusy(veilOf(outer)), "the outer spinner did not show after its delay");
		done(outerRequest);
	});

	test("nested local veils: the innermost takes the request", assert => {
		Wicket.Veil.local('veilOuter', OPTIONS);
		Wicket.Veil.local('veilInner', OPTIONS);

		const attrs = send({ c: 'veilInnerLink' });
		assert.ok(veilOf(document.getElementById('veilInner')), "the inner component was not veiled");
		assert.equal(veils().length, 1, "more than the innermost component was veiled");

		done(attrs);
	});

	test("a request from outside a local veil falls back to the page veil", assert => {
		Wicket.Veil.page(OPTIONS);
		Wicket.Veil.local('veilInner', OPTIONS);

		const attrs = send({ c: 'veilOuterLink' });
		assert.ok(veilOf(document.body), "the page was not veiled");
		assert.notOk(veilOf(document.getElementById('veilInner')), "an unrelated component was veiled");

		done(attrs);
	});

	test("a component replaced by an Ajax update is veiled again once re-registered", assert => {
		Wicket.Veil.local('veilOuter', OPTIONS);
		const old = document.getElementById('veilOuter');
		const replacement = old.cloneNode(true);
		old.parentNode.replaceChild(replacement, old);
		Wicket.Veil.local('veilOuter', OPTIONS);

		const attrs = send({ c: 'veilOuterLink' });
		assert.ok(veilOf(replacement), "the replacement component was not veiled");

		done(attrs);
		assert.equal(veils().length, 0, "the local veil stayed after the request finished");
	});

	test("a pushed veil message raises a local veil, with its timings, until it is hidden", assert => {
		Wicket.Veil.local('veilOuter', OPTIONS);
		const outer = document.getElementById('veilOuter');

		push('{"wicketVeil":"show","id":"veilOuter"}');
		const veil = veilOf(outer);
		assert.ok(veil, "the push did not raise the local veil");
		assert.ok(outer.classList.contains('wicket-veil-host'), "the component is not marked as host");
		clock.tick(299);
		assert.notOk(isBusy(veil), "the spinner showed before the delay");
		clock.tick(1);
		assert.ok(isBusy(veil), "the spinner did not show after the delay");

		Wicket.Veil.hide('veilOuter');
		clock.tick(499);
		assert.ok(veilOf(outer), "the spinner was removed before its minimum time");
		clock.tick(1);
		assert.equal(veils().length, 0, "the veil stayed after it was hidden");
	});

	test("a pushed unveil message lowers the veil", assert => {
		Wicket.Veil.local('veilOuter', OPTIONS);

		push('{"wicketVeil":"show","id":"veilOuter"}');
		push('{"wicketVeil":"hide","id":"veilOuter"}');

		assert.equal(veils().length, 0, "the unveil message did not lower the veil");
	});

	test("a veil whose component is replaced by the pushed update stays for the spinner's minimum time", assert => {
		Wicket.Veil.local('veilOuter', OPTIONS);

		push('{"wicketVeil":"show","id":"veilOuter"}');
		clock.tick(400);
		const old = document.getElementById('veilOuter');
		const replacement = old.cloneNode(false);
		replacement.className = '';
		old.parentNode.replaceChild(replacement, old);
		Wicket.Veil.local('veilOuter', OPTIONS);
		Wicket.Veil.hide('veilOuter');

		const veil = veilOf(replacement);
		assert.ok(veil, "the veil did not move onto the new element");
		assert.ok(isBusy(veil), "the moved veil lost its spinner");
		assert.ok(replacement.classList.contains('wicket-veil-host'), "the new element is not marked as host");
		clock.tick(399);
		assert.ok(veilOf(replacement), "the spinner was removed before its minimum time");
		clock.tick(1);
		assert.equal(veils().length, 0, "the veil stayed beyond the spinner's minimum time");
		assert.notOk(replacement.classList.contains('wicket-veil-host'), "the host class stayed");
	});

	test("other WebSocket messages, unknown ids and unmatched hides are ignored", assert => {
		Wicket.Veil.page(OPTIONS);
		Wicket.Veil.local('veilOuter', OPTIONS);

		push('hello');
		push('{"wicketVeil":');
		push('{"wicketVeil":"show","id":"veilGone"}');
		Wicket.Veil.hide('veilOuter');
		assert.equal(veils().length, 0, "a veil appeared");

		const attrs = send({ c: 'veilOuterLink' });
		assert.ok(veilOf(document.getElementById('veilOuter')),
			"an unmatched hide broke the next request's veil");
		done(attrs);
	});
});
