package com.example.imageDownloader

import androidx.test.core.app.ActivityScenario
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Test
    fun testActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity: MainActivity ->
                Assert.assertTrue(activity.supportFragmentManager.fragments.size == 1)
            }
        }
    }

}