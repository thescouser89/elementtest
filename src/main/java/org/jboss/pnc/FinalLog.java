package org.jboss.pnc;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import java.util.Set;

@Entity
public class FinalLog extends PanacheEntity {

    @ElementCollection
    public Set<String> tags;
}
