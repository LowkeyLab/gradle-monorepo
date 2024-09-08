package com.github.lowkeylab.ptoscheduler.user.db

import com.github.lowkeylab.ptoscheduler.user.User
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "user_entity")
class UserEntity(
    var name: String,
    var maxPtoDays: Int,
    @ElementCollection
    var ptoDays: MutableSet<LocalDate> = mutableSetOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    var id: Long? = null,
)

interface UserEntityRepository : JpaRepository<UserEntity, Long>

interface UserRepository {
    fun findUserById(id: Long): User?

    fun save(user: User): User
}

class JpaUserRepository(
    private val userEntityRepository: UserEntityRepository,
    private val userEntityMapper: UserEntityMapper,
) : UserRepository {
    override fun findUserById(id: Long): User? {
        val userEntity = userEntityRepository.findById(id).orElse(null)
        return userEntity?.let { User(it.name, it.maxPtoDays, it.ptoDays) }
    }

    override fun save(user: User): User = userEntityMapper.toDto(userEntityRepository.save(userEntityMapper.toEntity(user)))
}
