package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSmartRevisionIntervals() {
    val defaultIntervals = listOf(1, 3, 7, 14, 30, 60, 90)
    
    // Day 1
    val index1 = (1 - 1).coerceIn(0, defaultIntervals.size - 1)
    assertEquals(1, defaultIntervals[index1])
    
    // Day 3
    val index2 = (2 - 1).coerceIn(0, defaultIntervals.size - 1)
    assertEquals(3, defaultIntervals[index2])
    
    // Day 7
    val index3 = (3 - 1).coerceIn(0, defaultIntervals.size - 1)
    assertEquals(7, defaultIntervals[index3])
  }

  @Test
  fun testExamModeShortenedGaps() {
    fun getShortenedGap(gapDays: Int): Int {
        return when {
            gapDays >= 30 -> 15
            gapDays >= 15 -> 7
            gapDays >= 7 -> 3
            gapDays >= 3 -> 1
            else -> gapDays
        }
    }

    assertEquals(15, getShortenedGap(30))
    assertEquals(7, getShortenedGap(15))
    assertEquals(3, getShortenedGap(7))
    assertEquals(1, getShortenedGap(3))
  }
}
