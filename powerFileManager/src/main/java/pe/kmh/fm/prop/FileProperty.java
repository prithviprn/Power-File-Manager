package pe.kmh.fm.prop;

import android.view.View;

public class FileProperty {

    private String FileIcon; // Extension
    private String FileName; // Name
    private String FileDate; // Date
    private String FileSize; // Size
    private int checked = View.INVISIBLE;

    public FileProperty() {
    }

    public FileProperty(String _icon, String _name, String _date, String _size) {
        FileIcon = _icon;
        FileName = _name;
        FileDate = _date;
        FileSize = _size;
    }

    public String getIcon() {
        return FileIcon;
    }

    public void setIcon(String _icon) {
        FileIcon = _icon;
    }

    public String getName() {
        return FileName;
    }

    public void setName(String _name) {
        FileName = _name;
    }

    public String getDate() {
        return FileDate;
    }

    public void setDate(String _date) {
        FileDate = _date;
    }

    public String getSize() {
        return FileSize;
    }

    public void setSize(String _size) {
        FileSize = _size;
    }

    public int getChecked() {
        return checked;
    }

    public void setChecked(int _checked) {
        checked = _checked;
    }
}