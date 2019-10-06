package net.javacrumbs.jsonunit.jsonpath;

import org.junit.jupiter.api.Test;


import static net.javacrumbs.jsonunit.jsonpath.InternalJsonPathUtils.fromBracketNotation;
import static org.assertj.core.api.Assertions.assertThat;

class InternalJsonPathUtilsTest {

    @Test
    void shouldConvertFromBracketNotation() {
        assertThat(fromBracketNotation("$['tool']['jsonpath'][2]")).isEqualTo("$.tool.jsonpath[2]");
        assertThat(fromBracketNotation("$['tool'][2]['jsonpath']")).isEqualTo("$.tool[2].jsonpath");
        assertThat(fromBracketNotation("$[0][0]")).isEqualTo("$[0][0]");
    }

}