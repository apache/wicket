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

import { RichTextEditor } from '@mantine/tiptap';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { cspNonce, useFormState, useFormValue } from 'wicket-islands-react';
import BoardTheme from './BoardTheme';
import '@mantine/tiptap/styles.css';
import './FormFields.css';

/**
 * The description editor of RichTextField.java. Its value is the editor's HTML, which the server
 * sanitizes: nothing the browser sends is trusted.
 */
export default function RichTextField() {
	const [value, setValue] = useFormValue<string>();
	const { enabled, errors } = useFormState();
	const editor = useEditor({
		extensions: [StarterKit.configure({ link: { openOnClick: false } })],
		content: value ?? '',
		editable: enabled,
		injectNonce: cspNonce(),
		onUpdate: ({ editor }) => setValue(editor.isEmpty ? '' : editor.getHTML())
	});

	return (
		<BoardTheme>
			<div className="rich-text">
				<RichTextEditor editor={editor}>
					<RichTextEditor.Toolbar>
						<RichTextEditor.ControlsGroup>
							<RichTextEditor.Bold />
							<RichTextEditor.Italic />
							<RichTextEditor.Underline />
							<RichTextEditor.Code />
						</RichTextEditor.ControlsGroup>
						<RichTextEditor.ControlsGroup>
							<RichTextEditor.BulletList />
							<RichTextEditor.OrderedList />
							<RichTextEditor.Blockquote />
						</RichTextEditor.ControlsGroup>
						<RichTextEditor.ControlsGroup>
							<RichTextEditor.Link popoverProps={{ withinPortal: false }} />
							<RichTextEditor.Unlink />
						</RichTextEditor.ControlsGroup>
					</RichTextEditor.Toolbar>
					<RichTextEditor.Content />
				</RichTextEditor>
				{errors.length > 0 && (
					<ul className="field-errors">
						{errors.map(error => <li key={error}>{error}</li>)}
					</ul>
				)}
			</div>
		</BoardTheme>
	);
}
