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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

class ArrayUtils {
    static List<Double> toDoubleList(double[] source) {
        return Arrays.stream(source).boxed().collect(toList());
    }

    static List<Integer> toIntList(int[] source) {
        return Arrays.stream(source).boxed().collect(toList());
    }

    // No streams for booleans
    static List<Boolean> toBoolList(boolean[] source) {
        List<Boolean> result = new ArrayList<>(source.length);
        for (boolean value : source) {
            result.add(value);
        }
        return result;
    }
}
