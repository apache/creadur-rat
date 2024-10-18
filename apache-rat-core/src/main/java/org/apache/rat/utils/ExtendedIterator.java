/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rat.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.TransformIterator;

/**
 * A WrappedIterator is an ExtendedIterator wrapping around a plain
 * (or presented as plain) Iterator. The wrapping allows the usual extended
 * operations (filtering, concatenating) to be done on an Iterator derived
 * from some other source.
*/
public class ExtendedIterator<T> implements Iterator<T> {
    /**
     * Set to <code>true</code> if this wrapping doesn't permit the use of
     * {@link #remove()}, otherwise removal is delegated to the base iterator.
     */
    protected boolean removeDenied;



    /**
     * Answer an ExtendedIterator wrapped round <code>it</code>,
     * which does not permit <code>.remove()</code>
     * even if <code>it</code> does.
     */
    public static <T> ExtendedIterator<T> createNoRemove(Iterator<T> it) {
        return new ExtendedIterator<>(it, true);
    }

    /**
     * Answer an ExtendedIterator wrapped round a {@link Stream}.
     * The extended iterator does not permit <code>.remove()</code>.
     * <p>
     * The stream should not be used directly. The effect of doing so is
     * undefined.
     */
    public static <T> ExtendedIterator<T> ofStream(Stream<T> stream) {
        return new ExtendedIterator<T>(stream.iterator(), true) {
            // do nothing
        };
    }

    /**
     * Given an Iterator that returns Iterators, this creates an
     * Iterator over the next level values.
     * Similar to list splicing in lisp.
     */
    public static <T> ExtendedIterator<T> unwindIterator(Iterator<Iterator<T>> it) {
        return new ExtendedIterator(new UnwindingIterator<T>(it), false );
    }

    public static ExtendedIterator<?> emptyIterator() {
        return new ExtendedIterator<>(Collections.emptyIterator(), false);
    }

    /**
     * Answer an ExtendedIterator returning the elements of <code>it</code>.
     * If <code>it</code> is itself an ExtendedIterator, return that;
     * otherwise wrap <code>it</code>.
     */
    public static <T> ExtendedIterator<T> create(Iterator<T> it) {
        return it instanceof ExtendedIterator<?>
                ? (ExtendedIterator<T>) it
                : new ExtendedIterator<>(it, false);
    }

    /** the base iterator that we wrap */
    protected final Iterator<? extends T> base;

    public Iterator<? extends T> forTestingOnly_getBase() {
        return base;
    }

    /**
     * Constructor: remember the base iterator
     */
    protected ExtendedIterator(Iterator<? extends T> base) {
        this(base, false);
    }

    /**
     * Initialise this wrapping with the given base iterator and remove-control.
     * @param base the base iterator that this iterator wraps
     * @param removeDenied true if .remove() must throw an exception
     */
    protected ExtendedIterator(Iterator<? extends T> base, boolean removeDenied) {
        this.base = base;
        this.removeDenied = removeDenied;
    }

    /**
     * hasNext: defer to the base iterator
     */
    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    /**
     * next: defer to the base iterator
     */
    @Override
    public T next() {
        return base.next();
    }

    /**
     * forEachRemaining: defer to the base iterator
     */

    public void forEachRemaining(Consumer<? super T> action) {
        base.forEachRemaining(action);
    }

    public void remove() {
        if (removeDenied) {
            throw new UnsupportedOperationException();
        }
        base.remove();
    }

    public static void close(Iterator<?> it) throws IOException {
        if (it instanceof Closeable) {
            ((Closeable) it).close();
        }
    }

    /**
     * Answer the next object, and remove it.
     */

    public T removeNext() {
        T result = next();
        remove();
        return result;
    }


    public <X extends T> ExtendedIterator<T> andThen(Iterator<X> other) {
        if (base instanceof IteratorChain) {
            ((IteratorChain<T>)base).addIterator(other);
            return this;
        }
        return new ExtendedIterator<T>(new IteratorChain<T>(this.base, other), this.removeDenied);
    }


    public ExtendedIterator<T> filter(Predicate<T> f) {
        return new ExtendedIterator<T>(new FilterIterator<>(this, f::test), this.removeDenied){};
    }

    public <U> ExtendedIterator<U> map(Function<T, U> map) {
        return new ExtendedIterator<U>(new TransformIterator<>(this, map::apply), false);
    }

    public void forEach(Consumer<? super T> consumer) {
        forEachRemaining(consumer);
    }

    protected <U extends Collection<T>> U populateCollection(U collection) {
        this.forEachRemaining(collection::add);
        return collection;
    }

    /**
     * Answer a list of the elements of <code>it</code> in order, consuming this iterator.
     * Canonical implementation of {@code toSet()}.
     */
    public Set<T> toSet() {
        return populateCollection(new HashSet<>());
    }

    /**
     * Answer a list of the elements from <code>it</code>, in order, consuming
     * that iterator. Canonical implementation of {@code toList()}.
     */
    public List<T> toList() {
        return populateCollection(new ArrayList<>());
    }

    protected void ensureHasNext() {
        if (!hasNext())
            throw new NoSuchElementException();
    }

    /**
     * Utility method for this and other (sub)classes: raise the appropriate
     * "no more elements" exception. I note that we raised the wrong exception
     * in at least one case ...
     *
     * @param message the string to include in the exception
     * @return never - but we have a return type to please the compiler
     */
    protected T noElements(String message) {
        throw new NoSuchElementException(message);
    }

    private static class UnwindingIterator<T> implements Iterator<T> {
        final private Iterator<Iterator<T>> inner;
        private Iterator<T> outer;

        UnwindingIterator(Iterator<Iterator<T>> inner) {
            this.inner = inner;
        }

        @Override
        public boolean hasNext() {
            if (outer == null) {
                if (!inner.hasNext()) {
                    return false;
                }
                outer = inner.next();
            }
            while (!outer.hasNext()) {
                if (!inner.hasNext()) {
                    return false;
                }
                outer = inner.next();
            }
            return true;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return outer.next();
        }
    }
}
