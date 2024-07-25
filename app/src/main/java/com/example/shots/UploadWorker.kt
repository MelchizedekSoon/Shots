package com.example.shots

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.shots.ui.theme.IfSeenReceivedShotViewModel
import com.example.shots.ui.theme.ReceivedShotViewModel
import com.example.shots.ui.theme.SentShotViewModel

class SendCallbackWorker(
    val context: Context, workerParams: WorkerParameters,
    val userId: String,
    val receivedShotViewModel: ReceivedShotViewModel,
    private val receivedShotData: MutableMap<String, Uri> = mutableMapOf(),
    private val ifSeenReceivedShotViewModel: IfSeenReceivedShotViewModel,
    private val ifSeenReceivedShotData: MutableMap<String, Boolean> = mutableMapOf(),
    val sentShotViewModel: SentShotViewModel,
    private val sentShotData: MutableMap<String, Uri>
) :
    Worker(context, workerParams) {
    override fun doWork(): Result {

        receivedShotViewModel.saveReceivedShot(
            userId,
            receivedShotData,
            context
        )
        ifSeenReceivedShotViewModel.saveIfSeenReceivedShot(
            userId,
            ifSeenReceivedShotData
        )
        sentShotViewModel.saveSentShot(
            userId,
            sentShotData,
            context
        )

        // Do the work here--in this case, upload the images.
        sentShotViewModel.saveSentShot(userId, sentShotData, context)

        // Indicate whether the work finished successfully with the Result
        Log.d("SendCallbackWorker", "Work finished successfully")
        return Result.success()
    }
}

class RemoveSentShotWorker(
    val context: Context, workerParams: WorkerParameters, val sentShotViewModel: SentShotViewModel,
    val sentShotId: String, val sentShotData: MutableMap<String, Uri>
) :
    Worker(context, workerParams) {
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.
        sentShotViewModel.saveSentShot(sentShotId, sentShotData, context)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}