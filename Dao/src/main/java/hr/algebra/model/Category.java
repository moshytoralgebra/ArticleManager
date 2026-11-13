package hr.algebra.model;

public final class Category extends BaseEntity implements Comparable<Category> {

    private String name;
    private String description;

    public Category() {}

    public Category(int id, String name, String description) {
        this(name, description);
        setId(id);
    }

    public Category(String name, String description) {
        setName(name);
        setDescription(description);
    }

    @Override
    public String getDisplayName() { return name; }

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }

    public String getDescription()           { return description; }
    public void setDescription(String desc)  { this.description = desc; }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getId();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)  return true;
        if (obj == null)  return false;
        if (getClass() != obj.getClass()) return false;
        return getId() == ((Category) obj).getId();
    }

    @Override
    public int compareTo(Category other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return getId() + " - " + name;
    }
}
