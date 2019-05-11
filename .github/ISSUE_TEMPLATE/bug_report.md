---
name: Bug report
about: Create a report to help us improve
title: ''
labels: ''
assignees: ''

---

**Describe the bug**
1. Describe which variant of the API are you using (AssertJ, Hamcrest, ...)
2. Create a small reproducible example and paste it the the bug report.
3. Describe expected behavior

For example:

I expected this test to pass but it returns: `java.lang.AssertionError: Different value found in node "a.b", Expected :<null> Actual   :<missing>.`
```
assertThatJson("{\"a\":1}").node("a.b").isNull()
```
