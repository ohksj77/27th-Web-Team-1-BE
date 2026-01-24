package kr.co.lokit.api.domain.workspace.infrastructure

import kr.co.lokit.api.common.exception.BusinessException
import kr.co.lokit.api.domain.user.infrastructure.UserJpaRepository
import kr.co.lokit.api.domain.workspace.domain.WorkSpace
import kr.co.lokit.api.domain.workspace.mapping.toDomain
import kr.co.lokit.api.domain.workspace.mapping.toEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class WorkspaceRepositoryImpl(
    private val workspaceJpaRepository: WorkspaceJpaRepository,
    private val userJpaRepository: UserJpaRepository,
) : WorkspaceRepository {
    override fun save(workspace: WorkSpace): WorkSpace {
        val entity = workspace.toEntity()
        return workspaceJpaRepository.save(entity).toDomain()
    }

    override fun findById(id: Long): WorkSpace? =
        workspaceJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun saveWithUser(workspace: WorkSpace, userId: Long): WorkSpace {
        val userEntity = userJpaRepository.findByIdOrNull(userId)
            ?: throw BusinessException.UserNotFoundException()

        val workspaceEntity = workspace.toEntity()
        val savedWorkspace = workspaceJpaRepository.save(workspaceEntity)

        val workspaceUser = WorkspaceUserEntity(
            workspace = savedWorkspace,
            user = userEntity,
        )
        savedWorkspace.addUser(workspaceUser)

        return savedWorkspace.toDomain()
    }
}
