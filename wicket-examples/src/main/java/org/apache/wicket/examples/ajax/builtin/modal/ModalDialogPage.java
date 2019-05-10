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
package org.apache.wicket.examples.ajax.builtin.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.ajax.builtin.BasePage;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialogReferences;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;

/**
 * @author Igor Vaynberg (ivaynberg)
 */
public class ModalDialogPage extends BasePage {

	public ModalDialogPage() {

		ModalDialog dialog1 = new ModalDialog("dialog1");
		queue(dialog1);

		queue(new AjaxLink<Void>("openDialog1") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				dialog1.open(target, new ModalFragment1(ModalDialog.CONTENT_ID) {
					@Override
					protected void onClose(AjaxRequestTarget target) {
						dialog1.close(target);
					}
				});
			}
		});

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		// include default modal skin css
		response.render(CssHeaderItem.forReference(ModalDialogReferences.CSS_SKIN));
	}

	private abstract class ModalFragment1 extends Fragment {
		public ModalFragment1(String id) {
			super(id, "modalFragment1", ModalDialogPage.this);

			queue(new AjaxLink<Void>("close") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					onClose(target);
				}

			});
		}

		protected abstract void onClose(AjaxRequestTarget target);

	}

}
