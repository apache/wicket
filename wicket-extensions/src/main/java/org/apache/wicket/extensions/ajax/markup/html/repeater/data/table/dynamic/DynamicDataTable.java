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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxUtils;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.ajax.markup.html.modal.TrapFocusBehavior;
import org.apache.wicket.extensions.ajax.veil.LocalVeilBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IMovableColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IMovableColumnsTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IResizableColumnsTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.MovableColumnsBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ResizableColumnsBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * A table whose body is rendered in the browser from JSON, instead of by Wicket components.
 * <p>
 * Wicket renders only the {@code <table>} and its header. The rows come as JSON, either pulled
 * from the table's own endpoint (on load, on {@link #refresh(IPartialPageRequestHandler)} and,
 * optionally, {@link #setPollInterval(Duration) periodically}) or pushed by the application, for
 * example over a web socket with an {@link IWebSocketLightWeightMessage}. Each cell is
 * the column's template evaluated against the row's JSON by the {@link #setTemplateEngine
 * template engine}.
 * <p>
 * Links in the cells are plain markup, not components: an element with a {@code data-dt-action}
 * attribute sends the action and the row's key to the table, which resolves the row through
 * {@link IDynamicDataProvider#findByKey(Serializable)} and hands it to the
 * {@link IAjaxActionColumnContributor} registered for the action. A click on a row that has
 * changed or gone since it was rendered therefore still reaches the table, rather than a component
 * that no longer exists, and ends up in
 * {@link #onNotFoundAction(AjaxRequestTarget, String, Serializable, Object)}.
 * <p>
 * Action contributors are registered from the {@link CompoundDynamicColumn}s the table is given,
 * and with {@link #addActionContributor(IAjaxActionColumnContributor)} for contributors used in
 * other columns.
 * <p>
 * The table is paged like a {@link DataTable}: the endpoint serves the rows of the current page,
 * and a {@link DynamicNavigationToolbar navigation}, in the footer, above the headers or both
 * ({@link #setNavigationPosition(ToolbarPosition)}), switches pages and repaints the rows via Ajax.
 * When a reload finds that the number of rows or the current page has changed, the browser has the
 * toolbars re-rendered. Rows pushed by the application are shown as they are.
 * <p>
 * Above and below the rows, the table renders toolbars: its navigation, its
 * {@link DynamicSelectionToolbar selection bar}, and toolbars of the application's own, added
 * with {@link #addTopToolbar(AbstractDynamicToolbar)} and
 * {@link #addBottomToolbar(AbstractDynamicToolbar)}. The built-in toolbars come from factory
 * methods an application can override. {@link IDynamicToolbarAction Actions} on the whole table,
 * such as a {@link CsvExportToolbarAction}, are shown in the navigation.
 * <p>
 * With an {@link ISortableDynamicDataProvider}, the header of every {@link ISortableDynamicColumn}
 * is a link sorting the rows by the column, followed by an up and a down arrow, the one of the
 * current order highlighted. Sorting has the provider query the rows again, shows the first page,
 * repaints all its rows and clears the selection.
 * <p>
 * With a {@link ResizableColumnsBehavior} added, the user resizes the columns by dragging the
 * right edge of their header. With a {@link MovableColumnsBehavior} added, the user drags a column
 * marked {@link IMovableColumn} by the handle in its header to another place, a marker showing
 * where it will land, or moves it with the arrow keys on that handle; the other columns keep
 * their places. Columns marked {@link IHideableColumn} can be hidden by the
 * user with a {@link ColumnChooserToolbarAction}; the application can hide any column with
 * {@link #setColumnVisible(IDynamicColumn, boolean)}. The table keeps the order, the visibility
 * and the widths of its columns, re-renders itself when they change and saves them in its
 * {@link #setColumnStateStore(IColumnStateStore) column state store}, if it has one, for
 * example {@link SessionColumnStateStore}, so a table built again, for example after reloading
 * the page, shows its columns as the user left them.
 * <p>
 * The column templates are written into the page as markup and have to be authored by the
 * developer; see {@link IDynamicColumn}. Every property the {@link #setJsonSerializer serializer}
 * produces for a row is sent to the browser.
 * <p>
 * With a {@link SelectionColumn}, the user selects rows with checkboxes. The selection is kept on
 * the server, by key, and survives paging and refreshing; see {@link #getSelection()}. Once every
 * row of a page is selected, a bar below the top navigation, above the bottom one or both
 * ({@link #setSelectionToolbarPosition(ToolbarPosition)}) offers to select every row of the
 * provider; checking or unchecking a row afterwards turns the selection back into the rows the
 * page shows checked.
 * <p>
 * The table carries the CSS class {@value #CSS_CLASS}, and takes its colors from the
 * {@link org.apache.wicket.extensions.theme.Theme theme} it is rendered in, if any.
 * <p>
 * A column implementing {@link IHeaderContributor}, such as a {@link ProgressBarColumn}, renders
 * its header items, for example a style sheet, along with the table.
 * <p>
 * The table must be attached to a {@code <table>} tag.
 *
 * @param <T>
 *            the type of the rows
 * @param <K>
 *            the type of the keys of the rows
 * @since 11.0.0
 */
public class DynamicDataTable<T, K extends Serializable> extends Panel
	implements
		IPageableItems,
		IResizableColumnsTable,
		IMovableColumnsTable
{
	private static final long serialVersionUID = 1L;

	private static final ResourceReference JAVASCRIPT = new JavaScriptResourceReference(
		DynamicDataTable.class, "dynamic-data-table.js");

	private static final String ACTION_PARAMETER = "action";

	private static final String KEY_PARAMETER = "key";

	private static final ResourceReference CSS = new CssResourceReference(DynamicDataTable.class,
		"dynamic-data-table.css");

	private static final Map<SortOrder, String> SORT_CSS_CLASSES = new EnumMap<>(
		Map.of(SortOrder.ASCENDING, "wicket_orderUp", SortOrder.DESCENDING, "wicket_orderDown",
			SortOrder.NONE, "wicket_orderNone"));

	private static final Map<SortOrder, String> ARIA_SORT = new EnumMap<>(Map.of(
		SortOrder.ASCENDING, "ascending", SortOrder.DESCENDING, "descending", SortOrder.NONE,
		"none"));

	/**
	 * The prefix of the actions the table handles itself; an action contributor must not use it.
	 */
	public static final String RESERVED_ACTION_PREFIX = "_wicket";

	/**
	 * The action selecting the row of the key sent with it.
	 */
	public static final String SELECT_ACTION = RESERVED_ACTION_PREFIX + "-select";

	/**
	 * The action deselecting the row of the key sent with it.
	 */
	public static final String DESELECT_ACTION = RESERVED_ACTION_PREFIX + "-deselect";

	/**
	 * The action selecting every row of the current page.
	 */
	public static final String SELECT_PAGE_ACTION = RESERVED_ACTION_PREFIX + "-select-page";

	/**
	 * The action deselecting every row of the current page.
	 */
	public static final String DESELECT_PAGE_ACTION = RESERVED_ACTION_PREFIX + "-deselect-page";

	/**
	 * The action the browser sends when the user presses Escape in the table's overlay.
	 */
	public static final String CLOSE_OVERLAY_ACTION = RESERVED_ACTION_PREFIX + "-close-overlay";

	/**
	 * The action a {@link ColumnChooserToolbarAction} sends the columns to show with: their
	 * indexes in {@link #getColumns()}, separated by commas.
	 */
	public static final String SHOW_COLUMNS_ACTION = RESERVED_ACTION_PREFIX + "-show-columns";

	/**
	 * The id a component shown with {@link #showOverlay(Component, IPartialPageRequestHandler)}
	 * has to carry.
	 */
	public static final String OVERLAY_CONTENT_ID = "content";

	/** The CSS class of the table. */
	public static final String CSS_CLASS = "dynamic-data-table";

	private static final int FETCH_CHUNK = 100;

	private static final double MAX_COLUMN_WIDTH = 100_000;

	/**
	 * Where a {@link DynamicDataTable} shows a toolbar.
	 */
	public enum ToolbarPosition
	{
		/** Above the headers. */
		TOP,

		/** In the footer. */
		BOTTOM,

		/** Above the headers and in the footer. */
		BOTH
	}

	/**
	 * Where a {@link DynamicDataTable} shows its overlay, see
	 * {@link DynamicDataTable#showOverlay(Component, IPartialPageRequestHandler)}.
	 */
	public enum OverlayPlacement
	{
		/** Over the table: the veil covers the table only and the window is shown in it. */
		LOCAL,

		/**
		 * Over the whole page: the veil covers the browser window and the window is shown in its
		 * middle, whatever part of the table is in view. The overlay is fixed to the browser
		 * window, unless an element around the table has a {@code transform}, {@code filter} or
		 * {@code contain} style, which CSS makes it relative to instead.
		 */
		BODY
	}

	/**
	 * The unit of the height of the body of a {@link DynamicDataTable}, see
	 * {@link DynamicDataTable#setBodyHeight(int, BodyHeightUnits)}.
	 */
	public enum BodyHeightUnits
	{
		/** Pixels. */
		PIXELS("px"),

		/**
		 * Percent of the height of the element the table is in. As for any percentage height in
		 * CSS, that element needs a height of its own; without one the body takes the height of
		 * its rows.
		 */
		PERCENT("%"),

		/** Percent of the height of the browser's viewport. */
		VIEWPORT_HEIGHT("vh");

		private final String cssUnit;

		BodyHeightUnits(String cssUnit)
		{
			this.cssUnit = cssUnit;
		}

		/**
		 * @return the unit in CSS, such as {@code px}
		 */
		public String getCssUnit()
		{
			return cssUnit;
		}
	}

	private final List<IDynamicColumn<T>> columns;

	private final List<IDynamicColumn<T>> initialColumns;

	private IColumnStateStore columnStateStore;

	private final Set<IDynamicColumn<T>> hiddenColumns = Collections
		.newSetFromMap(new IdentityHashMap<>());

	private final IDynamicDataProvider<T, K> provider;

	private final Map<String, IAjaxActionColumnContributor<T>> actionContributors =
		new LinkedHashMap<>();

	private final Map<String, IWebSocketMessageType> messageTypes = new LinkedHashMap<>();

	private final DataBehavior dataBehavior;

	private final ActionBehavior actionBehavior;

	private final NavigationBehavior navigationBehavior;

	private ITemplateEngine templateEngine = BuiltInTemplateEngine.INSTANCE;

	private IJsonSerializer jsonSerializer = JacksonJsonSerializer.INSTANCE;

	private Duration pollInterval;

	private boolean pushEnabled;

	private final WebMarkupContainer headersRow;

	private final RepeatingView topToolbars;

	private final RepeatingView bottomToolbars;

	private final WebMarkupContainer overlay;

	private final WebMarkupContainer overlayWindow;

	private boolean overlayShown;

	private OverlayPlacement overlayPlacement = OverlayPlacement.LOCAL;

	private final List<IDynamicToolbarAction<T, K>> toolbarActions = new ArrayList<>();

	private int toolbarCount;

	private ToolbarPosition selectionToolbarPosition = ToolbarPosition.TOP;

	private boolean allSelected;

	private final LinkedHashSet<K> selectedKeys = new LinkedHashSet<>();

	private ToolbarPosition navigationPosition = ToolbarPosition.BOTTOM;

	private List<Long> itemsPerPageOptions = new ArrayList<>();

	private double[] columnWidths;

	private double[] columnProportions;

	private boolean adjustToParentWidth;

	private int bodyHeight = -1;

	private BodyHeightUnits bodyHeightUnits = BodyHeightUnits.PIXELS;

	private long itemsPerPage;

	private long currentPage;

	private transient Long itemCount;

	private transient Map<String, String> templates;

	private transient List<T> currentPageRows;

	/**
	 * @param id
	 *            component id
	 * @param columns
	 *            the columns, in display order, all of them shown
	 * @param provider
	 *            supplies the rows and resolves them back from their keys
	 * @param rowsPerPage
	 *            the number of rows shown on a page
	 */
	public DynamicDataTable(String id, List<? extends IDynamicColumn<T>> columns,
		IDynamicDataProvider<T, K> provider, long rowsPerPage)
	{
		super(id);
		this.columns = new ArrayList<>(Args.notEmpty(columns, "columns"));
		initialColumns = List.copyOf(this.columns);
		checkColumnIds();
		this.provider = Args.notNull(provider, "provider");
		setItemsPerPage(rowsPerPage);

		setOutputMarkupId(true);

		add(topToolbars = new RepeatingView("topToolbars"));
		add(bottomToolbars = new RepeatingView("bottomToolbars"));

		overlay = new WebMarkupContainer("overlay")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(overlayShown);
			}

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				if (overlayPlacement == OverlayPlacement.BODY)
				{
					tag.append("class", "dynamic-data-table-overlay-body", " ");
				}
			}
		};
		overlay.setOutputMarkupPlaceholderTag(true);
		add(overlay);
		overlayWindow = new WebMarkupContainer("window")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);
				response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
				response.render(OnDomReadyHeaderItem.forScript(
					"Wicket.DynamicDataTable.placeOverlay(" + JSONObject.quote(getMarkupId()) + ");"));
			}
		};
		overlayWindow.setOutputMarkupId(true);
		overlayWindow.add(new TrapFocusBehavior());
		overlayWindow.add(new WebMarkupContainer(OVERLAY_CONTENT_ID));
		overlay.add(overlayWindow);

		headersRow = new WebMarkupContainer("headersRow");
		headersRow.setOutputMarkupId(true);
		add(headersRow);
		headersRow.add(new ListView<>("headers", this::getVisibleColumns)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<IDynamicColumn<T>> item)
			{
				populateHeader(item, item.getModelObject());
			}
		});
		headersRow.add(new WebMarkupContainer("filler")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(hasFiller());
			}
		});

		for (IDynamicColumn<T> column : this.columns)
		{
			IDynamicColumn<T> unwrapped = column instanceof SortableDynamicColumn<T, ?> sortable
				? sortable.getColumn() : column;
			if (unwrapped instanceof CompoundDynamicColumn<T> compound)
			{
				for (IDynamicColumnContributor<T> contributor : compound.getContributors())
				{
					if (contributor instanceof IAjaxActionColumnContributor<T> actionContributor)
					{
						addActionContributor(actionContributor);
					}
				}
			}
		}

		addMessageType(UpdateRowsMessageType.INSTANCE);
		addMessageType(RefreshMessageType.INSTANCE);
		addMessageType(UpdateRowMessageType.INSTANCE);

		add(dataBehavior = new DataBehavior());
		add(actionBehavior = new ActionBehavior());
		add(navigationBehavior = new NavigationBehavior());
	}

	/**
	 * Registers the contributor handling an action. Contributors in the
	 * {@link CompoundDynamicColumn}s given to the constructor are registered already.
	 *
	 * @param contributor
	 *            the contributor
	 * @return {@code this}
	 * @throws IllegalArgumentException
	 *             if another contributor is registered for the same action, or the action
	 *             starts with {@value #RESERVED_ACTION_PREFIX}
	 */
	public DynamicDataTable<T, K> addActionContributor(IAjaxActionColumnContributor<T> contributor)
	{
		Args.notNull(contributor, "contributor");
		String actionId = Args.notEmpty(contributor.getActionId(), "actionId");
		if (actionId.startsWith(RESERVED_ACTION_PREFIX))
		{
			throw new IllegalArgumentException("The action '" + actionId +
				"' starts with the reserved prefix '" + RESERVED_ACTION_PREFIX + "'");
		}
		IAjaxActionColumnContributor<T> registered = actionContributors.putIfAbsent(actionId,
			contributor);
		if (registered != null && registered != contributor)
		{
			throw new IllegalArgumentException(
				"Another contributor is already registered for action '" + actionId + "'");
		}
		if (registered == null)
		{
			contributor.bind(this);
		}
		return this;
	}

	/**
	 * Registers the browser-side function handling the messages of a type pushed to this table.
	 * {@link UpdateRowsMessageType}, {@link UpdateRowMessageType} and {@link RefreshMessageType} are
	 * registered already.
	 *
	 * @param messageType
	 *            the message type
	 * @return {@code this}
	 * @throws IllegalArgumentException
	 *             if another message type is registered with the same id
	 */
	public DynamicDataTable<T, K> addMessageType(IWebSocketMessageType messageType)
	{
		Args.notNull(messageType, "messageType");
		String typeId = Args.notEmpty(messageType.getTypeId(), "typeId");
		IWebSocketMessageType registered = messageTypes.putIfAbsent(typeId, messageType);
		if (registered != null && registered != messageType)
		{
			throw new IllegalArgumentException(
				"Another message type is already registered as '" + typeId + "'");
		}
		return this;
	}

	/**
	 * Dispatches a click on an element with a {@code data-dt-action} attribute to the
	 * {@link IAjaxActionColumnContributor} registered for the action, or to
	 * {@link #onNotFoundAction(AjaxRequestTarget, String, Serializable, Object)} if there is none
	 * or the row no longer exists.
	 *
	 * @param target
	 *            the Ajax request target
	 * @param action
	 *            the value of the clicked element's {@code data-dt-action} attribute
	 * @param key
	 *            the key of the row the element belongs to, or {@code null} if it is not in a row
	 *            or the browser sent no valid key
	 * @param row
	 *            the row the element belongs to, or {@code null} if it no longer exists
	 */
	protected final void onAction(AjaxRequestTarget target, String action, K key, T row)
	{
		IAjaxActionColumnContributor<T> contributor = actionContributors.get(action);
		if (contributor == null || row == null)
		{
			onNotFoundAction(target, action, key, row);
		}
		else
		{
			contributor.onAction(row, target);
		}
	}

	/**
	 * Called when a click cannot be dispatched: the row no longer exists, for example because it
	 * was deleted after it was rendered, or no contributor is registered for the action. Does
	 * nothing by default.
	 * <p>
	 * Both the action and the key come from the browser.
	 *
	 * @param target
	 *            the Ajax request target
	 * @param action
	 *            the value of the clicked element's {@code data-dt-action} attribute
	 * @param key
	 *            the key the row was looked up by, or {@code null} if the element is not in a row
	 *            or the browser sent no valid key
	 * @param row
	 *            the row, or {@code null} if it no longer exists
	 */
	protected void onNotFoundAction(AjaxRequestTarget target, String action, K key, T row)
	{
	}

	/**
	 * Shows a component in a window on top of the table, over a veil that blocks the table until
	 * {@link #closeOverlay(IPartialPageRequestHandler)} is called or the user presses Escape, or on
	 * top of the whole page, see {@link #setOverlayPlacement(OverlayPlacement)}. The
	 * focus moves into the window and stays there while it is shown. Showing another component
	 * replaces the current one.
	 *
	 * @param content
	 *            the component to show, with the id {@value #OVERLAY_CONTENT_ID}, for example a
	 *            panel with a form editing a row
	 * @param target
	 *            the handler of the current Ajax or web socket request
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> showOverlay(Component content, IPartialPageRequestHandler target)
	{
		Args.notNull(content, "content");
		Args.isTrue(OVERLAY_CONTENT_ID.equals(content.getId()),
			"the content of the overlay must have the id '" + OVERLAY_CONTENT_ID + "'");
		overlayWindow.replace(content);
		overlayShown = true;
		target.add(overlay);
		return this;
	}

	/**
	 * Closes the overlay, if it is shown, giving the table back to the user.
	 *
	 * @param target
	 *            the handler of the current Ajax or web socket request
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> closeOverlay(IPartialPageRequestHandler target)
	{
		if (overlayShown)
		{
			overlayShown = false;
			overlayWindow.replace(new WebMarkupContainer(OVERLAY_CONTENT_ID));
			target.add(overlay);
			onOverlayClosed(target);
		}
		return this;
	}

	/**
	 * @return where the overlay is shown, {@link OverlayPlacement#LOCAL} by default
	 * @since 11.0.0
	 */
	public OverlayPlacement getOverlayPlacement()
	{
		return overlayPlacement;
	}

	/**
	 * Sets where the overlay is shown: over the table, or over the whole page.
	 *
	 * @param placement
	 *            where the overlay is shown
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> setOverlayPlacement(OverlayPlacement placement)
	{
		overlayPlacement = Args.notNull(placement, "placement");
		return this;
	}

	/**
	 * @return whether the overlay is shown
	 * @since 11.0.0
	 */
	public boolean isOverlayShown()
	{
		return overlayShown;
	}

	/**
	 * Called after the overlay was closed, by {@link #closeOverlay(IPartialPageRequestHandler)} or
	 * by the user pressing Escape. Does nothing by default.
	 *
	 * @param target
	 *            the handler of the current Ajax or web socket request
	 * @since 11.0.0
	 */
	protected void onOverlayClosed(IPartialPageRequestHandler target)
	{
	}

	/**
	 * Queries the rows of the current page again, has the browser repaint all of them from the
	 * response and re-renders the toolbars.
	 *
	 * @param target
	 *            the handler of the current Ajax or web socket request
	 */
	public void refresh(IPartialPageRequestHandler target)
	{
		itemCount = null;
		currentPageRows = null;
		refreshToolbars(target);
		target.appendJavaScript("if (" + getVarName() + ") { " + getVarName() + ".update(" +
			currentPageJson() + "); }");
	}

	private void refreshToolbars(IPartialPageRequestHandler target)
	{
		for (AbstractDynamicToolbar toolbar : getToolbars())
		{
			toolbar.refresh(target);
		}
	}

	private List<AbstractDynamicToolbar> getToolbars()
	{
		List<AbstractDynamicToolbar> toolbars = new ArrayList<>();
		for (String id : List.of("topNavigation", "topSelection", "bottomSelection",
			"bottomNavigation"))
		{
			if (get(id) instanceof AbstractDynamicToolbar toolbar)
			{
				toolbars.add(toolbar);
			}
		}
		for (Component toolbar : topToolbars)
		{
			toolbars.add((AbstractDynamicToolbar)toolbar);
		}
		for (Component toolbar : bottomToolbars)
		{
			toolbars.add((AbstractDynamicToolbar)toolbar);
		}
		return toolbars;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		Behavior veil = newVeilBehavior();
		if (veil != null)
		{
			add(veil);
		}
		add(checkToolbar(newNavigationToolbar("topNavigation", ToolbarPosition.TOP),
			"topNavigation").add(new PositionBehavior(true, ToolbarPosition.TOP)));
		add(checkToolbar(newSelectionToolbar("topSelection", ToolbarPosition.TOP), "topSelection")
			.add(new PositionBehavior(false, ToolbarPosition.TOP)));
		add(checkToolbar(newSelectionToolbar("bottomSelection", ToolbarPosition.BOTTOM),
			"bottomSelection").add(new PositionBehavior(false, ToolbarPosition.BOTTOM)));
		add(checkToolbar(newNavigationToolbar("bottomNavigation", ToolbarPosition.BOTTOM),
			"bottomNavigation").add(new PositionBehavior(true, ToolbarPosition.BOTTOM)));
		if (columnStateStore != null)
		{
			ColumnState state = columnStateStore.load(getColumnStateKey());
			if (state != null)
			{
				setColumnState(state);
			}
		}
	}

	/**
	 * Creates the behavior blocking the table while one of its Ajax requests runs, for example
	 * while a row is saved or deleted from the overlay, or a page is fetched: a
	 * {@link LocalVeilBehavior} by default, which shows a spinner over the table when a request
	 * takes a while.
	 *
	 * @return the behavior, or {@code null} to leave the table usable during its requests
	 * @since 11.0.0
	 */
	protected Behavior newVeilBehavior()
	{
		return new LocalVeilBehavior();
	}

	/**
	 * Allows a built-in toolbar to be shown only where the table's settings want it.
	 */
	private class PositionBehavior extends Behavior
	{
		private static final long serialVersionUID = 1L;

		private final boolean navigation;

		private final ToolbarPosition position;

		PositionBehavior(boolean navigation, ToolbarPosition position)
		{
			this.navigation = navigation;
			this.position = position;
		}

		@Override
		public void onConfigure(Component component)
		{
			component.setVisibilityAllowed(isShown(
				navigation ? navigationPosition : selectionToolbarPosition, position));
		}
	}

	private static AbstractDynamicToolbar checkToolbar(AbstractDynamicToolbar toolbar, String id)
	{
		Args.notNull(toolbar, "toolbar");
		if (id.equals(toolbar.getId()) == false)
		{
			throw new IllegalStateException("The toolbar must have the id '" + id + "'");
		}
		return toolbar;
	}

	private static boolean isShown(ToolbarPosition setting, ToolbarPosition position)
	{
		return setting == position || setting == ToolbarPosition.BOTH;
	}

	/**
	 * Creates a navigation toolbar of the table, a {@link DynamicNavigationToolbar} by default.
	 * Override it to customize the navigation, for example with a filter in front of it.
	 *
	 * @param id
	 *            the id the toolbar must have
	 * @param position
	 *            whether the toolbar is above or below the rows, {@link ToolbarPosition#TOP} or
	 *            {@link ToolbarPosition#BOTTOM}
	 * @return the toolbar
	 */
	protected AbstractDynamicToolbar newNavigationToolbar(String id, ToolbarPosition position)
	{
		return new DynamicNavigationToolbar<>(id, this, position);
	}

	/**
	 * Creates a selection bar of the table, a {@link DynamicSelectionToolbar} by default.
	 *
	 * @param id
	 *            the id the toolbar must have
	 * @param position
	 *            whether the toolbar is above or below the rows, {@link ToolbarPosition#TOP} or
	 *            {@link ToolbarPosition#BOTTOM}
	 * @return the toolbar
	 */
	protected AbstractDynamicToolbar newSelectionToolbar(String id, ToolbarPosition position)
	{
		return new DynamicSelectionToolbar(id, this);
	}

	/**
	 * Adds a toolbar above the headers, below the built-in ones.
	 *
	 * @param toolbar
	 *            the toolbar, created with {@link AbstractDynamicToolbar#AbstractDynamicToolbar(
	 *            DynamicDataTable) the table}
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> addTopToolbar(AbstractDynamicToolbar toolbar)
	{
		topToolbars.add(Args.notNull(toolbar, "toolbar"));
		return this;
	}

	/**
	 * Adds a toolbar below the rows, above the built-in ones.
	 *
	 * @param toolbar
	 *            the toolbar, created with {@link AbstractDynamicToolbar#AbstractDynamicToolbar(
	 *            DynamicDataTable) the table}
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> addBottomToolbar(AbstractDynamicToolbar toolbar)
	{
		bottomToolbars.add(Args.notNull(toolbar, "toolbar"));
		return this;
	}

	/**
	 * @return a new id for a toolbar of this table
	 */
	public String newToolbarId()
	{
		return "toolbar" + toolbarCount++;
	}

	/**
	 * Adds an action on the whole table to its navigation, after the paging links.
	 *
	 * @param action
	 *            the action
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> addToolbarAction(IDynamicToolbarAction<T, K> action)
	{
		toolbarActions.add(Args.notNull(action, "action"));
		return this;
	}

	/**
	 * @return the actions on the whole table
	 */
	public List<IDynamicToolbarAction<T, K>> getToolbarActions()
	{
		return List.copyOf(toolbarActions);
	}

	/**
	 * @return every column, shown or hidden, in display order
	 */
	public List<IDynamicColumn<T>> getColumns()
	{
		return List.copyOf(columns);
	}

	/**
	 * @return the columns the table shows, in display order
	 * @since 11.0.0
	 */
	public List<IDynamicColumn<T>> getVisibleColumns()
	{
		List<IDynamicColumn<T>> visible = new ArrayList<>(columns.size());
		for (IDynamicColumn<T> column : columns)
		{
			if (hiddenColumns.contains(column) == false)
			{
				visible.add(column);
			}
		}
		return visible;
	}

	/**
	 * @param column
	 *            a column of the table
	 * @return whether the table shows the column
	 * @since 11.0.0
	 */
	public boolean isColumnVisible(IDynamicColumn<T> column)
	{
		return hiddenColumns.contains(column) == false;
	}

	/**
	 * Shows or hides a column, whether or not it is {@link #isHideable(IDynamicColumn)
	 * hideable}. Re-render the table for the change to show.
	 *
	 * @param column
	 *            a column of the table
	 * @param visible
	 *            whether the table shows the column
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> setColumnVisible(IDynamicColumn<T> column, boolean visible)
	{
		indexOfColumn(column);
		if (visible)
		{
			hiddenColumns.remove(column);
		}
		else
		{
			hiddenColumns.add(column);
		}
		return this;
	}

	/**
	 * @param column
	 *            a column of the table
	 * @return whether the user can hide the column: whether it, or the column a
	 *         {@link SortableDynamicColumn} wraps, implements {@link IHideableColumn}
	 * @since 11.0.0
	 */
	public boolean isHideable(IDynamicColumn<T> column)
	{
		return column instanceof IHideableColumn ||
			(column instanceof SortableDynamicColumn<T, ?> sortable &&
				sortable.getColumn() instanceof IHideableColumn);
	}

	/**
	 * @param column
	 *            a column of the table
	 * @return whether the user can move the column: whether it, or the column a
	 *         {@link SortableDynamicColumn} wraps, implements {@link IMovableColumn}
	 * @since 11.0.0
	 */
	public boolean isMovable(IDynamicColumn<T> column)
	{
		return column instanceof IMovableColumn ||
			(column instanceof SortableDynamicColumn<T, ?> sortable &&
				sortable.getColumn() instanceof IMovableColumn);
	}

	/**
	 * Moves a column to another place, whether or not it is {@link #isMovable(IDynamicColumn)
	 * movable}, taking its width and proportion along. Re-render the table for the change to show.
	 *
	 * @param column
	 *            a column of the table
	 * @param index
	 *            the index the column has in {@link #getColumns()} afterwards
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> moveColumn(IDynamicColumn<T> column, int index)
	{
		int from = indexOfColumn(column);
		Args.withinRange(0, columns.size() - 1, index, "index");
		columns.add(index, columns.remove(from));
		columnWidths = move(columnWidths, from, index);
		columnProportions = move(columnProportions, from, index);
		return this;
	}

	private static double[] move(double[] values, int from, int to)
	{
		if (values == null)
		{
			return null;
		}
		List<Double> list = new ArrayList<>(values.length);
		for (double value : values)
		{
			list.add(value);
		}
		list.add(to, list.remove(from));
		return list.stream().mapToDouble(Double::doubleValue).toArray();
	}

	/**
	 * @return the index of the column in {@link #getColumns()}
	 * @throws IllegalArgumentException
	 *             if the column is not one of the table
	 */
	private int indexOfColumn(IDynamicColumn<T> column)
	{
		for (int i = 0; i < columns.size(); i++)
		{
			if (columns.get(i) == column)
			{
				return i;
			}
		}
		throw new IllegalArgumentException("The column is not one of the table");
	}

	/**
	 * @param column
	 *            a column of the table
	 * @return the id of the column in a {@link ColumnState}: its {@link IDynamicColumn#getId()},
	 *         or else its index in the list of columns the table was constructed with. Give the
	 *         columns ids when their states are kept, for example in a database, across changes
	 *         to the columns of the table.
	 * @throws IllegalArgumentException
	 *             if the column is not one of the table
	 * @since 11.0.0
	 */
	public final String getColumnId(IDynamicColumn<T> column)
	{
		if (column.getId() != null)
		{
			indexOfColumn(column);
			return column.getId();
		}
		for (int i = 0; i < initialColumns.size(); i++)
		{
			if (initialColumns.get(i) == column)
			{
				return String.valueOf(i);
			}
		}
		throw new IllegalArgumentException("The column is not one of the table");
	}

	/**
	 * @throws IllegalArgumentException
	 *             if two columns have the same id
	 */
	private void checkColumnIds()
	{
		Set<String> ids = new HashSet<>();
		for (IDynamicColumn<T> column : initialColumns)
		{
			String columnId = getColumnId(column);
			Args.isTrue(ids.add(columnId), "The id of every column has to be unique, '%s' is not",
				columnId);
		}
	}

	/**
	 * @return the key the table's {@link ColumnState} is saved under in its
	 *         {@link #setColumnStateStore(IColumnStateStore) store}: by default the class of the
	 *         page and the path of the table in it. Override to tell apart tables a page shows
	 *         for different purposes, or to share a state between pages.
	 * @since 11.0.0
	 */
	public String getColumnStateKey()
	{
		return getPage().getClass().getName() + ":" + getPageRelativePath();
	}

	/**
	 * @return the store keeping the table's {@link ColumnState}, or {@code null} if it is kept
	 *         only as long as the table
	 * @since 11.0.0
	 */
	public IColumnStateStore getColumnStateStore()
	{
		return columnStateStore;
	}

	/**
	 * Sets the store keeping the table's {@link ColumnState}. The table loads its state from the
	 * store when it is initialized, so the store has to be set before, and saves it whenever the
	 * user moves, shows, hides or resizes a column.
	 *
	 * @param columnStateStore
	 *            the store, for example {@link SessionColumnStateStore}; {@code null}, the
	 *            default, to keep the state only as long as the table
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> setColumnStateStore(IColumnStateStore columnStateStore)
	{
		this.columnStateStore = columnStateStore;
		return this;
	}

	/**
	 * @return the order, the visibility and the known widths of the columns
	 * @throws IllegalArgumentException
	 *             if two columns have the same id
	 * @since 11.0.0
	 */
	public ColumnState getColumnState()
	{
		checkColumnIds();
		List<String> order = new ArrayList<>(columns.size());
		List<String> hidden = new ArrayList<>();
		Map<String, Double> widths = new LinkedHashMap<>();
		for (int i = 0; i < columns.size(); i++)
		{
			IDynamicColumn<T> column = columns.get(i);
			String id = getColumnId(column);
			order.add(id);
			if (hiddenColumns.contains(column))
			{
				hidden.add(id);
			}
			if (columnWidths != null && columnWidths[i] > 0)
			{
				widths.put(id, columnWidths[i]);
			}
		}
		return new ColumnState(order, hidden, widths);
	}

	/**
	 * Arranges the columns as a state says, as far as the user could have: the
	 * {@link #isMovable(IDynamicColumn) movable} columns take the order of the state among the
	 * places of the movable columns, the {@link #isHideable(IDynamicColumn) hideable} ones are
	 * shown or hidden, and the columns get the widths of the state, which must be positive and at
	 * most {@value #MAX_COLUMN_WIDTH} pixels. Ids the table does not know are ignored, and columns
	 * the state does not know keep their places after the known ones. Re-render the table for the
	 * change to show.
	 *
	 * @param state
	 *            the state
	 * @return {@code this}
	 * @throws IllegalArgumentException
	 *             if two columns have the same id
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> setColumnState(ColumnState state)
	{
		Args.notNull(state, "state");
		checkColumnIds();
		List<String> order = state.getOrder();
		List<IDynamicColumn<T>> movable = new ArrayList<>(columns.stream().filter(this::isMovable)
			.toList());
		movable.sort(Comparator.comparingInt(column -> {
			int rank = order.indexOf(getColumnId(column));
			return rank >= 0 ? rank : order.size();
		}));
		Iterator<IDynamicColumn<T>> placed = movable.iterator();
		List<IDynamicColumn<T>> arranged = new ArrayList<>(columns.size());
		for (IDynamicColumn<T> column : columns)
		{
			arranged.add(isMovable(column) ? placed.next() : column);
		}
		double[] widths = new double[columns.size()];
		boolean anyWidth = false;
		for (int i = 0; i < arranged.size(); i++)
		{
			IDynamicColumn<T> column = arranged.get(i);
			Double width = state.getWidths().get(getColumnId(column));
			if (width != null && Double.isFinite(width) && width > 0 && width <= MAX_COLUMN_WIDTH)
			{
				widths[i] = width;
				anyWidth = true;
			}
			else if (columnWidths != null)
			{
				widths[i] = columnWidths[indexOfColumn(column)];
				anyWidth |= widths[i] > 0;
			}
		}
		if (columnProportions != null)
		{
			double[] proportions = new double[columns.size()];
			for (int i = 0; i < arranged.size(); i++)
			{
				proportions[i] = columnProportions[indexOfColumn(arranged.get(i))];
			}
			columnProportions = proportions;
		}
		columnWidths = anyWidth ? widths : null;
		columns.clear();
		columns.addAll(arranged);
		for (IDynamicColumn<T> column : columns)
		{
			if (isHideable(column))
			{
				setColumnVisible(column, state.getHidden().contains(getColumnId(column)) == false);
			}
		}
		if (getVisibleColumns().isEmpty())
		{
			hiddenColumns.clear();
		}
		return this;
	}

	/**
	 * Saves the state of the columns in the store, if any.
	 */
	private void saveColumnState()
	{
		if (columnStateStore != null)
		{
			columnStateStore.save(getColumnStateKey(), getColumnState());
		}
	}

	/**
	 * Called after the user moved, showed or hid columns, before the table is re-rendered. Does
	 * nothing by default; override to keep the columns' order and visibility elsewhere, such as in
	 * a user's preferences.
	 *
	 * @param target
	 *            the Ajax request target
	 * @since 11.0.0
	 */
	protected void onColumnsChanged(AjaxRequestTarget target)
	{
	}

	/**
	 * @return whether the rows end with an empty cell, leaving room after the last column, which
	 *         is the case while the columns can be resized or moved, and for the vertical scroll
	 *         bar of a body with a height
	 */
	private boolean hasFiller()
	{
		return isResizable() || (isMoving() && hasMovableColumns()) || bodyHeight > 0;
	}

	/**
	 * @return the number of cells in a row: one per shown column, and the empty one at the end
	 *         while the columns can be resized or moved or the body has a height; the
	 *         {@code colspan} of a cell spanning a whole row, as in a toolbar
	 * @since 11.0.0
	 */
	public int getCellCount()
	{
		return getVisibleColumns().size() + (hasFiller() ? 1 : 0);
	}

	/**
	 * @return whether the table has a {@link ResizableColumnsBehavior}
	 */
	private boolean isResizable()
	{
		return getBehaviors(ResizableColumnsBehavior.class).isEmpty() == false;
	}

	/**
	 * @return whether the table has a {@link MovableColumnsBehavior}
	 */
	private boolean isMoving()
	{
		return getBehaviors(MovableColumnsBehavior.class).isEmpty() == false;
	}

	/**
	 * @return whether a column the table shows can be moved
	 */
	private boolean hasMovableColumns()
	{
		return getVisibleColumns().stream().anyMatch(this::isMovable);
	}

	/**
	 * Moves a column as the browser reported it, as two indexes separated by a comma.
	 *
	 * @return whether the column was moved
	 */
	/**
	 * Moves the column shown at {@code from} in front of the one shown at {@code to}, or to the end
	 * if {@code to} is the number of shown columns, as long as it is movable and the columns that
	 * are not keep their places.
	 *
	 * @return whether the column was moved
	 */
	private boolean moveVisibleColumn(int from, int to)
	{
		List<IDynamicColumn<T>> visible = getVisibleColumns();
		if (from < 0 || from >= visible.size() || to < 0 || to > visible.size() || to == from ||
			to == from + 1 || isMovable(visible.get(from)) == false)
		{
			return false;
		}
		IDynamicColumn<T> column = visible.get(from);
		List<IDynamicColumn<T>> moved = new ArrayList<>(visible);
		moved.remove(from);
		moved.add(to > from ? to - 1 : to, column);
		for (int i = 0; i < visible.size(); i++)
		{
			if (isMovable(visible.get(i)) == false && moved.get(i) != visible.get(i))
			{
				return false;
			}
		}
		int index = to < visible.size() ? indexOfColumn(visible.get(to))
			: indexOfColumn(visible.get(visible.size() - 1)) + 1;
		moveColumn(column, index > indexOfColumn(column) ? index - 1 : index);
		return true;
	}

	/**
	 * Shows the columns at the given indexes of {@link #getColumns()} and hides the other
	 * {@link #isHideable(IDynamicColumn) hideable} ones, unless that leaves no column shown.
	 *
	 * @return whether the columns were changed
	 */
	private boolean showColumns(String indexes)
	{
		Set<IDynamicColumn<T>> shown = Collections.newSetFromMap(new IdentityHashMap<>());
		for (String index : Strings.isEmpty(indexes) ? new String[0] : indexes.split(","))
		{
			try
			{
				shown.add(columns.get(Integer.parseInt(index.trim())));
			}
			catch (NumberFormatException | IndexOutOfBoundsException e)
			{
				return false;
			}
		}
		Set<IDynamicColumn<T>> hidden = Collections.newSetFromMap(new IdentityHashMap<>());
		for (IDynamicColumn<T> column : columns)
		{
			boolean hide = isHideable(column) ? shown.contains(column) == false
				: hiddenColumns.contains(column);
			if (hide)
			{
				hidden.add(column);
			}
		}
		if (hidden.size() == columns.size() || hidden.equals(hiddenColumns))
		{
			return false;
		}
		hiddenColumns.clear();
		hiddenColumns.addAll(hidden);
		return true;
	}

	/**
	 * @return where the selection bar is shown, {@link ToolbarPosition#TOP} by default
	 */
	public ToolbarPosition getSelectionToolbarPosition()
	{
		return selectionToolbarPosition;
	}

	/**
	 * @param selectionToolbarPosition
	 *            where the selection bar is shown, when it is
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setSelectionToolbarPosition(
		ToolbarPosition selectionToolbarPosition)
	{
		this.selectionToolbarPosition = Args.notNull(selectionToolbarPosition,
			"selectionToolbarPosition");
		return this;
	}

	/**
	 * @return where the navigation is shown, {@link ToolbarPosition#BOTTOM} by default
	 */
	public ToolbarPosition getNavigationPosition()
	{
		return navigationPosition;
	}

	/**
	 * @param navigationPosition
	 *            where the navigation is shown when there is more than one page
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setNavigationPosition(ToolbarPosition navigationPosition)
	{
		this.navigationPosition = Args.notNull(navigationPosition, "navigationPosition");
		return this;
	}

	/**
	 * @return the numbers of rows per page the user can choose from, empty if there is no choice
	 */
	public List<Long> getItemsPerPageOptions()
	{
		return List.copyOf(itemsPerPageOptions);
	}

	/**
	 * Lets the user choose the number of rows per page from a drop-down in the navigation, which
	 * is then shown even when there is only one page.
	 *
	 * @param options
	 *            the numbers of rows per page to choose from, positive; none for no choice
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setItemsPerPageOptions(Long... options)
	{
		List<Long> checked = new ArrayList<>();
		for (Long option : options)
		{
			checked.add(Args.withinRange(1L, Long.MAX_VALUE, option, "option"));
		}
		itemsPerPageOptions = checked;
		return this;
	}

	/**
	 * @return the widths of the columns in pixels, one per column of {@link #getColumns()}, as last
	 *         resized by the user, or {@code null} if they were not resized; 0 for a column whose
	 *         width is not known, because it was hidden when the user resized the others
	 */
	public double[] getColumnWidths()
	{
		return columnWidths != null ? columnWidths.clone() : null;
	}

	/**
	 * @param widths
	 *            the widths of the columns in pixels, one per column of {@link #getColumns()}, for
	 *            example restored from a user's preferences; {@code null} to let the browser lay
	 *            out the columns. The widths are applied while every shown column has a positive
	 *            one.
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setColumnWidths(double... widths)
	{
		if (widths != null)
		{
			Args.isTrue(widths.length == columns.size(), "one width per column is needed");
		}
		columnWidths = widths != null ? widths.clone() : null;
		return this;
	}

	/**
	 * @return the relative widths of the columns, or {@code null} to size them by their content
	 * @since 11.0.0
	 */
	public double[] getColumnProportions()
	{
		return columnProportions != null ? columnProportions.clone() : null;
	}

	/**
	 * Sizes the columns in proportion to each other, for example {@code 1, 2, 1} to make the
	 * second column twice as wide as the others. The proportions set the initial layout: widths
	 * the user resized the columns to, or set with {@link #setColumnWidths(double...)}, take
	 * precedence.
	 *
	 * @param proportions
	 *            one positive number per column of {@link #getColumns()}; {@code null} to size the
	 *            columns by their content
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> setColumnProportions(double... proportions)
	{
		if (proportions != null)
		{
			Args.isTrue(proportions.length == columns.size(), "one proportion per column is needed");
			for (double proportion : proportions)
			{
				Args.isTrue(Double.isFinite(proportion) && proportion > 0,
					"proportions have to be positive numbers");
			}
		}
		columnProportions = proportions != null ? proportions.clone() : null;
		return this;
	}

	/**
	 * @return whether the table initially takes the full width of its parent, {@code false} by
	 *         default
	 * @since 11.0.0
	 */
	public boolean isAdjustToParentWidth()
	{
		return adjustToParentWidth;
	}

	/**
	 * Makes the table take the full width of the element it is in, sharing it among the columns
	 * by their {@link #setColumnProportions(double...) proportions} or, without proportions, by
	 * their content. While the user has not resized a column, the table follows changes of the
	 * window's size.
	 *
	 * @param adjustToParentWidth
	 *            whether the table takes the full width of its parent
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> setAdjustToParentWidth(boolean adjustToParentWidth)
	{
		this.adjustToParentWidth = adjustToParentWidth;
		return this;
	}

	/**
	 * @return the height of the body of the table in {@link #getBodyHeightUnits()}, or a negative
	 *         number, {@code -1} by default, when the body is as high as its rows
	 * @since 11.0.0
	 */
	public int getBodyHeight()
	{
		return bodyHeight;
	}

	/**
	 * @return the unit of {@link #getBodyHeight()}, {@link BodyHeightUnits#PIXELS} by default
	 * @since 11.0.0
	 */
	public BodyHeightUnits getBodyHeightUnits()
	{
		return bodyHeightUnits;
	}

	/**
	 * Gives the body of the table, the rows between the headers and the footer, a fixed height.
	 * Only the body scrolls, vertically, its scroll bar next to the rows, while the headers, the
	 * toolbars and the footer stay where they are. Fewer rows leave the rest of the height empty,
	 * so the table keeps its height whatever the page shows. A table wider than its space is as
	 * wide as its columns, as without a body height. In the browser, the rows are put into a
	 * table of their own, in a {@code div} with the class {@code dynamic-data-table-body} in the
	 * table's body, and they end with an empty cell, which leaves room for the vertical scroll
	 * bar.
	 *
	 * @param height
	 *            the height of the body in the given units; any negative number, such as
	 *            {@code -1}, to let the body take the height of its rows, as by default
	 * @param units
	 *            the units of the height
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public DynamicDataTable<T, K> setBodyHeight(int height, BodyHeightUnits units)
	{
		Args.isTrue(height != 0, "the body height has to be positive, or negative for none");
		bodyHeightUnits = Args.notNull(units, "units");
		bodyHeight = height;
		return this;
	}

	/**
	 * Called after the user resized a column and the new widths are stored. Does nothing by
	 * default; override to keep the widths elsewhere, such as in a user's preferences.
	 *
	 * @param target
	 *            the Ajax request target
	 * @param widths
	 *            the widths of the columns in pixels, as returned by {@link #getColumnWidths()}
	 */
	protected void onColumnsResized(AjaxRequestTarget target, double[] widths)
	{
	}

	/**
	 * Stores the widths the user resized the shown columns to, as reported by a
	 * {@link ResizableColumnsBehavior}, in {@link #getColumnWidths()} and the column state, unless
	 * there is not one for each shown column, and calls
	 * {@link #onColumnsResized(AjaxRequestTarget, double[])}.
	 */
	@Override
	public void columnsResized(AjaxRequestTarget target, double[] widths)
	{
		List<IDynamicColumn<T>> visible = getVisibleColumns();
		if (widths == null || widths.length != visible.size())
		{
			return;
		}
		double[] all = columnWidths != null ? columnWidths.clone() : new double[columns.size()];
		for (int i = 0; i < widths.length; i++)
		{
			all[indexOfColumn(visible.get(i))] = widths[i];
		}
		columnWidths = all;
		saveColumnState();
		onColumnsResized(target, all.clone());
	}

	/**
	 * @return the widths of the shown columns, from {@link #getColumnWidths()}, or {@code null}
	 *         if not all of them are known
	 */
	@Override
	public double[] getShownColumnWidths()
	{
		double[] widths = visibleValues(columnWidths, getVisibleColumns());
		return widths != null && Arrays.stream(widths).allMatch(width -> width > 0) ? widths
			: null;
	}

	/**
	 * @return whether each shown column is {@link #isMovable(IDynamicColumn) movable}
	 */
	@Override
	public boolean[] getMovableColumns()
	{
		List<IDynamicColumn<T>> visible = getVisibleColumns();
		boolean[] movable = new boolean[visible.size()];
		for (int i = 0; i < movable.length; i++)
		{
			movable[i] = isMovable(visible.get(i));
		}
		return movable;
	}

	/**
	 * Moves a shown column as reported by a {@link MovableColumnsBehavior}, saves the column state,
	 * calls {@link #onColumnsChanged(AjaxRequestTarget)} and renders the table again.
	 */
	@Override
	public void moveColumn(AjaxRequestTarget target, int from, int to)
	{
		if (moveVisibleColumn(from, to))
		{
			saveColumnState();
			onColumnsChanged(target);
			target.add(this);
		}
	}

	/**
	 * @return the rows the user selected, a view that follows the selection as it changes
	 */
	public ISelection<T, K> getSelection()
	{
		return new Selection();
	}

	/**
	 * Deselects every row. The browser shows it once the table is {@link #refresh refreshed}.
	 *
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> clearSelection()
	{
		allSelected = false;
		selectedKeys.clear();
		return this;
	}

	/**
	 * Called after the user changed the selection. Does nothing by default.
	 *
	 * @param target
	 *            the Ajax request target
	 */
	protected void onSelectionChanged(AjaxRequestTarget target)
	{
	}

	/**
	 * @param row
	 *            a row
	 * @return the key of the row as the browser shows it, {@link IDynamicDataProvider#formatKey
	 *         formatted} by the provider
	 */
	public String getRowKey(T row)
	{
		return provider.formatKey(provider.keyOf(row));
	}

	/**
	 * Resolves a row from a key as the browser shows it, for example one sent with a request.
	 *
	 * @param key
	 *            the key as the browser shows it, possibly not a key at all
	 * @return the row, or {@code null} if there is none for the key
	 */
	public T findRow(String key)
	{
		K parsed = provider.parseKey(key);
		return parsed != null ? provider.findByKey(parsed) : null;
	}

	private boolean isSelected(K key)
	{
		return allSelected || selectedKeys.contains(key);
	}

	private void selectionAction(AjaxRequestTarget target, String action, K key)
	{
		if (SELECT_ACTION.equals(action) || DESELECT_ACTION.equals(action))
		{
			if (key == null || provider.findByKey(key) == null)
			{
				return;
			}
			leaveAllSelected();
			if (SELECT_ACTION.equals(action))
			{
				selectedKeys.add(key);
			}
			else
			{
				selectedKeys.remove(key);
			}
		}
		else if (SELECT_PAGE_ACTION.equals(action))
		{
			leaveAllSelected();
			selectedKeys.addAll(getCurrentPageKeys());
		}
		else if (DESELECT_PAGE_ACTION.equals(action))
		{
			leaveAllSelected();
			getCurrentPageKeys().forEach(selectedKeys::remove);
		}
		else
		{
			return;
		}
		selectionChanged(target);
	}

	/**
	 * Turns a selection of all rows into one of the rows the browser shows checked, the rows of
	 * the current page.
	 */
	private void leaveAllSelected()
	{
		if (allSelected)
		{
			allSelected = false;
			selectedKeys.clear();
			selectedKeys.addAll(getCurrentPageKeys());
		}
	}

	void selectAll(AjaxRequestTarget target, boolean all)
	{
		allSelected = all;
		selectedKeys.clear();
		target.appendJavaScript("if (" + getVarName() + ") { " + getVarName() + ".selectRows(" +
			all + "); }");
		selectionChanged(target);
	}

	private void selectionChanged(AjaxRequestTarget target)
	{
		((AbstractDynamicToolbar)get("topSelection")).refresh(target);
		((AbstractDynamicToolbar)get("bottomSelection")).refresh(target);
		onSelectionChanged(target);
	}

	boolean isAllSelected()
	{
		return allSelected;
	}

	/**
	 * @return the rows of the current page, queried once per request
	 */
	private List<T> getCurrentPageRows()
	{
		if (currentPageRows == null)
		{
			long first = getCurrentPage() * itemsPerPage;
			long count = Math.min(itemsPerPage, getItemCount() - first);
			currentPageRows = count > 0 ? new ArrayList<>(provider.rows(first, count)) : List.of();
		}
		return currentPageRows;
	}

	List<K> getCurrentPageKeys()
	{
		List<K> keys = new ArrayList<>();
		for (T row : getCurrentPageRows())
		{
			keys.add(provider.keyOf(row));
		}
		return keys;
	}

	/**
	 * @return the rows of the current page as the browser reads them, with the paging state, and
	 *         calls {@link #onRowsSent(List)}
	 */
	private String currentPageJson()
	{
		List<T> rows = getCurrentPageRows();
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("rows", toRowEntries(rows, getCurrentPage() * itemsPerPage + 1));
		data.put("itemCount", getItemCount());
		data.put("currentPage", getCurrentPage());
		String json = jsonSerializer.toJson(data);
		onRowsSent(getCurrentPageKeys());
		return json;
	}

	/**
	 * Called after the rows of the current page were sent to the browser, which shows them from
	 * now on, for example to push updates only for these rows. Does nothing by default.
	 *
	 * @param keys
	 *            the keys of the rows sent, in display order
	 */
	protected void onRowsSent(List<K> keys)
	{
	}

	/**
	 * Returns every row of the provider in its order, read lazily a chunk at a time while
	 * iterating. Iterate it within the request that fetched it.
	 *
	 * @return the rows
	 */
	public Iterable<T> fetchAllRows()
	{
		return AllRowsIterator::new;
	}

	/**
	 * @return whether every row of the current page is selected and there are more rows than the
	 *         page shows, so that selecting all of them is worth offering
	 */
	boolean isPageSelected()
	{
		if (selectedKeys.isEmpty())
		{
			return false;
		}
		List<K> keys = getCurrentPageKeys();
		return keys.isEmpty() == false && getItemCount() > keys.size() &&
			selectedKeys.containsAll(keys);
	}

	private void populateHeader(ListItem<IDynamicColumn<T>> item, IDynamicColumn<T> column)
	{
		AjaxLink<Void> sort = new AjaxLink<>("sort")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				boolean sortable = isSortable(column);
				setEnabled(sortable);
				setOutputMarkupId(sortable);
				setRenderBodyOnly(sortable == false);
			}

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				sort(column, target);
			}
		};
		item.add(sort);
		sort.add(column.newHeader("header"));
		item.add(new WebMarkupContainer("move")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(isMoving() && isMovable(column));
			}

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				String label = getString("MovableColumnsBehavior.moveColumn", null,
					"Move the column");
				tag.put("title", label);
				tag.put("aria-label", label);
			}
		});
		item.add(new AjaxLink<Void>("arrows")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(isSortable(column));
			}

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				sort(column, target);
			}
		});

		if (column.getCssClass() != null)
		{
			item.add(AttributeModifier.append("class", column.getCssClass()));
		}
		item.add(AttributeModifier.append("class", () -> SORT_CSS_CLASSES.get(getSortOrder(column))));
		item.add(AttributeModifier.replace("data-resizable",
			() -> column.isResizable() ? null : "false"));
		item.add(AttributeModifier.replace("data-movable",
			() -> isMoving() && isMovable(column) ? "true" : null));
		item.add(AttributeModifier.replace("aria-sort",
			() -> ARIA_SORT.get(getSortOrder(column))));
	}

	@SuppressWarnings("unchecked")
	private ISortState<Object> getSortState()
	{
		return provider instanceof ISortStateLocator<?> locator
			? (ISortState<Object>)locator.getSortState() : null;
	}

	private boolean isSortable(IDynamicColumn<T> column)
	{
		return getSortState() != null && column instanceof ISortableDynamicColumn<?, ?> sortable &&
			sortable.isSortable();
	}

	/**
	 * @return the order the rows are sorted in by the column, or {@code null} if the column is
	 *         not sortable
	 */
	private SortOrder getSortOrder(IDynamicColumn<T> column)
	{
		if (isSortable(column) == false)
		{
			return null;
		}
		return getSortState().getPropertySortOrder(
			((ISortableDynamicColumn<?, ?>)column).getSortProperty());
	}

	private void sort(IDynamicColumn<T> column, AjaxRequestTarget target)
	{
		if (isSortable(column) == false)
		{
			return;
		}
		getSortState().setPropertySortOrder(
			((ISortableDynamicColumn<?, ?>)column).getSortProperty(),
			getSortOrder(column) == SortOrder.ASCENDING ? SortOrder.DESCENDING
				: SortOrder.ASCENDING);
		setCurrentPage(0);
		boolean selected = getSelection().isEmpty() == false;
		clearSelection();
		target.add(headersRow);
		refresh(target);
		if (selected)
		{
			onSelectionChanged(target);
		}
	}

	/**
	 * Makes the browser fetch the rows of the current page again by pushing a
	 * {@link #newRefreshMessage() refresh message} over the page's web socket connection. The
	 * table has to {@link #setPushEnabled(boolean) accept pushes}.
	 *
	 * @return {@code false} if the page has no open web socket connection, {@code true} if the
	 *         message was sent
	 * @throws IOException
	 *             if the message cannot be sent
	 * @see IWebSocketLightWeightMessage#send(org.apache.wicket.Component)
	 */
	public boolean refreshViaWebSockets() throws IOException
	{
		return newRefreshMessage().send(this);
	}

	@Override
	public long getCurrentPage()
	{
		return Math.max(0, Math.min(currentPage, getPageCount() - 1));
	}

	@Override
	public void setCurrentPage(long page)
	{
		currentPage = Math.max(0, page);
		currentPageRows = null;
	}

	@Override
	public long getPageCount()
	{
		return (getItemCount() + itemsPerPage - 1) / itemsPerPage;
	}

	@Override
	public long getItemCount()
	{
		if (itemCount == null)
		{
			itemCount = provider.size();
		}
		return itemCount;
	}

	@Override
	public long getItemsPerPage()
	{
		return itemsPerPage;
	}

	@Override
	public void setItemsPerPage(long itemsPerPage)
	{
		this.itemsPerPage = Args.withinRange(1L, Long.MAX_VALUE, itemsPerPage, "itemsPerPage");
		currentPageRows = null;
	}

	/**
	 * Builds a message of the {@link UpdateRowsMessageType update rows} type, replacing the rows of this table with the given
	 * ones. The rows are shown as they are, regardless of the current page.
	 *
	 * @param rows
	 *            the rows to show
	 * @return the message
	 */
	public IWebSocketLightWeightMessage newUpdateRowsMessage(List<T> rows)
	{
		return new UpdateRowsMessage(getMarkupId(),
			toRowEntries(rows, getCurrentPage() * itemsPerPage + 1));
	}

	/**
	 * Builds a message of the {@code refresh} type, making this table fetch the rows of its
	 * current page again.
	 *
	 * @return the message
	 */
	public IWebSocketLightWeightMessage newRefreshMessage()
	{
		return new RefreshMessage(getMarkupId());
	}

	/**
	 * Builds a message of the {@link UpdateRowMessageType update row} type, repainting one row of
	 * this table, if the browser shows it, from the given row.
	 *
	 * @param row
	 *            the row in its current state
	 * @return the message
	 * @see UpdateRowMessageType#newMessage(String, String, Object)
	 */
	public IWebSocketLightWeightMessage newUpdateRowMessage(T row)
	{
		return UpdateRowMessageType.newMessage(getMarkupId(), getRowKey(row), row);
	}

	/**
	 * @param templateEngine
	 *            the engine evaluating the column templates in the browser
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setTemplateEngine(ITemplateEngine templateEngine)
	{
		this.templateEngine = Args.notNull(templateEngine, "templateEngine");
		return this;
	}

	/**
	 * @param jsonSerializer
	 *            the serializer turning rows into JSON
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setJsonSerializer(IJsonSerializer jsonSerializer)
	{
		this.jsonSerializer = Args.notNull(jsonSerializer, "jsonSerializer");
		return this;
	}

	/**
	 * @param pollInterval
	 *            how often the browser fetches the rows again, or {@code null} not to poll
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setPollInterval(Duration pollInterval)
	{
		this.pollInterval = pollInterval;
		return this;
	}

	/**
	 * @param pushEnabled
	 *            whether the table picks up the {@link IWebSocketLightWeightMessage}s addressed to
	 *            it that arrive over Wicket's web socket connection
	 * @return {@code this}
	 */
	public DynamicDataTable<T, K> setPushEnabled(boolean pushEnabled)
	{
		this.pushEnabled = pushEnabled;
		return this;
	}

	/**
	 * @return the serializer turning rows into JSON
	 */
	public IJsonSerializer getJsonSerializer()
	{
		return jsonSerializer;
	}

	/**
	 * @return the provider of the rows
	 */
	public IDynamicDataProvider<T, K> getProvider()
	{
		return provider;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "table");
		super.onComponentTag(tag);
		tag.append("class", CSS_CLASS, " ");
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		CoreLibrariesContributor.contributeAjax(getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
		response.render(CssHeaderItem.forReference(CSS));
		templateEngine.renderHead(response);
		for (IDynamicColumn<T> column : getVisibleColumns())
		{
			if (column instanceof IHeaderContributor contributor)
			{
				contributor.renderHead(response);
			}
		}

		boolean partial = getRequestCycle().find(IPartialPageRequestHandler.class).isPresent();
		response.render(OnDomReadyHeaderItem.forScript("new Wicket.DynamicDataTable(" +
			buildConfig() + ", " +
			actionBehavior.getCallbackFunction(CallbackParameter.explicit(ACTION_PARAMETER),
				CallbackParameter.explicit(KEY_PARAMETER)) +
			", " + navigationBehavior.getCallbackFunction() + ", " + buildMessageHandlers() +
			(partial ? ", " + escapeForScript(currentPageJson()) : "") + ");"));
	}

	/**
	 * Reads a resource template for this table's style, variation and locale. Outside of
	 * development mode the template is read once and cached.
	 */
	String getTemplate(ResourceTemplate template)
	{
		String style = getStyle();
		String variation = getVariation();
		Locale locale = getLocale();
		if (getApplication().usesDevelopmentConfig())
		{
			return template.load(style, variation, locale);
		}
		if (templates == null)
		{
			templates = new HashMap<>();
		}
		return templates.computeIfAbsent(template.getCacheKey(style, variation, locale),
			key -> template.load(style, variation, locale));
	}

	/**
	 * @return the JSON with {@code <}, {@code >} and {@code &} escaped, so it cannot end the script
	 *         or the section of the Ajax response it is written into
	 */
	private static String escapeForScript(String json)
	{
		return json.replace("<", "\\u003c").replace(">", "\\u003e").replace("&", "\\u0026");
	}

	private String buildMessageHandlers()
	{
		StringJoiner handlers = new StringJoiner(", ", "{", "}");
		for (IWebSocketMessageType messageType : messageTypes.values())
		{
			handlers.add(JSONObject.quote(messageType.getTypeId()) + ": " +
				messageType.getTypeFunction());
		}
		return handlers.toString();
	}

	@Override
	protected void onRemove()
	{
		super.onRemove();
		AjaxUtils.executeIfAjaxOrWebSockets(target -> target.appendJavaScript(
			"if (" + getVarName() + ") { " + getVarName() + ".destroy(); }"));
	}

	/**
	 * @return the expression evaluating to the table's client-side instance
	 */
	private String getVarName()
	{
		return "Wicket.DynamicDataTable.instances[" + JSONObject.quote(getMarkupId()) + "]";
	}

	private JSONObject buildConfig()
	{
		List<IDynamicColumn<T>> visible = getVisibleColumns();
		JSONArray columnConfigs = new JSONArray();
		for (IDynamicColumn<T> column : visible)
		{
			JSONObject columnConfig = new JSONObject();
			columnConfig.put("template", column.getTemplate(this));
			columnConfig.put("cssClass", column.getCssClass());
			columnConfigs.put(columnConfig);
		}

		JSONObject config = new JSONObject();
		config.put("id", getMarkupId());
		config.put("dataUrl", dataBehavior.getCallbackUrl().toString());
		config.put("engine", templateEngine.getName());
		config.put("columns", columnConfigs);
		config.put("push", pushEnabled);
		config.put("itemCount", getItemCount());
		config.put("currentPage", getCurrentPage());
		config.put("filler", hasFiller());
		if (bodyHeight > 0)
		{
			JSONObject height = new JSONObject();
			height.put("value", bodyHeight);
			height.put("unit", bodyHeightUnits.getCssUnit());
			config.put("bodyHeight", height);
		}
		if (pollInterval != null)
		{
			config.put("pollMillis", pollInterval.toMillis());
		}
		if (columnProportions != null || adjustToParentWidth)
		{
			JSONObject layout = new JSONObject();
			if (columnProportions != null)
			{
				layout.put("proportions", new JSONArray(visibleValues(columnProportions, visible)));
			}
			layout.put("adjustToParent", adjustToParentWidth);
			config.put("layout", layout);
		}
		if (isResizable())
		{
			JSONObject resize = new JSONObject();
			double[] widths = getShownColumnWidths();
			if (widths != null)
			{
				resize.put("widths", new JSONArray(widths));
			}
			config.put("resize", resize);
		}
		return config;
	}

	/**
	 * @return the values of the shown columns, from values given for every column
	 */
	private double[] visibleValues(double[] values, List<IDynamicColumn<T>> visible)
	{
		if (values == null)
		{
			return null;
		}
		return visible.stream().mapToDouble(column -> values[indexOfColumn(column)]).toArray();
	}

	private List<Map<String, Object>> toRowEntries(List<T> rows, long firstNumber)
	{
		List<Map<String, Object>> entries = new ArrayList<>(rows.size());
		long number = firstNumber;
		for (T row : rows)
		{
			K key = provider.keyOf(row);
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("key", provider.formatKey(key));
			entry.put("number", number++);
			entry.put("selected", isSelected(key));
			entry.put("data", row);
			entries.add(entry);
		}
		return entries;
	}

	@Override
	protected void onDetach()
	{
		itemCount = null;
		currentPageRows = null;
		provider.detach();
		for (IDynamicColumn<T> column : columns)
		{
			column.detach();
		}
		for (IAjaxActionColumnContributor<T> contributor : actionContributors.values())
		{
			contributor.detach();
		}
		super.onDetach();
	}

	abstract static class TableMessage implements IWebSocketLightWeightMessage
	{
		private final String tableId;

		private final String typeId;

		TableMessage(String tableId, String typeId)
		{
			this.tableId = tableId;
			this.typeId = typeId;
		}

		@Override
		public String getTableId()
		{
			return tableId;
		}

		@Override
		public String getTypeId()
		{
			return typeId;
		}
	}

	private static class RefreshMessage extends TableMessage
	{
		RefreshMessage(String tableId)
		{
			super(tableId, RefreshMessageType.TYPE_ID);
		}
	}

	private static class UpdateRowsMessage extends TableMessage
	{
		private final List<Map<String, Object>> rows;

		UpdateRowsMessage(String tableId, List<Map<String, Object>> rows)
		{
			super(tableId, UpdateRowsMessageType.TYPE_ID);
			this.rows = rows;
		}

		public List<Map<String, Object>> getRows()
		{
			return rows;
		}
	}

	static class UpdateRowMessage extends TableMessage
	{
		private final String key;

		private final Object data;

		UpdateRowMessage(String tableId, String key, Object data)
		{
			super(tableId, UpdateRowMessageType.TYPE_ID);
			this.key = key;
			this.data = data;
		}

		public String getKey()
		{
			return key;
		}

		public Object getData()
		{
			return data;
		}
	}

	private class Selection implements ISelection<T, K>
	{
		@Override
		public SelectionType getType()
		{
			return allSelected ? SelectionType.ALL : SelectionType.RANGE;
		}

		@Override
		public List<K> getKeys()
		{
			return allSelected ? List.of() : List.copyOf(selectedKeys);
		}

		@Override
		public boolean isSelected(K key)
		{
			return DynamicDataTable.this.isSelected(key);
		}

		@Override
		public long size()
		{
			return allSelected ? provider.size() : selectedKeys.size();
		}

		@Override
		public Iterable<T> fetch()
		{
			if (allSelected)
			{
				return fetchAllRows();
			}
			List<K> keys = List.copyOf(selectedKeys);
			return () -> keys.stream().map(provider::findByKey).filter(Objects::nonNull).iterator();
		}
	}

	private class AllRowsIterator implements Iterator<T>
	{
		private final long size = provider.size();

		private long next;

		private Iterator<T> chunk = List.<T> of().iterator();

		@Override
		public boolean hasNext()
		{
			if (chunk.hasNext() == false && next < size)
			{
				List<T> rows = provider.rows(next, Math.min(FETCH_CHUNK, size - next));
				next = rows.isEmpty() ? size : next + rows.size();
				chunk = rows.iterator();
			}
			return chunk.hasNext();
		}

		@Override
		public T next()
		{
			if (hasNext() == false)
			{
				throw new NoSuchElementException();
			}
			return chunk.next();
		}
	}

	private class DataBehavior extends AbstractAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void onRequest()
		{
			String json = currentPageJson();
			getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new TextRequestHandler("application/json", "UTF-8", json));
		}
	}

	private class NavigationBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void respond(AjaxRequestTarget target)
		{
			refreshToolbars(target);
		}
	}

	private class ActionBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void respond(AjaxRequestTarget target)
		{
			IRequestParameters parameters = getComponent().getRequest().getRequestParameters();
			String action = parameters.getParameterValue(ACTION_PARAMETER).toOptionalString();
			String key = parameters.getParameterValue(KEY_PARAMETER).toOptionalString();
			if (action == null)
			{
				return;
			}
			if (CLOSE_OVERLAY_ACTION.equals(action))
			{
				closeOverlay(target);
				return;
			}
			if (SHOW_COLUMNS_ACTION.equals(action))
			{
				if (showColumns(key))
				{
					saveColumnState();
					onColumnsChanged(target);
					target.add(DynamicDataTable.this);
				}
				return;
			}
			K parsedKey = provider.parseKey(key);
			if (action.startsWith(RESERVED_ACTION_PREFIX))
			{
				selectionAction(target, action, parsedKey);
				return;
			}
			T row = parsedKey != null ? provider.findByKey(parsedKey) : null;
			onAction(target, action, parsedKey, row);
		}
	}
}
