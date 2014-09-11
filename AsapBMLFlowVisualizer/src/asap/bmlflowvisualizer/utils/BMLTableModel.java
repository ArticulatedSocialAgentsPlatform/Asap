package asap.bmlflowvisualizer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Custom table model for the search dialog table. Allows to display the
 * required bml data and returns the correct classes for the different columns
 * for sorting. Also updates the displayed data for each new timestamp.
 * 
 * @author jpoeppel
 *
 */
public class BMLTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 575505704823276098L;
	private String[] columnNames = { "ID", "Status", "Submitted time" };;
	private ArrayList<Object[]> data;
	private Map<String, BMLBlock> blocks = Collections
			.synchronizedMap(new HashMap<String, BMLBlock>());;

	public BMLTableModel(Map<String, BMLBlock> blocks) {
		this.blocks = blocks;
		buildData();

	}

	/**
	 * Fill the displayed data.
	 */
	private void buildData() {
		data = new ArrayList<Object[]>();
		for (BMLBlock b : blocks.values()) {
			Object[] row = { b.getId(), b.getCurStatus(),
					b.getStatusTime(BMLBlockStatus.SUBMITTED) };
			data.add(row);
		}
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/**
	 * Updates the displayed data to display the status for the given time.
	 * @param timestamp The timestamp which is to be displayed.
	 */
	public void update(long timestamp) {
		if (blocks.size() != data.size()) {
			buildData();
		}
		for (int i = 0; i < data.size(); i++) {
			BMLBlock b = blocks.get(data.get(i)[0]);
			if (b != null) {
				String newValue = b.getStatusAt(timestamp);
				data.get(i)[1] = newValue;
			}
		}
	}

	public int getRowOfElement(String id, int startIndex) {

		for (int i = startIndex; i < data.size(); i++) {
			if (((String) data.get(i)[0]).startsWith(id)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex)[columnIndex];
	}

	public void reset() {
		buildData();
	}

}
