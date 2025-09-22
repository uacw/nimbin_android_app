package tech.nimbus.nimbin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.wakaztahir.codeeditor.highlight.model.CodeLang
import com.wakaztahir.codeeditor.highlight.prettify.PrettifyParser
import com.wakaztahir.codeeditor.highlight.theme.CodeThemeType
import com.wakaztahir.codeeditor.highlight.utils.parseCodeAsAnnotatedString
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Полноценный UI-компонент редактора кода с подсветкой
@Composable
fun CodeEditor(
    value: String,
    onValueChange: (String) -> Unit,
    language: CodeLang?,
    modifier: Modifier = Modifier,
    theme: CodeThemeType = CodeThemeType.Monokai,
    readOnly: Boolean = false,
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily = FontFamily.Monospace,
    containerColor: Color = Color(0xFF1E1F22),
    textColor: Color = Color(0xFFE6E6E6),
    cursorColor: Color = Color(0xFF99D1FF),
    minLines: Int = 16,
    maxLines: Int = Int.MAX_VALUE,
) {
    val parser = remember { PrettifyParser() }
    val themeObj = remember(theme) { theme.theme() }

    val annotated = remember(value, language, theme) {
        if (language == null) null else parseCodeAsAnnotatedString(
            parser = parser,
            theme = themeObj,
            lang = language,
            code = value
        )
    }
    val highlightTransformation = remember(annotated) {
        if (annotated == null) VisualTransformation.None
        else VisualTransformation { _ -> TransformedText(annotated, OffsetMapping.Identity) }
    }

    val effectiveTextColor = if (language == null) Color.White else textColor

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TextField(
            modifier = Modifier.fillMaxSize(),
            value = value,
            onValueChange = { if (!readOnly) onValueChange(it) },
            textStyle = TextStyle(
                color = effectiveTextColor,
                fontFamily = fontFamily,
                fontSize = fontSize
            ),
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                errorContainerColor = containerColor,
                disabledContainerColor = containerColor,
                focusedTextColor = effectiveTextColor,
                unfocusedTextColor = effectiveTextColor,
                cursorColor = cursorColor
            ),
            readOnly = readOnly,
            minLines = minLines,
            maxLines = maxLines,
            visualTransformation = highlightTransformation,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

// Демо-обёртка (для превью/примеров). Можно удалить, когда экраны подключат основной компонент
@Composable
fun CodeEditorSample(modifier: Modifier = Modifier) {
    var language by remember { mutableStateOf(CodeLang.Java) }
    var themeState by remember { mutableStateOf(CodeThemeType.Monokai) }

    val initialCode = """
        package com.example
        
        public class Main {
            public static void main(String[] args) {
                System.out.println("Hello, world!");
            }
        }
    """.trimIndent()

    var code by remember { mutableStateOf(initialCode) }

    CodeEditor(
        value = code,
        onValueChange = { code = it },
        language = language,
        theme = themeState,
        modifier = modifier
    )
}
