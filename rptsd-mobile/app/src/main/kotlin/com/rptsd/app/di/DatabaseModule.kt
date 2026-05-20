package com.rptsd.app.di

import android.content.Context
import androidx.room.Room
import com.rptsd.app.data.local.database.AppDatabase
import com.rptsd.app.data.local.database.dao.RideHistoryDao
import com.rptsd.app.data.local.database.dao.UserRulesDao
import com.rptsd.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME,
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserRulesDao(db: AppDatabase): UserRulesDao = db.userRulesDao()

    @Provides
    @Singleton
    fun provideRideHistoryDao(db: AppDatabase): RideHistoryDao = db.rideHistoryDao()
}
