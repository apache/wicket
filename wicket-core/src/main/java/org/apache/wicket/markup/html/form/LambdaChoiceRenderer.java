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

import org.danekja.java.util.function.serializable.SerializableFunction;

/**
 * Renders one choice. Separates the 'id' values used for internal representation from 'display
 * values' which are the values shown to the user of components that use this renderer.
 * <p>
 * Usage:
 *
 * <pre>
 * new DropDownChoice&lt;User&gt;(&quot;users&quot;, new Model&lt;User&gt;(selectedUser), listOfUsers,
 * 	new LambdaChoiceRenderer&lt;User&gt;(User::getName))
 * </pre>
 * <p>
 * creates a DropDownChoice of users and the display value will be looked up by function
 * (User::getName) and the id the index of the object in the ListOfUsers
 * </p>
 * <p>
 *
 * <pre>
 * new DropDownChoice&lt;User&gt;(&quot;users&quot;, new Model&lt;User&gt;(selectedUser), listOfUsers,
 * 	new LambdaChoiceRenderer&lt;User&gt;(User::getName, User::getId))
 * </pre>
 * <p>
 * creates a DropDownChoice of users and the display value will be looked up by function
 * (User::getName) and the id will be looked up by the function (User::getId)
 * </p>
 *
 * @param <T> The model object type
 */
public class LambdaChoiceRenderer<T> implements IChoiceRenderer<T> {
    private static final long serialVersionUID = 1L;

    /**
     * function for getting the display value.
     */
    private final SerializableFunction<T, ?> displayExpression;

    /**
     * function for getting the id.
     */
    private final SerializableFunction<T, ?> idExpression;

    /**
     * Constructor.
     * <p>
     * When you use this constructor, the display value will be determined by calling
     * toString() on the list object, and the id will be based on the list index. the id value will
     * be the index
     */
    public LambdaChoiceRenderer() {
        this(null);
    }

    /**
     * Constructor.
     * <p>
     * When you use this constructor, the display value will be determined by executing
     * the given function on the list object, and the id will be based on the list index.
     * The display value will be calculated by the given function
     *
     * @param displayExpression A function to get the display value
     */
    public LambdaChoiceRenderer(SerializableFunction<T, ?> displayExpression) {
        this(displayExpression, null);
    }

    /**
     * Constructor.
     * <p>
     * When you use this constructor, both the id and the display value will be
     * determined by executing the given functions on the list object.
     *
     * @param displayExpression A function to get the display value
     * @param idExpression      A function to get the id value
     */
    public LambdaChoiceRenderer(SerializableFunction<T, ?> displayExpression, SerializableFunction<T, ?> idExpression) {
        this.displayExpression = displayExpression;
        this.idExpression = idExpression;
    }

    @Override
    public Object getDisplayValue(T object) {
        Object returnValue = object;
        if ((displayExpression != null) && (object != null)) {
            returnValue = displayExpression.apply(object);
        }

        if (returnValue == null) {
            return "";
        }

        return returnValue;
    }

    @Override
    public String getIdValue(T object, int index) {
        if (idExpression == null) {
            return Integer.toString(index);
        }

        if (object == null) {
            return "";
        }

        Object returnValue = idExpression.apply(object);
        if (returnValue == null) {
            return "";
        }

        return returnValue.toString();
    }
}
