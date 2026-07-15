# Copilot Instructions

## Build and test commands

Use a Java 11-compatible toolchain for CI parity. JDK 17 also works locally; JDK 21 currently fails during compilation because Lombok 1.18.26 is not compatible with it in this project.

```bash
# Run the full test suite
mvn -B clean test --no-transfer-progress

# Build the jar (this also runs tests unless you add -DskipTests)
mvn -B package --file pom.xml

# Run one test class
mvn -q -Dtest=StateMachineTest test

# Run one test method
mvn -q -Dtest=StateMachineTest#testOnEntry test

# Generate coverage report locally
mvn -B test jacoco:report --no-transfer-progress
```

## High-level architecture

This repository is a Java library for defining and running hierarchical state machines.

- `de.am.common.sm.StateMachineFactory` builds a runtime `StateMachine` by scanning handler classes/objects for `@State`, `@Transition`, `@Transitions`, `@OnEntry`, and `@OnExit`.
- `de.am.common.sm.StateMachine` is the execution engine. It keeps the current `State` in a `StateContext`, processes events through weighted transitions, falls back to parent states when a child cannot handle an event, and supports re-entrant calls by queueing nested events.
- `de.am.common.sm.StateMachineProxyBuilder` is the main integration layer for consumers. It turns interface method calls into `Event` objects, looks up a `StateContext`, and forwards the event into the state machine.
- The extension-point packages are:
  - `context/` for how state is stored and found (`SingletonStateContextLookup` is the default)
  - `event/` for event creation (`DefaultEventFactory` uses the invoked method name as the event id)
  - `transition/` for transition implementations and method/signature binding
  - `exception/` for control-flow and error exceptions used during execution
- `src/test/java/de/am/common/sm/example/` and `StateMachineProxyBuilderTest` are the best end-to-end usage examples for annotated handlers and proxy-driven execution.

## Key conventions

- Define states as `@State` on `static final String` fields. Parent-child hierarchies are declared with `@State(PARENT_STATE_ID)`.
- If you call `StateMachineFactory.create(handler)` without a start-state argument, it assumes a state id of `"start"`. Most tests avoid that implicit default by passing the start state explicitly.
- `@Transition` methods are matched by event id and current state. `on = "*"` is the catch-all pattern used for parent-level fallback handlers.
- Lower transition `weight` means higher priority. Shared fallback behavior is typically modeled with parent states plus wildcard transitions.
- Transition method parameters are signature-driven: methods may optionally take `Event` first, then `StateContext`, then event arguments in order. `@OnEntry`/`@OnExit` methods may take `StateContext` and/or `State`.
- Programmatic control flow inside transition handlers is done through `StateControl.breakAnd...()` methods, which intentionally throw internal exceptions that `StateMachine` interprets.
- The default proxy path assumes `void` interface methods and side effects on the handler/context rather than return values.
- `StateMachineFactory` sorts handler methods before registering transitions, so behavior should not depend on source declaration order.
- Tests use JUnit 5 and often build minimal states/handlers inline to exercise one engine behavior at a time instead of going through a large fixture.
