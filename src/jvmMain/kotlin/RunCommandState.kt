sealed interface RunCommandState {

    object Idle: RunCommandState
    object Running: RunCommandState
    object Success: RunCommandState
    data class Failed(val msg: String): RunCommandState
}