package com.xyzbroker.cashoutapp.di

import com.xyzbroker.cashoutapp.data.repository.CashOutRepositoryImpl
import com.xyzbroker.cashoutapp.domain.repository.CashOutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCashOutRepository(impl: CashOutRepositoryImpl): CashOutRepository
}
