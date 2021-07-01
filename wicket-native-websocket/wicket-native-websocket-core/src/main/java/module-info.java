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

module org.apache.wicket.websocket.core {
    requires org.apache.wicket.util;
    requires org.apache.wicket.request;
    requires org.apache.wicket.core;
    requires javax.servlet.api;
    requires org.slf4j;

    exports org.apache.wicket.protocol.ws;
    exports org.apache.wicket.protocol.ws.api;
    exports org.apache.wicket.protocol.ws.api.event;
    exports org.apache.wicket.protocol.ws.api.message;
    exports org.apache.wicket.protocol.ws.api.registry;
    exports org.apache.wicket.protocol.ws.concurrent;
    exports org.apache.wicket.protocol.ws.util.tester;
}
