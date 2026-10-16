/*
 * Copyright 2026 Duck Apps Contributor
 * If you have any questions, suggestions, or other inquiries, please email Eltavine <me@eltavine.com>.
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

package com.dchecker.info.features.dataaudit.data

import java.util.concurrent.TimeUnit

/**
 * One command run outside the JVM through ProcessBuilder. The shell lens is its own vantage
 * point: it is not the same surface the SDK API reads, and root-hiding frameworks rewrite them
 * separately.
 */
public object AuditShell {

    /** Runs [command] and returns its trimmed stdout, or null when it failed or produced nothing. */
    public fun exec(
        command: List<String>,
        timeoutSeconds: Long = 2L,
    ): String? {
        return try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(false)
                .start()
            val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return null
            }
            if (process.exitValue() !in 0..1) {
                return null
            }
            process.inputStream.bufferedReader().use { reader ->
                reader.readText().trim().takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }

    public fun sh(script: String, timeoutSeconds: Long = 2L): String? =
        exec(listOf("sh", "-c", script), timeoutSeconds)
}
