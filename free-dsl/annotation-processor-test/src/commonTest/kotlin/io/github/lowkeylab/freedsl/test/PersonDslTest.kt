package io.github.lowkeylab.freedsl.test

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PersonDslTest {
    @Test
    fun testPersonDslWithRequiredProperties() {
        // Create a Person using the DSL with only required properties
        val person =
            person {
                name = "John Doe"
                age = 30
            }

        // Verify the properties
        assertEquals("John Doe", person.name)
        assertEquals(30, person.age)
        assertNull(person.email)
        assertNull(person.address)
        assertContentEquals(emptyList(), person.tags)
    }

    @Test
    fun testPersonDslWithAllProperties() {
        // Create a Person using the DSL with all properties
        val person =
            person {
                name = "Jane Smith"
                age = 25
                email = "jane@example.com"
                address =
                    address {
                        street = "123 Main St"
                        city = "Anytown"
                        zipCode = "12345"
                        country = "USA"
                    }
                tags = mutableListOf("developer", "kotlin")
            }

        // Verify the properties
        assertEquals("Jane Smith", person.name)
        assertEquals(25, person.age)
        assertEquals("jane@example.com", person.email)

        // Verify the nested Address
        assertEquals("123 Main St", person.address?.street)
        assertEquals("Anytown", person.address?.city)
        assertEquals("12345", person.address?.zipCode)
        assertEquals("USA", person.address?.country)

        // Verify the list of tags
        assertContentEquals(listOf("developer", "kotlin"), person.tags)
    }
}
