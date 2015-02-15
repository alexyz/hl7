package hu.ui;

import hu.*;

import java.util.List;

import javax.swing.table.AbstractTableModel;

class MsgPathTableModel extends AbstractTableModel {
	
	private final List<MsgPath> list;
	
	public MsgPathTableModel (List<MsgPath> list) {
		this.list = list;
	}
	
	@Override
	public int getRowCount () {
		return list.size();
	}
	
	@Override
	public int getColumnCount () {
		return 2;
	}
	
	@Override
	public String getColumnName (int col) {
		switch (col) {
			case 0:
				return "Path";
			case 1:
				return "Value";
			default:
				throw new RuntimeException();
		}
	}
	
	@Override
	public Object getValueAt (int row, int col) {
		final MsgPath p = list.get(row);
		switch (col) {
			case 0:
				return p.path;
			case 1:
				return p.value;
			default:
				throw new RuntimeException();
		}
	}
	
}