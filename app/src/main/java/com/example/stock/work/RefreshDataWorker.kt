package com.example.stock.work

import android.content.Context
import android.service.voice.AlwaysOnHotwordDetector
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Operation.SUCCESS
import androidx.work.WorkerParameters
import com.example.stock.database.getDatabase
import com.example.stock.repository.TickerRepository
import timber.log.Timber
import java.lang.Exception

class RefreshDataWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshAvailableTickerWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = TickerRepository(database)

        return try {
            if (repository.refreshAvailableTickers() == TickerRepository.OperationResult.Success)
                Result.success()
            else
                Result.retry()
        } catch(e: Exception){
            Result.retry()
        }
    }
}