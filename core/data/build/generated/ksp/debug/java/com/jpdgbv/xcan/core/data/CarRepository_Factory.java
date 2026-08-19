package com.jpdgbv.xcan.core.data;

import com.jpdgbv.xcan.core.database.dao.CarProfileDao;
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
public final class CarRepository_Factory implements Factory<CarRepository> {
  private final Provider<CarProfileDao> carProfileDaoProvider;

  private CarRepository_Factory(Provider<CarProfileDao> carProfileDaoProvider) {
    this.carProfileDaoProvider = carProfileDaoProvider;
  }

  @Override
  public CarRepository get() {
    return newInstance(carProfileDaoProvider.get());
  }

  public static CarRepository_Factory create(Provider<CarProfileDao> carProfileDaoProvider) {
    return new CarRepository_Factory(carProfileDaoProvider);
  }

  public static CarRepository newInstance(CarProfileDao carProfileDao) {
    return new CarRepository(carProfileDao);
  }
}
