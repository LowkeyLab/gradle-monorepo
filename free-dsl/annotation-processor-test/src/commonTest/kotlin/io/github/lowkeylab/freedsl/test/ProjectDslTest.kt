package io.github.lowkeylab.freedsl.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class ProjectDslTest {

    @Test
    fun testProjectDslWithRequiredProperties() {
        // Create a Project using the DSL with only required properties
        val project = project {
            name = "Test Project"
            owner = person {
                name = "John Doe"
                age = 30
            }
        }

        // Verify the properties
        assertEquals("Test Project", project.name)
        assertNull(project.description)

        // Verify the owner
        assertEquals("John Doe", project.owner.name)
        assertEquals(30, project.owner.age)

        // Verify the default collections
        assertContentEquals(emptyList(), project.contributors)
        assertTrue(project.tags.isEmpty())
        assertTrue(project.metadata.isEmpty())
    }

    @Test
    fun testProjectDslWithAllProperties() {
        // Create a Project using the DSL with all properties
        val project = project {
            name = "Complex Project"
            description = "A project with all properties set"

            owner = person {
                name = "Jane Smith"
                age = 35
                email = "jane@example.com"
            }

            contributors = mutableListOf(
                person {
                    name = "Alice Johnson"
                    age = 28
                    email = "alice@example.com"
                },
                person {
                    name = "Bob Williams"
                    age = 42
                }
            )

            tags = mutableSetOf("kotlin", "dsl", "testing")

            metadata = mutableMapOf(
                "version" to "1.0.0",
                "status" to "active"
            )
        }

        // Verify the basic properties
        assertEquals("Complex Project", project.name)
        assertEquals("A project with all properties set", project.description)

        // Verify the owner
        assertEquals("Jane Smith", project.owner.name)
        assertEquals(35, project.owner.age)
        assertEquals("jane@example.com", project.owner.email)

        // Verify the contributors
        assertEquals(2, project.contributors.size)

        val alice = project.contributors[0]
        assertEquals("Alice Johnson", alice.name)
        assertEquals(28, alice.age)
        assertEquals("alice@example.com", alice.email)

        val bob = project.contributors[1]
        assertEquals("Bob Williams", bob.name)
        assertEquals(42, bob.age)
        assertNull(bob.email)

        // Verify the tags
        assertEquals(3, project.tags.size)
        assertTrue(project.tags.contains("kotlin"))
        assertTrue(project.tags.contains("dsl"))
        assertTrue(project.tags.contains("testing"))

        // Verify the metadata
        assertEquals(2, project.metadata.size)
        assertEquals("1.0.0", project.metadata["version"])
        assertEquals("active", project.metadata["status"])
    }

    @Test
    fun testProjectDslWithBlockSyntax() {
        // Create a Project using the DSL with block syntax for list properties
        val project = project {
            name = "Block Syntax Project"
            description = "A project using block syntax for list properties"

            owner = person {
                name = "Jane Smith"
                age = 35
                email = "jane@example.com"
            }

            // Use block syntax for contributors
            contributors {
                contributor(person {
                    name = "Alice Johnson"
                    age = 28
                    email = "alice@example.com"
                })

                contributor(person {
                    name = "Bob Williams"
                    age = 42
                })
            }
        }

        // Verify the basic properties
        assertEquals("Block Syntax Project", project.name)
        assertEquals("A project using block syntax for list properties", project.description)

        // Verify the owner
        assertEquals("Jane Smith", project.owner.name)
        assertEquals(35, project.owner.age)
        assertEquals("jane@example.com", project.owner.email)

        // Verify the contributors
        assertEquals(2, project.contributors.size)

        val alice = project.contributors[0]
        assertEquals("Alice Johnson", alice.name)
        assertEquals(28, alice.age)
        assertEquals("alice@example.com", alice.email)

        val bob = project.contributors[1]
        assertEquals("Bob Williams", bob.name)
        assertEquals(42, bob.age)
        assertNull(bob.email)
    }
}
