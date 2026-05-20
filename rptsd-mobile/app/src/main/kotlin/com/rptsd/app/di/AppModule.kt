package com.rptsd.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// TokenManager uses @Inject constructor + @Singleton so Hilt injects it directly.
// NetworkModule provides AuthApi, SubscriptionApi.
// DatabaseModule provides AppDatabase.
@Module
@InstallIn(SingletonComponent::class)
object AppModule
