package io.github.lowkeylab.freedsl.test

import kotlin.test.Test
import kotlin.test.assertEquals

class AddressDslTest {
    @Test
    fun testAddressDslWithAllProperties() {
        // Create an Address using the DSL
        val address =
            address {
                street = "123 Main St"
                city = "Anytown"
                zipCode = "12345"
                country = "USA"
            }

        // Verify the properties
        assertEquals("123 Main St", address.street)
        assertEquals("Anytown", address.city)
        assertEquals("12345", address.zipCode)
        assertEquals("USA", address.country)
    }

    @Test
    fun testAddressDslWithDefaultCountry() {
        // Create an Address using the DSL with default country
        val address =
            address {
                street = "456 Oak Ave"
                city = "Othertown"
                zipCode = "67890"
                // country is not set, should use default value
            }

        // Verify the properties
        assertEquals("456 Oak Ave", address.street)
        assertEquals("Othertown", address.city)
        assertEquals("67890", address.zipCode)
        assertEquals("", address.country)
    }
}
