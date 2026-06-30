package com.copymanga.downloader.ui.screens.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.copymanga.downloader.data.model.FavoriteItem
import com.copymanga.downloader.data.model.GetFavoriteOrdering
import com.copymanga.downloader.di.AppContainer
import com.copymanga.downloader.domain.repository.CopyRepository
import kotlinx.coroutines.launch

private const val FAVORITE_LIMIT = 12

class FavoriteViewModel(
    private val copyRepository: CopyRepository,
) : ViewModel() {

    var ordering by mutableStateOf(GetFavoriteOrdering.Added)
        private set

    var results by mutableStateOf<List<FavoriteItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var hasMore by mutableStateOf(true)
        private set

    private var currentPage by mutableIntStateOf(0)

    init {
        load()
    }

    fun updateOrdering(value: GetFavoriteOrdering) {
        if (value == ordering) return
        ordering = value
        currentPage = 0
        results = emptyList()
        hasMore = true
        errorMessage = null
        load()
    }

    fun loadMore() {
        if (isLoading || isLoadingMore || !hasMore) return
        load(isLoadMore = true)
    }

    fun refresh() {
        currentPage = 0
        results = emptyList()
        hasMore = true
        errorMessage = null
        load()
    }

    fun dismissError() {
        errorMessage = null
    }

    private fun load(isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (isLoadMore) {
                isLoadingMore = true
            } else {
                isLoading = true
            }
            val page = currentPage + 1
            copyRepository.getFavorite(ordering, page)
                .onSuccess { list ->
                    results = if (page == 1) list else results + list
                    hasMore = list.size >= FAVORITE_LIMIT
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
                    return FavoriteViewModel(container.copyRepository) as T
                }
            }
    }
}
