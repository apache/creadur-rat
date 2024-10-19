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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.TransformIterator;

/**
 * A ExtendedIterator is an Iterator wrapping around a plain
 * (or presented as plain) Iterator. The wrapping allows the usual
 * operations found on streams (filtering, concatenating, mapping) to be done on an Iterator derived
 * from some other source.  It also provides convenience methods for common operations.
 * @param <T> The type of object returned from the iterator.
 */
public class ExtendedIterator<T> implements Iterator<T> {
    /**
     * Set to <code>true</code> if this wrapping doesn't permit the use of
     * {@link #remove()}, otherwise removal is delegated to the base iterator.
     */
    protected final boolean removeDenied;

    /**
     * Answer an ExtendedIterator wrapped round <code>it</code>,
     * which does not permit <code>.remove()</code>
     * even if <code>it</code> does.
     * @param it The Iterator to wrap.
     * @return an Extended iterator on {@Code it}
     */
    public static <T> ExtendedIterator<T> createNoRemove(final Iterator<T> it) {
        return new ExtendedIterator<>(it, true);
    }

    /**
     * Answer an ExtendedIterator wrapped round a {@link Stream}.
     * The extended iterator does not permit <code>.remove()</code>.
     * <p>
     * The stream should not be used directly. The effect of doing so is
     * undefined.
     * </p>
     * @param stream the Stream to create an iterator from.
     * @return an Extended iterator on the {@code stream} iterator.
     */
    public static <T> ExtendedIterator<T> ofStream(final Stream<T> stream) {
        return new ExtendedIterator<T>(stream.iterator(), true) {
            // do nothing
        };
    }

    /**
     * Given an Iterator that returns Iterators, this creates an
     * Iterator over the next level values.
     * Similar to list splicing in lisp.
     * @param it An iterator of iterators.
     * @return An iterator over the logical concatenation of the inner iterators.
     */
    public static <T> ExtendedIterator<T> unwind(final Iterator<Iterator<T>> it) {
        return new ExtendedIterator<>(new UnwindingIterator<T>(it), false);
    }

    /**
     * An empty Extended iterator
     * @return An empty Extended iterator.
     */
    public static ExtendedIterator<?> emptyIterator() {
        return new ExtendedIterator<>(Collections.emptyIterator(), false);
    }

    /**
     * Answer an ExtendedIterator returning the elements of <code>it</code>.
     * If <code>it</code> is itself an ExtendedIterator, return that;
     * otherwise wrap <code>it</code>.
     * @param it The iterator to wrap.
     * @return An Extended iterator wrapping {@code it}
     */
    public static <T> ExtendedIterator<T> create(final Iterator<T> it) {
        return it instanceof ExtendedIterator<?>
                ? (ExtendedIterator<T>) it
                : new ExtendedIterator<>(it, false);
    }

    /** the base iterator that we wrap */
    protected final Iterator<? extends T> base;

    /**
     * Constructor.
     * @param base The iterator to wrap.
     */
    protected ExtendedIterator(final Iterator<? extends T> base) {
        this(base, false);
    }

    /**
     * Initialise this wrapping with the given base iterator and remove-control.
     * @param base the base iterator that this iterator wraps
     * @param removeDenied true if .remove() must throw an exception
     */
    protected ExtendedIterator(final Iterator<? extends T> base, final boolean removeDenied) {
        this.base = base;
        this.removeDenied = removeDenied;
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public T next() {
        return base.next();
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        base.forEachRemaining(action);
    }

    @Override
    public void remove() {
        if (removeDenied) {
            throw new UnsupportedOperationException();
        }
        base.remove();
    }

    /**
     * Returns the next item and removes it from the iterator.
     * @return the next item from the iterator.
     */
    public T removeNext() {
        T result = next();
        remove();
        return result;
    }

    /**
     * Chains the {@code other} iterator to the end of this one.
     * @param other the other iterator to extend this iterator with.
     * @return A new iterator returning the contenst of {@code this} iterator followed by the contents of {@code other{ iterator.}}
     * @param <X> The type of object returned from the other iterator.
     */
    public <X extends T> ExtendedIterator<T> andThen(final Iterator<X> other) {
        if (base instanceof IteratorChain) {
            ((IteratorChain<T>) base).addIterator(other);
            return this;
        }
        return new ExtendedIterator<T>(new IteratorChain<T>(this.base, other), this.removeDenied);
    }

    /**
     * Filter this iterator using a predicate.  Only items for which the predicate returns true will
     * be included in the result.
     * @param predicate The predicate to filter the items with.
     * @return An iterator filtered by the predicate.
     */
    public ExtendedIterator<T> filter(final Predicate<T> predicate) {
        return new ExtendedIterator<T>(new FilterIterator<>(this, predicate::test), this.removeDenied);
    }

    /**
     * Map the elements of the iterator to a now type.
     * @param function The function to map elements of {@code <T>} to type {@code <U>}.
     * @return An Extended iterator that returns a {@code <U>} for very {@code <T>} in the original iterator.
     * @param <U> The object type to return.
     */
    public <U> ExtendedIterator<U> map(final Function<T, U> function) {
        return new ExtendedIterator<U>(new TransformIterator<>(this, function::apply), false);
    }

    /**
     * A method to add the remaining elements in the iterator an arbitrary collection.
     * This method consumes the iterator.
     * @param collection THe collection to add elements to.
     * @return the {@code collection} with the elements added.
     * @param <U> A collection of objects of type {@code <T>}.
     */
    public <U extends Collection<T>> U addTo(final U collection) {
        this.forEachRemaining(collection::add);
        return collection;
    }

    /**
     * A class to unwind an iterator of iterators.
     * @param <T> The type of the object returned from the iterator.
     */
    private static class UnwindingIterator<T> implements Iterator<T> {
        /** The innermost iterator */
        private final Iterator<Iterator<T>> inner;
        /** The iterator extracted from the inner iterator */
        private Iterator<T> outer;

        /**
         * Constructs an iterator from an iterator of iterators.
         * @param it The iterator of iterators to unwind.
         */
        UnwindingIterator(final Iterator<Iterator<T>> it) {
            this.inner = it;
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
