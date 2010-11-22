package com.billkuker.rocketry.motorsim.visual;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;


public abstract class MultiObjectEditor<OBJECT, EDITOR extends Component> extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(MultiObjectEditor.class);
	
	protected abstract class ObjectCreator {
		public abstract OBJECT newObject();
		public abstract String getName();
	}
	
	private final Frame frame;
	
	private final String noun;
	
	private List<ObjectCreator> creators = new Vector<ObjectCreator>();
	
	private final Map<OBJECT, EDITOR> objectToEditor = new HashMap<OBJECT, EDITOR>();
	private final Map<EDITOR, OBJECT> editorToObject = new HashMap<EDITOR, OBJECT>();
	private final Map<File, EDITOR> fileToEditor = new HashMap<File, EDITOR>();
	private final Map<EDITOR, File> editorToFile = new HashMap<EDITOR, File>();
	
	
	private final Set<OBJECT> dirty = new HashSet<OBJECT>();
	
	public MultiObjectEditor(final Frame frame, final String noun){
		this.frame = frame;
		this.noun = " " + noun.trim();
	}
	
	protected final void addCreator(ObjectCreator c){
		creators.add(c);
	}
	
	public final void dirty(final OBJECT o){
		if ( !dirty.contains(o) )
			setTitleAt(indexOfComponent(objectToEditor.get(o)), "*" + getTitleAt(indexOfComponent(objectToEditor.get(o))));
		dirty.add(o);
	}
	
	public final void dirty(final EDITOR e){
		dirty(editorToObject.get(e));
	}
	
	private final void undirty(final OBJECT o){
		if ( dirty.contains(o) )
			setTitleAt(indexOfComponent(objectToEditor.get(o)), getTitleAt(indexOfComponent(objectToEditor.get(o))).replaceAll("^\\*", ""));
		dirty.remove(o);
	}
	
	public JMenu getMenu(){
		JMenu ret = new JMenu("File");
		for ( JMenuItem i : getMenuItems() )
			ret.add(i);
		return ret;
	}
	
	private void menuNew(ObjectCreator c){
		add(c.newObject());
	}
	
	@SuppressWarnings("unchecked")
	public EDITOR getSelectedEditor(){
		try {
			return (EDITOR)super.getSelectedComponent();
		} catch ( ClassCastException e ){
			return null;
		}
	}
	
	private void close(){
		EDITOR e = getSelectedEditor();
		OBJECT o = editorToObject.get(e);
		File f = editorToFile.get(e);
		
		if ( dirty.contains(o) ){
			 int response = JOptionPane.showConfirmDialog(this, "Object is unsaved. Save Before Closing?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
			 if ( response == JOptionPane.YES_OPTION ){
				 saveDialog();
			 } else if ( response == JOptionPane.CANCEL_OPTION ){
				 return;
			 }
		}
		
		objectToEditor.remove(o);
		editorToObject.remove(e);
		fileToEditor.remove(f);
		editorToFile.remove(e);
		remove(e);
	}
	
	private void saveDialog(){
		EDITOR e = getSelectedEditor();
		if ( !editorToFile.containsKey(e) ){
			log.debug("Editor has no file, saving as...");
			saveAsDialog();
			return;
		}
		File file = editorToFile.get(e);
		log.debug("Saving to " + file.getAbsolutePath());
		try {
			saveToFile(editorToObject.get(e), file);
			undirty(editorToObject.get(e));
		} catch (IOException e1) {
			errorDialog(e1);
		}
	}
	private void saveAsDialog(){
		EDITOR e = getSelectedEditor();
		final FileDialog fd = new FileDialog(frame, "Save" + noun + " As", FileDialog.SAVE);
		fd.setVisible(true);
		if (fd.getFile() != null ) {
			File file = new File(fd.getDirectory() + fd.getFile());
			try {
				OBJECT o = editorToObject.get(e);
				saveToFile(o, file);
				undirty(o);
				objectToEditor.put(o, e);
				editorToObject.put(e, o);
				fileToEditor.put(file, e);
				editorToFile.put(e, file);
				setTitleAt(
						getSelectedIndex(),
						file.getName());
			} catch (Exception e1) {
				errorDialog(e1);
			}
		}
	}
	
	private void openDialog(){
		final FileDialog fd = new FileDialog(frame, "Open" + noun + "...", FileDialog.LOAD);
		fd.setVisible(true);
		if ( fd.getFile() != null ) {
			File file = new File(fd.getDirectory() + fd.getFile());
			log.debug("Opening File " + file.getAbsolutePath());
			if ( fileToEditor.containsKey(file) ){
				log.debug("File " + file.getAbsolutePath() + "Already open, focusing");
				setSelectedComponent(fileToEditor.get(file));
				return;
			}
			try {
				OBJECT o = loadFromFile(file);
				EDITOR e = createEditor(o);
				objectToEditor.put(o, e);
				editorToObject.put(e, o);
				fileToEditor.put(file, e);
				editorToFile.put(e, file);
				addTab(file.getName(), e);									
			} catch (Exception e) {
				errorDialog(e);
			}
		}
	}
	
	public final List<JMenuItem> getMenuItems(){
		List<JMenuItem> ret = new Vector<JMenuItem>();
		if ( creators.size() == 1 ){
			final ObjectCreator c = creators.get(0);
			ret.add(new JMenuItem("New " + c.getName()){
				private static final long serialVersionUID = 1L;
				{
					addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent ae) {
							log.debug("New");
							menuNew(c);
						}
					});
				}
			});
		} else {
			ret.add(new JMenu("New"){
				private static final long serialVersionUID = 1L;
				{
					for (final ObjectCreator c : creators ){
						add(new JMenuItem("New " + c.getName()){
							private static final long serialVersionUID = 1L;
							{
								addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent ae) {
										log.debug("New");
										menuNew(c);
									}
								});
							}
						});
					}
				}
			});
		}
		ret.add(new JMenuItem("Open" + noun + "..."){
				private static final long serialVersionUID = 1L;
				{
					addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent ae) {
							log.debug("Open...");
							openDialog();
						}
					});
				}
			});
		ret.add(new JMenuItem("Close" + noun){
			private static final long serialVersionUID = 1L;
			{
				addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ae) {
						log.debug("Close");
						close();
					}
				});
			}
		});
		ret.add(new JMenuItem("Save" + noun){
			private static final long serialVersionUID = 1L;
			{
				addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ae) {
						log.debug("Save");
						saveDialog();
					}
				});
			}
		});
		ret.add(new JMenuItem("Save" + noun + " As..."){
			private static final long serialVersionUID = 1L;
			{
				addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ae) {
						log.debug("Save As...");
						saveAsDialog();
					}
				});
			}
		});
		return ret;
	}

	protected final void add(final OBJECT o){
		EDITOR e = createEditor(o);
		objectToEditor.put(o, e);
		editorToObject.put(e, o);
		addTab("new", e);
		dirty(o);
	}
	
	public final void load(final File f) throws IOException{
		OBJECT o = loadFromFile(f);
		EDITOR e = createEditor(o);
		objectToEditor.put(o, e);
		editorToObject.put(e, o);
		fileToEditor.put(f, e);
		editorToFile.put(e, f);
		addTab(f.getName(), e);
	}
	
	public abstract EDITOR createEditor(final OBJECT o);
	
	protected abstract OBJECT loadFromFile(final File f) throws IOException;
	
	protected abstract void saveToFile(final OBJECT o, final File f) throws IOException;

	
	private final void errorDialog(final Throwable t){
		t.printStackTrace();
		JOptionPane.showMessageDialog(MultiObjectEditor.this, t.getClass().getSimpleName() + ": " + t.getMessage());
	}
}
