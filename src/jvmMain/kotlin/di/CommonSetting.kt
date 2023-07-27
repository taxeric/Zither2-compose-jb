package di

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import entity.JksEntity
import entity.SettingEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream

@OptIn(DelicateCoroutinesApi::class)
object CommonSetting {

    private val scope = GlobalScope

    /**
     * 程序运行路径
     */
    val basePath by lazy {
        System.getProperty("user.dir")
    }

    private const val settingFilename = "setting.json"
    private var settingFilepath = basePath + File.separator + settingFilename

    val commonSettingFlow = MutableStateFlow<SettingEntity>(SettingEntity.default)

    /**
     * 通用输出路径
     */
    var outputPath: String = ""
    private var commonOutputPath = basePath + File.separator + "output" + File.separator

    /**
     * zipalign路径
     */
    lateinit var zipalignPath: String

    /**
     * apksigner路径
     */
    lateinit var apksignerPath: String

    private fun bind(data: SettingEntity) {
        outputPath = data.outputPath
        zipalignPath = data.zipalignPath
        apksignerPath = data.apksignerPath
    }

    fun readSetting() {
        scope.launch {
            val json = withContext(Dispatchers.IO) {
                val file = File(settingFilepath)
                if (!file.exists()) {
                    ""
                } else {
                    async {
                        file.readText()
                    }.await()
                }
            }
            if (json.isNotEmpty()) {
                val mapper = jacksonObjectMapper()
                val data = mapper.readValue<SettingEntity>(json)
                val mData = if (data.outputPath.isEmpty()) {
                    data.copy(outputPath = commonOutputPath)
                } else {
                    data
                }
                bind(mData)
                commonSettingFlow.tryEmit(mData)
            }
        }
    }

    fun writeSetting(entity: SettingEntity) {
        scope.launch {
            val file = File(settingFilepath)
            withContext(Dispatchers.IO) {
                val mapper = jacksonObjectMapper()
                val str = mapper.writeValueAsString(entity)
                if (!file.exists()) {
                    file.createNewFile()
                    file.writeText(str)
                } else {
                    FileOutputStream(file, false).use { it.write(str.toByteArray(Charsets.UTF_8)) }
                }
            }
        }
    }
}