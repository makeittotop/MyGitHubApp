package io.abhishekpareek.app.MyGistHub.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by apareek on 5/15/16.
 */
public class MyGist implements Parcelable {
    private String gistTitle;
    private String gistDescription;
    private String gistId;
    private String gistUpdatedAt;
    private boolean gistType;
    private boolean star;

    public MyGist() {
    }

    protected MyGist(Parcel in) {
        String[] data = new String[1];

        in.readStringArray(data);
        this.gistId = data[0];
    }

    public static final Creator<MyGist> CREATOR = new Creator<MyGist>() {
        @Override
        public MyGist createFromParcel(Parcel in) {
            return new MyGist(in);
        }

        @Override
        public MyGist[] newArray(int size) {
            return new MyGist[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.gistId});
    }


    public String getGistDescription() {
        return gistDescription;
    }

    public void setGistDescription(String gistDescription) {
        this.gistDescription = gistDescription;
    }

    public String getGistTitle() {
        return gistTitle;
    }

    public void setGistTitle(String gistTitle) {
        this.gistTitle = gistTitle;
    }


    public String getGistId() {
        return gistId;
    }

    public void setGistId(String gistId) {
        this.gistId = gistId;
    }

    public String getGistUpdatedAt() {
        return gistUpdatedAt;
    }

    public void setGistUpdatedAt(String gistUpdatedAt) {
        this.gistUpdatedAt = gistUpdatedAt;
    }

    public boolean getGistTypePublic() {
        return gistType;
    }

    public void setGistTypePublic(boolean gistType) {
        this.gistType = gistType;
    }

    public boolean isStarred() {
        return star;
    }

    public boolean isPublic() {
        if (gistType == true)
            return true;
        else
            return false;
    }


    public void setStarred(boolean star) {
        this.star = star;
    }
}
