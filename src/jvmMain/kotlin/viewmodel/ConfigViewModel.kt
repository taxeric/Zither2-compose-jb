package viewmodel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import di.CommonSetting
import entity.JksEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import viewmodel.base.ScopeViewModel
import viewmodel.base.ScopeViewModelImpl
import java.io.File
import java.io.FileOutputStream

/**
 * 包含本地和运行期间的配置
 */
class ConfigViewModel(
    scope: CoroutineScope,
    private val delegate: ScopeViewModelImpl = ScopeViewModelImpl()
): ScopeViewModel by delegate {

    companion object {
        val jksCacheName = "jksConfig.json"
    }

    init {
        provideScope(scope)
        readJksConfig()
    }

    /**
     * 本地保存的签名文件
     */
    private val _jksConfigFlow = MutableStateFlow(listOf<JksEntity>())
    val jksConfigFlow: StateFlow<List<JksEntity>> = _jksConfigFlow.asStateFlow()

    private fun buildJksPath() = CommonSetting.basePath + File.separator + "jks" + File.separator

    fun deleteJksConfig(
        index: Int
    ) {
        val cacheJks = _jksConfigFlow.value
        val newJksConfig = mutableListOf<JksEntity>().apply {
            cacheJks.forEachIndexed { i, jksEntity ->
                if (index != i) {
                    add(jksEntity)
                }
            }
        }
        writeJksConfig(newJksConfig) {
            _jksConfigFlow.tryEmit(newJksConfig)
        }
    }

    fun readJksConfig(
        path: String = buildJksPath(),
        filename: String = jksCacheName
    ) {
        read<List<JksEntity>>(path, filename) { list ->
            _jksConfigFlow.tryEmit(list)
        }
    }

    fun writeJksConfig(
        jksEntity: JksEntity,
        onComplete: () -> Unit = {},
    ) {
        val cacheKey = jksConfigFlow.value
        writeJksConfig(
            list = mutableListOf<JksEntity>().apply {
                addAll(cacheKey)
                add(jksEntity)
            },
            false,
            onComplete,
        )
    }

    fun writeJksConfig(
        list: List<JksEntity>,
        append: Boolean = false,
        onComplete: () -> Unit = {},
    ) {
        if (list.isEmpty()) {
            return
        }
        write(
            list,
            buildJksPath(),
            jksCacheName,
            append,
            onComplete
        )
    }

    private inline fun <reified T: Any> read(
        filepath: String,
        filename: String,
        crossinline onComplete: (T) -> Unit
    ) {
        delegate.scope.launch {
            val json = withContext(Dispatchers.IO) {
                val file = File(filepath, filename)
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
                val value = mapper.readValue<T>(json)
                onComplete.invoke(value)
            }
        }
    }

    private fun write(
        any: Any,
        filepath: String,
        filename: String,
        append: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        delegate.scope.launch {
            withContext(Dispatchers.IO) {
                val path = File(filepath)
                if (!path.exists()) {
                    path.mkdir()
                }
                val file = File(path, filename)
                val mapper = jacksonObjectMapper()
                val str = mapper.writeValueAsString(any)
                if (!file.exists()) {
                    file.createNewFile()
                    file.writeText(str)
                } else {
                    FileOutputStream(file, append).use { it.write(str.toByteArray(Charsets.UTF_8)) }
                }
                onComplete.invoke()
            }
        }
    }
}