package kr.co.lokit.api.domain.photo.application

import kr.co.lokit.api.common.exception.BusinessException
import kr.co.lokit.api.domain.photo.application.port.CommentRepositoryPort
import kr.co.lokit.api.domain.photo.application.port.EmoticonRepositoryPort
import kr.co.lokit.api.domain.photo.application.port.`in`.CommentUseCase
import kr.co.lokit.api.domain.photo.application.port.`in`.EmoticonUseCase
import kr.co.lokit.api.domain.photo.domain.Comment
import kr.co.lokit.api.domain.photo.domain.CommentWithEmoticons
import kr.co.lokit.api.domain.photo.domain.Emoticon
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepositoryPort,
    private val emoticonRepository: EmoticonRepositoryPort,
) : CommentUseCase,
    EmoticonUseCase {
    @Transactional
    override fun createComment(photoId: Long, userId: Long, content: String): Comment {
        val comment = Comment(photoId = photoId, userId = userId, content = content)
        return commentRepository.save(comment)
    }

    @Transactional(readOnly = true)
    override fun getComments(photoId: Long, currentUserId: Long): List<CommentWithEmoticons> =
        commentRepository.findAllByPhotoIdWithEmoticons(photoId, currentUserId)

    @Transactional
    override fun addEmoticon(commentId: Long, userId: Long, emoji: String): Emoticon {
        val count = emoticonRepository.countByCommentIdAndUserId(commentId, userId)
        if (count >= MAX_EMOTICONS_PER_USER_PER_COMMENT) {
            throw BusinessException.CommentMaxEmoticonsExceededException(
                errors = mapOf("commentId" to commentId.toString(), "userId" to userId.toString()),
            )
        }
        val emoticon = Emoticon(commentId = commentId, userId = userId, emoji = emoji)
        return emoticonRepository.save(emoticon)
    }

    @Transactional
    override fun removeEmoticon(commentId: Long, userId: Long, emoji: String) {
        emoticonRepository.delete(commentId, userId, emoji)
    }

    companion object {
        private const val MAX_EMOTICONS_PER_USER_PER_COMMENT = 10
    }
}
