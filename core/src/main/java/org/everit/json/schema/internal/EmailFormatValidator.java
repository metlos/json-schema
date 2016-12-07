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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.everit.json.schema.FormatValidator;

/**
 * Implementation of the "email" format value.
 * This is heavily inspired by {@code EmailValidator} in {@code commons-validation}.
 */
public class EmailFormatValidator implements FormatValidator {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^\\s*?(.+)@(.+?)\\s*$");
    private static final int MAX_USERNAME_LENGTH = 64;

    private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
    private static final String VALID_CHARS = "(\\\\.)|[^\\s" + SPECIAL_CHARS + "]";
    private static final String QUOTED_USER = "(\"(\\\\\"|[^\"])*\")";
    private static final String WORD = "((" + VALID_CHARS + "|')+|" + QUOTED_USER + ")";

    private static final Pattern USER_REGEX = Pattern.compile("^\\s*" + WORD + "(\\." + WORD + ")*$");

    private static final IPV4Validator ip4Validator = new IPV4Validator();
    private static final IPV6Validator ip6Validator = new IPV6Validator();
    private static final HostnameFormatValidator hostNameValidator = new HostnameFormatValidator();

    @Override
    public Optional<String> validate(final String subject) {
        if (subject == null || subject.endsWith(".")) {
            return invalid(subject);
        }

        Matcher matcher = EMAIL_REGEX.matcher(subject);

        if (!matcher.matches()) {
            return invalid(subject);
        }

        String user = matcher.group(1);
        String host = matcher.group(2);

        if (user == null || user.length() > MAX_USERNAME_LENGTH) {
            return invalid(subject);
        }

        if (!USER_REGEX.matcher(user).matches()) {
            return invalid(subject);
        }

        if (ip4Validator.validate(host).isPresent() && ip6Validator.validate(host).isPresent()
                && hostNameValidator.validate(host).isPresent()) {
            return invalid(subject);
        }

        return Optional.empty();
    }

    private static Optional<String> invalid(String subject) {
        return Optional.of(String.format("[%s] is not a valid email address", subject));
    }

    @Override
    public String formatName() {
        return "email";
    }
}
