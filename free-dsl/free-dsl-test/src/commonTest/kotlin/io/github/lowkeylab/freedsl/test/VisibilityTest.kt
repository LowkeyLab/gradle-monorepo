package io.github.lowkeylab.freedsl.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VisibilityTest {
    @Test
    fun testPublicClassDslIsAccessible() {
        // Create a PublicClass using the DSL - this should be accessible
        val publicClass = publicClass {
            name = "Test Public Class"
            value = 42
        }

        // Verify the properties
        assertEquals("Test Public Class", publicClass.name)
        assertEquals(42, publicClass.value)
    }

    @Test
    fun testInternalClassDslIsAccessible() {
        // Create an InternalClass using the DSL - this should be accessible within the module
        val internalClass = internalClass {
            name = "Test Internal Class"
            value = 100
        }

        // Verify the properties
        assertEquals("Test Internal Class", internalClass.name)
        assertEquals(100, internalClass.value)
    }

    @Test
    fun testRegularClassDslIsAccessible() {
        // Create a RegularClass using the DSL - should be public by default
        val regularClass = regularClass {
            name = "Test Regular Class"
            value = 200
        }

        // Verify the properties
        assertEquals("Test Regular Class", regularClass.name)
        assertEquals(200, regularClass.value)
    }
}