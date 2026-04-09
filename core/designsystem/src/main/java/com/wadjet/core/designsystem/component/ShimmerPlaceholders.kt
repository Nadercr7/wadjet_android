package com.wadjet.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Shimmer placeholder for a list of card-style items. */
@Composable
fun ShimmerCardList(
    itemCount: Int = 4,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(itemCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ShimmerEffect(
                    modifier = Modifier
                        .size(64.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(16.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(12.dp),
                    )
                }
            }
        }
    }
}

/** Shimmer placeholder for a 2-column grid of items. */
@Composable
fun ShimmerGrid(
    itemCount: Int = 6,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat((itemCount + 1) / 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                )
                ShimmerEffect(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                )
            }
        }
    }
}

/** Shimmer placeholder for a detail page layout. */
@Composable
fun ShimmerDetail(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        )
        Spacer(Modifier.height(16.dp))
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(24.dp),
        )
        Spacer(Modifier.height(12.dp))
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp),
        )
        Spacer(Modifier.height(24.dp))
        repeat(4) {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
