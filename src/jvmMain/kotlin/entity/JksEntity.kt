package entity

import views.BaseRadioTab
import java.io.File

data class JksEntity(
    val name: String,
    val path: String,
    val pwd: String,
    val alias: String,
    val ksPwd: String,
    val fileAlias: String = "",
    val default: Boolean = false,
): BaseRadioTab(text = name) {

    companion object {

        val default = JksEntity(
            name = "",
            path = "",
            pwd = "",
            alias = "",
            ksPwd = "",
            default = true
        )

        fun buildEntityByPath(
            path: String,
            pwd: String,
            alias: String,
            ksPwd: String,
        ) : JksEntity {
            val mName = path.substring(path.lastIndexOf(File.separator) + 1, path.length)
            return JksEntity(
                name = mName,
                path = path,
                pwd = pwd,
                alias = alias,
                ksPwd = ksPwd,
                fileAlias = mName
            )
        }
    }
}
