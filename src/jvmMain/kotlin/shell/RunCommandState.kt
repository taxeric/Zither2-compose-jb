package shell

sealed interface RunCommandState {

    object Idle: RunCommandState
    object Running: RunCommandState
    data class Success<T>(val data: T?): RunCommandState
    data class Failed(val msg: String): RunCommandState
}