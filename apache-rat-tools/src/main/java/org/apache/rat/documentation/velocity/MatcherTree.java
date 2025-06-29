/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.documentation.velocity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Description;

/**
 * The representation of the Matcher tree found in a License.
 */
public class MatcherTree {
    /**
     * The root of the tree.
     */
    private final IHeaderMatcher root;

    /**
     * Constructor.
     * @param root the root of the tree.  The matcher from a license.
     */
    public MatcherTree(final IHeaderMatcher root) {
        this.root = root;
    }

    /**
     * Gets the description of the root of the tree.
     * @return the description of the root of the tree.
     */
    public Description getRoot() {
        return root.getDescription();
    }

    /**
     * executes an in order traversal of the tree.
     * @return A list of {@link Node}s from an in order traversal.
     */
    public List<Node> traverse() {
        List<Node> result = new ArrayList<>();
        result.add(new Node(0, root));
        processNode(result, result.get(0));
        return result;
    }

    /**
     * process a node and crate all the children of it.
     * @param result the result to add the children to.
     * @param node the node to process.
     */
    private void processNode(final List<Node> result, final Node node) {
        Collection<Matcher> children = node.getChildren();
        if (children.isEmpty()) {
            return;
        }
        for (Matcher child : children) {
            Node childNode = new Node(node.level + 1, child);
            result.add(childNode);
            processNode(result, childNode);
        }
    }

    /**
     * The representation of a node in a License Matcher tree.
     * A node is a Matcher that tracks the level of the tree in which is appears.
     */
    public static class Node extends Matcher {
        /** The level of the tree that his node appears in. */
        private final int level;

        /**
         * Constructor.
         * @param level the level of the tree. (root = 0)
         * @param matcher the matcher being processed.
         */
        Node(final int level, final Matcher matcher) {
            super(matcher);
            this.level = level;
        }

        /**
         * Constructor.
         * @param level the level of the tree. (root = 0)
         * @param root the matcher to wrap.
         */
        Node(final int level, final IHeaderMatcher root) {
            super(root);
            this.level = level;
        }

        /**
         * Gets the level in the tree of this node.
         * @return the level in the tree of this node.
         */
        public int level() {
            return level;
        }

        /**
         * Gets the enclosed node only if it is another matcher.
         * @return the enclosed node.
         */
        @Override
        public Enclosed getEnclosed() {
            Enclosed result = super.getEnclosed();
            return result != null && IHeaderMatcher.class.equals(result.getChildType()) ? null : result;
        }

        /**
         * Gets the attributes only if they have values.
         * @return the collection of Attributes that have values.
         */
        @Override
        public Collection<Attribute> getAttributes() {
            return super.getAttributes().stream().filter(attr -> attr.getValue() != null).collect(Collectors.toList());
        }
    }
}
