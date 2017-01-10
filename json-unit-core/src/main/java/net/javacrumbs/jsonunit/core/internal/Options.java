/**
 * Copyright 2009-2017 the original author or authors.
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

import net.javacrumbs.jsonunit.core.Option;

import java.util.EnumSet;
import java.util.Set;

/**
 * Options enum set wrapper
 */
public class Options {
    private static final Options EMPTY_OPTIONS = new Options(EnumSet.noneOf(Option.class));
    private final EnumSet<Option> options;

    private Options(EnumSet<Option> options) {
        this.options = options;
    }

    public Options(Option first, Option... rest) {
        this(EnumSet.of(first, rest));
    }

    public static Options empty() {
        return EMPTY_OPTIONS;
    }

    public boolean contains(Option option) {
        return options.contains(option);
    }

    public Options with(Option option, Option... otherOptions) {
        EnumSet<Option> optionsWith = EnumSet.copyOf(options);
        optionsWith.addAll(EnumSet.of(option, otherOptions));
        return new Options(optionsWith);
    }


    public Options without(Option option) {
        EnumSet<Option> optionsWithout = EnumSet.copyOf(options);
        optionsWithout.remove(option);
        return new Options(optionsWithout);
    }

    public Set<Option> values() {
        return EnumSet.copyOf(options);
    }
}
