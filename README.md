# State Machine

A state machine framework for kotlin multi-platform project.

StateMachine handles the state management of your views across the jvm, ios and js targets. All the logic can be 
delegated to the shared library and let each client focus on its specific requirements and UI.

## Installation
```
plugins {
    ...
    // optional, only required if you want code generation
    id("com.google.devtools.ksp")
    ...
}

val commonMain by getting {
    dependencies {
        api("com.jeantuffier.statemachine:core:$stateMachineVersion")
        api("com.jeantuffier.statemachine:orchestrate:$stateMachineVersion")
    }
}

// optional, only required if you want code generation
dependencies {
    add("kspCommonMainMetadata", "com.jeantuffier.statemachine:processor:$stateMachineVersion")
}
```

## Getting started

If you only want the bare minimum, `com.jeantuffier.statemachine:core` will give you the tools for creating a 
state machine yourself :
```kotlin
private data class Movie(val id: String, val title: String, val genre: String)
private data class Actor(val id: String, val firstName: String, val lastName: String)

// The object representing the state of your screen
private data class MovieScreenState(
    val isLoadingMovie: Boolean = false,
    val movie: Movie? = null,
    val isLoadingActors: Boolean = false,
    val actors: List<Actor> = emptyList(),
)

// The different actions a user can trigger within that screen
private sealed class MovieScreenStateActions {
    data class LoadMovie(val id: String) : MovieScreenStateActions()
    data class LoadActors(val movieId: String) : MovieScreenStateActions()
    data class LoadComments(val movieId: String) : MovieScreenStateActions()
}

// Matches user actions with logic to execute
private fun movieScreenReducer(
    onLoadMovie: suspend (MovieScreenStateActions.LoadMovie) -> Flow<StateUpdate<MovieScreenState>>,
    onLoadActors: suspend (MovieScreenStateActions.LoadActors) -> Flow<StateUpdate<MovieScreenState>>,
    onLoadComments: suspend (MovieScreenStateActions.LoadComments) -> Flow<StateUpdate<MovieScreenState>>,
) = Reducer<MovieScreenStateActions, MovieScreenState> { action ->
    when (action) {
        is MovieScreenStateActions.LoadMovie -> onLoadMovie(action)
        is MovieScreenStateActions.LoadActors -> onLoadActors(action)
        is MovieScreenStateActions.LoadComments -> onLoadComments(action)
    }
}

// Done, you can use this object in each supported clients.
val statemachine = StateMachine(
    initialValue = MovieScreenState(),
    coroutineDispatcher = StandardTestDispatcher(testScheduler),
    reducer = movieScreenReducer(
        onLoadMovie = { _ -> flowOf() },
        onLoadActors = { _ -> flowOf() },
        onLoadComments = { _ -> flowOf() },
    ),
).state.test {
    assertEquals(MovieScreenState(), awaitItem())
}
```

You can find more details and examples in `commonTest`.

Now, if you want to use the full potential of the library with code generation, here's how you do it :

```kotlin
@Orchestration(
    baseName = "MovieScreen",
    errorType = AppError::class,
    actions = [SaveAsFavorite::class],
)
interface MovieScreenOrchestration {
    val isFavorite: Boolean

    @Orchestrated(
        trigger = LoadData::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val movie: OrchestratedData<Movie>

    @Orchestrated(
        trigger = LoadData::class,
        loadingStrategy = LoadingStrategy.SUSPEND,
    )
    val actors: OrchestratedPage<Actor>

    @Orchestrated(
        trigger = LoadComments::class,
        loadingStrategy = LoadingStrategy.FLOW,
    )
    val comments: OrchestratedPage<Comment>
}
```
And that's it : the state, action, reducer and state machine will be created for you! You can fin more details in the
`example` package and in this demo project : https://github.com/jeantuffier/Tavla-Common