package com.jpdgbv.xcan.feature.config;

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
public final class ConfigViewModel_Factory implements Factory<ConfigViewModel> {
  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private ConfigViewModel_Factory(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public ConfigViewModel get() {
    return newInstance(userPreferencesRepositoryProvider.get());
  }

  public static ConfigViewModel_Factory create(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new ConfigViewModel_Factory(userPreferencesRepositoryProvider);
  }

  public static ConfigViewModel newInstance(UserPreferencesRepository userPreferencesRepository) {
    return new ConfigViewModel(userPreferencesRepository);
  }
}
