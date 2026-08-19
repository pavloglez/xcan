package com.jpdgbv.xcan.core.data.sync;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.jpdgbv.xcan.core.data.MaintenanceRepository;
import dagger.internal.DaggerGenerated;
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
public final class SyncWorker_Factory {
  private final Provider<MaintenanceRepository> maintenanceRepositoryProvider;

  private SyncWorker_Factory(Provider<MaintenanceRepository> maintenanceRepositoryProvider) {
    this.maintenanceRepositoryProvider = maintenanceRepositoryProvider;
  }

  public SyncWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, maintenanceRepositoryProvider.get());
  }

  public static SyncWorker_Factory create(
      Provider<MaintenanceRepository> maintenanceRepositoryProvider) {
    return new SyncWorker_Factory(maintenanceRepositoryProvider);
  }

  public static SyncWorker newInstance(Context appContext, WorkerParameters workerParams,
      MaintenanceRepository maintenanceRepository) {
    return new SyncWorker(appContext, workerParams, maintenanceRepository);
  }
}
