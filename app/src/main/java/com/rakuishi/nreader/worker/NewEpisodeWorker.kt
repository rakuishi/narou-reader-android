package com.rakuishi.nreader.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.rakuishi.nreader.App
import com.rakuishi.nreader.util.NotificationHelper
import java.util.*
import java.util.concurrent.TimeUnit

class NewEpisodeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {

        private const val TAG = "NewEpisodeWorker"
        private const val REPEAT_INTERVAL_MINUTES = 30L

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NewEpisodeWorker>(
                REPEAT_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(REPEAT_INTERVAL_MINUTES, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(TAG)
        }
    }

    override suspend fun doWork(): Result {
        if (applicationContext !is App) {
            return Result.failure()
        }

        val appContext = (applicationContext as App)
        val novels = appContext.novelRepository.fetchList(false)
        val compareTo =
            Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(REPEAT_INTERVAL_MINUTES))
        val newEpisodeNovels = novels.filter {
            it.latestEpisodeUpdatedAt.after(compareTo) && it.hasNewEpisode
        }

        Log.d(TAG, "doWork: hasNewEpisode ${newEpisodeNovels.isNotEmpty()}")

        if (newEpisodeNovels.isNotEmpty()) {
            NotificationHelper.notifyNewEpisode(appContext, newEpisodeNovels.first())
        }

        return Result.success()
    }
}