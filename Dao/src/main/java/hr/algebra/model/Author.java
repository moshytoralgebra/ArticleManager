package hr.algebra.model;

public final class Author extends BaseEntity implements Comparable<Author> {

    private String name;
    private String bio;
    private String email;

    public Author() {}

    public Author(int id, String name, String bio, String email) {
        this(name, bio, email);
        setId(id);
    }

    public Author(String name, String bio, String email) {
        setName(name);
        setBio(bio);
        setEmail(email);
    }

    @Override
    public String getDisplayName() { return name; }

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public String getBio()               { return bio; }
    public void setBio(String bio)       { this.bio = bio; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

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
        return getId() == ((Author) obj).getId();
    }

    @Override
    public int compareTo(Author other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return getId() + " - " + name;
    }
}
