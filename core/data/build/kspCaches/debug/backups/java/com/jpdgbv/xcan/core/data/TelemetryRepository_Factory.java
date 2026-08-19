package com.jpdgbv.xcan.core.data;

import com.jpdgbv.xcan.core.database.dao.TelemetryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class TelemetryRepository_Factory implements Factory<TelemetryRepository> {
  private final Provider<TelemetryDao> telemetryDaoProvider;

  private TelemetryRepository_Factory(Provider<TelemetryDao> telemetryDaoProvider) {
    this.telemetryDaoProvider = telemetryDaoProvider;
  }

  @Override
  public TelemetryRepository get() {
    return newInstance(telemetryDaoProvider.get());
  }

  public static TelemetryRepository_Factory create(Provider<TelemetryDao> telemetryDaoProvider) {
    return new TelemetryRepository_Factory(telemetryDaoProvider);
  }

  public static TelemetryRepository newInstance(TelemetryDao telemetryDao) {
    return new TelemetryRepository(telemetryDao);
  }
}
