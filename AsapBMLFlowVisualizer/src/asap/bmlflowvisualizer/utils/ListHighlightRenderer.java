package asap.bmlflowvisualizer.utils;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
/**
 * Custom highlight renderer to highlight the current status in the block lists.
 * @author jpoeppel
 *
 * @param <E> The type of the list content
 */
@SuppressWarnings("serial")
public class ListHighlightRenderer<E> extends JLabel implements
		ListCellRenderer<E> {

	private int indexToHighlight = -1;

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list,
			E value, int index, boolean isSelected, boolean cellHasFocus) {
		this.setText(value.toString());
		this.setOpaque(true);
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setForeground(list.getForeground());
			if (index == indexToHighlight) {
				this.setBackground(Color.YELLOW);
			} else {
				setBackground(list.getBackground());
			}
		}

		return this;
	}

	public void setIndexToHighlight(int index) {
		indexToHighlight = index;
	}

}
