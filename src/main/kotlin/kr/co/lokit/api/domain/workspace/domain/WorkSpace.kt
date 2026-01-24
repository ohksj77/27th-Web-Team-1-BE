package kr.co.lokit.api.domain.workspace.domain

data class WorkSpace(
    val id: Long = 0,
    val name: String,
) {
    companion object {
        fun createDefault(): WorkSpace = WorkSpace(name = "default")
    }
}
