package com.example.aismartexpensetracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The on-device fallback categorizer. This is what runs when the ML server is
 * unreachable, so the app must still produce a real category offline.
 */
class CategoryKeywordsTest {

    @Test
    fun `categorizes common food merchants`() {
        assertEquals("Food", CategoryKeywords.categorize("Swiggy"))
        assertEquals("Food", CategoryKeywords.categorize("Zomato Order"))
    }

    @Test
    fun `is case insensitive`() {
        assertEquals("Food", CategoryKeywords.categorize("SWIGGY BANGALORE"))
        assertEquals("Travel", CategoryKeywords.categorize("uber"))
    }

    @Test
    fun `matches within a longer merchant string`() {
        assertEquals("Groceries", CategoryKeywords.categorize("BigBasket Delivery #4471"))
    }

    @Test
    fun `prefers the more specific keyword`() {
        // "JioMart" contains "jio" (Bills) and "jiomart" (Groceries). The
        // longest-keyword-first ordering is what makes Groceries win.
        assertEquals("Groceries", CategoryKeywords.categorize("JioMart Order"))
        assertEquals("Bills", CategoryKeywords.categorize("Jio Recharge"))
    }

    @Test
    fun `returns Uncategorized for an unknown merchant`() {
        assertEquals(
            CategoryKeywords.UNCATEGORIZED,
            CategoryKeywords.categorize("Zzz Unknown Vendor 9931")
        )
    }

    @Test
    fun `exposes every category plus the fallback for the correction UI`() {
        assertTrue(CategoryKeywords.ALL_CATEGORIES.contains("Food"))
        assertTrue(CategoryKeywords.ALL_CATEGORIES.contains(CategoryKeywords.UNCATEGORIZED))
        assertEquals(11, CategoryKeywords.ALL_CATEGORIES.size)
    }
}
