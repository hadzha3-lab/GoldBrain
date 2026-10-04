package com.hadzha3.goldbrain.background

object AutoIndexPolicy {
    fun shouldSchedule(
        enabled: Boolean,
        hasGalleryAccess: Boolean
    ): Boolean =
        enabled &&
            hasGalleryAccess
}
