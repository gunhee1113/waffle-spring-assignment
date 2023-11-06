package com.wafflestudio.seminar.spring2023.customplaylist.service

import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistEntity
import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistRepository
import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistSongEntity
import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistSongRepository
import com.wafflestudio.seminar.spring2023.song.repository.SongRepository
import com.wafflestudio.seminar.spring2023.song.service.Song
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import kotlin.jvm.optionals.getOrNull

/**
 * 스펙:
 *  1. 커스텀 플레이리스트 생성시, 자동으로 생성되는 제목은 "내 플레이리스트 #{내 커스텀 플레이리스트 갯수 + 1}"이다.
 *  2. 곡 추가 시  CustomPlaylistSongEntity row 생성, CustomPlaylistEntity의 songCnt의 업데이트가 atomic하게 동작해야 한다. (둘 다 모두 성공하거나, 둘 다 모두 실패해야 함)
 *
 * 조건:
 *  1. Synchronized 사용 금지.
 *  2. 곡 추가 요청이 동시에 들어와도 동시성 이슈가 없어야 한다.(PlaylistViewServiceImpl에서 동시성 이슈를 해결한 방법과는 다른 방법을 사용할 것)
 *  3. JPA의 변경 감지 기능을 사용해야 한다.
 */
@Service
class CustomPlaylistServiceImpl(
    private val customPlaylistRepository: CustomPlaylistRepository,
    private val customPlaylistSongRepository: CustomPlaylistSongRepository,
    private val songRepository: SongRepository,
    txManager: PlatformTransactionManager,
) : CustomPlaylistService {
    private val transactionTemplate = TransactionTemplate(txManager)
    override fun get(userId: Long, customPlaylistId: Long): CustomPlaylist {
        var retVal = transactionTemplate.execute {
            val customPlaylistEntity = customPlaylistRepository.findById(customPlaylistId).get()
            CustomPlaylist(customPlaylistEntity.id, customPlaylistEntity.title, customPlaylistEntity.songs.map {
                Song(it.song)
            })
        }
        return retVal!!
    }

    override fun gets(userId: Long): List<CustomPlaylistBrief> {
        val customPlaylistEntities = customPlaylistRepository.findByUserId(userId)
        return customPlaylistEntities.map {
            CustomPlaylistBrief(it.id, it.title, it.songCnt)
        }
    }

    override fun create(userId: Long): CustomPlaylistBrief {
        val customPlaylistEntity = CustomPlaylistEntity(userId = userId, title = "내 플레이리스트 #${1+gets(userId).size}")
        customPlaylistRepository.save(customPlaylistEntity)
        return CustomPlaylistBrief(customPlaylistEntity.id, customPlaylistEntity.title, customPlaylistEntity.songCnt)
    }

    override fun patch(userId: Long, customPlaylistId: Long, title: String): CustomPlaylistBrief {
        val retVal = transactionTemplate.execute {
            var customPlaylistEntity = customPlaylistRepository.findByUserIdAndId(userId, customPlaylistId)
            if(customPlaylistEntity==null){
                throw CustomPlaylistNotFoundException()
            }
            customPlaylistEntity.title = title
            CustomPlaylistBrief(customPlaylistEntity.id, customPlaylistEntity.title, customPlaylistEntity.songCnt)
        }
        return retVal!!
    }

    override fun addSong(userId: Long, customPlaylistId: Long, songId: Long): CustomPlaylistBrief {
        val song = songRepository.findById(songId).orElseThrow{SongNotFoundException()}
        val retVal = transactionTemplate.execute {
            var customPlaylistEntity = customPlaylistRepository.findByUserIdAndId(userId, customPlaylistId)
            if(customPlaylistEntity==null){
                throw CustomPlaylistNotFoundException()
            }
            customPlaylistRepository.incrementSongCnt(customPlaylistId)
            customPlaylistSongRepository.save(CustomPlaylistSongEntity(customPlaylist = customPlaylistEntity, song = song))
            CustomPlaylistBrief(customPlaylistEntity.id, customPlaylistEntity.title, customPlaylistEntity.songCnt)
        }
        return retVal!!
    }
}
