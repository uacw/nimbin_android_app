package tech.nimbus.nimbin.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.nimbus.nimbin.data.repository.AuthRepositoryImpl
import tech.nimbus.nimbin.data.repository.PasteRepositoryImpl
import tech.nimbus.nimbin.data.repository.ProfileRepositoryImpl
import tech.nimbus.nimbin.domain.repository.AuthRepository
import tech.nimbus.nimbin.domain.repository.PasteRepository
import tech.nimbus.nimbin.domain.repository.ProfileRepository
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds [AuthRepositoryImpl] as the implementation for [AuthRepository].
     * @param authRepositoryImpl The concrete implementation of [AuthRepository].
     * @return An instance of [AuthRepository].
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Binds [PasteRepositoryImpl] as the implementation for [PasteRepository].
     * @param impl The concrete implementation of [PasteRepository].
     * @return An instance of [PasteRepository].
     */
    @Binds
    @Singleton
    abstract fun bindPasteRepository(
        impl: PasteRepositoryImpl
    ): PasteRepository

    /**
     * Binds [ProfileRepositoryImpl] as the implementation for [ProfileRepository].
     * @param impl The concrete implementation of [ProfileRepository].
     * @return An instance of [ProfileRepository].
     */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
}