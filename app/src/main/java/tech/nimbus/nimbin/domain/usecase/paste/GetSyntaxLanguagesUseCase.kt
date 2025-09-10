package tech.nimbus.nimbin.domain.usecase.paste

import kotlinx.coroutines.flow.Flow
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.PasteResult
import javax.inject.Inject

class GetSyntaxLanguagesUseCase @Inject constructor(
    private val repository: PasteRepository
) {
    operator fun invoke(): Flow<PasteResult<List<String>>> = repository.getSyntaxLanguages()
}

