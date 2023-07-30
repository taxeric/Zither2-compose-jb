package entity

data class SettingsEntity(
    val outputPath: String,
    val zipalignPath: String,
    val apksignerPath: String,
    val keytoolPath: String = "",
    val default: Boolean = false
) {
    companion object {
        val default = SettingsEntity(
            outputPath = "",
            zipalignPath = "",
            apksignerPath = "",
            keytoolPath = "",
            default = true
        )
    }
}