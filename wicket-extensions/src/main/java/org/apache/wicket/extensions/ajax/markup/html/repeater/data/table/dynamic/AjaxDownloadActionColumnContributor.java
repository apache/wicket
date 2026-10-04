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

import java.nio.charset.StandardCharsets;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior.Location;
import org.apache.wicket.extensions.markup.html.icon.IIcon;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.cookies.CookieDefaults;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * An action that downloads the clicked row as a JSON file, through an
 * {@link AjaxDownloadBehavior} added to the table.
 * <p>
 * The file holds what the table's {@link DynamicDataTable#getJsonSerializer() serializer} produces
 * for the row, looked up again by its key when the download is requested; override
 * {@link #getContent(Object)} and {@link #getContentType()} for another format. The action is
 * shown as a link with the label, or as an icon button with the label as its tooltip once an
 * {@link #setIcon(IIcon) icon} is set. Every
 * {@link AjaxDownloadBehavior} setting is available: the {@link #setLocation(Location) location}
 * (a blob for modern browsers, an iframe, the same window or a new window) and the
 * {@link #setSameSite(CookieDefaults.SameSite) SameSite} attribute of the cookie marking the
 * download as completed. A download in a {@link Location#NewWindow new window} is sent
 * {@link ContentDisposition#INLINE inline}, every other one as an
 * {@link ContentDisposition#ATTACHMENT attachment}.
 *
 * @param <T>
 *            the type of the rows
 * @since 11.0.0
 */
public class AjaxDownloadActionColumnContributor<T> implements IAjaxActionColumnContributor<T>
{
	private static final long serialVersionUID = 1L;

	private final String actionId;

	private final IModel<String> label;

	private final DownloadBehavior download = new DownloadBehavior();

	private DynamicDataTable<T, ?> table;

	private String pendingKey;

	private IIcon icon;

	/**
	 * @param actionId
	 *            the value of the link's {@code data-dt-action} attribute, unique within the
	 *            table
	 * @param label
	 *            the text of the link, written escaped
	 */
	public AjaxDownloadActionColumnContributor(String actionId, IModel<String> label)
	{
		this.actionId = Args.notEmpty(actionId, "actionId");
		this.label = Args.notNull(label, "label");
	}

	@Override
	public String getActionId()
	{
		return actionId;
	}

	/**
	 * Writes a link carrying the action, with the {@link #AjaxDownloadActionColumnContributor
	 * label} escaped.
	 */
	@Override
	public String getTemplate()
	{
		return link(label.getObject());
	}

	/**
	 * Shows the action as an icon button, with the label as its tooltip.
	 *
	 * @param icon
	 *            the icon of the button; {@code null} for a link with the label
	 * @return {@code this}
	 * @since 11.0.0
	 */
	public AjaxDownloadActionColumnContributor<T> setIcon(IIcon icon)
	{
		this.icon = icon;
		return this;
	}

	/**
	 * @return the icon of the button, or {@code null} if the action is a link
	 * @since 11.0.0
	 */
	public IIcon getIcon()
	{
		return icon;
	}

	/**
	 * Like {@link #getTemplate()}, with a label that is an {@link IComponentAssignedModel}, such
	 * as a {@link org.apache.wicket.model.ResourceModel}, resolved against the table.
	 */
	@Override
	public String getTemplate(DynamicDataTable<T, ?> table)
	{
		return link(IconBasedRowAction.resolve(label, table));
	}

	private String link(String text)
	{
		IIcon buttonIcon = getIcon();
		if (buttonIcon != null)
		{
			return IconBasedRowAction.button(actionId, buttonIcon.getMarkup(), text);
		}
		return "<a href=\"#\" data-dt-action=\"" + Strings.escapeMarkup(actionId) + "\">" +
			Strings.escapeMarkup(text) + "</a>";
	}

	@Override
	public void bind(DynamicDataTable<T, ?> table)
	{
		this.table = table;
		table.add(download);
	}

	@Override
	public void onAction(T row, AjaxRequestTarget target)
	{
		pendingKey = table.getRowKey(row);
		download.initiate(target);
	}

	/**
	 * @return where the browser downloads the file, {@link Location#Blob} by default
	 */
	public Location getLocation()
	{
		return download.getLocation();
	}

	/**
	 * @param location
	 *            where the browser downloads the file
	 * @return {@code this}
	 */
	public AjaxDownloadActionColumnContributor<T> setLocation(Location location)
	{
		download.setLocation(location);
		return this;
	}

	/**
	 * @return the SameSite attribute of the cookie marking the download as completed
	 */
	public CookieDefaults.SameSite getSameSite()
	{
		return download.getSameSite();
	}

	/**
	 * @param sameSite
	 *            the SameSite attribute of the cookie marking the download as completed
	 * @return {@code this}
	 */
	public AjaxDownloadActionColumnContributor<T> setSameSite(CookieDefaults.SameSite sameSite)
	{
		download.setSameSite(sameSite);
		return this;
	}

	/**
	 * @param row
	 *            the row being downloaded
	 * @return the name of the file, the {@link DynamicDataTable#getRowKey(Object) row's key} with a
	 *         {@code .json} extension by default
	 */
	protected String getFileName(T row)
	{
		return table.getRowKey(row) + ".json";
	}

	/**
	 * @param row
	 *            the row being downloaded
	 * @return the content of the file, what the table's
	 *         {@link DynamicDataTable#getJsonSerializer() serializer} produces for the row by
	 *         default
	 * @since 11.0.0
	 */
	protected String getContent(T row)
	{
		return table.getJsonSerializer().toJson(row);
	}

	/**
	 * @return the content type of the file, {@code application/json} by default
	 * @since 11.0.0
	 */
	protected String getContentType()
	{
		return "application/json";
	}

	/**
	 * Called when the file was downloaded. Does nothing by default.
	 *
	 * @param target
	 *            the Ajax request target
	 */
	protected void onDownloadSuccess(AjaxRequestTarget target)
	{
	}

	/**
	 * Called when the download failed, for example because the row no longer exists. Not called
	 * for a download in the {@link Location#SameWindow same window}, whose failure the browser
	 * cannot report. Does nothing by default.
	 *
	 * @param target
	 *            the Ajax request target
	 */
	protected void onDownloadFailed(AjaxRequestTarget target)
	{
	}

	@Override
	public void detach()
	{
		label.detach();
	}

	private class DownloadBehavior extends AjaxDownloadBehavior
	{
		private static final long serialVersionUID = 1L;

		DownloadBehavior()
		{
			super(new RowResource());
		}

		@Override
		protected void onDownloadSuccess(AjaxRequestTarget target)
		{
			AjaxDownloadActionColumnContributor.this.onDownloadSuccess(target);
		}

		@Override
		protected void onDownloadFailed(AjaxRequestTarget target)
		{
			AjaxDownloadActionColumnContributor.this.onDownloadFailed(target);
		}
	}

	private class RowResource extends AbstractResource
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ResourceResponse newResourceResponse(Attributes attributes)
		{
			ResourceResponse response = new ResourceResponse();
			T row = table.findRow(pendingKey);
			if (row == null)
			{
				response.setError(404, "The row no longer exists");
				return response;
			}

			byte[] json = getContent(row).getBytes(StandardCharsets.UTF_8);
			response.setContentType(getContentType());
			response.setTextEncoding(StandardCharsets.UTF_8.name());
			response.setFileName(getFileName(row));
			response.setContentDisposition(getLocation() == Location.NewWindow
				? ContentDisposition.INLINE : ContentDisposition.ATTACHMENT);
			response.setContentLength(json.length);
			response.disableCaching();
			response.setWriteCallback(new WriteCallback()
			{
				@Override
				public void writeData(Attributes attributes)
				{
					attributes.getResponse().write(json);
				}
			});
			return response;
		}
	}
}
