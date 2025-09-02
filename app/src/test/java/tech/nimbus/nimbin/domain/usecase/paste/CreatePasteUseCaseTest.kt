package tech.nimbus.nimbin.domain.usecase.paste

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Unit тесты для CreatePasteUseCase.
 */
class CreatePasteUseCaseTest {

    private lateinit var pasteRepository: PasteRepository
    private lateinit var createPasteUseCase: CreatePasteUseCase

    @Before
    fun setup() {
        pasteRepository = mock()
        createPasteUseCase = CreatePasteUseCase(pasteRepository)
    }

    @Test
    fun `valid paste data should return success`() = runTest {
        // Given
        val title = "Test Paste"
        val content = "Hello World"
        val visibility = PasteVisibility.PUBLIC
        val language = "text"

        val expectedPaste = PasteDto(
            id = "paste123",
            title = title,
            content = content,
            visibility = visibility,
            language = language,
            createdAt = "2025-01-20T12:00:00Z",
            userId = null,
            authorUsername = null,
            authorDisplayName = null,
            expiresAt = null,
            viewCount = 0
        )

        whenever(pasteRepository.createPaste(title, content, visibility, language))
            .thenReturn(flowOf(PasteResult.Success(expectedPaste)))

        // When & Then
        createPasteUseCase(title, content, visibility, language).test {
            val result = awaitItem()
            assertTrue(result is PasteResult.Success)
            assertEquals(expectedPaste, (result as PasteResult.Success<PasteDto>).data)
            awaitComplete()
        }
    }

    @Test
    fun `repository error should return error`() = runTest {
        // Given
        val title = "Test Paste"
        val content = "Hello World"
        val visibility = PasteVisibility.PUBLIC
        val language = "text"
        val errorMessage = "Server error"

        whenever(pasteRepository.createPaste(title, content, visibility, language))
            .thenReturn(flowOf(PasteResult.Error(errorMessage)))

        // When & Then
        createPasteUseCase(title, content, visibility, language).test {
            val result = awaitItem()
            assertTrue(result is PasteResult.Error)
            assertEquals(errorMessage, (result as PasteResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `valid paste with custom language should return success`() = runTest {
        // Given
        val title = "Kotlin Code"
        val content = "fun main() { println(\"Hello\") }"
        val visibility = PasteVisibility.PRIVATE
        val language = "kotlin"

        val expectedPaste = PasteDto(
            id = "paste456",
            title = title,
            content = content,
            visibility = visibility,
            language = language,
            createdAt = "2025-01-20T12:00:00Z",
            userId = "user123",
            authorUsername = "testuser",
            authorDisplayName = "Test User",
            expiresAt = null,
            viewCount = 0
        )

        whenever(pasteRepository.createPaste(title, content, visibility, language))
            .thenReturn(flowOf(PasteResult.Success(expectedPaste)))

        // When & Then
        createPasteUseCase(title, content, visibility, language).test {
            val result = awaitItem()
            assertTrue(result is PasteResult.Success)
            assertEquals(expectedPaste, (result as PasteResult.Success<PasteDto>).data)
            awaitComplete()
        }
    }

    @Test
    fun `empty title should throw exception`() = runTest {
        // Given
        val title = ""
        val content = "Content"
        val visibility = PasteVisibility.PUBLIC

        // When & Then
        try {
            createPasteUseCase(title, content, visibility).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `empty content should throw exception`() = runTest {
        // Given
        val title = "Title"
        val content = ""
        val visibility = PasteVisibility.PUBLIC

        // When & Then
        try {
            createPasteUseCase(title, content, visibility).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `blank title should throw exception`() = runTest {
        // Given
        val title = "   "
        val content = "Content"
        val visibility = PasteVisibility.PUBLIC

        // When & Then
        try {
            createPasteUseCase(title, content, visibility).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `blank content should throw exception`() = runTest {
        // Given
        val title = "Title"
        val content = "   "
        val visibility = PasteVisibility.PUBLIC

        // When & Then
        try {
            createPasteUseCase(title, content, visibility).test {
                // Should not reach here
            }
            assertTrue("Expected exception was not thrown", false)
        } catch (e: IllegalArgumentException) {
            // Expected
            assertTrue(true)
        }
    }

    @Test
    fun `network error should return error`() = runTest {
        // Given
        val title = "Test Paste"
        val content = "Hello World"
        val visibility = PasteVisibility.PUBLIC
        val networkError = "Network error"

        whenever(pasteRepository.createPaste(title, content, visibility))
            .thenReturn(flowOf(PasteResult.Error(networkError)))

        // When & Then
        createPasteUseCase(title, content, visibility).test {
            val result = awaitItem()
            assertTrue(result is PasteResult.Error)
            assertEquals(networkError, (result as PasteResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `validation error should return error`() = runTest {
        // Given
        val title = "Test Paste"
        val content = "Hello World"
        val visibility = PasteVisibility.PUBLIC
        val validationError = "Content too long"

        whenever(pasteRepository.createPaste(title, content, visibility))
            .thenReturn(flowOf(PasteResult.Error(validationError)))

        // When & Then
        createPasteUseCase(title, content, visibility).test {
            val result = awaitItem()
            assertTrue(result is PasteResult.Error)
            assertEquals(validationError, (result as PasteResult.Error).message)
            awaitComplete()
        }
    }

    @Test
    fun `multiple paste creation should work independently`() = runTest {
        // Given
        val title1 = "First Paste"
        val content1 = "First Content"
        val title2 = "Second Paste"
        val content2 = "Second Content"
        val visibility = PasteVisibility.PUBLIC

        val paste1 = PasteDto(
            id = "paste1",
            title = title1,
            content = content1,
            visibility = visibility,
            language = "text",
            createdAt = "2025-01-20T12:00:00Z",
            userId = null,
            authorUsername = null,
            authorDisplayName = null,
            expiresAt = null,
            viewCount = 0
        )

        val paste2 = PasteDto(
            id = "paste2",
            title = title2,
            content = content2,
            visibility = visibility,
            language = "text",
            createdAt = "2025-01-20T12:01:00Z",
            userId = null,
            authorUsername = null,
            authorDisplayName = null,
            expiresAt = null,
            viewCount = 0
        )

        whenever(pasteRepository.createPaste(title1, content1, visibility))
            .thenReturn(flowOf(PasteResult.Success(paste1)))
        whenever(pasteRepository.createPaste(title2, content2, visibility))
            .thenReturn(flowOf(PasteResult.Success(paste2)))

        // When & Then
        createPasteUseCase(title1, content1, visibility).test {
            val result = awaitItem()
            assertTrue(result is PasteResult.Success)
            assertEquals(paste1, (result as PasteResult.Success<PasteDto>).data)
            awaitComplete()
        }

        createPasteUseCase(title2, content2, visibility).test {
            val result = awaitItem()
            assertTrue(result is PasteResult.Success)
            assertEquals(paste2, (result as PasteResult.Success<PasteDto>).data)
            awaitComplete()
        }
    }
}
