/**
 * Copyright 2016 Lloyd Torres
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

package com.lloydtorres.stately.dto;

import java.util.LinkedHashMap;

/**
 * Created by Lloyd on 2016-07-24.
 * A holder for a generic data card in the nation fragment.
 */
public class NationGenericCardData {
    public String title;
    public String mainContent;
    public LinkedHashMap<String, String> items;
    public String nationCensusTarget;
    public int idCensusTarget;

    public NationGenericCardData() { super(); }
}
