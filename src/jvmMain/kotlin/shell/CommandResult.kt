package shell

data class CommandResult(
    val code: Int,
    val stdout: String,
    val stderr: String,
)
