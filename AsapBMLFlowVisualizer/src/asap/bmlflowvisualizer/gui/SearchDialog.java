package asap.bmlflowvisualizer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import asap.bmlflowvisualizer.BMLFlowVisualizerPort;
import asap.bmlflowvisualizer.utils.BMLBlock;
import asap.bmlflowvisualizer.utils.BMLTableModel;
import asap.bmlflowvisualizer.utils.ClickListener;

public class SearchDialog extends JFrame {

	private BMLFlowVisualization visualization;
	private SearchDialog ref;
	private JTable table;
	private BMLTableModel model;
	private JTextField searchField;
	private int lastIndex;

	public SearchDialog(Map<String, BMLBlock> blocks,
			BMLFlowVisualization vis) {
		super();
		lastIndex = 0;
		this.visualization = vis;
		this.ref = this;
		this.setLayout(new BorderLayout());
		model = new BMLTableModel(blocks);
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		table.addMouseListener(new ClickListener(200) {
			@Override
			public void doubleClick(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				System.out.println("Table dclicked at row: "
						+ target.getSelectedRow());
				visualization.addPopup((String) target.getModel().getValueAt(
						target.getSelectedRow(), 0));
			}
		});

		this.add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel ctrlPanel = new JPanel();
		ctrlPanel.add(new JLabel("BML Id to find: "));
		searchField = new JTextField();
		searchField.setPreferredSize(new Dimension(150,20));
		searchField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				searchForId();
				
			}
		});
		ctrlPanel.add(searchField);
		JButton searchB = new JButton("Search");
		searchB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				searchForId();
			}
		});
		ctrlPanel.add(searchB);
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				visualization.notifySearchClose();
				ref.dispose();
			}
		});
		ctrlPanel.add(closeB);
		this.add(ctrlPanel,BorderLayout.SOUTH);
		this.setTitle("Search..");
		this.setPreferredSize(new Dimension(600, 600));
		this.pack();
		this.setVisible(true);
	}
	
	private void searchForId() {
		
		int row = model.getRowOfElement(searchField.getText(), lastIndex+1);
		if (row == -1 && lastIndex != 0) {
			lastIndex = 0;
			row = model.getRowOfElement(searchField.getText(), lastIndex);
		}
		if (row != -1) {
			lastIndex = row;
			row = table.convertRowIndexToView(row);
			table.setRowSelectionInterval(row, row);
		} else {
			table.clearSelection();
		}
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			visualization.notifySearchClose();
			this.dispose();
		}
	}

	public void update(long curTimestamp) {
		model.update(curTimestamp);
		table.revalidate();
		table.repaint();
	}
	
	public void reset() {
		table.clearSelection();
		model.reset();
	}

}
