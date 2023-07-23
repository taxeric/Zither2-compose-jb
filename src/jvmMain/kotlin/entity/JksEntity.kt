package entity

data class JksEntity(
    val name: String,
    val path: String,
    val pwd: String,
    val alias: String,
    val ksPwd: String,
    val fileAlias: String = "",
)
