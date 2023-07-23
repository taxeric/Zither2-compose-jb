package viewmodel.base

import kotlinx.coroutines.CoroutineScope

interface ScopeViewModel {

    fun provideScope(scope: CoroutineScope)
}