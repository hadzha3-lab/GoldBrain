package com.hadzha3.goldbrain.domain.source

object PhotoSourceQueryParser {
    fun filterFor(
        normalizedQuery: String
    ): Set<PhotoSourceType>? =
        when {
            normalizedQuery.contains(
                "goldbrain"
            ) ||
                normalizedQuery.contains(
                    "голдбрейн"
                ) ->
                setOf(
                    PhotoSourceType.GOLDBRAIN
                )

            containsAny(
                normalizedQuery,
                DOWNLOAD_STEMS
            ) ->
                setOf(
                    PhotoSourceType.DOWNLOAD
                )

            containsAny(
                normalizedQuery,
                SCREENSHOT_STEMS
            ) ->
                setOf(
                    PhotoSourceType.SCREENSHOT
                )

            containsAny(
                normalizedQuery,
                MESSENGER_STEMS
            ) ->
                setOf(
                    PhotoSourceType.MESSENGER
                )

            containsAny(
                normalizedQuery,
                CAMERA_STEMS
            ) ->
                setOf(
                    PhotoSourceType.CAMERA,
                    PhotoSourceType.GOLDBRAIN
                )

            else ->
                null
        }

    fun searchableText(
        type: PhotoSourceType
    ): String =
        when (type) {
            PhotoSourceType.CAMERA ->
                "камера снято камерой сделано мной мои фото camera captured taken"

            PhotoSourceType.DOWNLOAD ->
                "скачано скачанные загрузка загрузки download downloaded"

            PhotoSourceType.SCREENSHOT ->
                "скриншот скрин screenshots screenshot screen capture"

            PhotoSourceType.MESSENGER ->
                "мессенджер telegram телеграм whatsapp ватсап signal viber messenger"

            PhotoSourceType.GOLDBRAIN ->
                "goldbrain голдбрейн камера снято в goldbrain сделано мной мои фото"

            PhotoSourceType.UNKNOWN ->
                ""
        }

    fun isSourceIntentWord(
        word: String
    ): Boolean =
        SOURCE_INTENT_STEMS
            .any(
                word::startsWith
            )

    private fun containsAny(
        value: String,
        stems: List<String>
    ): Boolean =
        stems.any {
            value.contains(it)
        }

    private val DOWNLOAD_STEMS =
        listOf(
            "скачан",
            "загруз",
            "download"
        )

    private val SCREENSHOT_STEMS =
        listOf(
            "скриншот",
            "скрин",
            "screenshot"
        )

    private val MESSENGER_STEMS =
        listOf(
            "мессендж",
            "telegram",
            "телеграм",
            "whatsapp",
            "ватсап",
            "signal",
            "viber",
            "messenger"
        )

    private val CAMERA_STEMS =
        listOf(
            "снято",
            "снял",
            "сняла",
            "камер",
            "сделан мной",
            "сделал",
            "сделала",
            "мои фото",
            "my photo",
            "camera",
            "captured"
        )

    private val SOURCE_INTENT_STEMS =
        (
            DOWNLOAD_STEMS +
                SCREENSHOT_STEMS +
                MESSENGER_STEMS +
                listOf(
                    "сня",
                    "камер",
                    "сдел",
                    "goldbrain",
                    "голдбрейн",
                    "download",
                    "camera",
                    "captur"
                )
            )
            .distinct()
}
