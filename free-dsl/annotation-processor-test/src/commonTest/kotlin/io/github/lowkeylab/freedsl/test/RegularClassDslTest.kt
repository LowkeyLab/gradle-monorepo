package io.github.lowkeylab.freedsl.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegularClassDslTest {
    @Test
    fun testRegularClassDslWithRequiredProperties() {
        // Create a RegularClass using the DSL with only required properties
        val regularClass =
            regularClass {
                name = "Test Regular Class"
                value = 42
            }

        // Verify the properties
        assertEquals("Test Regular Class", regularClass.name)
        assertEquals(42, regularClass.value)
        assertNull(regularClass.description)
    }

    @Test
    fun testRegularClassDslWithAllProperties() {
        // Create a RegularClass using the DSL with all properties
        val regularClass =
            regularClass {
                name = "Test Regular Class"
                value = 42
                description = "This is a test regular class"
            }

        // Verify the properties
        assertEquals("Test Regular Class", regularClass.name)
        assertEquals(42, regularClass.value)
        assertEquals("This is a test regular class", regularClass.description)
    }
}