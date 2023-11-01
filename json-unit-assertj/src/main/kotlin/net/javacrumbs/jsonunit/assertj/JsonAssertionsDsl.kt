package net.javacrumbs.jsonunit.assertj

import net.javacrumbs.jsonunit.assertj.JsonAssert.ConfigurableJsonAssert
import net.javacrumbs.jsonunit.core.ConfigurationWhen.ApplicableForPath
import net.javacrumbs.jsonunit.core.ConfigurationWhen.PathsParam
import net.javacrumbs.jsonunit.core.Option

/**
 * Assert json properties with possibility to chain assertion callbacks like this
 *
 * ```kotlin
 * assertThatJson("{\"test1\":2, \"test2\":1}") {
 *    inPath("test1").isEqualTo(2)
 *    inPath("test2").isEqualTo(1)
 * }
 * ```
 */
fun assertThatJson(actual: Any, lambda: ConfigurableJsonAssert.() -> Unit = {}): ConfigurableJsonAssert {
    val jsonAssert = JsonAssertions.assertThatJson(actual)
    lambda(jsonAssert)
    return jsonAssert
}

/** Synonym to [ConfigurableJsonAssert.when] */
fun ConfigurableJsonAssert.whenever(first: Option, vararg other: Option) = this.`when`(first, *other)

/** Synonym to [ConfigurableJsonAssert.when] */
fun ConfigurableJsonAssert.whenever(pathParam: PathsParam, vararg actions: ApplicableForPath) =
    this.`when`(pathParam, *actions)
