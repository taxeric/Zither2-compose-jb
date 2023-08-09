package shell

class CommandBuilder(runByCMD: Boolean = false) {

    private val builder = mutableListOf<String>()

    init {
        if (runByCMD) {
            builder.add("cmd.exe")
            builder.add("/c")
        }
    }

    fun append(commandOrValue: String): CommandBuilder {
        builder.add(commandOrValue)
        return this
    }

    fun build(): List<String> {
        return builder
    }
}