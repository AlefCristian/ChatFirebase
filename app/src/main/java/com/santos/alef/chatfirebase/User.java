package com.santos.alef.chatfirebase;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String userId;
    private String userName;
    private String urlPhotoPerfil;

    public User()  {}

    public User(String userId, String userName, String urlPhotoPerfil) {
        this.userId = userId;
        this.userName = userName;
        this.urlPhotoPerfil = urlPhotoPerfil;
    }

    protected User(Parcel in) {
        userId = in.readString();
        userName = in.readString();
        urlPhotoPerfil = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUrlPhotoPerfil() {
        return urlPhotoPerfil;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(urlPhotoPerfil);

    }
}
