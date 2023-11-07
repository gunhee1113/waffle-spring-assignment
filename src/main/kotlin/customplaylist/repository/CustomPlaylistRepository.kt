package com.wafflestudio.seminar.spring2023.customplaylist.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CustomPlaylistRepository : JpaRepository<CustomPlaylistEntity, Long>{

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update custom_playlists p set p.songCnt = p.songCnt + 1 where p.id = :id")
    fun incrementSongCnt(id: Long)

    fun findByUserIdAndId(userId: Long, id: Long) : CustomPlaylistEntity?

    fun findByUserId(userId: Long) : List<CustomPlaylistEntity>

}
