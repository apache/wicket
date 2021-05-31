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
package org.apache.wicket.extensions.markup.html.repeater.util;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A {@link Set} implementation utilizing a {@link IDataProvider}'s models to keep containing
 * elements.
 *
 * @param <T> type of data
 * @author Roland Kurucz
 * @see IDataProvider#model(Object)
 */
public class DataProviderSubset<T> implements Set<T>, IDetachable {

    private static final long serialVersionUID = 1L;

    private final IDataProvider<T> provider;

    private final Set<IModel<T>> models = new HashSet<>(0);

    /**
     * Create an empty subset.
     *
     * @param provider the provider of the complete set
     */
    public DataProviderSubset(IDataProvider<T> provider) {
        this.provider = provider;
    }

    @Override
    public void detach() {
        for (IModel<T> model : models) {
            model.detach();
        }
    }

    @Override
    public int size() {
        return models.size();
    }

    @Override
    public boolean isEmpty() {
        return models.size() == 0;
    }

    @Override
    public void clear() {
        detach();

        models.clear();
    }

    @Override
    public boolean contains(Object o) {
        IModel<T> model = model(o);

        boolean contains = models.contains(model);

        model.detach();

        return contains;
    }

    @Override
    public boolean add(T t) {
        return models.add(model(t));
    }

    @Override
    public boolean remove(Object o) {
        IModel<T> model = model(o);

        boolean removed = models.remove(model);

        model.detach();

        return removed;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private Iterator<IModel<T>> iterator = models.iterator();

            private IModel<T> current;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                current = iterator.next();

                return current.getObject();
            }

            @Override
            public void remove() {
                iterator.remove();

                current.detach();
                current = null;
            }
        };
    }

    @Override
    public boolean addAll(Collection<? extends T> ts) {
        boolean changed = false;

        for (T t : ts) {
            changed |= add(t);
        }

        return changed;
    }

    @Override
    public boolean containsAll(Collection<?> cs) {
        for (Object c : cs) {
            if (!contains(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> cs) {
        boolean changed = false;

        for (Object c : cs) {
            changed |= remove(c);
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S> S[] toArray(S[] a) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    private IModel<T> model(Object o) {
        return provider.model((T) o);
    }

    /**
     * Create a model holding this set.
     *
     * @return model
     */
    public IModel<Set<T>> createModel() {
        return new IModel<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Set<T> getObject() {
                return DataProviderSubset.this;
            }

            @Override
            public void detach() {
                DataProviderSubset.this.detach();
            }
        };
    }
}
