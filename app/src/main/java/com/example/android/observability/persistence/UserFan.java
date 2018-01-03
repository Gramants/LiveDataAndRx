package com.example.android.observability.persistence;

/**
 * Created by amitshekhar on 27/08/16.
 */
public class UserFan {
    public long id;
    public String firstname;
    public String lastname;
    public boolean isFollowing;

    public UserFan() {
    }

    public UserFan(ApiUser apiUser) {
        this.id = apiUser.id;
        this.firstname = apiUser.firstname;
        this.lastname = apiUser.lastname;
    }

    @Override
    public String toString() {
        return "UserFan{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", isFollowing=" + isFollowing +
                '}';
    }
}
