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

    /**
     * 需要编辑的签名
     */
    private val _needEditJksFlow = MutableStateFlow(JksEntity.default)
    val needEditJksEntity: StateFlow<JksEntity> = _needEditJksFlow.asStateFlow()

    private fun buildJksPath() = CommonSetting.basePath + File.separator + "jks" + File.separator

    /**
     * 发射需要编辑的jks
     */
    fun emitNeedEditJks(
        jksEntity: JksEntity
    ) {
        _needEditJksFlow.tryEmit(jksEntity)
    }

    fun saveEditJksConfig(
        newJksEntity: JksEntity
    ) {
        filterAndMapJksConfig { mutableList, i, jks ->
            mutableList.add(
                if (newJksEntity.fileUid == jks.fileUid) {
                    newJksEntity
                } else {
                    jks
                }
            )
        }
    }

    fun deleteJksConfig(
        jksEntity: JksEntity
    ) {
        filterAndMapJksConfig { mutableList, i, jks ->
            if (jksEntity.fileUid != jks.fileUid) {
                mutableList.add(jks)
            }
        }
    }

    fun deleteJksConfig(
        index: Int
    ) {
        filterAndMapJksConfig { mutableList, i, jks ->
            if (index != i) {
                mutableList.add(jks)
            }
        }
    }

    fun filterAndMapJksConfig(
        doSomething: (mutableList: MutableList<JksEntity>, i: Int, jks: JksEntity) -> Unit
    ) {
        val cacheJks = _jksConfigFlow.value
        val newJksConfig = mutableListOf<JksEntity>()
        cacheJks.forEachIndexed { i, jksEntity ->
            doSomething.invoke(newJksConfig, i, jksEntity)
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
        var cachedIndex = -1
        cacheKey.forEachIndexed { index, cacheJks ->
            if (cacheJks.fileUid == jksEntity.fileUid) {
                cachedIndex = index
                return@forEachIndexed
            }
        }
        if (cachedIndex != -1) {
            saveEditJksConfig(jksEntity)
        } else {
            writeJksConfig(
                list = mutableListOf<JksEntity>().apply {
                    addAll(cacheKey)
                    add(jksEntity)
                },
                false,
                onComplete,
            )
        }
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