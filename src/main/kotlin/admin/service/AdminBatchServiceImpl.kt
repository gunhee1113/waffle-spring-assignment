package com.wafflestudio.seminar.spring2023.admin.service

import com.wafflestudio.seminar.spring2023.song.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class AdminBatchServiceImpl(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val songRepository: SongRepository,
    private val songArtistRepository: SongArtistRepository,
    private val transactionTemplate: TransactionTemplate,
) : AdminBatchService {
    private val threads = Executors.newFixedThreadPool(5)
    override fun insertAlbums(albumInfos: List<BatchAlbumInfo>) {
        albumInfos.forEach {
            batchAlbumInfo ->
            threads.submit {
                transactionTemplate.execute {
                    var artistEntity = artistRepository.findByName(batchAlbumInfo.artist)
                    if(artistEntity == null){
                        artistEntity = ArtistEntity(
                            name = batchAlbumInfo.artist,
                        )
                    }
                    artistRepository.save(artistEntity)
                    var albumEntity = AlbumEntity(
                        title = batchAlbumInfo.title,
                        image = batchAlbumInfo.image,
                        artist = artistEntity,
                    )
                    albumRepository.save(albumEntity)
                    batchAlbumInfo.songs.forEach { batchSongInfo ->
                        var songEntity = SongEntity(
                            title = batchSongInfo.title,
                            duration = batchSongInfo.duration,
                            album = albumEntity,
                        )
                        songRepository.save(songEntity)
                        batchSongInfo.artists.forEach{artistName ->
                            var artistOfSong = artistRepository.findByName(artistName)
                            if(artistOfSong == null){
                                artistOfSong = ArtistEntity(
                                    name = artistName,
                                )
                                artistRepository.save(artistOfSong)
                            }
                            songArtistRepository.save(SongArtistEntity(song = songEntity, artist = artistOfSong))
                        }
                    }
                }
            }
        }
        threads.shutdown()
        try {
            threads.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        } catch (e: Exception){
            Thread.currentThread().interrupt()
        }
    }
}
