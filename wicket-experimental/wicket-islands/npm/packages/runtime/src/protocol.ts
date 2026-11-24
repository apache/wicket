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

/**
 * The version of the protocol between the runtime and the Wicket Islands Java module: the markup
 * of the host, the island state, the calls and their answers. The islands of separately built
 * bundles share the runtime of the bundle that loaded first, so it also covers the contract
 * between the runtime and the island modules, `IslandContext` and `IslandModule`: it
 * changes with every change to them, additions included. It is the same number as
 * `Island.PROTOCOL` on the server, and changes with it.
 */
export const PROTOCOL = 1;
