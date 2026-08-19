package com.jpdgbv.xcan.core.database.di;

import com.jpdgbv.xcan.core.database.XCanDatabase;
import com.jpdgbv.xcan.core.database.dao.CarProfileDao;
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
public final class DatabaseModule_ProvideCarProfileDaoFactory implements Factory<CarProfileDao> {
  private final Provider<XCanDatabase> databaseProvider;

  private DatabaseModule_ProvideCarProfileDaoFactory(Provider<XCanDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CarProfileDao get() {
    return provideCarProfileDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideCarProfileDaoFactory create(
      Provider<XCanDatabase> databaseProvider) {
    return new DatabaseModule_ProvideCarProfileDaoFactory(databaseProvider);
  }

  public static CarProfileDao provideCarProfileDao(XCanDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCarProfileDao(database));
  }
}
