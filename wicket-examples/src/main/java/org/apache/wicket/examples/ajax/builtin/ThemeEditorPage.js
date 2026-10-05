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
 * The theme editor of the examples: reads the theme properties the preview gets from its theme
 * into the editor's fields, sets the edited ones on the preview through the CSS object model, so
 * no inline style is needed, and writes the CSS class defining the theme.
 */
;(function () {
	'use strict';

	const HEX = /^#([0-9a-f])([0-9a-f])([0-9a-f])$/i;

	Wicket.ThemeEditor = {

		init: function (cfg) {
			const self = this;
			this.cfg = cfg;
			const editor = document.getElementById(cfg.editor);
			['input', 'change'].forEach(function (type) {
				editor.addEventListener(type, function (event) {
					if (event.target.hasAttribute('data-theme-property')) {
						self.apply(event.target);
					}
				});
			});
			document.querySelector('.theme-editor-class').addEventListener('input', function () {
				self.update();
			});
			editor.querySelector('.theme-editor-copy').addEventListener('click', function (event) {
				Wicket.Clipboard.copy(editor.querySelector('.theme-editor-css').value,
					event.currentTarget);
			});
			this.load();
		},

		fields: function () {
			return Array.prototype.slice.call(document.getElementById(this.cfg.editor)
				.querySelectorAll('[data-theme-property]'));
		},

		preview: function () {
			return document.getElementById(this.cfg.preview);
		},

		/**
		 * Drops the edits and reads the values of the preview's theme into the fields.
		 */
		load: function () {
			const preview = this.preview();
			const fields = this.fields();
			fields.forEach(function (field) {
				preview.style.removeProperty(field.getAttribute('data-theme-property'));
			});
			const style = window.getComputedStyle(preview);
			fields.forEach(function (field) {
				const value = style.getPropertyValue(field.getAttribute('data-theme-property')).trim();
				field.value = field.type === 'color' ? value.replace(HEX, '#$1$1$2$2$3$3') : value;
			});
			this.update();
		},

		apply: function (field) {
			this.preview().style.setProperty(field.getAttribute('data-theme-property'), field.value);
			this.update();
		},

		/**
		 * @return the CSS class defining the edited theme
		 */
		css: function () {
			const name = document.querySelector('.theme-editor-class').value.trim() || 'my-theme';
			return '/* put the classes "wicket-theme ' + name + '" on a page or a component */\n' +
				'.' + name + ' {\n' + this.fields().map(function (field) {
				return '\t' + field.getAttribute('data-theme-property') + ': ' + field.value + ';';
			}).join('\n') + '\n}\n';
		},

		update: function () {
			document.getElementById(this.cfg.editor).querySelector('.theme-editor-css').value =
				this.css();
		}
	};
})();
