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
package org.apache.wicket.request.handler.render;

public class VariationIterator<T> {

	private VariationIterator<?> prev;
	private final Variation<T> variation;
	int idx=0;

	public VariationIterator(Variation<T> variation) {
		this.variation = variation;
	}

	public VariationIterator(VariationIterator<?> prev,Variation<T> variation) {
		this.prev = prev;
		this.variation = variation;
	}

	public static <T> VariationIterator<T> of(Variation<T> variation) {
		return new VariationIterator<T>(variation);
	}

	public static <T> VariationIterator<T> of(VariationIterator<?> prev,Variation<T> variation) {
		return new VariationIterator<T>(prev,variation);
	}

	public T value() {
		return variation.values().get(idx);
	}

	public boolean hasNextVariation() {
		return thisHasNextVariation() || prevHasNextVariation();
	}

	private boolean prevHasNextVariation() {
		return (prev!=null ? prev.hasNextVariation() : false);
	}

	private boolean thisHasNextVariation() {
		return variation.values().size() > idx+1;
	}

	public void nextVariation() {
		if (thisHasNextVariation()) {
			idx++;
		} else {
			if (prevHasNextVariation()) {
				idx=0;
				prev.nextVariation();
			} else {
				throw new IllegalArgumentException("no variation left, but next called");
			}
		}
	}
}
