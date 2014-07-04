package pe.kmh.fm.prop;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchedRootFileProperty extends RootFileProperty implements Parcelable {

    public static final Parcelable.Creator<SearchedRootFileProperty> CREATOR = new Parcelable.Creator<SearchedRootFileProperty>() {

        @Override
        public SearchedRootFileProperty createFromParcel(Parcel source) {
            return new SearchedRootFileProperty(source);
        }

        @Override
        public SearchedRootFileProperty[] newArray(int size) {
            return new SearchedRootFileProperty[size];
        }

    };
    private String FilePath;

    public SearchedRootFileProperty(String _icon, String _name, String _date, String _size, String _perm, String _path) {
        super(_icon, _name, _date, _size, _perm);
        FilePath = _path;
    }

    public SearchedRootFileProperty(Parcel in) {
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
        dest.writeString(super.getPerm());
    }

    public void readFromParcel(Parcel in) {
        super.setIcon(in.readString());
        super.setName(in.readString());
        FilePath = in.readString();
        super.setDate(in.readString());
        super.setSize(in.readString());
        super.setPerm(in.readString());
    }
}