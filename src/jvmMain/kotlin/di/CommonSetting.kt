package di

object CommonSetting {

    /**
     * 程序运行路径
     */
    val basePath by lazy {
        System.getProperty("user.dir")
    }

    /**
     * zipalign路径
     */
    lateinit var zipalignPath: String

    /**
     * apksigner路径
     */
    lateinit var apksignerPath: String
}