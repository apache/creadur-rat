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
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A sorted set that reports insertion collisions.
 * @param <T> the type of object
 */
public class ReportingSet<T> implements SortedSet<T> {
    private final SortedSet<T> delegate;
    private boolean failOnDuplicate = false;
    private Log.Level duplicateLogLevel = Log.Level.WARN;
    private Log log = DefaultLog.INSTANCE;
    private Function<T,String> duplicateFmt = (t) -> String.format("Duplicate %s (%s) detected %s", t.getClass(), t);

    /**
     * Constructor.
     * Creates a TreeSet of type T.
     */
    public ReportingSet() {
        this(new TreeSet<T>());
    }
    
    /**
     * Constructs.
     * 
     * @param delegate the SortedSet to delegate to.
     */
    public ReportingSet(SortedSet<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * Set the message format used to format display.
     * @param msgFormat a format string taking the Class name and string representation of the 
     * @return
     */
    public ReportingSet<T> setMsgFormat(Function<T,String> msgFmt) {
        duplicateFmt = msgFmt;
        return this;
    }
    /**
     * If set true attempts to duplicate will throw an IllegalArgumentException.
     * The default state is false;.
     * @param state the state to set.
     * @return this for chaining.
     */
    public ReportingSet<T> setFailOnDuplicate(boolean state) {
        this.failOnDuplicate = state;
        return this;
    }

    /**
     * Sets the log that the reporting set will log to.
     * if not set the DefaultLog is used.
     * @param log the Log implementation to use.
     * @return this for chaining.
     */
    public ReportingSet<T> setLog(Log log) {
        this.log = log;
        return this;
    }

    /**
     * Sets the log level that the reporting set will log at.
     * if not set the default level is WARN.
     * @param level the log level to use.
     * @return this for chaining.
     */
    public ReportingSet<T> setLogLevel(Log.Level level) {
        this.duplicateLogLevel = level;
        return this;
    }

    private ReportingSet<T> sameConfig(SortedSet<T> delegate) {
        ReportingSet<T> result = delegate instanceof ReportingSet ? (ReportingSet<T>) delegate : new ReportingSet<>(delegate);
        return result.setFailOnDuplicate(this.failOnDuplicate).setLog(this.log).setLogLevel(this.duplicateLogLevel);
    }

    /**
     * Adds the item if it is not present.  Does not report collisions.
     * @param e the item to add.
     * @return true if the item was added, false otherwise.
     */
    public boolean addIfNotPresent(T e) {
        return add(false, e);
    }
    
    @Override
    public boolean add(T e) {
        return add(true, e);
    }
    
    /**
     * Attempts to add an item.  Report failures if reportDup is true.
     * @param reportDup the reporting flag.
     * @param e the item to add
     * @return true if the item was added.
     */
    private boolean add(boolean reportDup, T e) {
        if (delegate.contains(e)) {
            String msg = String.format("%s",ReportingSet.this.duplicateFmt.apply(e));
            if (reportDup) {
                msg +=  failOnDuplicate ? "" : " - duplicate ignored";
                log.log(duplicateLogLevel, msg);
            } 
            if (failOnDuplicate) {
                throw new IllegalArgumentException(msg);
            }
            return false;
        }
        return delegate.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean updated = false;
        for (T e : c) {
            updated |= add(e);
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
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public T first() {
        return delegate.first();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        delegate.forEach(action);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public ReportingSet<T> headSet(T toElement) {
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
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
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
    public ReportingSet<T> subSet(T fromElement, T toElement) {
        return sameConfig(delegate.subSet(fromElement, toElement));
    }

    @Override
    public ReportingSet<T> tailSet(T fromElement) {
        return sameConfig(delegate.tailSet(fromElement));
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }
}