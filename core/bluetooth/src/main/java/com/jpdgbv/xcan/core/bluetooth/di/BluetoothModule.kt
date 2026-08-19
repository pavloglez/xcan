package com.jpdgbv.xcan.core.bluetooth.di

import com.jpdgbv.xcan.core.bluetooth.BleDataSource
import com.jpdgbv.xcan.core.bluetooth.internal.BleDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothModule {

    @Binds
    abstract fun bindBleDataSource(
        bleDataSourceImpl: BleDataSourceImpl
    ): BleDataSource
}
