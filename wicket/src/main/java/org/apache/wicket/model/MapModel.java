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
package org.apache.wicket.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Based on <code>Model</code> but for mapss of serializable objects.
 *
 * @author Timo Rantalaiho
 */
public class MapModel<K,V> extends CollectionModelBase<Map<K,V>>
{
    public MapModel()
    {
    }

    public MapModel(Map<K,V> object)
    {
        setObject(object);
    }

    @Override
    protected Map<K, V> createSerializableVersionOf(Map<K, V> object)
    {
        return new HashMap<K,V>(object);
    }
}