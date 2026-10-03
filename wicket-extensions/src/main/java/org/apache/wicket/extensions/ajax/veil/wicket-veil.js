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
 * Veils the page, or a single component, while Ajax requests are in flight.
 *
 * The veil is transparent and only blocks the mouse. If a request is still running after the
 * target's spinner delay, the veil gets the 'wicket-veil-busy' class, which shows a spinner;
 * once shown, the spinner stays for at least the target's minimum time, so it does not flicker.
 * A request carrying the extra parameter 'wicket_nb' is never veiled.
 *
 * A local veil can also be raised by the server, for a component it is about to update through a
 * WebSocket push: a WebSocket text message {"wicketVeil":"show","id":"<markup id>"} raises it,
 * Wicket.Veil.hide(id) - evaluated after the pushed update - or {"wicketVeil":"hide",...} lowers it.
 */
;(function (undefined) {
	'use strict';

	if (typeof(Wicket.Veil) === "object") {
		return;
	}

	const NO_VEIL_PARAMETER = 'wicket_nb';
	const VEIL_CLASS = 'wicket-veil';
	const BUSY_CLASS = 'wicket-veil-busy';
	const HOST_CLASS = 'wicket-veil-host';
	const STATIC_HOST_CLASS = 'wicket-veil-host-static';
	const WEBSOCKET_MESSAGE_TOPIC = '/websocket/message';
	const MESSAGE_PREFIX = '{"wicketVeil"';

	let pageTarget = null;
	let localTargets = Object.create(null);
	let subscribed = false;

	// the veil scrolls with the content of its host, so it is moved back over the visible part
	function follow(target) {
		const style = target.veil.style;
		const top = target.host.scrollTop;
		const left = target.host.scrollLeft;
		style.top = top ? top + 'px' : '';
		style.bottom = top ? -top + 'px' : '';
		style.left = left ? left + 'px' : '';
		style.right = left ? -left + 'px' : '';
	}

	function createTarget(id, options) {
		const target = {
			id: id,
			delay: options.delay,
			minimum: options.minimum,
			count: 0,
			raised: 0,
			host: null,
			veil: null,
			shownAt: -1,
			spinnerTimer: null,
			hideTimer: null
		};
		target.onScroll = function () {
			follow(target);
		};
		return target;
	}

	function configure(target, options) {
		target.delay = options.delay;
		target.minimum = options.minimum;
	}

	function isOptedOut(attrs) {
		const ep = attrs.ep;
		if (Array.isArray(ep)) {
			return ep.some(function (parameter) {
				return parameter && parameter.name === NO_VEIL_PARAMETER;
			});
		}
		return !!ep && typeof(ep) === "object" &&
			Object.prototype.hasOwnProperty.call(ep, NO_VEIL_PARAMETER);
	}

	function findTarget(attrs) {
		let node = attrs.event && attrs.event.target;
		if (!node || !node.isConnected) {
			node = typeof(attrs.c) === "string" ? document.getElementById(attrs.c) : null;
		}
		for (; node && node !== document; node = node.parentNode) {
			const target = node.id && localTargets[node.id];
			if (target) {
				return target;
			}
		}
		return pageTarget;
	}

	function dropStaleTargets() {
		Object.keys(localTargets).forEach(function (id) {
			if (localTargets[id].count === 0 && !document.getElementById(id)) {
				delete localTargets[id];
			}
		});
	}

	function attach(target, host) {
		host.classList.add(HOST_CLASS);
		if (getComputedStyle(host).position === 'static') {
			host.classList.add(STATIC_HOST_CLASS);
		}
		host.appendChild(target.veil);
		host.addEventListener('scroll', target.onScroll);
		target.host = host;
		follow(target);
	}

	function detach(target) {
		target.host.classList.remove(HOST_CLASS, STATIC_HOST_CLASS);
		target.host.removeEventListener('scroll', target.onScroll);
	}

	function hide(target) {
		const clock = Wicket.Veil._clock;
		clock.clearTimeout(target.spinnerTimer);
		clock.clearTimeout(target.hideTimer);
		target.spinnerTimer = null;
		target.hideTimer = null;
		target.shownAt = -1;
		if (target.veil && target.veil.parentNode) {
			target.veil.parentNode.removeChild(target.veil);
		}
		if (target.host && target !== pageTarget) {
			detach(target);
		}
		target.veil = null;
		target.host = null;
	}

	function show(target) {
		const clock = Wicket.Veil._clock;
		if (target.hideTimer !== null) {
			if (document.body.contains(target.veil)) {
				// the previous request's spinner is still on its minimum time: carry on with it
				clock.clearTimeout(target.hideTimer);
				target.hideTimer = null;
				return;
			}
			hide(target);
		}

		const veil = document.createElement('div');
		veil.className = VEIL_CLASS;
		target.veil = veil;
		if (target === pageTarget) {
			document.body.appendChild(veil);
			target.host = document.body;
		} else {
			attach(target, document.getElementById(target.id));
		}

		target.spinnerTimer = clock.setTimeout(function () {
			target.spinnerTimer = null;
			veil.classList.add(BUSY_CLASS);
			target.shownAt = clock.now();
		}, target.delay);
	}

	function reattach(target) {
		if (target === pageTarget || !target.veil || document.body.contains(target.veil)) {
			return;
		}
		const host = document.getElementById(target.id);
		if (host) {
			detach(target);
			attach(target, host);
		}
	}

	function release(target) {
		const clock = Wicket.Veil._clock;
		if (target.shownAt < 0) {
			hide(target);
			return;
		}
		const remaining = target.minimum - (clock.now() - target.shownAt);
		if (remaining > 0) {
			// the update may have replaced the element, and the veil with it
			reattach(target);
			target.hideTimer = clock.setTimeout(function () {
				hide(target);
			}, remaining);
		} else {
			hide(target);
		}
	}

	function acquire(target) {
		target.count++;
		if (target.count === 1) {
			show(target);
		}
	}

	function releaseOne(target) {
		if (target.count > 0) {
			target.count--;
			if (target.count === 0) {
				release(target);
			}
		}
	}

	function lowerAll() {
		const targets = Object.keys(localTargets).map(function (id) {
			return localTargets[id];
		});
		if (pageTarget !== null) {
			targets.push(pageTarget);
		}
		targets.forEach(function (target) {
			target.count = 0;
			target.raised = 0;
			hide(target);
		});
	}

	function onBeforeSend(jqEvent, attrs) {
		if (!attrs || isOptedOut(attrs)) {
			return;
		}
		dropStaleTargets();
		const target = findTarget(attrs);
		if (target === null) {
			return;
		}
		attrs.wicketVeil = target;
		acquire(target);
	}

	function onDone(jqEvent, attrs) {
		const target = attrs && attrs.wicketVeil;
		if (!target) {
			return;
		}
		delete attrs.wicketVeil;
		releaseOne(target);
	}

	function onDomNodeAdded() {
		Object.keys(localTargets).forEach(function (id) {
			reattach(localTargets[id]);
		});
	}

	function onWebSocketMessage(jqEvent, message) {
		if (typeof(message) !== "string" || message.indexOf(MESSAGE_PREFIX) !== 0) {
			return;
		}
		let command;
		try {
			command = JSON.parse(message);
		} catch (e) {
			return;
		}
		if (command.wicketVeil === 'show') {
			Wicket.Veil.show(command.id);
		} else if (command.wicketVeil === 'hide') {
			Wicket.Veil.hide(command.id);
		}
	}

	function subscribe() {
		if (subscribed === false) {
			subscribed = true;
			Wicket.Event.subscribe(Wicket.Event.Topic.AJAX_CALL_BEFORE_SEND, onBeforeSend);
			Wicket.Event.subscribe(Wicket.Event.Topic.AJAX_CALL_DONE, onDone);
			Wicket.Event.subscribe(Wicket.Event.Topic.DOM_NODE_ADDED, onDomNodeAdded);
			Wicket.Event.subscribe(WEBSOCKET_MESSAGE_TOPIC, onWebSocketMessage);
		}
	}

	Wicket.Veil = {

		/**
		 * Veils the whole page during every Ajax request that no local veil claims.
		 *
		 * @param options {Object} - 'delay': milliseconds before the spinner shows,
		 *      'minimum': milliseconds the spinner stays once shown
		 */
		page: function (options) {
			subscribe();
			if (pageTarget === null) {
				pageTarget = createTarget(null, options);
			} else {
				configure(pageTarget, options);
			}
		},

		/**
		 * Veils only the element with the given id, during the Ajax requests fired by
		 * components inside it.
		 *
		 * @param id {String} - the markup id of the element to veil
		 * @param options {Object} - as for page()
		 */
		local: function (id, options) {
			subscribe();
			const target = localTargets[id];
			if (target) {
				configure(target, options);
			} else {
				localTargets[id] = createTarget(id, options);
			}
		},

		/**
		 * Raises the local veil registered for the given id, as an Ajax request from inside it
		 * would, with the same timings. Each call has to be matched by a call to hide().
		 *
		 * @param id {String} - the markup id of a component with a local veil
		 */
		show: function (id) {
			const target = localTargets[id];
			if (target && document.getElementById(id)) {
				target.raised++;
				acquire(target);
			}
		},

		/**
		 * Lowers the local veil raised by show(), respecting the spinner's minimum time. Calls
		 * without a matching show() are ignored, and leave the veil of a running Ajax request
		 * alone.
		 *
		 * @param id {String} - the markup id of a component with a local veil
		 */
		hide: function (id) {
			const target = localTargets[id];
			if (target && target.raised > 0) {
				target.raised--;
				releaseOne(target);
			}
		},

		_clock: {
			now: function () {
				return Date.now();
			},
			setTimeout: function (fn, delay) {
				return window.setTimeout(fn, delay);
			},
			clearTimeout: function (timer) {
				if (timer !== null) {
					window.clearTimeout(timer);
				}
			}
		},

		_reset: function () {
			lowerAll();
			pageTarget = null;
			localTargets = Object.create(null);
		}
	};
})();
