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
package org.apache.wicket.examples.repeater;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior.Location;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.AbstractDynamicToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.AjaxDownloadActionColumnContributor;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ColumnChooserToolbarAction;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ColumnState;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.CompoundDynamicColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.CsvExportToolbarAction;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable.BodyHeightUnits;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable.OverlayPlacement;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable.ToolbarPosition;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicNavigationToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IColumnStateStore;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IconBasedRowAction;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IconBasedToolbarAction;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IDynamicColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IDynamicColumnContributor;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IHideableColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IMovableColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.MovableColumnsBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ResizableColumnsBehavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ISelection.SelectionType;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ISelection;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IWebSocketLightWeightMessage;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ProgressBarColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ResourceDynamicColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.RowNumberColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.SelectionColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.SessionColumnStateStore;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.SortableDynamicColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.SortableDynamicDataProvider;
import org.apache.wicket.extensions.markup.html.collapsible.CollapsiblePanel;
import org.apache.wicket.extensions.markup.html.floating.FloatingPanel;
import org.apache.wicket.extensions.markup.html.icon.FontAwesomeIcon;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.extensions.markup.html.icon.SvgIcon;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.message.AbortedMessage;
import org.apache.wicket.protocol.ws.api.message.ClosedMessage;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.string.Strings;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * A table rendered in the browser from JSON, whose links are handled by the table itself.
 */
public class DynamicDataTablePage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static final int ROWS_PER_PAGE = 25;

	private static final Duration SLOW_ACTION = Duration.ofSeconds(2);

	private static final double[] PROPORTIONS = { 0.4, 0.4, 1.1, 0.5, 1.1, 1.1, 0.9, 2.2, 1.8,
			1.3, 1.1, 1.1, 1.0, 1.0 };

	private final ContactDetailsPanel details;

	private final DynamicDataTable<Contact, Long> table;

	private final ResizableColumnsBehavior resizableColumns =
		new ResizableColumnsBehavior(ResizableColumnsBehavior.Mode.STRETCH);

	private Icons icons = Icons.BUILT_IN;

	private final ColumnState initialColumnState;

	/**
	 * Constructor.
	 */
	public DynamicDataTablePage()
	{
		get("selectedLabel").setOutputMarkupId(true);
		get("feedback").setOutputMarkupId(true);

		details = new ContactDetailsPanel("details", new PropertyModel<>(this, "selected"));
		add(details);

		List<IDynamicColumn<Contact>> columns = new ArrayList<>();
		columns.add(new SelectionColumn<Contact>().setId("selection"));
		columns.add(new RowNumberColumn<Contact>(Model.of("#")).setId("number"));
		columns.add(new CompoundDynamicColumn<>(new ResourceModel("actions"),
			List.<IDynamicColumnContributor<Contact>> of(new EditAction(), new DeleteAction(),
				new JCardDownload()))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isResizable()
			{
				return false;
			}
		}.setId("actions"));
		columns.add(new ResourceDynamicColumn<Contact>(new ResourceModel("id"),
			DynamicDataTablePage.class, "IdColumn")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isResizable()
			{
				return false;
			}
		}.setExportValue(Contact::getId).setId("id"));
		columns.add(new SortableDynamicColumn<>(
			new TemplateColumn("firstName", "FirstNameColumn", Contact::getFirstName), "firstName"));
		columns.add(new SortableDynamicColumn<>(
			new TemplateColumn("lastName", "LastNameColumn", Contact::getLastName), "lastName"));
		columns.add(new SortableDynamicColumn<>(
			new TemplateColumn("bornDate", "BornDateColumn", DynamicDataTablePage::bornOn),
			"bornDate"));
		columns.add(new TemplateColumn("address", "AddressColumn",
			contact -> contact.getAddress() + ", " + contact.getCity() + ", " +
				contact.getCountryName()));
		columns.add(new PhonesColumn(new ResourceModel("phones"))
			.setExportValue(contact -> contact.getHomePhone() + " / " + contact.getCellPhone())
			.setId("phones"));
		columns.add(new ProgressColumn());
		List<IDynamicColumn<Contact>> hidden = List.of(
			new ValueColumn("homePhone", "{{homePhone}}", Contact::getHomePhone),
			new ValueColumn("cellPhone", "{{cellPhone}}", Contact::getCellPhone),
			new ValueColumn("city", "{{city}}", Contact::getCity),
			new ValueColumn("country", "{{countryName}}", Contact::getCountryName));
		columns.addAll(hidden);

		ContactsProvider provider = new ContactsProvider();
		table = new DynamicDataTable<>("table", columns, provider, ROWS_PER_PAGE)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected AbstractDynamicToolbar newNavigationToolbar(String id,
				ToolbarPosition position)
			{
				return new DynamicNavigationToolbar<>(id, this, position)
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected Component newPrefix(String id)
					{
						return new ContactFilterPanel(id, new PropertyModel<>(provider, "filter"))
						{
							private static final long serialVersionUID = 1L;

							@Override
							protected void onConfigure()
							{
								super.onConfigure();
								setVisible(isFilterShownAt(getPosition()));
							}

							@Override
							protected void onFilter(AjaxRequestTarget target)
							{
								table.setCurrentPage(0);
								table.refresh(target);
							}
						};
					}
				};
			}

			@Override
			protected void onRowsSent(List<Long> keys)
			{
				ProgressSimulator.show(getSession().getId(), getPage().getPageId(), keys);
			}

			@Override
			protected void onNotFoundAction(AjaxRequestTarget target, String action,
				Long key, Contact contact)
			{
				warn("That contact no longer exists.");
				target.add(DynamicDataTablePage.this.get("feedback"));
			}

			@Override
			protected void onSelectionChanged(AjaxRequestTarget target)
			{
				target.add(DynamicDataTablePage.this.get("selection"));
			}
		};
		hidden.forEach(column -> table.setColumnVisible(column, false));
		initialColumnState = new ColumnState(columns.stream().map(IDynamicColumn::getId).toList(),
			hidden.stream().map(IDynamicColumn::getId).toList(), Map.of());
		table.setColumnStateStore(SessionColumnStateStore.INSTANCE);
		table.setPushEnabled(true);
		table.setNavigationPosition(ToolbarPosition.BOTH);
		table.setItemsPerPageOptions(25L, 50L, 100L, 500L);
		table.setAdjustToParentWidth(true);
		table.add(resizableColumns);
		table.add(new MovableColumnsBehavior());
		table.setBodyHeight(800, BodyHeightUnits.PIXELS);
		table.setOverlayPlacement(OverlayPlacement.BODY);
		table.addToolbarAction(new AddContactAction());
		table.addToolbarAction(new SlowAction());
		table.addToolbarAction(new ColumnChooserToolbarAction<>());
		table.addToolbarAction(new CsvExportToolbarAction<Contact, Long>()
			.setFileName("contacts.csv"));

		add(table);
		add(new Label("selection", this::describeSelection).setOutputMarkupId(true));

		WebMarkupContainer options = new WebMarkupContainer("options");
		options.setOutputMarkupId(true);
		add(options);
		options.add(choice("resize", List.of(Resizing.values()),
			() -> table.getBehaviors().contains(resizableColumns) ? Resizing.STRETCH
				: Resizing.NONE,
			this::resize, resizing -> "resize." + resizing.name()));
		options.add(choice("width", List.of(false, true), table::isAdjustToParentWidth,
			this::adjustToParentWidth, parent -> parent ? "width.parent" : "width.content"));
		options.add(choice("proportions", List.of(false, true),
			() -> table.getColumnProportions() != null,
			set -> proportions(set ? PROPORTIONS : null),
			set -> set ? "proportions.set" : "proportions.content"));
		options.add(choice("navigation", List.of(ToolbarPosition.values()),
			table::getNavigationPosition, table::setNavigationPosition,
			position -> "navigation." + position.name()));
		options.add(choice("selectionBar", List.of(ToolbarPosition.values()),
			table::getSelectionToolbarPosition, table::setSelectionToolbarPosition,
			position -> "navigation." + position.name()));
		options.add(new NumberTextField<>("bodyHeight", LambdaModel.of(table::getBodyHeight,
			height -> table.setBodyHeight(height == 0 ? -1 : height, table.getBodyHeightUnits())),
			Integer.class).setRequired(true).add(new AjaxFormComponentUpdatingBehavior("change")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					target.add(table);
				}
			}));
		options.add(choice("bodyHeightUnits", List.of(BodyHeightUnits.values()),
			table::getBodyHeightUnits, units -> table.setBodyHeight(table.getBodyHeight(), units),
			units -> "bodyHeightUnits." + units.name()));
		options.add(choice("overlay", List.of(OverlayPlacement.values()),
			table::getOverlayPlacement, table::setOverlayPlacement,
			placement -> "overlay." + placement.name()));
		options.add(choice("icons", List.of(Icons.values()), () -> icons, set -> icons = set,
			set -> "icons." + set.name()));
		options.add(choice("columnState", List.of(false, true),
			() -> table.getColumnStateStore() == FileColumnStateStore.INSTANCE,
			this::keepColumnStateInFile, file -> file ? "columnState.file" : "columnState.session"));
		options.add(new AjaxLink<Void>("resetColumns")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				table.setColumnState(initialColumnState).setColumnWidths((double[])null);
				table.getColumnStateStore().save(table.getColumnStateKey(), table.getColumnState());
				target.add(table);
			}
		});

		add(new WebSocketBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConnect(ConnectedMessage message)
			{
				ProgressSimulator.start(message.getApplication(), message.getSessionId(),
					getPageId(), message.getKey(), table.getMarkupId(),
					DatabaseLocator.getLargeDatabase());
			}

			@Override
			protected void onClose(ClosedMessage message)
			{
				ProgressSimulator.stop(message.getSessionId(), getPageId());
			}

			@Override
			protected void onAbort(AbortedMessage message)
			{
				ProgressSimulator.stop(message.getSessionId(), getPageId());
			}
		});

		add(new Link<Void>("switchLocale")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				getSession().setLocale(isGerman() ? Locale.ENGLISH : Locale.GERMAN);
			}
		}.setBody(() -> isGerman() ? "English" : "Deutsch"));

		add(new AjaxLink<Void>("addContact")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				Contact contact = ContactGenerator.getInstance().generate();
				contact.setId(0);
				contact.setFirstName("Aaron");
				DatabaseLocator.getLargeDatabase().save(contact);

				push(target, table.newRefreshMessage(), "Added " + contact.getFirstName() + " " +
					contact.getLastName() + "; the table was refreshed via web sockets.");
			}
		});

		add(new AjaxLink<Void>("pushRows")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				List<Contact> rows = currentPageRows();
				Collections.reverse(rows);

				push(target, table.newUpdateRowsMessage(rows),
					"Pushed the rows of this page in reverse order via web sockets.");
			}
		});
	}

	/**
	 * @return the day the contact was born, or {@code null} if it is not known
	 */
	private static LocalDate bornOn(Contact contact)
	{
		Date born = contact.getBornDate();
		return born != null ? born.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
	}

	/**
	 * @return whether the filter is shown in the navigation at the position: in the top one,
	 *         unless there is only a bottom one
	 */
	private boolean isFilterShownAt(ToolbarPosition position)
	{
		return position == ToolbarPosition.TOP
			? table.getNavigationPosition() != ToolbarPosition.BOTTOM
			: table.getNavigationPosition() == ToolbarPosition.BOTTOM;
	}

	/**
	 * Switches the store of the table's column state, taking the state the new store holds, if
	 * any, or else saving the current one in it.
	 */
	private void keepColumnStateInFile(boolean file)
	{
		IColumnStateStore store = file ? FileColumnStateStore.INSTANCE
			: SessionColumnStateStore.INSTANCE;
		table.setColumnStateStore(store);
		ColumnState state = store.load(table.getColumnStateKey());
		if (state != null)
		{
			table.setColumnState(state);
		}
		else
		{
			store.save(table.getColumnStateKey(), table.getColumnState());
		}
	}

	private void adjustToParentWidth(boolean adjust)
	{
		table.setAdjustToParentWidth(adjust).setColumnWidths((double[])null);
	}

	private void proportions(double[] proportions)
	{
		table.setColumnProportions(proportions).setColumnWidths((double[])null);
	}

	/**
	 * @return a drop-down choosing one of the values of a table setting, repainting the table when
	 *         it changes
	 */
	private <V> DropDownChoice<V> choice(String id, List<V> values, SerializableSupplier<V> getter,
		SerializableConsumer<V> setter, SerializableFunction<V, String> key)
	{
		DropDownChoice<V> choice = new DropDownChoice<>(id, LambdaModel.of(getter, setter), values,
			new ChoiceRenderer<>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(V value)
				{
					return getString(key.apply(value));
				}

				@Override
				public String getIdValue(V value, int index)
				{
					return String.valueOf(index);
				}
			});
		choice.setRequired(true);
		choice.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				target.add(table);
			}
		});
		return choice;
	}

	@Override
	protected Component newExplanation(String id, IModel<String> explanation)
	{
		return new CollapsiblePanel(id, new ResourceModel("aboutThisExample"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newBody(String id)
			{
				return DynamicDataTablePage.super.newExplanation(id, explanation);
			}
		}.setRememberExpanded(true).setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(DynamicDataTablePage.class, "DynamicDataTablePage.css")));
		response.render(FontAwesomeResourceReference.styleSheet());
	}

	private String describeSelection()
	{
		ISelection<Contact, Long> selection = table.getSelection();
		if (selection.isEmpty())
		{
			return getString("selection.none");
		}
		String key = selection.getType() == SelectionType.ALL ? "selection.all" : "selection.some";
		return new StringResourceModel(key, this).setParameters(selection.size()).getString();
	}

	private boolean isGerman()
	{
		return Locale.GERMAN.getLanguage().equals(getLocale().getLanguage());
	}

	private List<Contact> currentPageRows()
	{
		long first = table.getCurrentPage() * table.getItemsPerPage();
		long count = Math.min(table.getItemsPerPage(), table.getItemCount() - first);
		return count > 0 ? table.getProvider().rows(first, count) : new ArrayList<>();
	}

	private void push(AjaxRequestTarget target, IWebSocketLightWeightMessage message,
		String success)
	{
		try
		{
			if (message.send(table))
			{
				info(success);
			}
			else
			{
				warn("The page has no open web socket connection.");
			}
		}
		catch (IOException e)
		{
			error("Pushing to the table via web sockets failed: " + e.getMessage());
		}
		target.add(get("feedback"));
	}

	/**
	 * Whether the columns can be resized: by the {@link ResizableColumnsBehavior} added to the
	 * table, or not at all.
	 */
	private enum Resizing
	{
		NONE, STRETCH
	}

	private void resize(Resizing resizing)
	{
		if (resizing == Resizing.STRETCH && table.getBehaviors().contains(resizableColumns) == false)
		{
			table.add(resizableColumns);
		}
		else if (resizing == Resizing.NONE && table.getBehaviors().contains(resizableColumns))
		{
			table.remove(resizableColumns);
		}
	}

	/**
	 * The icons the row actions are drawn with.
	 */
	private enum Icons
	{
		BUILT_IN, SVG, FONT_AWESOME;

		IIcon pick(IIcon builtIn, SvgIcon svg, FontAwesomeIcon fontAwesome)
		{
			return switch (this)
			{
				case BUILT_IN -> builtIn;
				case SVG -> svg;
				case FONT_AWESOME -> fontAwesome;
			};
		}
	}

	/**
	 * Edits the contact in the table's overlay, and selects it.
	 */
	private class EditAction extends IconBasedRowAction<Contact>
	{
		private static final long serialVersionUID = 1L;

		EditAction()
		{
			super("edit", Icon.EDIT, new ResourceModel("edit"));
		}

		@Override
		public IIcon getIcon()
		{
			return icons.pick(Icon.EDIT, SvgIcon.PEN, FontAwesomeIcon.PEN);
		}

		@Override
		protected void onRowAction(Contact contact, AjaxRequestTarget target)
		{
			setSelected(contact);
			target.add(details, DynamicDataTablePage.this.get("selectedLabel"));
			showContactForm(contact,
				new StringResourceModel("editContact", DynamicDataTablePage.this, Model.of(contact)),
				target);
		}
	}

	/**
	 * Adds a contact, entered in the same form as an edited one, shown from an icon in the
	 * navigation.
	 */
	private class AddContactAction extends IconBasedToolbarAction<Contact, Long>
	{
		private static final long serialVersionUID = 1L;

		AddContactAction()
		{
			super(IconBasedRowAction.Icon.ADD, new ResourceModel("newContact"));
		}

		@Override
		public IIcon getIcon()
		{
			return icons.pick(IconBasedRowAction.Icon.ADD, SvgIcon.USER_PLUS,
				FontAwesomeIcon.USER_PLUS);
		}

		@Override
		protected void onClick(DynamicDataTable<Contact, Long> table, AjaxRequestTarget target)
		{
			showContactForm(new Contact(), new ResourceModel("newContact"), target);
		}
	}

	/**
	 * Takes a while on the server, so that the veil over the table shows its spinner.
	 */
	private class SlowAction extends IconBasedToolbarAction<Contact, Long>
	{
		private static final long serialVersionUID = 1L;

		private static final IIcon HOURGLASS = () -> "<svg aria-hidden=\"true\" " +
			"focusable=\"false\" viewBox=\"0 0 24 24\" width=\"18\" height=\"18\" " +
			"fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" " +
			"stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M6 3h12 M6 21h12 " +
			"M7 3c0 5 10 6 10 9s-10 4-10 9 M17 3c0 5-10 6-10 9s10 4 10 9\"/></svg>";

		SlowAction()
		{
			super(HOURGLASS, new ResourceModel("slowAction"));
		}

		@Override
		public IIcon getIcon()
		{
			return icons.pick(HOURGLASS, SvgIcon.HOURGLASS_HALF, FontAwesomeIcon.HOURGLASS_HALF);
		}

		@Override
		protected void onClick(DynamicDataTable<Contact, Long> table, AjaxRequestTarget target)
		{
			try
			{
				Thread.sleep(SLOW_ACTION.toMillis());
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			info(getString("slowAction.done"));
			target.add(DynamicDataTablePage.this.get("feedback"));
		}
	}

	/**
	 * Shows the form of a contact in the table's overlay; the saved contact is selected.
	 */
	private void showContactForm(Contact contact, IModel<String> title, AjaxRequestTarget target)
	{
		table.showOverlay(new FloatingPanel(DynamicDataTable.OVERLAY_CONTENT_ID, title)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newBody(String id)
			{
				return new ContactEditPanel(id, contact)
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected void onSaved(Contact saved, AjaxRequestTarget target)
					{
						setSelected(saved);
						table.closeOverlay(target);
						table.refresh(target);
						target.add(details, DynamicDataTablePage.this.get("selectedLabel"));
					}

					@Override
					protected void onCancel(AjaxRequestTarget target)
					{
						table.closeOverlay(target);
					}
				};
			}

			@Override
			protected void onClose(AjaxRequestTarget target)
			{
				table.closeOverlay(target);
			}
		}, target);
	}

	/**
	 * A column the user can hide and move, reading its template from {@code templateName.html} next to this
	 * page.
	 */
	private static class TemplateColumn extends ResourceDynamicColumn<Contact>
		implements
			IHideableColumn,
			IMovableColumn
	{
		private static final long serialVersionUID = 1L;

		TemplateColumn(String headerKey, String templateName,
			SerializableFunction<Contact, ?> exportValue)
		{
			super(new ResourceModel(headerKey), DynamicDataTablePage.class, templateName);
			setId(headerKey);
			setExportValue(exportValue);
		}
	}

	/**
	 * A column the user can hide and move, showing one value of the contact.
	 */
	private static class ValueColumn extends DynamicColumn<Contact>
		implements
			IHideableColumn,
			IMovableColumn
	{
		private static final long serialVersionUID = 1L;

		ValueColumn(String headerKey, String template, SerializableFunction<Contact, ?> exportValue)
		{
			super(new ResourceModel(headerKey), template);
			setId(headerKey);
			setExportValue(exportValue);
		}
	}

	/**
	 * The progress of the contact, a column the user can hide and move.
	 */
	private static class ProgressColumn extends ProgressBarColumn<Contact>
		implements
			IHideableColumn,
			IMovableColumn
	{
		private static final long serialVersionUID = 1L;

		ProgressColumn()
		{
			super(new ResourceModel("progress"), "progress");
			setId("progress");
		}
	}

	/**
	 * Deletes the contact once the user confirmed it in the table's overlay.
	 */
	private class DeleteAction extends IconBasedRowAction<Contact>
	{
		private static final long serialVersionUID = 1L;

		DeleteAction()
		{
			super("delete", Icon.DELETE, new ResourceModel("delete"));
		}

		@Override
		public IIcon getIcon()
		{
			return icons.pick(Icon.DELETE, SvgIcon.TRASH_CAN, FontAwesomeIcon.TRASH_CAN);
		}

		@Override
		protected IModel<String> getConfirmation(Contact contact)
		{
			return new StringResourceModel("delete.confirm", DynamicDataTablePage.this,
				Model.of(contact));
		}

		@Override
		protected void onRowAction(Contact contact, AjaxRequestTarget target)
		{
			if (Objects.equals(getSelected(), contact))
			{
				setSelected(null);
				target.add(details, DynamicDataTablePage.this.get("selectedLabel"));
			}
			DatabaseLocator.getLargeDatabase().delete(contact);
			info("Deleted " + contact.getFirstName() + " " + contact.getLastName() + ".");
			target.add(DynamicDataTablePage.this.get("feedback"));
			table.refresh(target);
		}
	}

	/**
	 * Downloads the contact as a jCard (RFC 7095), the JSON form of a vCard, which address books
	 * and contact libraries read.
	 */
	private class JCardDownload extends AjaxDownloadActionColumnContributor<Contact>
	{
		private static final long serialVersionUID = 1L;

		JCardDownload()
		{
			super("download", new ResourceModel("download"));
			setIcon(IconBasedRowAction.Icon.DOWNLOAD);
			setLocation(Location.SameWindow);
		}

		@Override
		public IIcon getIcon()
		{
			return icons.pick(IconBasedRowAction.Icon.DOWNLOAD, SvgIcon.DOWNLOAD,
				FontAwesomeIcon.DOWNLOAD);
		}

		@Override
		protected String getFileName(Contact contact)
		{
			return "contact-" + contact.getId() + ".json";
		}

		@Override
		protected String getContentType()
		{
			return "application/vcard+json";
		}

		@Override
		protected String getContent(Contact contact)
		{
			return toJCard(contact).toString(2);
		}
	}

	/**
	 * @return the contact as a jCard: version, formatted and structured name, birthday, phones
	 *         and address
	 */
	static JSONArray toJCard(Contact contact)
	{
		JSONArray properties = new JSONArray();
		properties.put(property("version", new JSONObject(), "text", "4.0"));
		properties.put(property("fn", new JSONObject(), "text",
			contact.getFirstName() + " " + contact.getLastName()));
		properties.put(property("n", new JSONObject(), "text",
			new JSONArray(List.of(contact.getLastName(), contact.getFirstName(), "", "", ""))));
		LocalDate born = bornOn(contact);
		if (born != null)
		{
			properties.put(property("bday", new JSONObject(), "date", born.toString()));
		}
		if (contact.getHomePhone() != null)
		{
			properties.put(property("tel", new JSONObject().put("type", new JSONArray(
				List.of("home", "voice"))), "uri", "tel:" + contact.getHomePhone()));
		}
		if (contact.getCellPhone() != null)
		{
			properties.put(property("tel", new JSONObject().put("type", new JSONArray(
				List.of("cell", "voice"))), "uri", "tel:" + contact.getCellPhone()));
		}
		properties.put(property("adr", new JSONObject(), "text",
			new JSONArray(List.of("", "", Objects.toString(contact.getAddress(), ""),
				Objects.toString(contact.getCity(), ""), "", "",
				Objects.toString(contact.getCountryName(), "")))));
		return new JSONArray().put("vcard").put(properties);
	}

	private static JSONArray property(String name, JSONObject parameters, String type,
		Object value)
	{
		return new JSONArray().put(name).put(parameters).put(type).put(value);
	}

	/**
	 * Sorts by first or last name with the database's indexes, and by born date in memory, and
	 * filters by first and last name in memory.
	 */
	private static class ContactsProvider extends SortableDynamicDataProvider<Contact, Long, String>
	{
		private static final long serialVersionUID = 1L;

		ContactsProvider()
		{
			super(Long.class, Contact::getId);
		}

		private static final String BORN_DATE = "bornDate";

		private String filter;

		public String getFilter()
		{
			return filter;
		}

		public void setFilter(String filter)
		{
			this.filter = filter;
		}

		@Override
		public List<Contact> rows(long first, long count)
		{
			List<Contact> contacts = contacts();
			return new ArrayList<>(contacts.subList((int)first,
				(int)Math.min(contacts.size(), first + count)));
		}

		@Override
		public long size()
		{
			return contacts().size();
		}

		/**
		 * @return the contacts whose first or last name contains the filter, in the order of the
		 *         sort
		 */
		private List<Contact> contacts()
		{
			SortParam<String> sort = getSort();
			boolean byBornDate = sort != null && BORN_DATE.equals(sort.getProperty());
			List<Contact> index = DatabaseLocator.getLargeDatabase()
				.getIndex(byBornDate ? null : sort);
			List<Contact> contacts;
			synchronized (index)
			{
				contacts = new ArrayList<>(index);
			}
			if (Strings.isEmpty(filter) == false)
			{
				String term = filter.trim().toLowerCase(Locale.ROOT);
				contacts.removeIf(contact -> contains(contact.getFirstName(), term) == false &&
					contains(contact.getLastName(), term) == false);
			}
			if (byBornDate)
			{
				Comparator<Contact> byBorn = Comparator.comparing(Contact::getBornDate,
					Comparator.nullsFirst(Comparator.naturalOrder()));
				contacts.sort(sort.isAscending() ? byBorn : byBorn.reversed());
			}
			return contacts;
		}

		private static boolean contains(String name, String term)
		{
			return name != null && name.toLowerCase(Locale.ROOT).contains(term);
		}

		@Override
		public Contact findByKey(Long key)
		{
			try
			{
				return DatabaseLocator.getLargeDatabase().get(key);
			}
			catch (RuntimeException e)
			{
				return null;
			}
		}
	}
}
