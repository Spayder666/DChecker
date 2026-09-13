/*
 * Copyright 2026 Duck Apps Contributor
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

package com.dchecker.info.features.systemproperties.data.native

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemPropertiesFullSnapshotParseTest {

    private val bridge = SystemPropertiesNativeBridge()

    @Test
    fun `parseFullSnapshot reads flags entries and shell properties`() {
        val snapshot = bridge.parseFullSnapshot(
            """
            FOREACH_AVAILABLE=1
            TOTAL_COUNT=2
            SHELL_AVAILABLE=1
            ENTRY=ro.build.type|user|user
            ENTRY=ro.product.model|Pixel 9|Pixel 9
            SHELL=ro.build.type|user
            """.trimIndent(),
        )

        assertTrue(snapshot.foreachAvailable)
        assertTrue(snapshot.shellAvailable)
        assertEquals(2, snapshot.totalCount)
        assertEquals(2, snapshot.entries.size)
        assertEquals("user", snapshot.callbackValue("ro.build.type"))
        assertEquals("user", snapshot.legacyValue("ro.build.type"))
        assertEquals("Pixel 9", snapshot.callbackValue("ro.product.model"))
        assertEquals("user", snapshot.shellProperties["ro.build.type"])
        assertEquals(2, snapshot.callbackHitCount)
    }

    @Test
    fun `parseFullSnapshot ignores malformed entry lines`() {
        val snapshot = bridge.parseFullSnapshot(
            """
            FOREACH_AVAILABLE=1
            TOTAL_COUNT=3
            ENTRY=broken
            ENTRY=ro.secure|1
            SHELL=broken
            """.trimIndent(),
        )

        assertEquals(0, snapshot.entries.size)
        assertEquals(3, snapshot.totalCount)
        assertEquals(0, snapshot.shellProperties.size)
    }

    @Test
    fun `parseFullSnapshot blank raw returns defaults`() {
        val snapshot = bridge.parseFullSnapshot("")

        assertFalse(snapshot.foreachAvailable)
        assertFalse(snapshot.shellAvailable)
        assertEquals(0, snapshot.totalCount)
        assertTrue(snapshot.entries.isEmpty())
    }

    @Test
    fun `parseInlineSnapshot reads callback and legacy values`() {
        val entries = bridge.parseInlineSnapshot(
            """
            AVAILABLE=1
            INLINE=ro.secure|1|1
            INLINE=ro.debuggable|0|0
            """.trimIndent(),
        )

        assertEquals(2, entries.size)
        assertEquals("1", entries.getValue("ro.secure").callbackValue)
        assertEquals("0", entries.getValue("ro.debuggable").legacyValue)
    }

    @Test
    fun `parseInlineSnapshot blank raw returns empty map`() {
        assertTrue(bridge.parseInlineSnapshot("").isEmpty())
    }
}
