package com.example.thedraft;

import com.google.firebase.firestore.DocumentReference;



public class ClassModel {

    private String  name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private DocumentReference  creator;

    public ClassModel() { }

    public ClassModel(String name, DocumentReference creator) {
        this.name = name;
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DocumentReference getCreator() {
        return creator;
    }

    public void setCreator(DocumentReference creator) {
        this.creator = creator;
    }


}
