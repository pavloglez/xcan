package com.jpdgbv.xcan.feature.maintenance;

import com.jpdgbv.xcan.core.data.CarRepository;
import com.jpdgbv.xcan.core.data.MaintenanceRepository;
import com.jpdgbv.xcan.core.data.UserPreferencesRepository;
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
public final class MaintenanceViewModel_Factory implements Factory<MaintenanceViewModel> {
  private final Provider<MaintenanceRepository> maintenanceRepositoryProvider;

  private final Provider<CarRepository> carRepositoryProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private MaintenanceViewModel_Factory(
      Provider<MaintenanceRepository> maintenanceRepositoryProvider,
      Provider<CarRepository> carRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.maintenanceRepositoryProvider = maintenanceRepositoryProvider;
    this.carRepositoryProvider = carRepositoryProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public MaintenanceViewModel get() {
    return newInstance(maintenanceRepositoryProvider.get(), carRepositoryProvider.get(), userPreferencesRepositoryProvider.get());
  }

  public static MaintenanceViewModel_Factory create(
      Provider<MaintenanceRepository> maintenanceRepositoryProvider,
      Provider<CarRepository> carRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new MaintenanceViewModel_Factory(maintenanceRepositoryProvider, carRepositoryProvider, userPreferencesRepositoryProvider);
  }

  public static MaintenanceViewModel newInstance(MaintenanceRepository maintenanceRepository,
      CarRepository carRepository, UserPreferencesRepository userPreferencesRepository) {
    return new MaintenanceViewModel(maintenanceRepository, carRepository, userPreferencesRepository);
  }
}
