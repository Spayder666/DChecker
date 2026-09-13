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

data class FullPropertyNativeEntry(
    val key: String,
    val callbackValue: String,
    val legacyValue: String,
)

data class FullPropertyNativeSnapshot(
    val foreachAvailable: Boolean = false,
    val totalCount: Int = 0,
    val shellAvailable: Boolean = false,
    val entries: Map<String, FullPropertyNativeEntry> = emptyMap(),
    val shellProperties: Map<String, String> = emptyMap(),
) {
    val callbackHitCount: Int
        get() = entries.values.count { it.callbackValue.isNotBlank() }

    val legacyHitCount: Int
        get() = entries.values.count { it.legacyValue.isNotBlank() }

    val shellHitCount: Int
        get() = shellProperties.values.count { it.isNotBlank() }

    fun callbackValue(
        property: String,
    ): String {
        return entries[property]?.callbackValue.orEmpty()
    }

    fun legacyValue(
        property: String,
    ): String {
        return entries[property]?.legacyValue.orEmpty()
    }
}
