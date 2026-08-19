package com.jpdgbv.xcan.feature.diagnostics;

import com.jpdgbv.xcan.core.bluetooth.BleDataSource;
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
public final class DiagnosticsViewModel_Factory implements Factory<DiagnosticsViewModel> {
  private final Provider<BleDataSource> bleDataSourceProvider;

  private DiagnosticsViewModel_Factory(Provider<BleDataSource> bleDataSourceProvider) {
    this.bleDataSourceProvider = bleDataSourceProvider;
  }

  @Override
  public DiagnosticsViewModel get() {
    return newInstance(bleDataSourceProvider.get());
  }

  public static DiagnosticsViewModel_Factory create(Provider<BleDataSource> bleDataSourceProvider) {
    return new DiagnosticsViewModel_Factory(bleDataSourceProvider);
  }

  public static DiagnosticsViewModel newInstance(BleDataSource bleDataSource) {
    return new DiagnosticsViewModel(bleDataSource);
  }
}
