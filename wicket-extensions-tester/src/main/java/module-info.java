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

module org.apache.wicket.tester.extensions {
    requires org.apache.wicket.util;
    requires org.apache.wicket.request;
    requires org.apache.wicket.core;
    requires org.apache.wicket.extensions;
    requires org.apache.wicket.tester;
    requires org.slf4j;
    requires static jakarta.servlet;
    requires org.junit.jupiter.api;
    requires org.apache.commons.collections4;
    requires org.danekja.jdk.serializable.functional;
    requires org.assertj.core;

    exports org.apache.wicket.extensions.tester;
}
