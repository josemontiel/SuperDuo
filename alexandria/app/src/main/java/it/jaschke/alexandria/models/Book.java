package it.jaschke.alexandria.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jose on 7/2/15.
 */
public class Book implements Parcelable{
    private String ean;
    private String title;
    private String subTitle;
    private String description;
    private String imageUrl;

    public Book(String ean, String title, String subTitle, String description, String imageUrl) {
        this.ean = ean;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    protected Book(Parcel in) {
        ean = in.readString();
        title = in.readString();
        subTitle = in.readString();
        description = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getEan() {
        return ean;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ean);
        dest.writeString(title);
        dest.writeString(subTitle);
        dest.writeString(description);
        dest.writeString(imageUrl);
    }
}
