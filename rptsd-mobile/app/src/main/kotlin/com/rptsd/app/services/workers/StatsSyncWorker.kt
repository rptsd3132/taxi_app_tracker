package com.rptsd.app.services.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rptsd.app.data.repository.StatsRepository
import com.rptsd.app.domain.model.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StatsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val statsRepository: StatsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when (statsRepository.syncTodayStats()) {
            is com.rptsd.app.domain.model.Result.Success -> Result.success()
            is com.rptsd.app.domain.model.Result.Error -> if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "stats_sync_periodic"
    }
}
