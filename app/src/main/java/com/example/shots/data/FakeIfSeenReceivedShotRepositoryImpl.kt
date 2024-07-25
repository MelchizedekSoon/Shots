package com.example.shots.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeIfSeenReceivedShotRepositoryImpl(
    val ifSeenReceivedShotLocalDataSource: IfSeenReceivedShotLocalDataSource,
    val ifSeenReceivedShotRemoteDataSource: IfSeenReceivedShotRemoteDataSource
) : IfSeenReceivedShotRepository {

    override fun getYourUserId(): String {
        return "John"
    }

    override suspend fun saveIfSeenReceivedShot(
        ifSeenReceivedShotId: String,
        ifSeenReceivedShotData: MutableMap<String, Boolean>
    ) {
        Log.d("FakeIfSeenReceivedShotRepositoryImpl", "ifSeenReceivedShotId = $ifSeenReceivedShotId")
        val success =
            ifSeenReceivedShotRemoteDataSource.writeIfSeenReceivedShotToFirebase(
                ifSeenReceivedShotId,
                ifSeenReceivedShotData
            )
        Log.d("FakeIfSeenReceivedShotRepositoryImpl", "success = $success")
        if (success) {
            val ifSeenReceivedShotList =
                ifSeenReceivedShotRemoteDataSource.getIfSeenReceivedShotsFromFirebase()
                    .toMutableList()
            try {
                var ifSeenReceivedShot = ifSeenReceivedShotLocalDataSource.findById(getYourUserId())
                ifSeenReceivedShot = ifSeenReceivedShot.copy(
                    ifSeenReceivedShotId = getYourUserId(),
                    ifSeenReceivedShots = ifSeenReceivedShotList
                )
                Log.d("FakeIfSeenReceivedShotRepositoryImpl", "$ifSeenReceivedShotList")
                ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
            } catch (npe: java.lang.NullPointerException) {
                val ifSeenReceivedShot =
                    IfSeenReceivedShot(getYourUserId(), ifSeenReceivedShotList.toMutableList())
                Log.d("FakeIfSeenReceivedShotRepositoryImpl", "ifSeenReceivedShot was stored!")
                try {
                    ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
                } catch (e: Exception) {
                    Log.d(
                        "FakeIfSeenReceivedShotRepositoryImpl",
                        "ifSeenReceivedShot failed to be stored!"
                    )
                }
            }
        }
    }

    override suspend fun storeIfSeenReceivedShot(ifSeenReceivedShot: IfSeenReceivedShot) {
        ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
    }

    override fun getIfSeenReceivedShot(userId: String): IfSeenReceivedShot {
        return ifSeenReceivedShotLocalDataSource.findById(getYourUserId())
    }

    override suspend fun getUpdatedIfSeenReceivedShots(): Flow<List<String>> {
        val ifSeenReceivedShots = ifSeenReceivedShotLocalDataSource.findById(getYourUserId())
        return flow { emit(ifSeenReceivedShots.ifSeenReceivedShots) }
    }

    override suspend fun fetchUpdatedIfSeenReceivedShots(): Flow<List<String>> {
        val ifSeenReceivedShots =
            ifSeenReceivedShotRemoteDataSource.getIfSeenReceivedShotsFromFirebase()
        val ifSeenReceivedShot =
            IfSeenReceivedShot(getYourUserId(), ifSeenReceivedShots.toMutableList())
        ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
        val storedIfSeenReceivedShots = ifSeenReceivedShotLocalDataSource.findById(getYourUserId())
        return flow { emit(storedIfSeenReceivedShots.ifSeenReceivedShots) }
    }

    override suspend fun removeIfSeenReceivedShot(userId: String) {
        val success = ifSeenReceivedShotRemoteDataSource.removeIfSeenReceivedShotFromFirebase(userId)
        if (success) {
            val ifSeenReceivedShotList =
                ifSeenReceivedShotRemoteDataSource.getIfSeenReceivedShotsFromFirebase()
                    .toMutableList()
            try {
                var ifSeenReceivedShot = ifSeenReceivedShotLocalDataSource.findById(getYourUserId())
                ifSeenReceivedShot = ifSeenReceivedShot.copy(
                    ifSeenReceivedShotId = getYourUserId(),
                    ifSeenReceivedShots = ifSeenReceivedShotList
                )
                ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
            } catch (npe: java.lang.NullPointerException) {
                val ifSeenReceivedShot =
                    IfSeenReceivedShot(getYourUserId(), ifSeenReceivedShotList.toMutableList())
                Log.d("IfSeenReceivedShotRepositoryImpl", "ifSeenReceivedShot was stored!")
                try {
                    ifSeenReceivedShotLocalDataSource.upsert(ifSeenReceivedShot)
                } catch (e: Exception) {
                    Log.d("IfSeenReceivedShotRepositoryImpl", "ifSeenReceivedShot failed to be stored!")
                }
            }
        }
    }

}