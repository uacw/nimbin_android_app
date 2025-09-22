package tech.nimbus.nimbin.ui.components

import com.wakaztahir.codeeditor.highlight.model.CodeLang

object CodeLangMapper {
    fun fromString(name: String?): CodeLang? {
        val n = name?.trim()?.lowercase() ?: return null
        return when (n) {
            // No highlighting
            "plaintext", "text", "txt" -> null

            // Exact support from library
            "c" -> CodeLang.C
            "cpp" -> CodeLang.CPP
            "csharp", "cs", "c#" -> CodeLang.CSharp
            "css" -> CodeLang.CSS
            "go", "golang" -> CodeLang.Go
            "java" -> CodeLang.Java
            "javascript", "js" -> CodeLang.JavaScript
            "json" -> CodeLang.JSON
            "lua" -> CodeLang.Lua
            "markdown", "md" -> CodeLang.Markdown
            "python", "py" -> CodeLang.Python
            "ruby", "rb" -> CodeLang.Ruby
            "rust", "rs" -> CodeLang.Rust
            "sql" -> CodeLang.SQL
            "xml" -> CodeLang.XML
            "yaml", "yml" -> CodeLang.YAML

            // Reasonable fallbacks
            "html" -> CodeLang.XML
            "typescript", "ts" -> CodeLang.JavaScript
            "php" -> CodeLang.JavaScript
            "swift" -> CodeLang.Java
            "kotlin", "kt" -> CodeLang.Java

            else -> null
        }
    }
}
