package com.hadzha3.goldbrain.domain.facts

enum class MemoryFactType {
    MONEY,
    EMAIL,
    PHONE,
    URL,
    DATE,
    ISBN
}

data class MemoryFact(
    val type: MemoryFactType,
    val value: String
)
