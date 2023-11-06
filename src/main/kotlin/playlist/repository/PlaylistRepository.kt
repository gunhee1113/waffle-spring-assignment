package com.wafflestudio.seminar.spring2023.playlist.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface PlaylistRepository : JpaRepository<PlaylistEntity, Long> {
    @Query("""
        SELECT p FROM playlists p 
        JOIN FETCH p.songs ps
        WHERE p.id = :id
    """)
    fun findByIdWithSongs(id: Long): PlaylistEntity?
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from playlists p where p.id = :id")
    fun findByIdForUpdate(id: Long): PlaylistEntity
}
