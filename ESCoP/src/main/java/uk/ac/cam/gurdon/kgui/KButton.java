package uk.ac.cam.gurdon.kgui;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class KButton extends JButton{
	private static final long serialVersionUID = 738214905559432490L;

	private Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	private Insets INSETS = new Insets(0,0,0,0);
	
	public KButton(String label, ActionListener listen){
		super(label);
		setFont(FONT);
		setMargin(INSETS);
		addActionListener(listen);
	}
	
}