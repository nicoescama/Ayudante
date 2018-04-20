package com.energismart.ayudante;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicoescama on 16/02/2018.
 */

public class User {


    public String uid;
    public String name;
    public String email;
    public String phoneNumber;
    public String profileUser;
    public String licenseType;
    public String licenseDueTo;

    public Map<String, Boolean> stars = new HashMap<>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public User(String uid, String name, String email, String phoneNumer, String profileUser, String licenseType, String licenseDueTo) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumer;
        this.profileUser = profileUser;
        this.licenseType = licenseType;
        this.licenseDueTo = licenseDueTo;
    }

    public User(String uid, String name, String email, String phoneNumer, String profileUser) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumer;
        this.profileUser = profileUser;
        this.licenseType = "";
        this.licenseDueTo = "";
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("phoneNumber", phoneNumber);
        result.put("profileUser", profileUser);
        result.put("licenseType",licenseType);
        result.put("licenseDueTo",licenseDueTo);
        return result;
    }

}
