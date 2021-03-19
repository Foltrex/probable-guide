package com.scn.jira.timesheet.util;

public class MyUser {
    private String name;
    private String fullName;

    public MyUser(String name, String fullName) {
        this.name = name;
        this.fullName = fullName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int hashCode() {
        //int prime = 31;
        int result = 1;
        result = 31 * result + ((this.fullName == null) ? 0 : this.fullName.hashCode());

        result = 31 * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (super.getClass() != obj.getClass())
            return false;
        MyUser other = (MyUser) obj;
        if (this.fullName == null)
            if (other.fullName != null)
                return false;
            else if (!this.fullName.equals(other.fullName))
                return false;
        if (this.name == null)
            if (other.name != null)
                return false;
            else if (!this.name.equals(other.name))
                return false;
        return true;
    }
}
