package com.oliveroneill.wilt.data.dao

import android.content.Context
import androidx.room.Room
import androidx.room.paging.LimitOffsetDataSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PlayHistoryDatabaseTest {
    private lateinit var dao: PlayHistoryDao
    private lateinit var db: PlayHistoryDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, PlayHistoryDatabase::class.java
        ).build()
        dao = db.historyDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        dao.deleteAll()
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun loadPlayHistory() {
        val items = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99, "x.com"),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12, "y.com"),
            ArtistRank("26-2019", LocalDate.parse("2019-06-25"), "Lomelda", 9, "s.com"),
            ArtistRank("25-2019", LocalDate.parse("2019-06-15"), "Hovvdy", 90, "z.com")
        )
        // An ordered version of the above list
        val expected = listOf(
            ArtistRank("26-2019", LocalDate.parse("2019-06-25"), "Lomelda", 9, "s.com"),
            ArtistRank("25-2019", LocalDate.parse("2019-06-15"), "Hovvdy", 90, "z.com"),
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99, "x.com"),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12, "y.com")
        )
        dao.insert(items)
        // Choose end date that includes everything
        val source = dao.loadPlayHistory(LocalDate.parse("2020-01-01")).create()
        // Apparently this is how we can convert the data source to a list.
        // Credit to https://stackoverflow.com/a/56787814
        val history = (source as LimitOffsetDataSource).loadRange(0, expected.count())
        assertEquals(expected, history)
    }

    @Test
    @Throws(Exception::class)
    fun loadPlayHistoryFromEndDate() {
        val items = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99, "x.com"),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12, "y.com"),
            ArtistRank("26-2019", LocalDate.parse("2019-06-25"), "Lomelda", 9, "z.com"),
            ArtistRank("25-2019", LocalDate.parse("2019-06-15"), "Hovvdy", 90, "s.com")
        )
        val expected = listOf(
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12, "y.com")
        )
        dao.insert(items)
        // Choose end date that only includes last element
        val source = dao.loadPlayHistory(LocalDate.parse("2019-01-01")).create()
        val history = (source as LimitOffsetDataSource).loadRange(0, expected.count())
        assertEquals(expected, history)
    }


    @Test
    @Throws(Exception::class)
    fun deleteAll() {
        val items = listOf(
            ArtistRank("09-2019", LocalDate.parse("2019-02-25"), "Pinegrove", 99, "x.com"),
            ArtistRank("52-2018", LocalDate.parse("2018-12-25"), "Bon Iver", 12, "y.com"),
            ArtistRank("26-2019", LocalDate.parse("2019-06-25"), "Lomelda", 9, "z.com"),
            ArtistRank("25-2019", LocalDate.parse("2019-06-15"), "Hovvdy", 90, "s.com")
        )
        dao.insert(items)
        // Delete all the elements
        dao.deleteAll()
        // Choose end date that only includes last element
        val source = dao.loadPlayHistory(LocalDate.parse("2019-01-01")).create()
        val history = (source as LimitOffsetDataSource).loadRange(0, 10)
        assertEquals(listOf<ArtistRank>(), history)
    }
}