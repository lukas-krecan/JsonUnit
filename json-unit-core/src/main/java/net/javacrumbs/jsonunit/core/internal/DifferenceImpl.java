/**
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.core.internal;

import net.javacrumbs.jsonunit.core.listener.Difference;

class DifferenceImpl implements Difference {
    private final Context context;
    private final Type type;

    DifferenceImpl(Context context, Type type) {
        this.context = context;
        this.type = type;
    }

    @Override
    public String getActualPath() {
        return context.getActualPath() != null ? context.getActualPath().getFullPath() : null;
    }

    @Override
    public String getExpectedPath() {
        return context.getExpectedPath() != null ? context.getExpectedPath().getFullPath() : null;
    }

    @Override
    public Object getActual() {
        Node actualNode = context.getActualNode();
        return actualNode != null && !actualNode.isMissingNode() ? actualNode.getValue() : null;
    }

    @Override
    public Object getExpected() {
        Node expectedNode = context.getExpectedNode();
        return expectedNode != null && !expectedNode.isMissingNode() ? expectedNode.getValue() : null;
    }

    @Override
    public String toString() {
        return getType() + " Expected " + getExpected() + " in " + getExpectedPath() + " got " + getActual() + " in " + getActualPath();
    }

    private static class MissingDifference extends DifferenceImpl {
        MissingDifference(Context context) {
            super(context, Type.MISSING);
        }

        @Override
        public String toString() {
            return getType() + " " + getExpected() + " in " + getExpectedPath();
        }
    }

    private static class ExtraDifference extends DifferenceImpl {
        ExtraDifference(Context context) {
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
        return new MissingDifference(context);
    }

    static Difference extra(Context context) {
        return new ExtraDifference(context);
    }

    static Difference different(Context context) {
        return new DifferenceImpl(context, Type.DIFFERENT);
    }
}
