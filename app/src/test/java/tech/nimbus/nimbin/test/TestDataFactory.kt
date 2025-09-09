package tech.nimbus.nimbin.test

import tech.nimbus.shared.dto.AuthResponseDto
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import tech.nimbus.shared.dto.UserDto
import tech.nimbus.shared.dto.UserProfileDto
import tech.nimbus.shared.dto.request.CreatePasteRequestDto
import tech.nimbus.shared.dto.request.LoginRequestDto
import tech.nimbus.shared.dto.request.RegisterRequestDto

/**
 * Factory для создания тестовых данных.
 *
 * Содержит предустановленные объекты для тестов с возможностью кастомизации.
 * Упрощает создание консистентных тестовых данных во всех тестах.
 */
object TestDataFactory {

    // === User Data ===

    fun createTestUser(
        id: String = "user_test_123",
        username: String = "testuser",
        email: String = "test@example.com",
        displayName: String? = "Test User",
        createdAt: String = "2025-01-20T12:00:00Z"
    ) = UserDto(
        id = id,
        username = username,
        email = email,
        displayName = displayName,
        createdAt = createdAt
    )

    fun createTestUserProfile(
        user: UserDto = createTestUser(),
        publicPastesCount: Int = 5,
        totalPastesCount: Int? = 10
    ) = UserProfileDto(
        user = user,
        publicPastesCount = publicPastesCount,
        totalPastesCount = totalPastesCount
    )

    // === Auth Data ===

    fun createTestAuthResponse(
        token: String = "jwt_test_token_123",
        user: UserDto = createTestUser()
    ) = AuthResponseDto(
        token = token,
        user = user
    )

    fun createTestLoginRequest(
        email: String = "test@example.com",
        password: String = "password123"
    ) = LoginRequestDto(
        email = email,
        password = password
    )

    fun createTestRegisterRequest(
        username: String = "testuser",
        email: String = "test@example.com",
        password: String = "password123"
    ) = RegisterRequestDto(
        username = username,
        email = email,
        password = password
    )

    // === Paste Data ===

    fun createTestPaste(
        id: String = "paste_test_123",
        title: String = "Test Paste",
        content: String = "This is test content",
        visibility: PasteVisibility = PasteVisibility.PUBLIC,
        syntaxLanguage: String = "plaintext",
        createdAt: String = "2025-01-20T12:00:00Z",
        updatedAt: String = createdAt,
        userId: String? = null,
        authorUsername: String? = null,
        authorDisplayName: String? = null,
        expiresAt: String? = null,
        viewCount: Int = 0
    ) = PasteDto(
        id = id,
        title = title,
        content = content,
        visibility = visibility,
        syntaxLanguage = syntaxLanguage,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userId = userId,
        authorUsername = authorUsername,
        authorDisplayName = authorDisplayName,
        expiresAt = expiresAt,
        viewCount = viewCount
    )

    fun createTestCreatePasteRequest(
        title: String = "Test Paste",
        content: String = "This is test content",
        visibility: PasteVisibility = PasteVisibility.PUBLIC,
        syntaxLanguage: String = "plaintext"
    ) = CreatePasteRequestDto(
        title = title,
        content = content,
        visibility = visibility,
        syntaxLanguage = syntaxLanguage
    )

    // === Lists ===

    fun createTestPasteList(count: Int = 3): List<PasteDto> {
        return (1..count).map { i ->
            createTestPaste(
                id = "paste_$i",
                title = "Test Paste $i",
                content = "Content for paste $i",
                viewCount = i * 10
            )
        }
    }

    fun createTestUserList(count: Int = 3): List<UserDto> {
        return (1..count).map { i ->
            createTestUser(
                id = "user_$i",
                username = "user$i",
                email = "user$i@example.com",
                displayName = "User $i"
            )
        }
    }
}
