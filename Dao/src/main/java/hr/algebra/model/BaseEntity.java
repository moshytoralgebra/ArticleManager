package hr.algebra.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseEntity {

    private int id;

    public int getId()        { return id; }
    public void setId(int id) { this.id = id; }

    public abstract String getDisplayName();
}
