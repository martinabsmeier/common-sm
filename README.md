[![Java CI with Maven](https://github.com/martinabsmeier/common-sm/actions/workflows/maven.yml/badge.svg)](https://github.com/martinabsmeier/common-sm/actions/workflows/maven.yml)
[![CodeQL](https://github.com/martinabsmeier/common-sm/actions/workflows/codeql.yml/badge.svg)](https://github.com/martinabsmeier/common-sm/actions/workflows/codeql.yml)
[![Coverage](https://github.com/martinabsmeier/common-sm/actions/workflows/coverage.yml/badge.svg)](https://github.com/martinabsmeier/common-sm/actions/workflows/coverage.yml)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)

# common-sm

`common-sm` is a small Java library for building event-driven state machines. It provides annotations for declaring states and transitions, a runtime engine for executing them, and a proxy builder that translates interface method calls into state machine events.

## Features

- Define states with `@State`
- Define transitions with `@Transition` and `@Transitions`
- Support hierarchical states via parent-child relationships
- Run entry and exit hooks with `@OnEntry` and `@OnExit`
- Dispatch interface method calls into a `StateMachine` through `StateMachineProxyBuilder`
- Null-safe event argument handling during context lookup and transition binding

## Requirements

- JDK 21
- Maven 3.9+

## Getting the source

```bash
git clone git@github.com:martinabsmeier/common-sm.git
cd common-sm
```

## Build and test

```bash
# run the full test suite
mvn clean test

# build the jar
mvn package

# run one test class
mvn -Dtest=StateMachineTest test

# run one test method
mvn -Dtest=StateMachineTest#testOnEntry test

# generate coverage report
mvn test jacoco:report
```

## Usage

Add the library to your Maven project:

```xml
<dependency>
    <groupId>de.am.common</groupId>
    <artifactId>common-sm</artifactId>
    <version>${version}</version>
</dependency>
```

`StateMachineProxyBuilder` is designed for command-style interfaces. Proxy methods must return `void`; non-`void` interface methods are rejected when the proxy is created.

Example interface:

```java
public interface TapeDeck {

    void load(String nameOfTape);

    void play();

    void pause();

    void stop();

    void eject();
}
```

Annotated handler:

```java
public class TapeDeckManager {

    @State
    public static final String STATE_EMPTY = "Empty";

    @State
    public static final String STATE_LOADED = "Loaded";

    @State
    public static final String STATE_PLAYING = "Playing";

    @State
    public static final String STATE_PAUSED = "Paused";

    @Transition(on = "load", in = STATE_EMPTY, next = STATE_LOADED)
    public void loadTape(String nameOfTape) {
    }

    @Transitions({
        @Transition(on = "play", in = STATE_LOADED, next = STATE_PLAYING),
        @Transition(on = "play", in = STATE_PAUSED, next = STATE_PLAYING)
    })
    public void playTape() {
    }

    @Transition(on = "pause", in = STATE_PLAYING, next = STATE_PAUSED)
    public void pauseTape() {
    }

    @Transition(on = "stop", in = STATE_PLAYING, next = STATE_LOADED)
    public void stopTape() {
    }

    @Transition(on = "eject", in = STATE_LOADED, next = STATE_EMPTY)
    public void ejectTape() {
    }
}
```

Create and use the state machine through a proxy:

```java
TapeDeckManager manager = TapeDeckManager.getInstance();

StateMachineFactory factory = StateMachineFactory.create(Transition.class);
StateMachine sm = factory.create(TapeDeckManager.STATE_EMPTY, manager);
TapeDeck deck = new StateMachineProxyBuilder().create(TapeDeck.class, sm);

deck.load("The Knife - Silent Shout");
deck.play();
deck.pause();
deck.play();
deck.stop();
deck.eject();
```

See `src/test/java/de/am/common/sm/example/` and `StateMachineProxyBuilderTest` for end-to-end examples.

## Current improvement ideas

| Priority | Area | Suggestion | Why it matters |
| --- | --- | --- | --- |
| Medium | Build metadata | Remove the remaining SonarCloud properties from `pom.xml` and fix the stale JaCoCo report path. | SonarCloud has already been removed from GitHub Actions, so the leftover Maven properties are obsolete and the current report path points to `statemachine/target/...` instead of this project. |
| Medium | API ergonomics | Tighten method-signature matching in `MethodSelfTransition` and add focused tests for custom `StateContext` subtypes. | The current `isAssignableFrom` checks are fragile and make entry/exit hook binding harder to reason about for subtype-based contexts. |
| Low | Documentation/examples | Promote the tape deck example to a first-class sample module or published example source. | The project is easiest to understand through the annotated example flow, but today that guidance lives only in tests and the README. |

## Contributing

Contributions are welcome. Please review [CONTRIBUTING.md](CONTRIBUTING.md) and make sure `mvn clean test` passes before opening a pull request.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.
