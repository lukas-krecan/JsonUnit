/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.listener.Difference;
import org.jspecify.annotations.Nullable;

class DifferenceImpl implements Difference {
    private final DiffContext context;
    private final Type type;

    DifferenceImpl(DiffContext context, Type type) {
        this.context = context;
        this.type = type;
    }

    @Override
    public @Nullable String getActualPath() {
        return context.actualPath() != null ? context.actualPath().getFullPath() : null;
    }

    @Override
    public @Nullable String getExpectedPath() {
        return context.expectedPath() != null ? context.expectedPath().getFullPath() : null;
    }

    @Override
    public @Nullable Object getActual() {
        Node actualNode = context.actualNode();
        return actualNode != null && !actualNode.isMissingNode() ? actualNode.getValue() : null;
    }

    @Override
    public @Nullable Object getExpected() {
        Node expectedNode = context.expectedNode();
        return expectedNode != null && !expectedNode.isMissingNode() ? expectedNode.getValue() : null;
    }

    @Override
    public String toString() {
        return getType() + " Expected " + getExpected() + " in " + getExpectedPath() + " got " + getActual() + " in "
                + getActualPath();
    }

    private static class MissingDifference extends DifferenceImpl {
        MissingDifference(DiffContext context) {
            super(context, Type.MISSING);
        }

        @Override
        public String toString() {
            return getType() + " " + getExpected() + " in " + getExpectedPath();
        }
    }

    private static class ExtraDifference extends DifferenceImpl {
        ExtraDifference(DiffContext context) {
            super(context, Type.EXTRA);
        }

        @Override
        public String toString() {
            return getType() + " " + getActual() + " in " + getActualPath();
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    static Difference missing(Context context) {
        return new MissingDifference(DiffContext.from(context).clearActual());
    }

    static Difference extra(Context context) {
        return new ExtraDifference(DiffContext.from(context).clearExpected());
    }

    static Difference different(Context context) {
        return new DifferenceImpl(DiffContext.from(context), Type.DIFFERENT);
    }

    private record DiffContext(
            @Nullable Node expectedNode,
            @Nullable Node actualNode,
            @Nullable Path expectedPath,
            @Nullable Path actualPath) {
        private static DiffContext from(Context context) {
            return new DiffContext(
                    context.expectedNode(), context.actualNode(), context.expectedPath(), context.actualPath());
        }

        private DiffContext clearActual() {
            return new DiffContext(expectedNode, null, expectedPath, null);
        }

        private DiffContext clearExpected() {
            return new DiffContext(null, actualNode, null, actualPath);
        }
    }
}
