package shell

class CommandBuilder {

    private val builder = mutableListOf<String>()

    fun append(commandOrValue: String): CommandBuilder {
        builder.add(commandOrValue)
        return this
    }

    fun build(): List<String> {
        return builder
    }
}