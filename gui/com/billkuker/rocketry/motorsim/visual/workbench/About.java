package com.billkuker.rocketry.motorsim.visual.workbench;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class About extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JFrame f;

	private final static URI webpage;
	static {
		try {
			webpage = new URI(
					"http://content.billkuker.com/projects/rocketry/software");
		} catch (URISyntaxException e) {
			throw new Error(e);
		}
	}

	public About(final JFrame f) {
		super(f, "About " + MotorWorkbench.name, true);
		this.f = f;
		setSize(400, 250);

		setIconImage(f.getIconImage());

		setLayout(new BorderLayout());
		add(new JLabel("<html>" + MotorWorkbench.name
				+ " &copy;2010 Bill Kuker</html>"), BorderLayout.NORTH);
		JTextArea text;
		add(new JScrollPane(text = new JTextArea()), BorderLayout.CENTER);
		text.setEditable(false);
		StringBuffer lic = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass()
				.getResourceAsStream("license.txt")));
		String line;
		try {
			while ((line = in.readLine()) != null) {
				lic.append(line);
				lic.append("\n");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		text.setText(lic.toString());
		JLabel link = new JLabel(
				"<html><u>Visit MotorSim on the Web</u></html>");
		link.setForeground(Color.BLUE);
		add(link, BorderLayout.SOUTH);
		link.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(webpage);
					} catch (IOException x) {

					}
				} else {

				}

			}
		});
	}

	@Override
	public void setVisible(boolean v) {
		int x = f.getLocation().x + f.getWidth() / 2 - getWidth() / 2;
		int y = f.getLocation().y + f.getHeight() / 2 - getHeight() / 2;
		setLocation(x, y);
		super.setVisible(v);
	}

	public static void main(String args[]) {
		JFrame f = new JFrame();
		f.setSize(1024, 768);
		f.setVisible(true);
		final About s = new About(f);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				s.setVisible(true);

			}
		});

	}
}
