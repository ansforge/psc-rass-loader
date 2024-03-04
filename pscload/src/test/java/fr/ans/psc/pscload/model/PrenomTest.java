/**
 * Copyright (C) 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
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
package fr.ans.psc.pscload.model;

import fr.ans.psc.model.FirstName;
import fr.ans.psc.pscload.PscloadApplication;
import fr.ans.psc.pscload.model.entities.Prenom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = PscloadApplication.class)
public class PrenomTest {

    @Test
    @DisplayName("general case")
    public void setFirstNamesTest() {

        FirstName fn1 = new FirstName("KADER", 0);
        FirstName fn2 = new FirstName("HASSAN", 1);
        FirstName fn3 = new FirstName("JOHNNY", 2);

        // check presence
        String rassFNames = "KADER'HASSAN'JOHNNY";
        List<FirstName> actual = Prenom.stringToList(rassFNames);
        assertEquals(fn1, actual.stream().filter(fn -> fn.getOrder().equals(0)).findAny().get());
        assertEquals(fn2, actual.stream().filter(fn -> fn.getOrder().equals(1)).findAny().get());
        assertEquals(fn3, actual.stream().filter(fn -> fn.getOrder().equals(2)).findAny().get());

        // check order precedence
        String differentOrderString = "KADER'JOHNNY'HASSAN";
        List<FirstName> differentOrder = Prenom.stringToList(differentOrderString);
        fn2.setOrder(2);
        fn3.setOrder(1);
        assertNotEquals(fn2, differentOrder.stream().filter(fn -> fn.getOrder().equals(1)).findAny().get());
        assertNotEquals(fn3, differentOrder.stream().filter(fn -> fn.getOrder().equals(2)).findAny().get());
        assertEquals(fn3, differentOrder.stream().filter(fn -> fn.getOrder().equals(1)).findAny().get());
        assertEquals(fn2, differentOrder.stream().filter(fn -> fn.getOrder().equals(2)).findAny().get());
    }

    @Test
    @DisplayName("test emptyness behavior")
    public void emptyFNamesTest() {
        FirstName fn1 = new FirstName("KADER", 0);
        FirstName fn3 = new FirstName("TOTO", 2);


        String rassFNames = "KADER''";
        List<FirstName> actual = Prenom.stringToList(rassFNames);
        assertEquals(3, actual.size());

        // creates empty first Name if middle one is missing
        String middleEmpty = "KADER''TOTO";
        List<FirstName> actual2 = Prenom.stringToList(middleEmpty);
        assertEquals(3, actual2.size());
        assertEquals("", actual2.stream()
        .filter(fn -> fn.getOrder().equals(1)).findAny().get().getFirstName());
        assertEquals(2, actual2.stream()
                .filter(fn -> fn.getFirstName().equals("TOTO")).findAny().get().getOrder());
    }

    @Test
    @DisplayName("list to string")
    public void setNamesAsStringTest() {
        FirstName fn1 = new FirstName("KADER", 0);
        FirstName fn2 = new FirstName("HASSAN", 1);
        FirstName fn3 = new FirstName("JOHNNY", 2);

        List<FirstName> fnList = new ArrayList<>();
        fnList.add(fn1);
        fnList.add(fn3);
        fnList.add(fn2);

        String namesString = Prenom.listToString(fnList);
        assertEquals("KADER'HASSAN'JOHNNY", namesString);

        fnList.remove(fn2);
        fnList.add(new FirstName("", 1));

        String middleEmpty = Prenom.listToString(fnList);
        assertEquals("KADER''JOHNNY", middleEmpty);

        fnList.remove(fn3);
        fnList.remove(fn2);
        String onlyOne = Prenom.listToString(fnList);
        assertEquals("KADER''", onlyOne);
    }

}
