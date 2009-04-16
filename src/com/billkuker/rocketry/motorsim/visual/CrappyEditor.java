package com.billkuker.rocketry.motorsim.visual;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import com.billkuker.rocketry.motorsim.Burn;
import com.billkuker.rocketry.motorsim.io.XStreamMotorIO;
import com.billkuker.rocketry.motorsim.motors.kuker.PVC9;

public class CrappyEditor extends JFrame {
	JTabbedPane tabs = new JTabbedPane();
	JPanel editor = new JPanel();
	JTextArea text = new JTextArea();
	public CrappyEditor(){
		setSize(1024, 768);
		
		setContentPane(tabs);
		
		try {
			text.setText(XStreamMotorIO.writeMotor(new PVC9()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		editor.setLayout(new BorderLayout());
		editor.add(text, BorderLayout.CENTER);
		editor.add(new JButton("Burn!"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						burn();
					}
				});
			}
			
		}, BorderLayout.SOUTH);
		
		tabs.addTab("Edit", editor);
		
		
	}
	
	private void burn(){
		try {
			Burn b = new Burn(XStreamMotorIO.readMotor(text.getText()));
			tabs.addTab("Burn", new BurnPanel(b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		new CrappyEditor().show();
	}
}
