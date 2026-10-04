package com.hadzha3.goldbrain.background

data class IndexingLoadPolicy(
    val batchSize: Int,
    val continuationDelaySeconds: Long
)

object IndexingLoadPolicySelector {
    fun select(
        isCharging: Boolean,
        isPowerSaveMode: Boolean
    ): IndexingLoadPolicy =
        when {
            isPowerSaveMode ->
                IndexingLoadPolicy(
                    batchSize = 5,
                    continuationDelaySeconds = 20
                )

            isCharging ->
                IndexingLoadPolicy(
                    batchSize = 30,
                    continuationDelaySeconds = 2
                )

            else ->
                IndexingLoadPolicy(
                    batchSize = 15,
                    continuationDelaySeconds = 6
                )
        }
}
