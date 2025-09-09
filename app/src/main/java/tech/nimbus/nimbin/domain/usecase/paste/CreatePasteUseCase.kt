package tech.nimbus.nimbin.domain.usecase.paste

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import tech.nimbus.shared.dto.PasteDto
import tech.nimbus.shared.dto.PasteVisibility
import javax.inject.Inject

/**
 * Use case для создания новой текстовой заметки в системе NimBin.
 *
 * Обрабатывает создание заметок как для авторизованных, так и для анонимных пользователей.
 * Выполняет валидацию входных данных и применяет бизнес-правила создания заметок.
 *
 * ## Бизнес-логика:
 * - Валидация заголовка (1-255 символов) и содержимого (1-1,000,000 символов)
 * - Применение настроек видимости (PUBLIC/PRIVATE)
 * - Установка языка подсветки синтаксиса
 * - Опциональная установка времени автоудаления
 * - Привязка к пользователю (если авторизован) или создание анонимной заметки
 *
 * ## Типы заметок:
 * - **Авторизованные**: привязаны к пользователю, поддерживают приватность
 * - **Анонимные**: всегда публичные, нельзя редактировать или удалять
 *
 * ## Поддерживаемые языки подсветки:
 * plaintext, javascript, typescript, kotlin, java, python, cpp, c, html, css, sql, json, xml, markdown
 *
 * @see PasteRepository
 * @see PasteResult
 * @see PasteDto
 * @see PasteVisibility
 *
 * @author NimBin Team
 * @since 1.0
 */
class CreatePasteUseCase @Inject constructor(
    private val repository: PasteRepository
) {
    /**
     * Создает новую текстовую заметку.
     *
     * @param title Заголовок заметки. Обязательный параметр, 1-255 символов.
     *              Используется для поиска и отображения в списках.
     *
     * @param content Текстовое содержимое заметки. Обязательный параметр, 1-1,000,000 символов.
     *                Основной контент заметки, поддерживает любой текстовый формат.
     *
     * @param visibility Уровень видимости заметки:
     *                   - `PasteVisibility.PUBLIC` - видна всем пользователям
     *                   - `PasteVisibility.PRIVATE` - видна только автору (требует авторизации)
     *
     * @param syntaxLanguage Язык программирования для подсветки синтаксиса.
     *                 По умолчанию "plaintext" (без подсветки).
     *                 Поддерживаемые значения: javascript, kotlin, java, python, cpp и др.
     *
     * @param expiresAt Время автоудаления заметки в формате ISO 8601 (UTC).
     *                  Если null - заметка хранится бессрочно.
     *                  Пример: "2024-12-31T23:59:59Z"
     *
     * @return Flow<PasteResult<PasteDto>> Поток состояний создания заметки:
     *         - Loading: процесс отправки на сервер
     *         - Success: содержит созданную заметку с сгенерированным ID
     *         - Error: описание ошибки (валидация, сеть, сервер)
     *
     * @throws IllegalArgumentException если title или content пустые/превышают лимиты
     *
     * ## Примеры использования:
     * ```kotlin
     * // Создание публичной заметки с подсветкой Kotlin
     * createPasteUseCase(
     *     title = "Пример Kotlin к��да",
     *     content = "fun main() { println(\"Hello World!\") }",
     *     visibility = PasteVisibility.PUBLIC,
     *     syntaxLanguage = "kotlin"
     * ).collect { result ->
     *     when (result) {
     *         is PasteResult.Success -> navigateToPaste(result.data.id)
     *         is PasteResult.Error -> showError(result.message)
     *     }
     * }
     *
     * // Приватная заметка с автоудалением через неделю
     * createPasteUseCase(
     *     title = "Временные заметки",
     *     content = "Важная информация",
     *     visibility = PasteVisibility.PRIVATE,
     *     expiresAt = "2024-01-27T12:00:00Z"
     * )
     * ```
     */
    operator fun invoke(
        title: String,
        content: String,
        visibility: PasteVisibility,
        syntaxLanguage: String = "plaintext",
        expiresAt: String? = null
    ): Flow<PasteResult<PasteDto>> {
        require(title.isNotBlank() && title.length <= 255) {
            "Заголовок должен содержать 1-255 символов"
        }
        require(content.isNotBlank() && content.length <= 1_000_000) {
            "Содержимое должно содержать 1-1,000,000 символов"
        }

        return repository.createPaste(title, content, visibility, syntaxLanguage, expiresAt)
    }
}
