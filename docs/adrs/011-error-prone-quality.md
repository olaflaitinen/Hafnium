# ADR-011: Error Prone for Code Quality

## Status

Accepted

## Context

The backend requires static analysis tooling to catch bugs at compile time. The primary candidates are Error Prone and Checkstyle.

## Decision

We will use Error Prone for compile-time bug detection, combined with Spotless for code formatting.

## Rationale

1. **Bug Detection**: Error Prone catches real bugs at compile time, not just style issues.
2. **Google Engineering**: Developed and used extensively at Google for production code.
3. **Actionable Findings**: Error Prone findings are typically real issues, not false positives.
4. **Compiler Integration**: Runs as a compiler plugin, catching issues before tests.

## Why Not Checkstyle

- Checkstyle focuses on style enforcement, which Spotless handles better.
- Checkstyle does not catch logic bugs.
- Combining Error Prone (bugs) + Spotless (style) is more effective.

## Consequences

### Positive

- Fewer runtime bugs due to compile-time detection.
- Consistent code quality across services.
- Integration with Gradle build system.

### Negative

- Some Error Prone checks may be overly strict.
- Requires Java 11+ (we use Java 21, so this is not an issue).

## Configuration

Error Prone is configured in the root `build.gradle`:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode = true
        excludedPaths = '.*/build/generated/.*'
    }
}
```

Specific checks can be disabled per-file using `@SuppressWarnings("ErrorProneCheckName")`.
