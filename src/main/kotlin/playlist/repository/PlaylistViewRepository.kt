package com.wafflestudio.seminar.spring2023.playlist.repository

import org.springframework.data.jpa.repository.JpaRepository

interface PlaylistViewRepository : JpaRepository<PlaylistViewEntity, Long>{
    fun findByPlaylistIdAndUserId(playlistId: Long, userId: Long): List<PlaylistViewEntity>

    fun findByPlaylistId(playlistId: Long): List<PlaylistViewEntity>
}
