/*
 * Copyright (c) 2023 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.mule.extension;

import com.marklogic.client.document.ServerTransform;

class Utilities {
    static boolean hasText(String var) {
        return ((var != null) && !var.isEmpty());
    }

    static ServerTransform findServerTransform(String restTransform, String restTransformParameters, String restTransformParametersDelimiter) {
        if(!hasText(restTransform)){
            return null;
        }
        ServerTransform serverTransform = new ServerTransform(restTransform);
        if (Utilities.hasText(restTransformParameters)) {
            String[] parametersArray = restTransformParameters.split(restTransformParametersDelimiter);
            for (int i = 0; i < parametersArray.length; i = i + 2) {
                serverTransform.addParameter(parametersArray[i], parametersArray[i + 1]);
            }
        }
        return serverTransform;
    }
}
