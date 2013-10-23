/* Power File Manager / RootTools
 * 
    Copyright (C) 2013 Mu Hwan, Kim
    Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks

     This code is dual-licensed under the terms of the Apache License Version 2.0 and
    the terms of the General Public License (GPL) Version 2.
    You may use this code according to either of these licenses as is most appropriate
    for your project on a case-by-case basis.

    The terms of each license can be found in the root directory of this project's repository as well as at:

 * http://www.apache.org/licenses/LICENSE-2.0
 * http://www.gnu.org/licenses/gpl-2.0.txt
 
    Unless required by applicable law or agreed to in writing, software
    distributed under these Licenses is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See each License for the specific language governing permissions and
    limitations under that License.
 */

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

	public void setIntPerm(String _intperm) {
		intPerm = _intperm;
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