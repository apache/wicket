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
package org.apache.wicket.examples.builder.spike;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.examples.builder.spike.Builders.AjaxButtonBuilder;
import org.apache.wicket.examples.builder.spike.Builders.AjaxCheckBoxBuilder;
import org.apache.wicket.examples.builder.spike.Builders.AjaxLinkBuilder;
import org.apache.wicket.examples.builder.spike.Builders.ContainerBuilder;
import org.apache.wicket.examples.builder.spike.Builders.FormBuilder;
import org.apache.wicket.examples.builder.spike.Builders.LinkBuilder;
import org.apache.wicket.examples.builder.spike.Builders.ListBuilder;
import org.apache.wicket.examples.builder.spike.Builders.Simple;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * SPIKE - throwaway. The vocabulary wicket-core would ship: one entry point to its components,
 * for a page or panel to implement, alone or combined with the vocabularies of other modules.
 */
public interface CoreUi
{
	default MarkupContainer add(ComponentBuilder<?, ?>... builders)
	{
		MarkupContainer container = (MarkupContainer)this;
		for (ComponentBuilder<?, ?> builder : builders)
		{
			container.add(builder.build());
		}
		return container;
	}

	default Simple<Label> label(String id, IModel<?> model)
	{
		return new Simple<>(id, i -> new Label(i, model));
	}

	default Simple<MultiLineLabel> multiLineLabel(String id, IModel<?> model)
	{
		return new Simple<>(id, i -> new MultiLineLabel(i, model));
	}

	default <T> Simple<TextField<T>> textField(String id, IModel<T> model)
	{
		return new Simple<>(id, i -> new TextField<>(i, model));
	}

	default <T> Simple<TextArea<T>> textArea(String id, IModel<T> model)
	{
		return new Simple<>(id, i -> new TextArea<>(i, model));
	}

	default Simple<FeedbackPanel> feedback(String id)
	{
		return new Simple<>(id, FeedbackPanel::new);
	}

	default ContainerBuilder<Void> container(String id)
	{
		return new ContainerBuilder<>(id, null);
	}

	default <T> ContainerBuilder<T> container(String id, IModel<T> model)
	{
		return new ContainerBuilder<>(id, model);
	}

	default FormBuilder<Void> form(String id)
	{
		return new FormBuilder<>(id, null);
	}

	default <T> FormBuilder<T> form(String id, IModel<T> model)
	{
		return new FormBuilder<>(id, model);
	}

	default <T> ListBuilder<T> list(String id, IModel<? extends List<T>> items)
	{
		return new ListBuilder<>(id, items);
	}

	default LinkBuilder link(String id)
	{
		return new LinkBuilder(id);
	}

	default AjaxLinkBuilder ajaxLink(String id)
	{
		return new AjaxLinkBuilder(id);
	}

	default AjaxButtonBuilder ajaxButton(String id)
	{
		return new AjaxButtonBuilder(id);
	}

	default AjaxCheckBoxBuilder ajaxCheckBox(String id, IModel<Boolean> model)
	{
		return new AjaxCheckBoxBuilder(id, model);
	}

	default <T> IConverter<T> converter(SerializableFunction<String, T> parse,
		SerializableFunction<T, String> format)
	{
		return Builders.converter(parse, format);
	}
}
