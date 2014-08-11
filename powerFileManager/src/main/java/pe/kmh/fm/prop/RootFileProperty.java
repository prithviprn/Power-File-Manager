package pe.kmh.fm.prop;

public class RootFileProperty extends FileProperty {

    String FilePerm;
    String intPerm;

    public RootFileProperty() {
    }

    public RootFileProperty(String _icon, String _name, String _date, String _size, String _perm) {
        super(_icon, _name, _date, _size);
        FilePerm = _perm;
        if (!FilePerm.equals("")) intPerm = calcPerm(FilePerm);
    }

    public String getPerm() {
        return FilePerm;
    }

    public void setPerm(String _perm) {
        FilePerm = _perm;
    }

    public String getIntPerm() {
        return intPerm;
    }

    public String calcPerm(String perm) {
        if (perm.length() < 9) return String.format("%s [ERR]", FilePerm);
        int ret = 0;
        if (perm.charAt(0) == 'r') ret += 400;
        if (perm.charAt(1) == 'w') ret += 200;
        if (perm.charAt(2) == 'x') ret += 100;
        if (perm.charAt(3) == 'r') ret += 40;
        if (perm.charAt(4) == 'w') ret += 20;
        if (perm.charAt(5) == 'x') ret += 10;
        if (perm.charAt(6) == 'r') ret += 4;
        if (perm.charAt(7) == 'w') ret += 2;
        if (perm.charAt(8) == 'x') ret += 1;

        return String.format("%s [%03d]", FilePerm, ret);
    }
}