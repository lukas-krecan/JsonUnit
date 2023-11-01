package net.javacrumbs.jsonunit.test.base;

public class RegexBuilder {
    private final String expression;

    private RegexBuilder(String expression) {
        this.expression = expression;
    }

    static RegexBuilder regex() {
        return new RegexBuilder("\"${json-unit.regex}^");
    }

    RegexBuilder str(String staticString) {
        return new RegexBuilder(expression + "\\\\Q" + staticString + "\\\\E");
    }

    RegexBuilder exp(String regex) {
        return new RegexBuilder(expression + regex);
    }

    @Override
    public String toString() {
        return expression + "$\"";
    }
}
