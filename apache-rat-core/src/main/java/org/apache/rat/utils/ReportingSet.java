/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A sorted set that reports insertion collisions.
 * @param <T> the type of object
 */
public class ReportingSet<T> implements SortedSet<T> {
    /** The sorted set this reporting set delegates to */
    private final SortedSet<T> delegate;
    /** The what to do when duplicates are found */
    private Options duplicateOption = Options.IGNORE;
    /** What level to log duplicate found messages */
    private Log.Level duplicateLogLevel = Log.Level.WARN;
    /** The message to log the duplicate with */
    private Function<T, String> duplicateFmt = t -> String.format("Duplicate %s (%s) detected %s", t.getClass(), t);
    /** The Options for duplicate processing */
    public enum Options { OVERWRITE, IGNORE, FAIL }

    /**
     * Constructs.
     * @param delegate the SortedSet to delegate to.
     */
    public ReportingSet(final SortedSet<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * Sets the function to generate the log message.
     * @param msgFmt A function to return the string to be displayed when a collision occurs.
     * @return This for chaining.
     */
    public ReportingSet<T> setMsgFormat(final Function<T, String> msgFmt) {
        duplicateFmt = msgFmt;
        return this;
    }

    /**
     * If set true attempts to duplicate will throw an IllegalArgumentException.
     * The default state is false;.
     * @param state the state to set.
     * @return this for chaining.
     */
    public ReportingSet<T> setDuplicateOption(final Options state) {
        this.duplicateOption = state;
        return this;
    }

    /**
     * Sets the log level that the reporting set will log at.
     * if not set the default level is WARN.
     * @param level the log level to use.
     * @return this for chaining.
     */
    public ReportingSet<T> setLogLevel(final Log.Level level) {
        this.duplicateLogLevel = level;
        return this;
    }

    private ReportingSet<T> sameConfig(final SortedSet<T> delegate) {
        ReportingSet<T> result = delegate instanceof ReportingSet ? (ReportingSet<T>) delegate : new ReportingSet<>(delegate);
        return result.setDuplicateOption(this.duplicateOption).setLogLevel(this.duplicateLogLevel);
    }

    /**
     * Adds the item if it is not present.  Does not report collisions.
     * @param e the item to add.
     * @return true if the item was added, false otherwise.
     */
    public boolean addIfNotPresent(final T e) {
        return add(false, e);
    }

    @Override
    public boolean add(final T e) {
        return add(true, e);
    }

    /**
     * Attempts to add an item.  Report failures if reportDup is true.
     * @param reportDup the reporting flag.
     * @param e the item to add
     * @return true if the item was added.
     */
    private boolean add(final boolean reportDup, final T e) {
        if (delegate.contains(e)) {
            String msg = String.format("%s", ReportingSet.this.duplicateFmt.apply(e));
            if (reportDup) {
                msg =  String.format("%s (action: %s)", msg, duplicateOption);
                DefaultLog.getInstance().log(duplicateLogLevel, msg);
            }
            switch (duplicateOption) {
            case FAIL:
                throw new IllegalArgumentException(msg);
            case IGNORE:
                return false;
            case OVERWRITE:
                delegate.remove(e);
                return delegate.add(e);
            }
        }
        return delegate.add(e);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        boolean updated = false;
        for (T e : c) {
            updated |= add(e);
        }
        return updated;
    }

    public boolean addAllIfNotPresent(final Collection<? extends T> c) {
        boolean updated = false;
        for (T e : c) {
            updated |= addIfNotPresent(e);
        }
        return updated;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Comparator<? super T> comparator() {
        return delegate.comparator();
    }

    @Override
    public boolean contains(final Object o) {
        return delegate.contains(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean equals(final Object o) {
        return delegate.equals(o);
    }

    @Override
    public T first() {
        return delegate.first();
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        delegate.forEach(action);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public ReportingSet<T> headSet(final T toElement) {
        return sameConfig(delegate.headSet(toElement));
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public T last() {
        return delegate.last();
    }

    @Override
    public Stream<T> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public boolean remove(final Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return delegate.stream();
    }

    @Override
    public ReportingSet<T> subSet(final T fromElement, final T toElement) {
        return sameConfig(delegate.subSet(fromElement, toElement));
    }

    @Override
    public ReportingSet<T> tailSet(final T fromElement) {
        return sameConfig(delegate.tailSet(fromElement));
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return delegate.toArray(a);
    }
}
