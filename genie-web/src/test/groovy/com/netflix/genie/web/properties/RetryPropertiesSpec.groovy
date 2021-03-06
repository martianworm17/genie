/*
 *
 *  Copyright 2018 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.web.properties

import com.netflix.genie.test.suppliers.RandomSuppliers
import spock.lang.Specification

/**
 * Specifications for the {@link RetryProperties} class.
 *
 * @author tgianos
 */
class RetryPropertiesSpec extends Specification {

    def "Can get default properties"() {
        when:
        def properties = new RetryProperties()

        then:
        properties.getNoOfRetries() == 5
        properties.getInitialInterval() == 10_000L
        properties.getMaxInterval() == 60_000L

        when:
        def newNoOfRetries = RandomSuppliers.INT.get()
        def newInitialInterval = RandomSuppliers.LONG.get()
        def newMaxInterval = RandomSuppliers.LONG.get()
        properties.setNoOfRetries(newNoOfRetries)
        properties.setInitialInterval(newInitialInterval)
        properties.setMaxInterval(newMaxInterval)

        then:
        properties.getNoOfRetries() == newNoOfRetries
        properties.getInitialInterval() == newInitialInterval
        properties.getMaxInterval() == newMaxInterval
    }
}
