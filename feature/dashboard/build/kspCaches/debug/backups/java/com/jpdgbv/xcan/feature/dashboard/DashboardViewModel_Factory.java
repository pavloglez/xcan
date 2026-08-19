package com.jpdgbv.xcan.feature.dashboard;

import com.jpdgbv.xcan.core.bluetooth.BleDataSource;
import com.jpdgbv.xcan.core.data.CarRepository;
import com.jpdgbv.xcan.core.data.UserPreferencesRepository;
import com.jpdgbv.xcan.core.model.SensorRepository;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<BleDataSource> bleDataSourceProvider;

  private final Provider<CarRepository> carRepositoryProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private final Provider<SensorRepository> sensorRepositoryProvider;

  private DashboardViewModel_Factory(Provider<BleDataSource> bleDataSourceProvider,
      Provider<CarRepository> carRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<SensorRepository> sensorRepositoryProvider) {
    this.bleDataSourceProvider = bleDataSourceProvider;
    this.carRepositoryProvider = carRepositoryProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
    this.sensorRepositoryProvider = sensorRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(bleDataSourceProvider.get(), carRepositoryProvider.get(), userPreferencesRepositoryProvider.get(), sensorRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<BleDataSource> bleDataSourceProvider,
      Provider<CarRepository> carRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<SensorRepository> sensorRepositoryProvider) {
    return new DashboardViewModel_Factory(bleDataSourceProvider, carRepositoryProvider, userPreferencesRepositoryProvider, sensorRepositoryProvider);
  }

  public static DashboardViewModel newInstance(BleDataSource bleDataSource,
      CarRepository carRepository, UserPreferencesRepository userPreferencesRepository,
      SensorRepository sensorRepository) {
    return new DashboardViewModel(bleDataSource, carRepository, userPreferencesRepository, sensorRepository);
  }
}
