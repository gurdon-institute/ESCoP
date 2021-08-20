package uk.ac.cam.gurdon.kgui;

import java.awt.Dimension;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;

public class KSpinner extends JSpinner{
	private static final long serialVersionUID = -8997848843950051168L;

	private Dimension dim = new Dimension(60, 20);
	
	public KSpinner(int value, int min, int max, int step){
		super();
		SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
		setModel(model);
		
		NumberEditor ed = new JSpinner.NumberEditor(this, "#0.0#");
		JFormattedTextField field = (JFormattedTextField) ed.getComponent(0);
	    DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
	    formatter.setCommitsOnValidEdit(true);
		setEditor(ed);
		
		dim.width = Math.min(20 + (""+max).length()*10, 100);
	}
	
	public KSpinner(double value, double min, double max, double step){
		super();
		SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
		setModel(model);
		
		NumberEditor ed = new JSpinner.NumberEditor(this, "#0.0#");
		JFormattedTextField field = (JFormattedTextField) ed.getComponent(0);
	    DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
	    formatter.setCommitsOnValidEdit(true);
		setEditor(ed);
		
		dim.width = Math.min(20 + (""+max).length()*10, 100);
	}

	public double getDoubleValue(){
		Object val = getValue();
		if(val instanceof Integer){
			return ((Integer)val).doubleValue();
		}
		else{
			return ((Double)val).doubleValue();
		}
	}
	
	public Dimension getPreferredSize(){
		return dim;
	}
	
}