package com.wadjet.feature.dictionary.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.feature.dictionary.DictionaryViewModel
import com.wadjet.feature.dictionary.WriteViewModel
import com.wadjet.feature.dictionary.sheet.SignDetailSheet
import kotlinx.coroutines.launch

private val TABS = listOf("Browse", "Learn", "Write")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    onNavigateToLesson: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dictionaryViewModel: DictionaryViewModel = hiltViewModel(),
    writeViewModel: WriteViewModel = hiltViewModel(),
) {
    val browseState by dictionaryViewModel.state.collectAsStateWithLifecycle()
    val writeState by writeViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { TABS.size })

    Column(modifier = modifier.fillMaxSize()) {
        // Tab row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = WadjetColors.Night,
            contentColor = WadjetColors.Gold,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = WadjetColors.Gold,
                    )
                }
            },
        ) {
            TABS.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (pagerState.currentPage == index) WadjetColors.Gold else WadjetColors.TextMuted,
                        )
                    },
                )
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> BrowseTab(
                    state = browseState,
                    onSearchChange = dictionaryViewModel::onSearchQueryChange,
                    onCategorySelect = dictionaryViewModel::selectCategory,
                    onTypeSelect = dictionaryViewModel::selectType,
                    onSignClick = dictionaryViewModel::selectSign,
                    onLoadMore = dictionaryViewModel::loadMore,
                )
                1 -> LearnTab(onLessonClick = onNavigateToLesson)
                2 -> WriteTab(
                    state = writeState,
                    onInputChange = writeViewModel::onInputChange,
                    onModeSelect = writeViewModel::selectMode,
                    onConvert = writeViewModel::convert,
                    onClear = writeViewModel::clear,
                    onAppendGlyph = writeViewModel::appendGlyph,
                )
            }
        }
    }

    // Sign Detail Bottom Sheet
    if (browseState.selectedSign != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { dictionaryViewModel.selectSign(null) },
            sheetState = sheetState,
            containerColor = WadjetColors.Surface,
            contentColor = WadjetColors.Text,
        ) {
            SignDetailSheet(
                sign = browseState.selectedSign!!,
                onSpeak = { /* TTS wired in later phase */ },
            )
        }
    }
}
