/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.baseline.refaster;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Map;

public final class AssertjMapIsEmptyWithDescription<K, V> {

    @BeforeTemplate
    void before1(Map<K, V> things, String description) {
        assertThat(things.size() == 0).describedAs(description).isTrue();
    }

    @BeforeTemplate
    void before2(Map<K, V> things, String description) {
        assertThat(things.isEmpty()).describedAs(description).isTrue();
    }

    @BeforeTemplate
    void before3(Map<K, V> things, String description) {
        assertThat(things.size()).describedAs(description).isZero();
    }

    @BeforeTemplate
    void before4(Map<K, V> things, String description) {
        assertThat(things.size()).describedAs(description).isEqualTo(0);
    }

    @BeforeTemplate
    void before5(Map<K, V> things, String description) {
        assertThat(things).describedAs(description).hasSize(0);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    void after(Map<K, V> things, String description) {
        assertThat(things).describedAs(description).isEmpty();
    }
}