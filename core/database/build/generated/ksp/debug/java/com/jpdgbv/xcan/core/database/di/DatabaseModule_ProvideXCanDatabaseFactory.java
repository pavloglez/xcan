package com.jpdgbv.xcan.core.database.di;

import android.content.Context;
import com.jpdgbv.xcan.core.database.XCanDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideXCanDatabaseFactory implements Factory<XCanDatabase> {
  private final Provider<Context> contextProvider;

  private DatabaseModule_ProvideXCanDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public XCanDatabase get() {
    return provideXCanDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideXCanDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideXCanDatabaseFactory(contextProvider);
  }

  public static XCanDatabase provideXCanDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideXCanDatabase(context));
  }
}
