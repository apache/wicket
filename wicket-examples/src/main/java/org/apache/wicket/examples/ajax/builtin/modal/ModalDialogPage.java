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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.ajax.builtin.BasePage;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
import org.apache.wicket.extensions.ajax.markup.html.repeater.AjaxListPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * @author Igor Vaynberg (ivaynberg)
 */
public class ModalDialogPage extends BasePage
{

	private AjaxListPanel stackedDialogs;

	/**
	 * Should dialogs be stacked rather than nested
	 */
	private boolean stacked = false;

	public ModalDialogPage()
	{

		RadioGroup radioGroup = new RadioGroup("stacked", new PropertyModel<>(this, "stacked"));
		radioGroup
			.setRenderBodyOnly(false)
			.add(new AjaxFormChoiceComponentUpdatingBehavior() {
				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
				}
			});
		add(radioGroup);

		radioGroup.add(new Radio<Boolean>("yes", Model.of(true)));
		radioGroup.add(new Radio<Boolean>("no", Model.of(false)));

		add(new ModalFragment("start"));

		stackedDialogs = new AjaxListPanel("stackedDialogs");
		add(stackedDialogs);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(ModalDialogPage.class,
			"dialog.css")));
	}
	
	private class ModalFragment extends Fragment
	{

		private ModalDialog nestedDialog;

		public ModalFragment(String id)
		{
			super(id, "fragment", ModalDialogPage.this);

			Form<Void> form = new Form<Void>("form");
			add(form);
			
			nestedDialog = new ModalDialog("nestedDialog");
			nestedDialog.add(new DefaultTheme());
			nestedDialog.trapFocus();
			nestedDialog.closeOnEscape();
			form.add(nestedDialog);

			form.add(new AjaxLink<Void>("ajaxOpenDialog")
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					openDialog(target);
				}
			});

			form.add(new Link<Void>("openDialog")
			{
				@Override
				public void onClick()
				{
					openDialog(null);
				}
			});

			form.add(new TextField("text").add(new AjaxEventBehavior("keydown")
			{
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
				{
					super.updateAjaxAttributes(attributes);
					
					attributes.getAjaxCallListeners().add(new AjaxCallListener()
					{
						@Override
						public CharSequence getPrecondition(Component component)
						{
							return "if (Wicket.Event.keyCode(attrs.event) != 13) return false; Wicket.Event.fix(attrs.event).preventDefault(); return true;";
						}
					});
				}

				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					openDialog(target);
				}
			}));

			WebMarkupContainer closing = new WebMarkupContainer("closing")
			{
				@Override
				protected void onConfigure()
				{
					super.onConfigure();

					setVisible(findParent(ModalDialog.class) != null);
				}
			};
			form.add(closing);

			closing.add(new Link<Void>("close")
			{
				@Override
				public void onClick()
				{
					findParent(ModalDialog.class).close(null);
				}
			});
			
			final MultiLineLabel lorem = new MultiLineLabel("lorem", "");
			lorem.setOutputMarkupId(true);
			form.add(lorem);
			
			form.add(new AjaxLink<Void>("ipsum") {
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					lorem.setDefaultModelObject(lorem.getDefaultModelObject() + "\n\n" + getString("lorem"));
					
					target.add(lorem);
				}
			});
		}

		private void openDialog(AjaxRequestTarget target)
		{
			ModalFragment fragment = new ModalFragment(ModalDialog.CONTENT_ID);
			if (stacked)
			{
				// stack a new dialog
				ModalDialog dialog = new ModalDialog(stackedDialogs.newChildId())
				{
					@Override
					public ModalDialog close(AjaxRequestTarget target)
					{
						return stackedDialogs.delete(this, target);
					}
				};
				dialog.add(new DefaultTheme());
				dialog.trapFocus();
				dialog.closeOnEscape();
				dialog.setContent(fragment);
				stackedDialogs.append(dialog, target).open(target);
			}
			else
			{
				// use the nested dialog
				nestedDialog.open(fragment, target);
			}
		}
	}
}