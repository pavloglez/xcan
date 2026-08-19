package com.jpdgbv.xcan.core.database.di;

import com.jpdgbv.xcan.core.database.XCanDatabase;
import com.jpdgbv.xcan.core.database.dao.MaintenanceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideMaintenanceDaoFactory implements Factory<MaintenanceDao> {
  private final Provider<XCanDatabase> databaseProvider;

  private DatabaseModule_ProvideMaintenanceDaoFactory(Provider<XCanDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MaintenanceDao get() {
    return provideMaintenanceDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideMaintenanceDaoFactory create(
      Provider<XCanDatabase> databaseProvider) {
    return new DatabaseModule_ProvideMaintenanceDaoFactory(databaseProvider);
  }

  public static MaintenanceDao provideMaintenanceDao(XCanDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMaintenanceDao(database));
  }
}
