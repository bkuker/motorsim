package com.billkuker.rocketry.motorsim.visual.workbench;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.MotorPart;
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
			if (part instanceof MotorPart) {
				((MotorPart) part).addPropertyChangeListener(this);
			}
		}
	
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			nodeChanged(this);
		}
	
	}
	
	public class MotorNode extends DefaultMutableTreeNode implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		Motor motor;
		PartNode cn, nn, gn, fn;

		public MotorNode(Motor m) {
			super(m);
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
			if (m instanceof MotorPart) {
				((MotorPart) m).addPropertyChangeListener(this);
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
			}
		}

	}

	public WorkbenchTreeModel() {
		super(new DefaultMutableTreeNode(), true);
	}
	
	public void addMotor(Motor m){
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
		root.add(new MotorNode(m));
		nodesWereInserted(root, new int[]{root.getChildCount()-1});
		
	}

}
