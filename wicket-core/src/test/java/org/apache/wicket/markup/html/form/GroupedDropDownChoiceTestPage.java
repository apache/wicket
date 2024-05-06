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
package org.apache.wicket.markup.html.form;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.io.IClusterable;

/**
 * @author Juergen Donnerstag
 */
public class GroupedDropDownChoiceTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public static class Service implements IClusterable
	{
		private final String title;
		private final String group;

		public Service(String title, String group) {
			this.title = title;
			this.group = group;
		}

		public String getTitle() {
			return title;
		}

		public String getGroup() {
			return group;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Service)) return false;
			Service service = (Service) o;
			return Objects.equals(title, service.title);
		}

		@Override
		public int hashCode() {
			return Objects.hash(title);
		}
	}

	private Service service;

	public GroupedDropDownChoiceTestPage(Service service, List<Service> services, boolean nullValid)
	{
		this.service = service;

		Form<Void> form = new Form<Void>("form");
		add(form);

		final DropDownChoice<Service> ddc = new GroupedDropDownChoice<Service>("dropdown",
				new PropertyModel<Service>(this, "service"), services, new IChoiceRenderer<Service>() {
			@Override
			public Object getDisplayValue(Service object) {
				return object.getTitle();
			}
		}) {
			@Override
			protected boolean isNewGroup(Service previous, Service current) {
				return previous == null || (current.getGroup() != null && !current.getGroup().equals(previous.getGroup())) ;
			}

			@Override
			protected boolean hasNoGroup(Service current) {
				return current.getGroup() == null;
			}

			@Override
			protected IModel<String> getGroupLabel(Service current) {
				return Model.of(current.getGroup());
			}
		};
		ddc.setNullValid(nullValid);
		form.add(ddc);
	}
}
