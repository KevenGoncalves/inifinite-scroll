package com.example.paginationtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class PhotoViewModel : ViewModel() {
    private val repository = PhotoRepository()

    val pagingSource = Pager(
        PagingConfig(pageSize = 30, initialLoadSize = 30, enablePlaceholders = false)
    ) {
        PhotoPagingSource(repository)
    }.flow.cachedIn(viewModelScope)
}

class PhotoPagingSource(
    private val repository: PhotoRepository
) : PagingSource<Int, Photo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val page = params.key ?: 1
        val limit = params.loadSize

        return when (val result = repository.getPhotos(page, limit)) {
            is ApiResult.Success -> {
                LoadResult.Page(
                    data = result.data,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (result.data.isEmpty()) null else page + 1
                )
            }

            is ApiResult.Error -> {
                LoadResult.Error(Exception(result.message))
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}

class PhotoRepository {

    private val apiService = Retro.api

    suspend fun getPhotos(page: Int, limit: Int): ApiResult<List<Photo>> {
        return try {
            val response = apiService.getPhotos(page, limit)

            if (!response.isSuccessful) {
                ApiResult.Error(response.errorBody().toString())
            }

            if (response.body() == null) {
                return ApiResult.Error("No data found")
            }

            ApiResult.Success(response.body()!!)

        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val data: List<Photo>) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

data class Photo(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String
)

object Retro {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("photos")
    suspend fun getPhotos(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int
    ): Response<List<Photo>>
}