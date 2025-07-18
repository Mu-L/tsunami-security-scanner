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
package com.google.tsunami.main.cli.option;

import static org.junit.Assert.assertThrows;

import com.beust.jcommander.ParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MainCliOptions}. */
@RunWith(JUnit4.class)
public class MainCliOptionsTest {

  @Test
  public void validate_whenDumpAdvisoriesPathPassed_doesNotThrowParameterException() {
    MainCliOptions cliOptions = new MainCliOptions();

    cliOptions.dumpAdvisoriesPath = "path/to/dump/advisories";
    cliOptions.validate();
  }

  @Test
  public void validate_whenMissingScanTarget_throwsParameterException() {
    MainCliOptions cliOptions = new MainCliOptions();

    assertThrows(ParameterException.class, cliOptions::validate);
  }

  @Test
  public void validate_whenUriTargetPassedWithHostnameTarget_throwsParameterException() {
    MainCliOptions cliOptions = new MainCliOptions();

    cliOptions.hostnameTarget = "localhost";
    cliOptions.uriTarget = "https://localhost/function1";

    assertThrows(ParameterException.class, cliOptions::validate);
  }
}
