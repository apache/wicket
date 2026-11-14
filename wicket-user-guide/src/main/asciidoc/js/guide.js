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
 * The reference guide's page behaviour: the table of contents follows the section being read and
 * shows only that chapter's sections, every listing gets a bar with its language and a copy button,
 * and the places where markup binds to Java are marked in the code.
 */
(function () {
	'use strict';

	var LANGUAGES = { java: 'Java', html: 'HTML', xml: 'XML', javascript: 'JavaScript', js: 'JavaScript',
		bash: 'Shell', shell: 'Shell', properties: 'Properties', css: 'CSS', json: 'JSON' };

	function addLandmarks() {
		var content = document.getElementById('content');
		if (content) {
			content.setAttribute('role', 'main');
		}
		var toc = document.getElementById('toc');
		if (toc) {
			toc.setAttribute('role', 'navigation');
			toc.setAttribute('aria-label', 'Table of contents');
		}
	}

	function addCodeBars() {
		var status = document.createElement('div');
		status.className = 'visually-hidden';
		status.setAttribute('role', 'status');
		document.body.appendChild(status);

		document.querySelectorAll('.listingblock > .content > pre').forEach(function (pre) {
			var code = pre.querySelector('code');
			var lang = code && code.getAttribute('data-lang');
			var language = lang ? (LANGUAGES[lang] || lang) : '';
			var bar = document.createElement('div');
			bar.className = 'code-bar';

			var label = document.createElement('span');
			label.className = 'code-lang';
			label.textContent = language;
			bar.appendChild(label);

			if (navigator.clipboard) {
				var button = document.createElement('button');
				button.type = 'button';
				button.className = 'code-copy';
				button.setAttribute('aria-label', language ? 'Copy ' + language + ' code' : 'Copy code');
				button.textContent = 'Copy';
				button.addEventListener('click', function () {
					navigator.clipboard.writeText(pre.textContent).then(function () {
						button.textContent = 'Copied';
						status.textContent = 'Copied';
						setTimeout(function () {
							button.textContent = 'Copy';
							status.textContent = '';
						}, 2000);
					});
				});
				bar.appendChild(button);
			}
			pre.parentNode.insertBefore(bar, pre);
			pre.closest('.listingblock').classList.add('has-bar');
		});
	}

	function markWicketMarkup() {
		document.querySelectorAll('pre.rouge code[data-lang="html"], pre.rouge code[data-lang="xml"]')
			.forEach(function (code) {
				code.querySelectorAll('.na').forEach(function (attribute) {
					if (attribute.textContent.indexOf('wicket:') === 0) {
						attribute.classList.add('wicket-id');
					}
				});
				code.querySelectorAll('.nt').forEach(function (tag) {
					if (tag.textContent.indexOf('wicket:') >= 0) {
						tag.classList.add('wicket-tag');
					}
				});
			});
	}


	function followSections() {
		var toc = document.getElementById('toc');
		if (!toc) {
			return;
		}

		var links = Array.prototype.slice.call(toc.querySelectorAll('a[href^="#"]'));
		var entries = links.map(function (link) {
			var match = /^(\d+(?:\.\d+)*\.)\s+(.*)$/.exec(link.textContent);
			if (match) {
				link.innerHTML = '';
				var number = document.createElement('span');
				number.className = 'toc-num';
				number.textContent = match[1];
				link.appendChild(number);
				link.appendChild(document.createTextNode(match[2]));
			}

			var id = decodeURIComponent(link.getAttribute('href').substring(1));
			return {
				link: link,
				heading: document.getElementById(id)
			};
		}).filter(function (entry) {
			return entry.heading;
		});

		if (!entries.length) {
			return;
		}

		document.documentElement.classList.add('js-toc');

		var disclosures = [];
		var generatedId = 0;

		function syncDisclosureStates() {
			disclosures.forEach(function (disclosure) {
				var expanded = disclosure.list.getClientRects().length > 0;
				disclosure.button.setAttribute('aria-expanded', String(expanded));
			});
		}

		// Add a disclosure button to every chapter containing subsections.
		Array.prototype.slice.call(toc.querySelectorAll('li')).forEach(function (li) {
			var sublist = Array.prototype.slice.call(li.children).filter(function (child) {
				return child.tagName === 'UL';
			})[0];

			if (!sublist) {
				return;
			}

			if (!sublist.id) {
				do {
					generatedId++;
					sublist.id = 'toc-subsections-' + generatedId;
				} while (document.getElementById(sublist.id) !== sublist);
			}

			var link = Array.prototype.slice.call(li.children).filter(function (child) {
				return child.tagName === 'A';
			})[0];

			if (!link) {
				return;
			}

			var button = document.createElement('button');
			button.type = 'button';
			button.className = 'toc-toggle';
			button.setAttribute('aria-label', 'Sections for ' + link.textContent.trim());
			button.setAttribute('aria-controls', sublist.id);
			button.setAttribute('aria-expanded', 'false');

			link.parentNode.insertBefore(button, link.nextSibling);

			var disclosure = {
				list: sublist,
				button: button
			};

			disclosures.push(disclosure);

			button.addEventListener('click', function () {
				var expanded = button.getAttribute('aria-expanded') === 'true';

				if (expanded) {
					li.classList.remove('is-manually-open');
					li.classList.add('is-manually-closed');
				} else {
					li.classList.remove('is-manually-closed');
					li.classList.add('is-manually-open');
				}

				syncDisclosureStates();
			});
		});

		var current = null;

		function update() {
			var offset = parseFloat(
				getComputedStyle(document.documentElement).scrollPaddingTop
			) || 0;

			var line = offset + window.innerHeight / 4;
			var found = entries[0];

			for (var i = 0; i < entries.length; i++) {
				if (entries[i].heading.getBoundingClientRect().top > line) {
					break;
				}
				found = entries[i];
			}

			if (found === current) {
				syncDisclosureStates();
				return;
			}

			if (current) {
				current.link.classList.remove('is-current');
				current.link.removeAttribute('aria-current');
			}

			toc.querySelectorAll('li.is-open').forEach(function (li) {
				li.classList.remove('is-open');
			});

			current = found;
			current.link.classList.add('is-current');
			current.link.setAttribute('aria-current', 'location');

			for (
				var li = current.link.parentElement;
				li && li !== toc;
				li = li.parentElement
			) {
				if (li.tagName === 'LI') {
					li.classList.add('is-open');
				}
			}

			syncDisclosureStates();

			if (toc.scrollHeight > toc.clientHeight) {
				var linkBox = current.link.getBoundingClientRect();
				var tocBox = toc.getBoundingClientRect();

				if (
					linkBox.top < tocBox.top + 40 ||
					linkBox.bottom > tocBox.bottom - 40
				) {
					toc.scrollTop += linkBox.top - tocBox.top - tocBox.height / 3;
				}
			}
		}

		// Keep aria-expanded synchronized when keyboard focus changes.
		toc.addEventListener('focusin', syncDisclosureStates);
		toc.addEventListener('focusout', function () {
			requestAnimationFrame(syncDisclosureStates);
		});

		var pending = false;

		window.addEventListener('scroll', function () {
			if (!pending) {
				pending = true;

				requestAnimationFrame(function () {
					pending = false;
					update();
				});
			}
		}, { passive: true });

		window.addEventListener('hashchange', update);

		update();
	}


	addLandmarks();
	addCodeBars();
	markWicketMarkup();
	followSections();
})();
