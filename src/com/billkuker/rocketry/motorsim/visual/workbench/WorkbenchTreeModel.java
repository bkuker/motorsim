package com.billkuker.rocketry.motorsim.visual.workbench;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.billkuker.rocketry.motorsim.ChangeListening;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;

public class WorkbenchTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;

	public class MultiGrainNode extends PartNode{
		private static final long serialVersionUID = 1L;
		public MultiGrainNode(MultiGrain part) {
			super(part);
			setAllowsChildren(true);
			add(new PartNode(part.getGrain()));
		}
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if ( e.getPropertyName().equals("Grain")){
				remove(0);
				add(new PartNode(((MultiGrain)getUserObject()).getGrain()));
				nodesChanged(this, new int[]{0});
			}
			super.propertyChange(e);
		}
	}

	public class PartNode extends DefaultMutableTreeNode implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;

		public PartNode(Object part) {
			super(part, false);
			if (part instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) part).addPropertyChangeListener(this);
			}
		}
	
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			nodeChanged(this);
		}
	
	}
	
	public class MotorNode extends PartNode implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		Motor motor;
		PartNode cn, nn, gn, fn;

		public MotorNode(Motor m) {
			super(m);
			setAllowsChildren(true);
			motor = m;
			add( cn = new PartNode(m.getChamber()));
			add( nn = new PartNode(m.getNozzle()));
			if ( m.getGrain() instanceof MultiGrain ){
				gn = new MultiGrainNode(((MultiGrain)m.getGrain()));
			} else {
				gn = new PartNode(m.getGrain());
			}
			add(gn);
			add( fn = new PartNode(m.getFuel()));
			if (m instanceof ChangeListening.Subject) {
				((ChangeListening.Subject) m).addPropertyChangeListener(this);
			}
		}
		
		@Override
		public Motor getUserObject(){
			return (Motor)super.getUserObject();
		}

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if ( e.getPropertyName().equals("Fuel")){
				fn = new PartNode(motor.getFuel());
				remove(3);
				add(fn);
				nodesChanged(this, new int[]{3});
			} else {
				nodeChanged(this);
			}
			super.propertyChange(e);
		}

	}

	public WorkbenchTreeModel() {
		super(new DefaultMutableTreeNode(), true);
	}
	
	@Override
	public DefaultMutableTreeNode getRoot(){
		return (DefaultMutableTreeNode)super.getRoot();
	}
	
	public void addMotor(Motor m){
		DefaultMutableTreeNode root = getRoot();
		root.add(new MotorNode(m));
		nodesWereInserted(root, new int[]{root.getChildCount()-1});
		
	}
	
	@SuppressWarnings("unchecked")
	public void removeMotor(Motor m){
		Enumeration<TreeNode> e = getRoot().children();
		while ( e.hasMoreElements() ){
			TreeNode n = e.nextElement();
			if ( n instanceof MotorNode ){
				if ( ((MotorNode)n).getUserObject() == m ){
					removeNodeFromParent((MotorNode)n);
				}
			}
		}
	}

}
