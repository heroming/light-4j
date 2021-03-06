/*
 * Copyright 2010-2013 Coda Hale and Yammer, Inc., 2014-2017 Dropwizard Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dropwizard.metrics;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.SharedMetricRegistries;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SharedMetricRegistriesTest {
    @Before
    public void setUp() throws Exception {
        // Unset the defaultRegistryName field between tests for better isolation.
        final Field field = SharedMetricRegistries.class.getDeclaredField("defaultRegistryName");
        field.setAccessible(true);
        final Field modfiers = Field.class.getDeclaredField("modifiers");
        modfiers.setAccessible(true);
        modfiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, null);
        SharedMetricRegistries.clear();
    }

    @Test
    public void memorizesRegistriesByName() throws Exception {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");

        assertThat(one)
                .isSameAs(two);
    }

    @Test
    public void hasASetOfNames() throws Exception {
        SharedMetricRegistries.getOrCreate("one");

        assertThat(SharedMetricRegistries.names())
                .containsOnly("one");
    }

    @Test
    public void removesRegistries() throws Exception {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.remove("one");

        assertThat(SharedMetricRegistries.names())
                .isEmpty();

        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");
        assertThat(two)
                .isNotSameAs(one);
    }

    @Test
    public void clearsRegistries() throws Exception {
        SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.getOrCreate("two");

        SharedMetricRegistries.clear();

        assertThat(SharedMetricRegistries.names())
                .isEmpty();
    }

    @Test
    public void errorsWhenDefaultUnset() throws Exception {
        try {
            SharedMetricRegistries.getDefault();
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default registry name has not been set.");
        }
    }

    @Test
    public void createsDefaultRegistries() throws Exception {
        final String defaultName = "default";
        final MetricRegistry registry = SharedMetricRegistries.setDefault(defaultName);
        assertThat(registry).isNotNull();
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }

    @Test
    public void errorsWhenDefaultAlreadySet() throws Exception {
        try {
            SharedMetricRegistries.setDefault("foobah");
            SharedMetricRegistries.setDefault("borg");
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default metric registry name is already set.");
        }
    }

    @Test
    public void setsDefaultExistingRegistries() throws Exception {
        final String defaultName = "default";
        final MetricRegistry registry = new MetricRegistry();
        assertThat(SharedMetricRegistries.setDefault(defaultName, registry)).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }
}
