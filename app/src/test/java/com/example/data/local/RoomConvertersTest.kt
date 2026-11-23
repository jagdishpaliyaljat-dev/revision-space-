package com.example.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomConvertersTest {

    private val converters = RoomConverters()

    @Test
    fun toIntList_handlesNullAndEmpty() {
        assertTrue(converters.toIntList(null).isEmpty())
        assertTrue(converters.toIntList("").isEmpty())
        assertTrue(converters.toIntList("   ").isEmpty())
        assertTrue(converters.toIntList("[]").isEmpty())
    }

    @Test
    fun toIntList_parsesCommaSeparatedIntegers() {
        val parsed = converters.toIntList("1,3,7,14,30")
        assertEquals(listOf(1, 3, 7, 14, 30), parsed)
    }

    @Test
    fun toIntList_toleratesWhitespaceAndMalformedEntries() {
        val parsed = converters.toIntList(" 1 , abc , 5 , , 10 ")
        assertEquals(listOf(1, 5, 10), parsed)
    }

    @Test
    fun fromIntList_convertsListToString() {
        assertEquals("", converters.fromIntList(null))
        assertEquals("", converters.fromIntList(emptyList()))
        assertEquals("1,3,7,14,30", converters.fromIntList(listOf(1, 3, 7, 14, 30)))
    }

    @Test
    fun roundtrip_preservesEmptyListWithoutDefaultCorruption() {
        val empty = emptyList<Int>()
        val encoded = converters.fromIntList(empty)
        val decoded = converters.toIntList(encoded)
        assertEquals(empty, decoded)
    }

    @Test
    fun roundtrip_preservesCustomIntervals() {
        val intervals = listOf(2, 4, 8, 16)
        val encoded = converters.fromIntList(intervals)
        val decoded = converters.toIntList(encoded)
        assertEquals(intervals, decoded)
    }
}
