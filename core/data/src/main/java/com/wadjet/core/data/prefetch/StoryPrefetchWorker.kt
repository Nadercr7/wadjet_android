package com.wadjet.core.data.prefetch

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.wadjet.core.data.datastore.UserPreferencesDataStore
import com.wadjet.core.domain.repository.StoriesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * E-P1: prefetches every story's full content into the existing Room cache
 * (story_cache, via StoriesRepository.getStory's network-first caching) so
 * the whole library reads offline — not just previously-opened stories (E-02).
 *
 * Constraints: UNMETERED network + battery-not-low; runs once a day and is
 * gated by the Settings toggle (default on). Chapter images are generated
 * on demand server-side (AI) and are intentionally NOT prefetched.
 */
@HiltWorker
class StoryPrefetchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val storiesRepository: StoriesRepository,
    private val preferences: UserPreferencesDataStore,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!preferences.prefetchStoriesOnWifi.first()) {
            Timber.d("E-P1 prefetch disabled by setting — skipping")
            return Result.success()
        }

        val stories = storiesRepository.getStories().getOrNull()
            ?: return retryOrFail()

        var cached = 0
        for (summary in stories) {
            storiesRepository.getStory(summary.id)
                .onSuccess { cached++ }
                .onFailure { e -> Timber.w(e, "E-P1 prefetch failed for %s", summary.id) }
        }
        Timber.i("E-P1 prefetched %d/%d stories into Room cache", cached, stories.size)

        return if (cached > 0 || stories.isEmpty()) Result.success() else retryOrFail()
    }

    private fun retryOrFail(): Result =
        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()

    companion object {
        const val WORK_NAME = "story_prefetch"
        private const val MAX_ATTEMPTS = 3
    }
}

/** Schedules/cancels the daily prefetch according to the Settings toggle. */
@Singleton
class StoryPrefetchScheduler @Inject constructor() {

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<StoryPrefetchWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresBatteryNotLow(true)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            StoryPrefetchWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
        Timber.d("E-P1 prefetch scheduled (daily, unmetered, battery-not-low)")
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(StoryPrefetchWorker.WORK_NAME)
        Timber.d("E-P1 prefetch cancelled")
    }
}
