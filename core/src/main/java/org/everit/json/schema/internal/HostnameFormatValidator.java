/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "hostname" format value.
 */
public class HostnameFormatValidator implements FormatValidator {

    private static final int MAX_HOSTNAME_LENGTH = 253;
    private static final int MAX_HOSTNAME_PARTS = 127;
    private static final int MAX_HOSTNAME_PART_LENGTH = 63;

    @Override
    public Optional<String> validate(final String subject) {
        try {
            //the following is adapted from guava
            if (subject.length() > MAX_HOSTNAME_LENGTH) {
                throw new IllegalArgumentException("Hostname too long");
            }

            String[] parts = subject.split("\\.|\u3002|\uFF0E|\uFF61");

            if (parts.length > MAX_HOSTNAME_PARTS) {
                throw new IllegalArgumentException("Hostname has too many parts");
            }

            for (String part : parts) {
                if (part.isEmpty()) {
                    throw new IllegalArgumentException("'..' not allowed in hostname.");
                } else if (part.length() > MAX_HOSTNAME_PART_LENGTH) {
                    throw new IllegalArgumentException("Hostname part too long.");
                }

                if (isDash(part.charAt(0)) || isDash(part.charAt(part.length() - 1))) {
                    throw new IllegalArgumentException("Hostname part cannot start with a dash or underscore.");
                }

                for (int i = 0; i < part.length(); ++i) {
                    char c = part.charAt(i);

                    if (!Character.isLetterOrDigit(c) && !isDash(c)) {
                        throw new IllegalArgumentException("Illegal digit '" + c + "'.");
                    }
                }
            }

            //disallow the first character of the last part to be a digit
            if (Character.isDigit(parts[parts.length - 1].charAt(0))) {
                throw new IllegalArgumentException("Last hostname part cannot start with a digit.");
            }

            return Optional.empty();
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.of(String.format("[%s] is not a valid hostname", subject));
        }
    }

    @Override
    public String formatName() {
        return "hostname";
    }

    private boolean isDash(char c) {
        return "-_".indexOf(c) >= 0;
    }
}
