package com.jpdgbv.xcan.core.data;

import com.jpdgbv.xcan.core.database.dao.MaintenanceDao;
import com.jpdgbv.xcan.core.network.XCanApiService;
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
public final class MaintenanceRepository_Factory implements Factory<MaintenanceRepository> {
  private final Provider<MaintenanceDao> maintenanceDaoProvider;

  private final Provider<XCanApiService> apiServiceProvider;

  private final Provider<CarRepository> carRepositoryProvider;

  private MaintenanceRepository_Factory(Provider<MaintenanceDao> maintenanceDaoProvider,
      Provider<XCanApiService> apiServiceProvider, Provider<CarRepository> carRepositoryProvider) {
    this.maintenanceDaoProvider = maintenanceDaoProvider;
    this.apiServiceProvider = apiServiceProvider;
    this.carRepositoryProvider = carRepositoryProvider;
  }

  @Override
  public MaintenanceRepository get() {
    return newInstance(maintenanceDaoProvider.get(), apiServiceProvider.get(), carRepositoryProvider.get());
  }

  public static MaintenanceRepository_Factory create(
      Provider<MaintenanceDao> maintenanceDaoProvider, Provider<XCanApiService> apiServiceProvider,
      Provider<CarRepository> carRepositoryProvider) {
    return new MaintenanceRepository_Factory(maintenanceDaoProvider, apiServiceProvider, carRepositoryProvider);
  }

  public static MaintenanceRepository newInstance(MaintenanceDao maintenanceDao,
      XCanApiService apiService, CarRepository carRepository) {
    return new MaintenanceRepository(maintenanceDao, apiService, carRepository);
  }
}
