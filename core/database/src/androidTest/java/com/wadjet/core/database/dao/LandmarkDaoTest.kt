package com.wadjet.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wadjet.core.database.WadjetDatabase
import com.wadjet.core.database.entity.LandmarkEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandmarkDaoTest {

    private lateinit var database: WadjetDatabase
    private lateinit var landmarkDao: LandmarkDao

    private val testLandmark = LandmarkEntity(
        slug = "pyramids-of-giza",
        name = "Pyramids of Giza",
        nameAr = "أهرامات الجيزة",
        city = "Giza",
        type = "Pharaonic",
        era = "Old Kingdom",
        thumbnail = "https://example.com/pyramids.jpg",
        featured = true,
        popularity = 100,
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WadjetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        landmarkDao = database.landmarkDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveBySlug() = runTest {
        landmarkDao.insert(testLandmark)

        val result = landmarkDao.getBySlug("pyramids-of-giza")
        assertNotNull(result)
        assertEquals("Pyramids of Giza", result!!.name)
        assertEquals("Giza", result.city)
    }

    @Test
    fun getBySlug_notFound() = runTest {
        val result = landmarkDao.getBySlug("nonexistent")
        assertNull(result)
    }

    @Test
    fun getAll_orderedByPopularity() = runTest {
        landmarkDao.insertAll(
            listOf(
                testLandmark.copy(slug = "a", popularity = 50),
                testLandmark.copy(slug = "b", popularity = 100),
                testLandmark.copy(slug = "c", popularity = 75),
            ),
        )

        val results = landmarkDao.getAll().first()
        assertEquals(3, results.size)
        assertEquals("b", results[0].slug) // highest popularity first
        assertEquals("c", results[1].slug)
        assertEquals("a", results[2].slug)
    }

    @Test
    fun getFiltered_byCategory() = runTest {
        landmarkDao.insertAll(
            listOf(
                testLandmark.copy(slug = "a", type = "Pharaonic"),
                testLandmark.copy(slug = "b", type = "Islamic"),
                testLandmark.copy(slug = "c", type = "Pharaonic"),
            ),
        )

        val pharaonic = landmarkDao.getFiltered(category = "Pharaonic", city = null, limit = 10, offset = 0)
        assertEquals(2, pharaonic.size)

        val islamic = landmarkDao.getFiltered(category = "Islamic", city = null, limit = 10, offset = 0)
        assertEquals(1, islamic.size)
    }

    @Test
    fun getFiltered_byCity() = runTest {
        landmarkDao.insertAll(
            listOf(
                testLandmark.copy(slug = "a", city = "Cairo"),
                testLandmark.copy(slug = "b", city = "Luxor"),
                testLandmark.copy(slug = "c", city = "Cairo"),
            ),
        )

        val cairo = landmarkDao.getFiltered(category = null, city = "Cairo", limit = 10, offset = 0)
        assertEquals(2, cairo.size)
    }

    @Test
    fun getFiltered_nullParams_returnsAll() = runTest {
        landmarkDao.insertAll(
            listOf(
                testLandmark.copy(slug = "a"),
                testLandmark.copy(slug = "b"),
            ),
        )

        val all = landmarkDao.getFiltered(category = null, city = null, limit = 10, offset = 0)
        assertEquals(2, all.size)
    }

    @Test
    fun search() = runTest {
        landmarkDao.insertAll(
            listOf(
                testLandmark.copy(slug = "pyramids", name = "Pyramids of Giza"),
                testLandmark.copy(slug = "sphinx", name = "Great Sphinx"),
                testLandmark.copy(slug = "karnak", name = "Karnak Temple"),
            ),
        )

        val results = landmarkDao.search("pyramid")
        assertEquals(1, results.size)
        assertEquals("pyramids", results.first().slug)
    }

    @Test
    fun getCities() = runTest {
        landmarkDao.insertAll(
            listOf(
                testLandmark.copy(slug = "a", city = "Cairo"),
                testLandmark.copy(slug = "b", city = "Luxor"),
                testLandmark.copy(slug = "c", city = "Cairo"),
                testLandmark.copy(slug = "d", city = "Aswan"),
            ),
        )

        val cities = landmarkDao.getCities()
        assertEquals(3, cities.size) // distinct
        assertEquals(listOf("Aswan", "Cairo", "Luxor"), cities) // ordered ASC
    }

    @Test
    fun deleteAll() = runTest {
        landmarkDao.insertAll(listOf(testLandmark))
        assertEquals(1, landmarkDao.count())

        landmarkDao.deleteAll()
        assertEquals(0, landmarkDao.count())
    }
}
