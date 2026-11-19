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
package org.apache.wicket.examples.islands.treasurehunt;

import java.io.Serializable;

/**
 * The outcome of probing a cell.
 *
 * @param x
 *            the column
 * @param y
 *            the row
 * @param treasure
 *            whether the cell holds a treasure
 * @param distance
 *            the distance to the nearest treasure still hidden, 0 when there is none
 */
public record ProbeOutcome(int x, int y, boolean treasure, int distance) implements Serializable
{
}
