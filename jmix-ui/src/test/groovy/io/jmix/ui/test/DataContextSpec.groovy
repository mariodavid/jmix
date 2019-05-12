/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.ui.test

import io.jmix.core.JmixCoreConfiguration
import io.jmix.ui.JmixUiConfiguration
import io.jmix.data.JmixDataConfiguration
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [JmixCoreConfiguration, JmixUiConfiguration, JmixDataConfiguration, DataContextTestConfiguration])
class DataContextSpec extends Specification {
}
