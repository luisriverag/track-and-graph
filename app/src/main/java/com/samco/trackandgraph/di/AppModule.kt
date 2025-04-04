/*
 *  This file is part of Track & Graph
 *
 *  Track & Graph is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Track & Graph is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Track & Graph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.samco.trackandgraph.di

import android.content.ContentResolver
import android.content.Context
import com.samco.trackandgraph.adddatapoint.SuggestedValueHelper
import com.samco.trackandgraph.adddatapoint.SuggestedValueHelperImpl
import com.samco.trackandgraph.backupandrestore.BackupRestoreInteractor
import com.samco.trackandgraph.backupandrestore.BackupRestoreInteractorImpl
import com.samco.trackandgraph.base.helpers.PrefHelper
import com.samco.trackandgraph.base.helpers.PrefHelperImpl
import com.samco.trackandgraph.base.navigation.PendingIntentProvider
import com.samco.trackandgraph.lua.LuaEngine
import com.samco.trackandgraph.lua.LuaEngineImpl
import com.samco.trackandgraph.navigation.PendingIntentProviderImpl
import com.samco.trackandgraph.settings.TngSettings
import com.samco.trackandgraph.settings.TngSettingsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun getContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Provides
    fun getPendingIntentProvider(impl: PendingIntentProviderImpl): PendingIntentProvider = impl

    @Provides
    fun getSuggestedValueHelper(impl: SuggestedValueHelperImpl): SuggestedValueHelper = impl

    @Provides
    @Singleton
    //Must be singleton because it is a dependency of work manager worker
    fun getPrefHelper(impl: PrefHelperImpl): PrefHelper = impl

    @Provides
    @Singleton
    fun getTngSettings(impl: TngSettingsImpl): TngSettings = impl

    @Provides
    @Singleton
    //Must be singleton because it is a dependency of work manager worker
    fun getBackupRestoreInteractor(impl: BackupRestoreInteractorImpl): BackupRestoreInteractor = impl

    @Provides
    fun getLuaEngine(impl: LuaEngineImpl): LuaEngine = impl

}