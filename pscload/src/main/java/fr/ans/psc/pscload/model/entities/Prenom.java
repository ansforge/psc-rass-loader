package fr.ans.psc.pscload.model.entities;

import fr.ans.psc.model.FirstName;

import java.util.ArrayList;
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
}
