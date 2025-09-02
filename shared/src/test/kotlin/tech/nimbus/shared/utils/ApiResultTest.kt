package tech.nimbus.shared.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit тесты для ApiResult.
 *
 * Проверяют:
 * - Корректное создание состояний Success/Error/Loading
 * - Методы проверки состояний (isSuccess, isError)
 * - Утилитарные методы (getOrNull, getOrDefault)
 * - Функциональные методы (onSuccess, onError, map)
 */
class ApiResultTest {

    @Test
    fun `Success state should be created correctly`() {
        // Given
        val data = "test_data"

        // When
        val result = ApiResult.Success(data)

        // Then
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals(data, result.data)
    }

    @Test
    fun `Error state should be created correctly`() {
        // Given
        val message = "Test error"
        val code = 404

        // When
        val result = ApiResult.Error(message, code)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertEquals(message, result.message)
        assertEquals(code, result.code)
    }

    @Test
    fun `Error without code should work correctly`() {
        // Given
        val message = "Test error without code"

        // When
        val result = ApiResult.Error(message)

        // Then
        assertTrue(result.isError)
        assertEquals(message, result.message)
        assertNull(result.code)
    }

    @Test
    fun `getOrNull should return data for Success`() {
        // Given
        val data = "success_data"
        val result = ApiResult.Success(data)

        // When
        val retrieved = result.getOrNull()

        // Then
        assertEquals(data, retrieved)
    }

    @Test
    fun `getOrNull should return null for Error`() {
        // Given
        val result = ApiResult.Error("error message")

        // When
        val retrieved: String? = result.getOrNull()

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `getOrDefault should return data for Success`() {
        // Given
        val data = "success_data"
        val defaultValue = "default_data"
        val result = ApiResult.Success(data)

        // When
        val retrieved = result.getOrDefault(defaultValue)

        // Then
        assertEquals(data, retrieved)
    }

    @Test
    fun `getOrDefault should return default for Error`() {
        // Given
        val defaultValue = "default_data"
        val result: ApiResult<String> = ApiResult.Error("error message")

        // When
        val retrieved: String = result.getOrDefault(defaultValue)

        // Then
        assertEquals(defaultValue, retrieved)
    }

    @Test
    fun `onSuccess should execute action for Success`() {
        // Given
        val data = "test_data"
        val result = ApiResult.Success(data)
        var actionExecuted = false
        var receivedData: String? = null

        // When
        val returnedResult = result.onSuccess {
            actionExecuted = true
            receivedData = it
        }

        // Then
        assertTrue(actionExecuted)
        assertEquals(data, receivedData)
        assertEquals(result, returnedResult) // Should return same instance for chaining
    }

    @Test
    fun `onSuccess should not execute action for Error`() {
        // Given
        val result = ApiResult.Error("error")
        var actionExecuted = false

        // When
        val returnedResult: ApiResult<String> = result.onSuccess {
            actionExecuted = true
        }

        // Then
        assertFalse(actionExecuted)
        assertEquals(result, returnedResult)
    }

    @Test
    fun `onError should execute action for Error`() {
        // Given
        val message = "test error"
        val code = 500
        val result = ApiResult.Error(message, code)
        var actionExecuted = false
        var receivedMessage: String? = null
        var receivedCode: Int? = null

        // When
        val returnedResult = result.onError { msg, errorCode ->
            actionExecuted = true
            receivedMessage = msg
            receivedCode = errorCode
        }

        // Then
        assertTrue(actionExecuted)
        assertEquals(message, receivedMessage)
        assertEquals(code, receivedCode)
        assertEquals(result, returnedResult)
    }

    @Test
    fun `onError should not execute action for Success`() {
        // Given
        val result = ApiResult.Success("data")
        var actionExecuted = false

        // When
        val returnedResult = result.onError { _, _ ->
            actionExecuted = true
        }

        // Then
        assertFalse(actionExecuted)
        assertEquals(result, returnedResult)
    }

    @Test
    fun `map should transform Success data`() {
        // Given
        val originalData = "123"
        val result = ApiResult.Success(originalData)

        // When
        val mappedResult = result.map { it.toInt() }

        // Then
        assertTrue(mappedResult is ApiResult.Success)
        assertEquals(123, mappedResult.data)
    }

    @Test
    fun `map should preserve Error`() {
        // Given
        val message = "original error"
        val code = 404
        val result = ApiResult.Error(message, code)

        // When
        val mappedResult: ApiResult<String> = result.map { it.toString() }

        // Then
        assertTrue(mappedResult is ApiResult.Error)
        assertEquals(message, mappedResult.message)
        assertEquals(code, mappedResult.code)
    }

    @Test
    fun `chaining operations should work correctly`() {
        // Given
        val result = ApiResult.Success("test_data")
        var successCalled = false
        var errorCalled = false

        // When
        val finalResult = result
            .onSuccess { successCalled = true }
            .onError { _, _ -> errorCalled = true }
            .map { it.uppercase() }

        // Then
        assertTrue(successCalled)
        assertFalse(errorCalled)
        assertTrue(finalResult is ApiResult.Success)
        assertEquals("TEST_DATA", finalResult.data)
    }

    @Test
    fun `chaining with Error should skip success operations`() {
        // Given
        val result = ApiResult.Error("test error", 400)
        var successCalled = false
        var errorCalled = false

        // When
        val finalResult: ApiResult<String> = result
            .onSuccess { successCalled = true }
            .onError { _, _ -> errorCalled = true }
            .map { it.toString() }

        // Then
        assertFalse(successCalled)
        assertTrue(errorCalled)
        assertTrue(finalResult is ApiResult.Error)
        assertEquals("test error", finalResult.message)
    }

    @Test
    fun `complex transformation chain should work`() {
        // Given
        val result = ApiResult.Success(42)

        // When
        val finalResult = result
            .map { it * 2 }           // 42 -> 84
            .map { it.toString() }    // 84 -> "84"
            .map { "Number: $it" }    // "84" -> "Number: 84"

        // Then
        assertTrue(finalResult is ApiResult.Success)
        assertEquals("Number: 84", finalResult.data)
    }
}
