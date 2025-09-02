package tech.nimbus.nimbin.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Rule для настройки тестовых корутин.
 *
 * Автоматически устанавливает TestDispatcher для Main диспетчера
 * перед каждым тестом и восстанавливает исходное состояние после.
 *
 * Использование:
 * ```kotlin
 * class MyViewModelTest {
 *     @get:Rule
 *     val coroutineTestRule = CoroutineTestRule()
 *
 *     @Test
 *     fun myTest() = runTest {
 *         // Тест с корутинами
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
