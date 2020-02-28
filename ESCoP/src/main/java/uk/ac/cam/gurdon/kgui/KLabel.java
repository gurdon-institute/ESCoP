package uk.ac.cam.gurdon.kgui;

import java.awt.Dimension;

import javax.swing.JLabel;


/** A label taking up a fixed area to avoid moving other components when the text is changed
 * */
public class KLabel extends JLabel {
	private static final long serialVersionUID = 3852211439645177903L;
	
	private Dimension size = new Dimension(200,16);
	
	public KLabel(String txt, int w) {
		super(txt);
		size.width = w;
	}
	public KLabel(String txt) {
		new KLabel(txt, 200);
	}
	public KLabel() {
		new KLabel("",200);
	}

	@Override
	public Dimension getPreferredSize(){
		return size;
	}
}
