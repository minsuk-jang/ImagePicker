package com.jms.imagePicker.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jms.imagePicker.Constants
import kotlinx.coroutines.flow.Flow


//TODO: add parameters
internal class GalleryPagingStream {
    fun <Key : Any, Value : Any> load(
        block: suspend (PagingSource.LoadParams<Key>) -> PagingSource.LoadResult<Key, Value>
    ): Flow<PagingData<Value>> {
        return Pager(
            config = createConfig(pageSize = Constants.DEFAULT_PAGE_SiZE)
        ) {
            GalleryPagingSource(block = block)
        }.flow
    }

    private fun createConfig(
        pageSize: Int,
    ): PagingConfig {
        return PagingConfig(
            pageSize = pageSize
        )
    }
}

internal class GalleryPagingSource<Key : Any, Value : Any> constructor(
    private val block: suspend (params: LoadParams<Key>) -> LoadResult<Key, Value>
) : PagingSource<Key, Value>() {
    override fun getRefreshKey(state: PagingState<Key, Value>): Key? {
        return null
    }

    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> {
        return block(params)
    }
}


