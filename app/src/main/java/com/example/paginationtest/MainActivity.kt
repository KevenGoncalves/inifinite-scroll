package com.example.paginationtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.paginationtest.ui.theme.PaginationTestTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<PhotoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaginationTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        val photos = viewModel.pagingSource.collectAsLazyPagingItems()

                        when (photos.loadState.refresh) {
                            is LoadState.Loading -> {
                                Text("Loading...")
                            }

                            is LoadState.Error -> {
                                Text("Error")
                            }

                            else -> {
                                LazyColumn {
                                    items(photos.itemCount) {
                                        ListItem(
                                            headlineContent = { Text((photos[it]?.id.toString() + "." + photos[it]?.title)) },
                                            supportingContent = {
                                                Text(
                                                    photos[it]?.url ?: "No url"
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}