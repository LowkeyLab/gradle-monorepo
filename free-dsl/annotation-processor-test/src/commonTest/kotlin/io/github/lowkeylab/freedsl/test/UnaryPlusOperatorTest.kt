package io.github.lowkeylab.freedsl.test

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UnaryPlusOperatorTest {
    @Test
    fun testAddingItemsToLists() {
        // Create a Person using the DSL with helper functions for tags
        val person =
            person {
                name = "John Doe"
                age = 30

                // Add tags using helper functions
                tag("developer")
                tag("kotlin")
                tag("dsl")
            }

        // Verify the properties
        assertEquals("John Doe", person.name)
        assertEquals(30, person.age)

        // Verify the list of tags
        assertContentEquals(listOf("developer", "kotlin", "dsl"), person.tags)
    }

    @Test
    fun testAddingItemsToListsWithBlockSyntax() {
        // Create a Person using the DSL with block syntax for tags
        val person =
            person {
                name = "John Doe"
                age = 30

                // Add tags using block syntax
                tags {
                    tag("developer")
                    tag("kotlin")
                    tag("dsl")
                }
            }

        // Verify the properties
        assertEquals("John Doe", person.name)
        assertEquals(30, person.age)

        // Verify the list of tags
        assertContentEquals(listOf("developer", "kotlin", "dsl"), person.tags)
    }

    @Test
    fun testAddingComplexItemsToLists() {
        // Create a Project using the DSL with helper functions for contributors
        val project =
            project {
                name = "Test Project"
                owner =
                    person {
                        name = "Jane Smith"
                        age = 35
                    }

                // Add contributors using helper functions
                contributor(
                    person {
                        name = "Alice Johnson"
                        age = 28
                        email = "alice@example.com"
                    },
                )

                contributor(
                    person {
                        name = "Bob Williams"
                        age = 42
                    },
                )
            }

        // Verify the basic properties
        assertEquals("Test Project", project.name)

        // Verify the contributors
        assertEquals(2, project.contributors.size)

        val alice = project.contributors[0]
        assertEquals("Alice Johnson", alice.name)
        assertEquals(28, alice.age)
        assertEquals("alice@example.com", alice.email)

        val bob = project.contributors[1]
        assertEquals("Bob Williams", bob.name)
        assertEquals(42, bob.age)
    }

    @Test
    fun testAddingComplexItemsToListsWithBlockSyntax() {
        // Create a Project using the DSL with block syntax for contributors
        val project =
            project {
                name = "Test Project"
                owner =
                    person {
                        name = "Jane Smith"
                        age = 35
                    }

                // Add contributors using block syntax
                contributors {
                    contributor(
                        person {
                            name = "Alice Johnson"
                            age = 28
                            email = "alice@example.com"
                        },
                    )

                    contributor(
                        person {
                            name = "Bob Williams"
                            age = 42
                        },
                    )
                }
            }

        // Verify the basic properties
        assertEquals("Test Project", project.name)

        // Verify the contributors
        assertEquals(2, project.contributors.size)

        val alice = project.contributors[0]
        assertEquals("Alice Johnson", alice.name)
        assertEquals(28, alice.age)
        assertEquals("alice@example.com", alice.email)

        val bob = project.contributors[1]
        assertEquals("Bob Williams", bob.name)
        assertEquals(42, bob.age)
    }
}
