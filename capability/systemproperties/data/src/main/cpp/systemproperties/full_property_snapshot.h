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

#pragma once

#include <map>
#include <string>
#include <vector>

namespace systemproperties {

    struct FullPropertyEntry {
        std::string key;
        std::string callback_value;
        std::string legacy_value;
    };

    struct FullPropertySnapshot {
        bool foreach_available = false;
        std::vector<FullPropertyEntry> entries;
        std::map<std::string, std::string> shell_properties;
        bool shell_available = false;
    };

    FullPropertySnapshot collect_full_property_snapshot();

}  // namespace systemproperties
