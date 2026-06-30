package com.copymanga.downloader.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.copymanga.downloader.data.model.ComicInSearch
import com.copymanga.downloader.di.AppContainer
import com.copymanga.downloader.domain.repository.CopyRepository
import kotlinx.coroutines.launch

private const val SEARCH_LIMIT = 20

class SearchViewModel(
    private val copyRepository: CopyRepository,
) : ViewModel() {

    var keyword by mutableStateOf("")
        private set

    var results by mutableStateOf<List<ComicInSearch>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var hasMore by mutableStateOf(true)
        private set

    private var currentPage = 0

    fun updateKeyword(value: String) {
        keyword = value
    }

    fun search() {
        if (keyword.isBlank()) return
        currentPage = 0
        results = emptyList()
        hasMore = true
        errorMessage = null
        loadPage()
    }

    fun loadMore() {
        if (isLoading || isLoadingMore || !hasMore || keyword.isBlank()) return
        loadPage(isLoadMore = true)
    }

    fun dismissError() {
        errorMessage = null
    }

    private fun loadPage(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (isLoadMore) {
                isLoadingMore = true
            } else {
                isLoading = true
            }
            val page = currentPage + 1
            copyRepository.search(keyword, page)
                .onSuccess { list ->
                    results = if (page == 1) list else results + list
                    hasMore = list.size >= SEARCH_LIMIT
                    currentPage = page
                }
                .onFailure { e ->
                    errorMessage = e.message ?: ""
                }
            if (isLoadMore) {
                isLoadingMore = false
            } else {
                isLoading = false
            }
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(container.copyRepository) as T
                }
            }
    }
}
