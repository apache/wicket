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
package org.apache.wicket.examples.builder.spike.extensions;

import java.util.List;

import org.apache.wicket.model.IModel;

/**
 * SPIKE - throwaway. The vocabulary wicket-extensions would ship. Core does not know it exists; an
 * application combines the two in an interface of its own.
 */
public interface ExtensionsUi
{
	default <T> EditableLabelBuilder<T> editableLabel(String id, IModel<T> model)
	{
		return EditableLabelBuilder.singleLine(id, model);
	}

	default <T> EditableLabelBuilder<T> editableMultiLineLabel(String id, IModel<T> model)
	{
		return EditableLabelBuilder.multiLine(id, model);
	}

	default <T> EditableLabelBuilder<T> editableChoiceLabel(String id, IModel<T> model,
		List<? extends T> choices)
	{
		return EditableLabelBuilder.choice(id, model, choices);
	}
}
