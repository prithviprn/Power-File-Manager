package pe.kmh.fm.prop;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchedFileProperty extends FileProperty implements Parcelable {

    public static final Parcelable.Creator<SearchedFileProperty> CREATOR = new Parcelable.Creator<SearchedFileProperty>() {

        @Override
        public SearchedFileProperty createFromParcel(Parcel source) {
            return new SearchedFileProperty(source);
        }

        @Override
        public SearchedFileProperty[] newArray(int size) {
            return new SearchedFileProperty[size];
        }

    };
    private String FilePath;

    public SearchedFileProperty(String _icon, String _name, String _date, String _size, String _path) {
        super(_icon, _name, _date, _size);
        FilePath = _path;
    }

    public SearchedFileProperty(Parcel in) {
        readFromParcel(in);
    }

    public String getPath() {
        return FilePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(super.getIcon());
        dest.writeString(super.getName());
        dest.writeString(FilePath);
        dest.writeString(super.getDate());
        dest.writeString(super.getSize());
    }

    public void readFromParcel(Parcel in) {
        super.setIcon(in.readString());
        super.setName(in.readString());
        FilePath = in.readString();
        super.setDate(in.readString());
        super.setSize(in.readString());
    }
}