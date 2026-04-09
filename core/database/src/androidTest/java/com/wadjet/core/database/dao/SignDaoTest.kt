package com.wadjet.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wadjet.core.database.WadjetDatabase
import com.wadjet.core.database.entity.SignEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignDaoTest {

    private lateinit var database: WadjetDatabase
    private lateinit var signDao: SignDao

    private val testSign = SignEntity(
        code = "A1",
        glyph = "\uD80C\uDC02",
        transliteration = "s",
        phoneticValue = "s",
        meaning = "seated man",
        type = "logogram",
        category = "A",
        categoryName = "Man and his activities",
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WadjetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        signDao = database.signDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieve() = runTest {
        signDao.insertAll(listOf(testSign))

        val result = signDao.getByCode("A1")
        assertNotNull(result)
        assertEquals("seated man", result!!.meaning)
        assertEquals("logogram", result.type)
    }

    @Test
    fun getByCode_notFound() = runTest {
        val result = signDao.getByCode("Z99")
        assertNull(result)
    }

    @Test
    fun getAll_withPagination() = runTest {
        val signs = (1..10).map { testSign.copy(code = "A$it") }
        signDao.insertAll(signs)

        val page1 = signDao.getAll(limit = 5, offset = 0)
        assertEquals(5, page1.size)

        val page2 = signDao.getAll(limit = 5, offset = 5)
        assertEquals(5, page2.size)

        val page3 = signDao.getAll(limit = 5, offset = 10)
        assertEquals(0, page3.size)
    }

    @Test
    fun getByCategory() = runTest {
        signDao.insertAll(
            listOf(
                testSign.copy(code = "A1", category = "A"),
                testSign.copy(code = "B1", category = "B"),
                testSign.copy(code = "A2", category = "A"),
            ),
        )

        val result = signDao.getByCategory("A", limit = 10, offset = 0)
        assertEquals(2, result.size)
        assertEquals(listOf("A1", "A2"), result.map { it.code })
    }

    @Test
    fun getByType() = runTest {
        signDao.insertAll(
            listOf(
                testSign.copy(code = "A1", type = "logogram"),
                testSign.copy(code = "A2", type = "uniliteral"),
                testSign.copy(code = "A3", type = "logogram"),
            ),
        )

        val result = signDao.getByType("logogram", limit = 10, offset = 0)
        assertEquals(2, result.size)
    }

    @Test
    fun getByFilter_categoryAndType() = runTest {
        signDao.insertAll(
            listOf(
                testSign.copy(code = "A1", category = "A", type = "logogram"),
                testSign.copy(code = "A2", category = "A", type = "uniliteral"),
                testSign.copy(code = "B1", category = "B", type = "logogram"),
            ),
        )

        val result = signDao.getByFilter("A", "logogram", limit = 10, offset = 0)
        assertEquals(1, result.size)
        assertEquals("A1", result.first().code)
    }

    @Test
    fun upsert_replaceOnConflict() = runTest {
        signDao.insertAll(listOf(testSign))
        signDao.insertAll(listOf(testSign.copy(meaning = "updated meaning")))

        val result = signDao.getByCode("A1")
        assertEquals("updated meaning", result!!.meaning)
    }

    @Test
    fun count_returnsCorrectNumber() = runTest {
        assertEquals(0, signDao.count())

        signDao.insertAll(listOf(testSign, testSign.copy(code = "A2")))
        assertEquals(2, signDao.count())
    }

    @Test
    fun deleteAll() = runTest {
        signDao.insertAll(listOf(testSign))
        assertEquals(1, signDao.count())

        signDao.deleteAll()
        assertEquals(0, signDao.count())
    }
}
