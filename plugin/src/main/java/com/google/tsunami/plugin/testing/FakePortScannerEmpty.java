/*
 * Copyright 2025 Google LLC
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
package com.google.tsunami.plugin.testing;

import com.google.tsunami.plugin.PluginType;
import com.google.tsunami.plugin.PortScanner;
import com.google.tsunami.plugin.annotations.PluginInfo;
import com.google.tsunami.proto.PortScanningReport;
import com.google.tsunami.proto.ScanTarget;
import com.google.tsunami.proto.TargetInfo;

/** A fake PortScanner plugin for testing purpose only. */
@PluginInfo(
    type = PluginType.PORT_SCAN,
    name = "FakePortScannerEmpty",
    version = "v0.1",
    description = "Another fake PortScanner.",
    author = "fake",
    bootstrapModule = FakePortScannerBootstrapModuleEmpty.class)
public class FakePortScannerEmpty implements PortScanner {

  @Override
  public PortScanningReport scan(ScanTarget scanTarget) {
    return PortScanningReport.newBuilder()
        .setTargetInfo(TargetInfo.newBuilder().addNetworkEndpoints(scanTarget.getNetworkEndpoint()))
        .build();
  }
}
