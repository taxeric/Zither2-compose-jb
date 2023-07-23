package viewmodel.base

import kotlinx.coroutines.CoroutineScope

class ScopeViewModelImpl: ScopeViewModel {

    lateinit var scope: CoroutineScope

    override fun provideScope(scope: CoroutineScope) {
        this.scope = scope
    }
}