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
package org.apache.wicket.core.util.objects.checker;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.wicket.serialize.java.JavaSerializer;
import org.junit.Test;

/**
 * Test for https://issues.apache.org/jira/browse/WICKET-6704
 */
public class CheckingObjectOutputStreamPropertyChangeSupportTest {

    /**
     * The test should either pass and log an ERROR
     * or cause a JVM crash
     */
    @Test
    public void serializePropertyChangeSupport()
    {
        JavaSerializer serializer = new JavaSerializer("test");
        serializer.serialize(new ObjectToPersist());
    }

    static abstract class AbstractObjectToPersist implements Serializable {

        private static final long serialVersionUID = 1L;

        // if we move this field to the child class, the JVM crash is not reproducible, weird !
        private PropertyChangeSupport propertyChangeSupport;

        protected AbstractObjectToPersist() {
            super();
            // if we use PropertyChangeSupport directly, the JVM crash is not reproducible, weird !
            propertyChangeSupport = new ExtendedPropertyChangeSupport(this);
        }

    }

    static class ExtendedPropertyChangeSupport extends PropertyChangeSupport {

        ExtendedPropertyChangeSupport(Object sourceBean) {
            super(sourceBean);
        }

    }

    class ObjectToPersist extends AbstractObjectToPersist {

        // 1. this field is INTENTIONALLY not serializable to be able to trigger JVM crash
        // 2. normally wicket handle this correctly by throwing the NotSerializableException, but in this example the JVM crash
        private Future<Object> future;

        ObjectToPersist() {
            super();

            future = new FutureTask<Object>(new Callable() {
                public Object call() throws Exception {
                    return new Object();
                }
            });
        }
    }

}
