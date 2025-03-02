/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.tsunami.plugin;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.base.Stopwatch;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.tsunami.plugin.PluginExecutionResult.ExecutionStatus;
import java.time.Duration;
import javax.inject.Inject;

class PluginExecutorImpl implements PluginExecutor {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final ListeningScheduledExecutorService pluginExecutionThreadPool;
  private final Stopwatch executionStopwatch;

  @Inject
  PluginExecutorImpl(
      @PluginExecutionThreadPool ListeningScheduledExecutorService pluginExecutionThreadPool) {
    this(pluginExecutionThreadPool, Stopwatch.createUnstarted());
  }

  PluginExecutorImpl(
      ListeningScheduledExecutorService pluginExecutionThreadPool, Stopwatch executionStopwatch) {
    this.pluginExecutionThreadPool = checkNotNull(pluginExecutionThreadPool);
    this.executionStopwatch = checkNotNull(executionStopwatch);
  }

  @Override
  public <T> ListenableFuture<PluginExecutionResult<T>> executeAsync(
      PluginExecutorConfig<T> executorConfig) {
    // Executes the core plugin logic within the thread pool.
    return FluentFuture.from(
            pluginExecutionThreadPool.submit(
                () -> {
                  executionStopwatch.start();
                  return executorConfig.pluginExecutionLogic().call();
                }))
        // Terminate plugin if it runs over 1 hour.
        .withTimeout(Duration.ofHours(1), pluginExecutionThreadPool)
        // If execution succeeded, build successful execution result.
        .transform(resultData -> buildSucceededResult(resultData, executorConfig), directExecutor())
        // If execution failed, build failed execution result.
        .catching(
            Throwable.class,
            exception -> buildFailedResult(exception, executorConfig),
            directExecutor());
  }

  private <T> PluginExecutionResult<T> buildSucceededResult(
      T resultData, PluginExecutorConfig<T> executorConfig) {
    if (executionStopwatch.isRunning()) {
      executionStopwatch.stop();
    }
    logger.atInfo().log(
        "%s plugin execution finished in %d (ms)",
        executorConfig.matchedPlugin().pluginDefinition().name(),
        executionStopwatch.elapsed().toMillis());
    return PluginExecutionResult.<T>builder()
        .setExecutionStatus(ExecutionStatus.SUCCEEDED)
        .setResultData(resultData)
        .setExecutionStopwatch(executionStopwatch)
        .setExecutorConfig(executorConfig)
        .build();
  }

  private <T> PluginExecutionResult<T> buildFailedResult(
      Throwable t, PluginExecutorConfig<T> executorConfig) {
    logger.atWarning().log(
        "Plugin '%s' failed: %s", executorConfig.matchedPlugin().pluginId(), t.getMessage());
    if (executionStopwatch.isRunning()) {
      executionStopwatch.stop();
    }
    return PluginExecutionResult.<T>builder()
        .setExecutionStatus(ExecutionStatus.FAILED)
        .setExecutionStopwatch(executionStopwatch)
        .setException(wrapException(t, executorConfig))
        .setExecutorConfig(executorConfig)
        .build();
  }

  private static <T> PluginExecutionException wrapException(
      Throwable t, PluginExecutorConfig<T> executorConfig) {
    if (t instanceof PluginExecutionException) {
      return (PluginExecutionException) t;
    } else {
      return new PluginExecutionException(
          String.format(
              "Plugin execution error on '%s'.", executorConfig.matchedPlugin().pluginId()),
          t);
    }
  }
}
