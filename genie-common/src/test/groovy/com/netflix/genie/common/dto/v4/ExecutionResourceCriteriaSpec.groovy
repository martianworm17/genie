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
package com.netflix.genie.common.dto.v4

import com.google.common.collect.Lists
import com.netflix.genie.common.dto.CommandStatus
import com.netflix.genie.test.categories.UnitTest
import org.junit.experimental.categories.Category
import spock.lang.Specification

/**
 * Specifications for the {@link ExecutionResourceCriteria} dto.
 *
 * @author tgianos
 * @since 4.0.0
 */
@Category(UnitTest.class)
class ExecutionResourceCriteriaSpec extends Specification {

    def "Can't modify fields"() {
        def clusterCriteria = Lists.newArrayList(
                new Criterion.Builder().withId(UUID.randomUUID().toString()).build(),
                new Criterion.Builder().withId(UUID.randomUUID().toString()).build()
        )

        def commandCriterion = new Criterion.Builder().withStatus(CommandStatus.ACTIVE.toString()).build()
        def applicationIds = Lists.newArrayList(UUID.randomUUID().toString())
        def resourceCriteria1 = new ExecutionResourceCriteria(clusterCriteria, commandCriterion, applicationIds)
        def resourceCriteria2 = new ExecutionResourceCriteria(clusterCriteria, commandCriterion, null)

        when: "Try to modify the cluster criteria it throws an exception"
        resourceCriteria1
                .getClusterCriteria()
                .add(new Criterion.Builder().withStatus(CommandStatus.ACTIVE.toString()).build())

        then:
        thrown(RuntimeException)

        when: "Try to modify the list of application id's it throws an exception"
        resourceCriteria2
                .getApplicationIds()
                .add(UUID.randomUUID().toString())

        then:
        thrown(RuntimeException)
    }
}
