# State Machine

A state machine framework for kotlin multi-platform project.

StateMachine let you create and register transitions for a specific state object. Each transition
requires a predicate checking the current state of the machine to ensure it can be run.

## Gradle
For Kotlin multiplatform, inside `commonMain`
```
implementation "com.jeantuffier:statemachine:$version"
```
For the Jvm and Android
```
implementation "com.jeantuffier:statemachine-jvm:$version"
```
For iOS
```
implementation "com.jeantuffier:statemachine-ios:$version"
```
## Usage

Check the `example` package, it contains a small multiplatform project showing how to
create and use state machines with this library
