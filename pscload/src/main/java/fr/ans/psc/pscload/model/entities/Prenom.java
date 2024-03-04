/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
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
package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.FirstName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Prenom extends fr.ans.psc.model.FirstName {
    String firstName;
    Integer order;

    public Prenom() {
    }

    public Prenom(String firstName, Integer order) {
        this.firstName = firstName;
        this.order = order;
    }

    public Prenom(String firstName) {
        this.firstName = firstName;
        this.order = 0;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * Transforms a string of first names separated by an apostrophe (')
     * into a list of FirstName objects, in the same order as they appear in the string.
     *
     * @param string a string of first names separated by an apostrophe (') and containing exactly FIRST_NAME_COUNT-1 apostrophes (')
     * @return list of FirstName objects
     */
    public static List<FirstName> stringToList(String string) {
        String[] firstNameStrings = string.split("'", -1);
        List<FirstName> firstNames = new ArrayList<>();
        for (int i = 0; i < firstNameStrings.length; i++) {
            firstNames.add(new FirstName(firstNameStrings[i], i));
        }
        return firstNames;
    }

    /**
     * Transforms a list of FirstName objects into a string of first names separated by an apostrophe ('),
     * and containing exactly FIRST_NAME_COUNT-1 apostrophes (') even if the list contains less than FIRST_NAME_COUNT names.
     *
     * @param firstNames list of FirstName objects
     * @return a string of first names separated by an apostrophe (') and containing exactly FIRST_NAME_COUNT-1 apostrophes (')
     */
    public static String listToString(List<FirstName> firstNames) {
        StringBuilder stringBuilder = new StringBuilder();

        firstNames.sort(Comparator.comparing(FirstName::getOrder));
        for (int i = firstNames.size(); i < 3; i++) {
            firstNames.add(new FirstName("", i));
        }
        firstNames.forEach(fn -> stringBuilder.append(fn.getFirstName()).append("'"));
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }
}
