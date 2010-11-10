package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.billkuker.rocketry.motorsim.ChangeListening;
import com.billkuker.rocketry.motorsim.Fuel;
import com.billkuker.rocketry.motorsim.Motor;
import com.billkuker.rocketry.motorsim.grain.MultiGrain;

public class WorkbenchTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;
	
	//TreeNode root = new DefaultMutableTreeNode("Root");
	DefaultMutableTreeNode motors = new DefaultMutableTreeNode("All Motors");
	DefaultMutableTreeNode fuel = new DefaultMutableTreeNode("Fuels");
	
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
	
	public class FuelNode extends DefaultMutableTreeNode{
		private static final long serialVersionUID = 1L;
		Fuel f;
		public FuelNode(Component c, Fuel f){
			super(c, false);
			this.f = f;
		}
		
		@Override
		public Component getUserObject(){
			return (Component)super.getUserObject();
		}
		
		public Fuel getFuel(){
			return f;
		}
	}
	
	public class FuelEditNode extends FuelNode {
		private static final long serialVersionUID = 1L;

		public FuelEditNode(AbstractFuelEditor sr){
			super(sr, sr.getFuel());
			sr.getFuel().addPropertyChangeListener(new PropertyChangeListener(){

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					nodeChanged(FuelEditNode.this);
				}});
		}
		
		@Override
		public AbstractFuelEditor getUserObject(){
			return (AbstractFuelEditor)super.getUserObject();
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
				nodeChanged(this);
			super.propertyChange(e);
		}

	}

	public WorkbenchTreeModel() {
		super(new DefaultMutableTreeNode("Root"), true);
		getRoot().add(motors);
		getRoot().add(fuel);
	}
	
	@Override
	public DefaultMutableTreeNode getRoot(){
		return (DefaultMutableTreeNode)super.getRoot();
	}
	
	public DefaultMutableTreeNode getMotors(){
		return motors;
	}
	
	public DefaultMutableTreeNode getFuels(){
		return fuel;
	}
	
	public void addMotor(Motor m){
		motors.add(new MotorNode(m));
		nodesWereInserted(motors, new int[]{motors.getChildCount()-1});
		
	}
	
	@SuppressWarnings("unchecked")
	public void removeMotor(Motor m){
		Enumeration<TreeNode> e = motors.children();
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
