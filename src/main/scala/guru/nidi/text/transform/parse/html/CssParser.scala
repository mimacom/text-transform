/**
 * Copyright (C) 2013 Stefan Niederhauser (nidin@gmx.ch)
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
package guru.nidi.text.transform.parse.html

import java.util.regex.Pattern

import guru.nidi.text.transform.parse.CustomizerParser

/**
 * Parse customizing css attributes of an element.
 */
object CssParser {
  private val PATTERN = Pattern.compile("([A-Za-z-_]+)\\s*(:\\s*([^;]+))?")

  def apply(input: String, block: (String, String) => Unit) = CustomizerParser.apply(PATTERN, input, block)
}
