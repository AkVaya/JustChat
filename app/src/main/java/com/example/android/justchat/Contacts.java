package com.example.android.justchat;

public class Contacts {
    String name,status,image,ref;
    Boolean check1 , check2, check3;

    public Contacts(String name, String status, String image,String ref,Boolean check1,Boolean check2,Boolean check3) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.ref = ref;
        this.check1 = check1;
        this.check2 = check2;
        this.check3 = check3;
    }

    public Boolean getCheck1() { return check1; }

    public Boolean getCheck2() {
        return check2;
    }

    public Boolean getCheck3() { return check3; }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }
}
