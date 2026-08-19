package com.jpdgbv.xcan.core.bluetooth.internal;

import com.jpdgbv.xcan.core.model.SensorRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class BleDataSourceImpl_Factory implements Factory<BleDataSourceImpl> {
  private final Provider<SensorRepository> sensorRepoProvider;

  private BleDataSourceImpl_Factory(Provider<SensorRepository> sensorRepoProvider) {
    this.sensorRepoProvider = sensorRepoProvider;
  }

  @Override
  public BleDataSourceImpl get() {
    return newInstance(sensorRepoProvider.get());
  }

  public static BleDataSourceImpl_Factory create(Provider<SensorRepository> sensorRepoProvider) {
    return new BleDataSourceImpl_Factory(sensorRepoProvider);
  }

  public static BleDataSourceImpl newInstance(SensorRepository sensorRepo) {
    return new BleDataSourceImpl(sensorRepo);
  }
}
