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

/*global QUnit: true, KeyboardEvent: true */

;(function (undefined) {
	"use strict";

	Wicket.Event.add(window, 'domready', function() {
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

		function add(parentId, html) {
			const parent = document.getElementById(parentId);
			parent.insertAdjacentHTML('beforeend', html);
			return parent.lastElementChild;
		}

		function keyEvent(element, type, name, init) {
			let reached = false;
			const listener = function() {
				reached = true;
			};
			element.addEventListener(type, listener);
			const options = Object.assign(
				{ key: name, bubbles: true, cancelable: true, composed: true }, init);
			const event = new KeyboardEvent(type, options);
			element.dispatchEvent(event);
			element.removeEventListener(type, listener);
			return { reached: reached, prevented: event.defaultPrevented };
		}

		function press(element, name, init) {
			return {
				down: keyEvent(element, 'keydown', name, init),
				up: keyEvent(element, 'keyup', name, init)
			};
		}

		function onStage(html) {
			const stage = add('qunit-fixture', '<div></div>');
			stage.style.cssText = 'position: fixed; top: 0; left: 0; z-index: 1;';
			stage.innerHTML = html;
			return stage.firstElementChild;
		}

		function mouse(element, type, at) {
			let point = at;
			if (at instanceof Element) {
				const box = at.getBoundingClientRect();
				point = { clientX: box.left + box.width / 2, clientY: box.top + box.height / 2 };
			}
			element.dispatchEvent(new MouseEvent(type, {
				bubbles: true,
				cancelable: true,
				button: 0,
				clientX: point.clientX,
				clientY: point.clientY
			}));
		}

		function countClicks(element) {
			const counter = { count: 0 };
			element.addEventListener('click', function() {
				counter.count++;
			});
			return counter;
		}

		const PASS = { reached: true, prevented: false };
		const DROP = { reached: false, prevented: true };
		const PASSED = { down: PASS, up: PASS };
		const DROPPED = { down: DROP, up: DROP };

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
			assert.notOk(outer.classList.contains('wicket-veil-host-static'), "the static host class stayed");
		});

		test("a static host is positioned and isolated while it is veiled", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);
			const outer = document.getElementById('veilOuter');

			const attrs = send({ c: 'veilOuterLink' });
			assert.ok(outer.classList.contains('wicket-veil-host-static'), "the static host is not marked");
			assert.equal(getComputedStyle(outer).position, 'relative', "the static host is not positioned");
			assert.equal(getComputedStyle(outer).isolation, 'isolate', "the host is not isolated");

			done(attrs);
			assert.equal(getComputedStyle(outer).position, 'static', "the host stayed positioned");
		});

		test("a host positioned by a stylesheet rule keeps its position", assert => {
			const rule = document.createElement('style');
			rule.textContent = '.veil-test-absolute { position: absolute; }';
			document.head.insertBefore(rule, document.head.firstChild);
			Wicket.Veil.local('veilOuter', OPTIONS);
			const outer = document.getElementById('veilOuter');
			outer.classList.add('veil-test-absolute');

			const attrs = send({ c: 'veilOuterLink' });
			assert.notOk(outer.classList.contains('wicket-veil-host-static'), "the positioned host was marked static");
			assert.equal(getComputedStyle(outer).position, 'absolute', "the host lost its position");

			done(attrs);
			rule.remove();
		});

		test("a local veil covers the visible part of a scrolled host", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);
			const outer = document.getElementById('veilOuter');
			outer.style.height = '40px';
			outer.style.overflow = 'auto';
			const filler = document.createElement('div');
			filler.style.height = '400px';
			outer.appendChild(filler);
			outer.scrollTop = 100;

			const attrs = send({ c: 'veilOuterLink' });
			const veil = veilOf(outer);
			const covers = function () {
				const veilBox = veil.getBoundingClientRect();
				const hostBox = outer.getBoundingClientRect();
				return Math.round(veilBox.top) === Math.round(hostBox.top + outer.clientTop) &&
					Math.round(veilBox.height) === outer.clientHeight;
			};
			assert.ok(covers(), "the veil does not cover the visible part of the host");

			outer.scrollTop = 150;
			outer.dispatchEvent(new Event('scroll'));
			assert.ok(covers(), "the veil did not follow the host's scrolling");

			done(attrs);
			outer.scrollTop = 0;
			outer.dispatchEvent(new Event('scroll'));
			assert.equal(veils().length, 0, "the local veil stayed after the request finished");
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

		test("a delegated request is taken by the innermost veil around the element that was clicked", assert => {
			Wicket.Veil.page(OPTIONS);
			Wicket.Veil.local('veilOuter', OPTIONS);
			Wicket.Veil.local('veilInner', OPTIONS);

			const attrs = send({ c: 'veilOuter', event: { target: document.getElementById('veilInnerLink') } });
			assert.ok(veilOf(document.getElementById('veilInner')), "the veil around the clicked element was not raised");
			assert.equal(veils().length, 1, "more than the innermost veil was raised");

			done(attrs);
		});

		test("a request whose clicked element is gone is taken by the veil around its component", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);
			Wicket.Veil.local('veilInner', OPTIONS);

			const attrs = send({ c: 'veilOuterLink', event: { target: document.createElement('a') } });
			assert.ok(veilOf(document.getElementById('veilOuter')), "the component's veil was not raised");
			assert.equal(veils().length, 1, "more than the component's veil was raised");

			done(attrs);
		});

		test("markup ids naming Object members do not break the veils", assert => {
			Wicket.Veil.page(OPTIONS);
			Wicket.Veil.local('constructor', OPTIONS);
			const link = document.createElement('a');
			link.id = 'toString';
			document.getElementById('qunit-fixture').appendChild(link);

			const attrs = send({ c: 'toString' });
			assert.ok(veilOf(document.body), "the page was not veiled");
			assert.strictEqual(Object.delay, undefined, "the options were written onto Object");

			done(attrs);
			assert.equal(veils().length, 0, "the veil stayed after the request finished");
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

		test("a veil whose component is replaced during the request moves onto the new element at once", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);

			const attrs = send({ c: 'veilOuterLink' });
			clock.tick(300);
			Wicket.DOM.replace(document.getElementById('veilOuter'),
				'<div id="veilOuter"><a id="veilOuterLink" href="#outer">outer</a></div>');
			const replacement = document.getElementById('veilOuter');

			const veil = veilOf(replacement);
			assert.ok(veil, "the veil did not move onto the new element");
			assert.ok(isBusy(veil), "the moved veil lost its spinner");
			assert.ok(replacement.classList.contains('wicket-veil-host'), "the new element is not marked as host");

			done(attrs);
			clock.tick(500);
			assert.equal(veils().length, 0, "the veil stayed beyond the spinner's minimum time");
		});

		test("a pushed veil follows its component when a progress update replaces it", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);

			push('{"wicketVeil":"show","id":"veilOuter"}');
			Wicket.DOM.replace(document.getElementById('veilOuter'),
				'<div id="veilOuter"><a id="veilOuterLink" href="#outer">outer</a></div>');
			clock.tick(300);

			const veil = veilOf(document.getElementById('veilOuter'));
			assert.ok(veil, "the veil did not move onto the new element");
			assert.ok(isBusy(veil), "the spinner was not shown on the moved veil");

			Wicket.Veil.hide('veilOuter');
			clock.tick(500);
			assert.equal(veils().length, 0, "the veil stayed after it was hidden");
		});

		test("an unmatched hide leaves the veil of a running request alone", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);
			const outer = document.getElementById('veilOuter');

			const attrs = send({ c: 'veilOuterLink' });
			Wicket.Veil.hide('veilOuter');
			push('{"wicketVeil":"hide","id":"veilOuter"}');
			assert.ok(veilOf(outer), "an unmatched hide lowered the veil of a running request");

			done(attrs);
			assert.equal(veils().length, 0, "the veil stayed after the request finished");
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

		test("Enter and Space on an element under the page veil are dropped, and so is their release", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = add('qunit-fixture', '<button type="button">save</button>');
			assert.deepEqual(press(button, 'Enter'), PASSED, "Enter was dropped without a veil");
			assert.deepEqual(press(button, ' '), PASSED, "Space was dropped without a veil");

			const attrs = send({ c: 'veilPageLink' });
			assert.deepEqual(press(button, 'Enter'), DROPPED, "Enter reached the veiled button");
			assert.deepEqual(press(button, ' '), DROPPED, "Space reached the veiled button");
			assert.deepEqual(press(document.getElementById('veilPageLink'), 'Enter'), DROPPED,
				"Enter reached the veiled link");
			assert.deepEqual(press(document.body, ' '), DROPPED, "Space reached the veiled page");

			done(attrs);
			assert.deepEqual(press(button, 'Enter'), PASSED, "Enter was dropped after the veil came down");
		});

		test("other keys pass under the veil", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = add('qunit-fixture', '<button type="button">save</button>');

			const attrs = send({ c: 'veilPageLink' });
			['Tab', 'Escape', 'ArrowDown', 'a'].forEach(function(key) {
				assert.deepEqual(press(button, key), PASSED, "'" + key + "' was dropped");
			});

			done(attrs);
		});

		test("a local veil drops Enter and Space inside its host only", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);

			const attrs = send({ c: 'veilOuterLink' });
			assert.deepEqual(press(document.getElementById('veilOuterLink'), 'Enter'), DROPPED,
				"Enter reached an element of the veiled host");
			assert.deepEqual(press(document.getElementById('veilInnerLink'), ' '), DROPPED,
				"Space reached an element nested in the veiled host");
			assert.deepEqual(press(document.getElementById('veilPageLink'), 'Enter'), PASSED,
				"Enter was dropped outside the veiled host");

			done(attrs);
			assert.deepEqual(press(document.getElementById('veilOuterLink'), 'Enter'), PASSED,
				"Enter was dropped after the local veil came down");
		});

		test("the keys typing text pass under the veil", assert => {
			Wicket.Veil.page(OPTIONS);
			const text = add('qunit-fixture', '<input type="text">');
			const search = add('qunit-fixture', '<input type="search">');
			const checkbox = add('qunit-fixture', '<input type="checkbox">');
			const select = add('qunit-fixture', '<select><option>one</option></select>');
			const textarea = add('qunit-fixture', '<textarea></textarea>');
			const editable = add('qunit-fixture', '<div contenteditable="true">text</div>');
			const button = add('qunit-fixture', '<button type="button">save</button>');

			const attrs = send({ c: 'veilPageLink' });
			assert.deepEqual(press(text, ' '), PASSED, "Space was dropped in a text field");
			assert.deepEqual(press(search, ' '), PASSED, "Space was dropped in a search field");
			assert.deepEqual(press(select, ' '), PASSED, "Space was dropped in a select");
			assert.deepEqual(press(textarea, ' '), PASSED, "Space was dropped in a text area");
			assert.deepEqual(press(textarea, 'Enter'), PASSED, "Enter was dropped in a text area");
			assert.deepEqual(press(editable, 'Enter'), PASSED, "Enter was dropped in editable content");
			assert.deepEqual(press(button, 'Enter', { isComposing: true }), PASSED,
				"Enter was dropped during a composition");
			assert.deepEqual(press(button, 'Enter', { keyCode: 229 }), PASSED,
				"Enter was dropped while it committed a composition");

			assert.deepEqual(press(text, 'Enter'), DROPPED, "Enter submitted from a text field");
			assert.deepEqual(press(select, 'Enter'), DROPPED, "Enter reached a select");
			assert.deepEqual(press(checkbox, ' '), DROPPED, "Space toggled a checkbox");

			done(attrs);
		});

		test("the release of a dropped key is dropped once the veil has come down too", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = add('qunit-fixture', '<button type="button">save</button>');

			const attrs = send({ c: 'veilPageLink' });
			assert.deepEqual(keyEvent(button, 'keydown', ' '), DROP,
				"Space reached the veiled button");
			done(attrs);
			assert.deepEqual(keyEvent(button, 'keyup', ' '), DROP,
				"the release of the dropped Space went through");

			assert.deepEqual(press(button, ' '), PASSED, "the next Space was dropped");
		});

		test("a key pressed before the veil came up is released as usual", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = add('qunit-fixture', '<button type="button">save</button>');

			assert.deepEqual(keyEvent(button, 'keydown', ' '), PASS,
				"Space was dropped without a veil");
			const attrs = send({ c: 'veilPageLink' });
			assert.deepEqual(keyEvent(button, 'keyup', ' '), PASS,
				"the release of a Space pressed before the veil was dropped");

			done(attrs);
		});

		test("a key held as the veil comes up has its repeats dropped, not its release", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = add('qunit-fixture', '<button type="button">save</button>');

			assert.deepEqual(keyEvent(button, 'keydown', 'Enter'), PASS,
				"Enter was dropped without a veil");
			const attrs = send({ c: 'veilPageLink' });
			assert.deepEqual(keyEvent(button, 'keydown', 'Enter', { repeat: true }), DROP,
				"a repeat of Enter reached the veiled button");
			assert.deepEqual(keyEvent(button, 'keyup', 'Enter'), PASS,
				"the release of an Enter pressed before the veil was dropped");

			done(attrs);
		});

		test("a dropped key held as the veil comes down stays dropped until released", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = add('qunit-fixture', '<button type="button">save</button>');

			const attrs = send({ c: 'veilPageLink' });
			assert.deepEqual(keyEvent(button, 'keydown', ' '), DROP,
				"Space reached the veiled button");
			done(attrs);
			assert.deepEqual(keyEvent(button, 'keydown', ' ', { repeat: true }), DROP,
				"a repeat of the dropped Space went through");
			assert.deepEqual(keyEvent(button, 'keyup', ' '), DROP,
				"the release of the dropped Space went through");

			assert.deepEqual(press(button, ' '), PASSED, "the next Space was dropped");
		});

		test("the keys typing text in a shadow root pass under the veil", assert => {
			Wicket.Veil.page(OPTIONS);
			const host = add('qunit-fixture', '<div></div>');
			const root = host.attachShadow({ mode: 'open' });
			root.innerHTML = '<input type="text"><button type="button">save</button>';

			const attrs = send({ c: 'veilPageLink' });
			assert.deepEqual(press(root.querySelector('input'), ' '), PASSED,
				"Space was dropped in a text field inside a shadow root");
			assert.deepEqual(press(root.querySelector('button'), ' '), DROPPED,
				"Space reached a button inside a shadow root");

			done(attrs);
		});

		test("a key sent to the window passes a local veil", assert => {
			Wicket.Veil.local('veilOuter', OPTIONS);

			const attrs = send({ c: 'veilOuterLink' });
			assert.deepEqual(press(window, 'Enter'), PASSED,
				"Enter sent to the window was dropped");

			done(attrs);
		});

		test("a click whose release lands on the veil is replayed once the veil is lowered", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = onStage('<button type="button" id="veilTestSave">save</button>');
			const clicks = countClicks(button);

			mouse(button, 'mousedown', button);
			const attrs = send({ c: 'veilTestSave' });
			mouse(veilOf(document.body), 'mouseup', button);
			assert.equal(clicks.count, 0, "the click was replayed while the page was veiled");

			done(attrs);
			clock.tick(0);
			assert.equal(clicks.count, 1, "the click was not replayed once the veil was lowered");
			clock.tick(1000);
			assert.equal(clicks.count, 1, "the click was replayed more than once");
		});

		test("the click waits for the veil to be lowered after the spinner's minimum time", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = onStage('<button type="button" id="veilTestSave">save</button>');
			const clicks = countClicks(button);

			mouse(button, 'mousedown', button);
			const attrs = send({ c: 'veilTestSave' });
			clock.tick(350);
			mouse(veilOf(document.body), 'mouseup', button);
			done(attrs);
			clock.tick(449);
			assert.equal(clicks.count, 0, "the click was replayed while the spinner was shown");

			clock.tick(1);
			assert.equal(clicks.count, 1, "the click was not replayed once the veil was lowered");
		});

		test("the click waits while another request veils the element again", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = onStage('<button type="button" id="veilTestSave">save</button>');
			const clicks = countClicks(button);

			mouse(button, 'mousedown', button);
			const first = send({ c: 'veilTestSave' });
			mouse(veilOf(document.body), 'mouseup', button);
			done(first);
			const second = send({ c: 'veilPageLink' });
			clock.tick(0);
			assert.equal(clicks.count, 0, "the click was replayed under the veil of another request");

			done(second);
			clock.tick(0);
			assert.equal(clicks.count, 1, "the click was not replayed once the last veil was lowered");
		});

		test("a local veil replays the click on an element inside its host", assert => {
			const host = onStage('<div id="veilTestHost">' +
				'<button type="button" id="veilTestSave">save</button></div>');
			Wicket.Veil.local('veilTestHost', OPTIONS);
			const button = document.getElementById('veilTestSave');
			const clicks = countClicks(button);

			mouse(button, 'mousedown', button);
			const attrs = send({ c: 'veilTestSave' });
			mouse(veilOf(host), 'mouseup', button);
			done(attrs);
			clock.tick(0);

			assert.equal(clicks.count, 1, "the click was not replayed once the local veil was lowered");
		});

		test("a release away from the pressed element is not replayed on it", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = onStage('<button type="button" id="veilTestSave">save</button>');
			let clicks = 0;
			const count = function() {
				clicks++;
			};
			document.addEventListener('click', count);

			mouse(button, 'mousedown', button);
			const attrs = send({ c: 'veilTestSave' });
			const box = button.getBoundingClientRect();
			mouse(veilOf(document.body), 'mouseup',
				{ clientX: box.right + 200, clientY: box.bottom + 200 });
			done(attrs);
			clock.tick(0);
			document.removeEventListener('click', count);

			assert.equal(clicks, 0, "a click was replayed for a press dragged off the button");
		});

		test("a release that is not on a veil is left to the browser", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = onStage('<button type="button" id="veilTestSave">save</button>');
			const clicks = countClicks(button);

			mouse(button, 'mousedown', button);
			mouse(button, 'mouseup', button);
			const attrs = send({ c: 'veilTestSave' });
			done(attrs);
			clock.tick(0);

			assert.equal(clicks.count, 0, "a click was replayed for a release on the button");
		});

		test("a press on the veil leaves the click alone, a press elsewhere drops it", assert => {
			const host = onStage('<div id="veilTestHost">' +
				'<button type="button" id="veilTestSave">save</button></div>' +
				'<button type="button" id="veilTestOther">other</button>');
			Wicket.Veil.local('veilTestHost', OPTIONS);
			const button = document.getElementById('veilTestSave');
			const other = document.getElementById('veilTestOther');
			const clicks = countClicks(button);

			mouse(button, 'mousedown', button);
			let attrs = send({ c: 'veilTestSave' });
			mouse(veilOf(host), 'mouseup', button);
			mouse(veilOf(host), 'mousedown', button);
			mouse(veilOf(host), 'mouseup', button);
			done(attrs);
			clock.tick(0);
			assert.equal(clicks.count, 1, "a press on the veil dropped the click");

			mouse(button, 'mousedown', button);
			attrs = send({ c: 'veilTestSave' });
			mouse(veilOf(host), 'mouseup', button);
			mouse(other, 'mousedown', other);
			mouse(other, 'mouseup', other);
			done(attrs);
			clock.tick(0);
			assert.equal(clicks.count, 1, "the click was replayed after a new press elsewhere");
		});

		test("an element replaced while it was veiled gets the click under its id", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = onStage(
				'<button type="button" id="veilTestSave"><span>save</span></button>');

			mouse(button.firstChild, 'mousedown', button.firstChild);
			const attrs = send({ c: 'veilTestSave' });
			mouse(veilOf(document.body), 'mouseup', button.firstChild);
			Wicket.DOM.replace(button,
				'<button type="button" id="veilTestSave"><span>save</span></button>');
			const replacement = document.getElementById('veilTestSave');
			const clicks = countClicks(replacement);
			done(attrs);
			clock.tick(0);

			assert.equal(clicks.count, 1, "the element replacing the pressed one was not clicked");
		});

		test("a hidden or disabled element is not clicked", assert => {
			Wicket.Veil.page(OPTIONS);
			const button = onStage(
				'<button type="button" id="veilTestSave"><span>save</span></button>');
			const clicks = countClicks(button);

			mouse(button.firstChild, 'mousedown', button.firstChild);
			let attrs = send({ c: 'veilTestSave' });
			mouse(veilOf(document.body), 'mouseup', button.firstChild);
			button.disabled = true;
			done(attrs);
			clock.tick(0);
			assert.equal(clicks.count, 0, "the disabled button was clicked");

			button.disabled = false;
			mouse(button, 'mousedown', button);
			attrs = send({ c: 'veilTestSave' });
			mouse(veilOf(document.body), 'mouseup', button);
			button.style.display = 'none';
			done(attrs);
			clock.tick(0);
			assert.equal(clicks.count, 0, "the hidden button was clicked");
		});
	});
})();
