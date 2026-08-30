package com.app.flashlearn.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry

object DatabaseTestUtil {
    fun createInMemoryDb(): FlashLearnDatabase {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return Room.inMemoryDatabaseBuilder(context, FlashLearnDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}
