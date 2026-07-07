package com.wadjet.app.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors
import com.wadjet.core.designsystem.animation.FadeUp
import com.wadjet.core.designsystem.animation.GoldGradientText
import com.wadjet.core.designsystem.component.WadjetButton
import com.wadjet.core.designsystem.R as DesignR
import com.wadjet.app.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val body: Int,
    @StringRes val iconCd: Int,
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = DesignR.drawable.ic_nav_hieroglyphs,
        title = R.string.onboarding_scan_title,
        body = R.string.onboarding_scan_body,
        iconCd = R.string.onboarding_scan_icon_cd,
    ),
    OnboardingPage(
        icon = DesignR.drawable.ic_nav_explore,
        title = R.string.onboarding_explore_title,
        body = R.string.onboarding_explore_body,
        iconCd = R.string.onboarding_explore_icon_cd,
    ),
    OnboardingPage(
        icon = DesignR.drawable.ic_nav_thoth,
        title = R.string.onboarding_thoth_title,
        body = R.string.onboarding_thoth_body,
        iconCd = R.string.onboarding_thoth_icon_cd,
    ),
)

/**
 * U7: first-run carousel shown once before Welcome. Three pages (Scan / Explore /
 * Ask Thoth) in the bespoke Egyptian glyph language, skippable at any point, and
 * accessible (labelled icons, a page-position announcement, 48dp targets). Swiping
 * is user-driven; nothing auto-advances.
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WadjetColors.Night),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Skip — always reachable, top-end.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onFinish) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = WadjetColors.TextMuted,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { pageIndex ->
                val page = onboardingPages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(132.dp)
                            .clip(CircleShape)
                            .background(WadjetColors.Gold.copy(alpha = 0.10f))
                            .border(1.dp, WadjetColors.Gold.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(page.icon),
                            contentDescription = stringResource(page.iconCd),
                            tint = WadjetColors.Gold,
                            modifier = Modifier.size(60.dp),
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                    GoldGradientText(
                        text = stringResource(page.title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(page.body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = WadjetColors.TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Dot indicator + primary action.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val pageIndicatorCd = stringResource(
                    R.string.onboarding_page_indicator_cd,
                    pagerState.currentPage + 1,
                    onboardingPages.size,
                )
                Row(
                    modifier = Modifier
                        .padding(bottom = 28.dp)
                        .semantics { contentDescription = pageIndicatorCd },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    onboardingPages.indices.forEach { index ->
                        val selected = index == pagerState.currentPage
                        val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "dotWidth")
                        val color by animateColorAsState(
                            if (selected) WadjetColors.Gold else WadjetColors.Gold.copy(alpha = 0.28f),
                            label = "dotColor",
                        )
                        Box(
                            modifier = Modifier
                                .size(width = width, height = 8.dp)
                                .clip(CircleShape)
                                .background(color),
                        )
                    }
                }

                FadeUp(visible = true) {
                    WadjetButton(
                        text = stringResource(
                            if (isLastPage) R.string.onboarding_start else R.string.onboarding_next,
                        ),
                        onClick = {
                            if (isLastPage) {
                                onFinish()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
