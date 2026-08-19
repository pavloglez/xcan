package com.jpdgbv.xcan.core.network.di;

import com.jpdgbv.xcan.core.network.XCanApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideXCanApiServiceFactory implements Factory<XCanApiService> {
  @Override
  public XCanApiService get() {
    return provideXCanApiService();
  }

  public static NetworkModule_ProvideXCanApiServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static XCanApiService provideXCanApiService() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideXCanApiService());
  }

  private static final class InstanceHolder {
    static final NetworkModule_ProvideXCanApiServiceFactory INSTANCE = new NetworkModule_ProvideXCanApiServiceFactory();
  }
}
