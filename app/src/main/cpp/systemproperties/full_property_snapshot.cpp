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

#include "systemproperties/full_property_snapshot.h"
#include "systemproperties/property_utils.h"

#include <algorithm>
#include <cstdio>
#include <cstring>
#include <sys/system_properties.h>

namespace systemproperties {

    namespace {

        struct ForeachCookie {
            std::vector<FullPropertyEntry> entries;
        };

        struct EntryReadContext {
            std::string key;
            std::string value;
        };

        void entry_read_callback(
                void *cookie,
                const char *name,
                const char *value,
                uint32_t
        ) {
            auto *context = static_cast<EntryReadContext *>(cookie);
            if (context == nullptr) {
                return;
            }
            context->key = name != nullptr ? name : "";
            context->value = value != nullptr ? value : "";
        }

        void foreach_property_callback(
                const prop_info *pi,
                void *cookie
        ) {
            auto *context = static_cast<ForeachCookie *>(cookie);
            if (context == nullptr || pi == nullptr) {
                return;
            }

            EntryReadContext read;
            __system_property_read_callback(pi, entry_read_callback, &read);
            if (read.key.empty()) {
                return;
            }

            FullPropertyEntry entry;
            entry.key = read.key;
            entry.callback_value = read.value;

            // Legacy 92-byte entry point: reading the same key through the older
            // bionic API gives an independent vantage point that hooking
            // frameworks often miss when they only patch the callback path.
            char legacy_value[PROP_VALUE_MAX] = {0};
            const int legacy_length = __system_property_get(read.key.c_str(), legacy_value);
            entry.legacy_value = legacy_length > 0 ? std::string(legacy_value) : "";

            context->entries.push_back(std::move(entry));
        }

        std::map<std::string, std::string> read_shell_properties(bool &available) {
            std::map<std::string, std::string> properties;
            available = false;

            // getprop through a native shell pipe: a separate process image and
            // linker namespace, so in-process hooks do not apply to this read.
            FILE *pipe = popen("getprop", "r");
            if (pipe == nullptr) {
                return properties;
            }

            char line[PROP_NAME_MAX + PROP_VALUE_MAX + 16];
            while (fgets(line, sizeof(line), pipe) != nullptr) {
                const std::string trimmed = trim_copy(std::string(line));
                if (trimmed.size() < 3 || trimmed.front() != '[') {
                    continue;
                }
                const size_t key_end = trimmed.find(']', 1);
                if (key_end == std::string::npos) {
                    continue;
                }
                const size_t value_open = trimmed.find('[', key_end);
                if (value_open == std::string::npos) {
                    continue;
                }
                const size_t value_close = trimmed.rfind(']');
                if (value_close == std::string::npos || value_close <= value_open) {
                    continue;
                }

                const std::string key = trimmed.substr(1, key_end - 1);
                const std::string value = trimmed.substr(value_open + 1,
                                                         value_close - value_open - 1);
                if (!key.empty()) {
                    properties[key] = value;
                }
            }

            const int status = pclose(pipe);
            available = status != -1;
            return properties;
        }

    }  // namespace

    FullPropertySnapshot collect_full_property_snapshot() {
        FullPropertySnapshot snapshot;

        ForeachCookie cookie;
        if (__system_property_foreach(foreach_property_callback, &cookie) == 0 && !cookie.entries.empty()) {
            snapshot.foreach_available = true;
            std::sort(cookie.entries.begin(), cookie.entries.end(),
                      [](const FullPropertyEntry &left, const FullPropertyEntry &right) {
                          return left.key < right.key;
                      });
            snapshot.entries = std::move(cookie.entries);
        }

        snapshot.shell_properties = read_shell_properties(snapshot.shell_available);
        return snapshot;
    }

}  // namespace systemproperties
