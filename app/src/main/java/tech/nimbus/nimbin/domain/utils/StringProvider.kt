package tech.nimbus.nimbin.domain.utils

import androidx.annotation.StringRes

/**
 * Интерфейс для получения строковых ресурсов в слое Domain/ViewModel
 * без прямой зависимости от Android Context
 */
interface StringProvider {
    /**
     * Возвращает строку по ID ресурса
     */
    fun getString(@StringRes stringResId: Int): String

    /**
     * Возвращает форматированную строку по ID ресурса с аргументами
     */
    fun getString(@StringRes stringResId: Int, vararg formatArgs: Any): String
}
