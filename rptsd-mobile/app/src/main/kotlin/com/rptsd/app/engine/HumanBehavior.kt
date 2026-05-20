package com.rptsd.app.engine

import kotlin.random.Random

object HumanBehavior {
    fun getRandomDelay(): Long = Random.nextLong(2_000, 6_001)

    fun shouldRandomSkip(percent: Int): Boolean = Random.nextInt(100) < percent

    fun getRandomOffset(): Pair<Float, Float> =
        Pair(Random.nextFloat() * 16f - 8f, Random.nextFloat() * 16f - 8f)
}
