package com.jpdgbv.xcan.core.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SensorRepositoryImpl_Factory implements Factory<SensorRepositoryImpl> {
  @Override
  public SensorRepositoryImpl get() {
    return newInstance();
  }

  public static SensorRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SensorRepositoryImpl newInstance() {
    return new SensorRepositoryImpl();
  }

  private static final class InstanceHolder {
    static final SensorRepositoryImpl_Factory INSTANCE = new SensorRepositoryImpl_Factory();
  }
}
