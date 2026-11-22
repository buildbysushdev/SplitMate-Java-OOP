package model;

// Practical 1: CLASS, OBJECT and CONSTRUCTORS
public class Member {
    private String name;
    private String email;

    public Member() {                       // default constructor
        name = "Unknown";
        email = "NA";
    }

    public Member(String name, String email) {   // parameterized constructor
        this.name = name;
        this.email = email;
    }

    public Member(Member m) {               // copy constructor
        this(m.name, m.email);
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
}
