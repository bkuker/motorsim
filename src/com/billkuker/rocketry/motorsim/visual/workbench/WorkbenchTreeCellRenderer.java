package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.billkuker.rocketry.motorsim.Motor;

public class WorkbenchTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		if (value instanceof DefaultMutableTreeNode) {
			value = ((DefaultMutableTreeNode) value).getUserObject();
		}
		if (value instanceof Motor) {
			setText(((Motor) value).getName());
		} else if ( value == null ) {
			setText("");
		} else {
			setText(value.getClass().getSimpleName());
		}

		return this;
	}
}
