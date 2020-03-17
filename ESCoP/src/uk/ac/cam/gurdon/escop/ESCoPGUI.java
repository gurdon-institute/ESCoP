package uk.ac.cam.gurdon.escop;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import uk.ac.cam.gurdon.kgui.KButton;
import uk.ac.cam.gurdon.kgui.KLabel;
import uk.ac.cam.gurdon.kgui.KPanel;
import uk.ac.cam.gurdon.kgui.KSpinner;

public class ESCoPGUI extends JFrame implements ActionListener{
	private static final long serialVersionUID = -9073891634730000235L;

	private ESCoP escop;
	private JComboBox<Integer> comboCA, comboCB;
	private JComboBox<CorrelationCalculator.Method> methodCombo;
	private KSpinner offsetSpinner, itSpinner, thresholdASpinner, thresholdBSpinner;
	private JCheckBox scatterplotTick;
	private KButton okButton, cancelButton, helpButton;
	private KLabel statusLabel;
	
	
	public ESCoPGUI(ESCoP escop){
		super("ESCoP");
		this.escop = escop;
		setIconImage( Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo_icon.gif")) );
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		final Integer[] channels = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12};
		
		comboCA = new JComboBox<Integer>( channels );
		comboCA.setSelectedItem(Prefs.getInt("ESCoP.CA", 1));
		comboCB = new JComboBox<Integer>( channels );
		comboCB.setSelectedItem(Prefs.getInt("ESCoP.CB", 2));
		
		methodCombo = new JComboBox<CorrelationCalculator.Method>(CorrelationCalculator.Method.values());
		methodCombo.setSelectedIndex(Prefs.getInt("ESCoP.methodi", 0));
		
		offsetSpinner = new KSpinner(Prefs.get("ESCoP.maxOffset", 20.0), 0, 1000.0, 1.0);
		itSpinner = new KSpinner(Prefs.getInt("ESCoP.its", 20), 0, 1000, 1);
		thresholdASpinner = new KSpinner(Prefs.getInt("ESCoP.thresholdA", 0), 0, Integer.MAX_VALUE, 1);
		thresholdBSpinner = new KSpinner(Prefs.getInt("ESCoP.thresholdB", 0), 0, Integer.MAX_VALUE, 1);
		scatterplotTick = new JCheckBox("Scatter Plot", Prefs.get("ESCoP.scatterPlot", false));
		
		okButton = new KButton("OK", this);
		cancelButton = new KButton("Cancel", this);
		helpButton = new KButton("Help", this);
		
		statusLabel = new KLabel();
		
		add( new KPanel("Channel A:", comboCA, 10, "B:", comboCB) );
		add( new KPanel("Threshold A:", thresholdASpinner, 10, "B:", thresholdBSpinner) );
		add( new KPanel("Correlation Method", methodCombo) );
		add( new KPanel("Maximum Offset (calibrated units)", offsetSpinner) );
		add( new KPanel("Costes Randomisation Iterations", itSpinner) );
		add( new KPanel(scatterplotTick) );
		add( new KPanel(helpButton, 30, okButton, cancelButton) );
		KPanel statusPanel = new KPanel(statusLabel);
		statusPanel.setBackground(getBackground().darker());
		add(statusPanel);
		
	}
	
	public void display(){
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	void setStatus(String txt){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					statusLabel.setText(txt);
				}
			}
		);
	}
	
	@Override
	public void setEnabled(boolean en){
		okButton.setEnabled(en);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		if(src==cancelButton){
			escop.cancel();
			setEnabled(true);
		}
		else if(src==helpButton){
			HelpFrame.display(this);
		}
		else if(src==okButton){

			ImagePlus imp = IJ.getImage();
			int CA = (int) comboCA.getSelectedItem();
			int CB = (int) comboCB.getSelectedItem();
			CorrelationCalculator.Method method = (CorrelationCalculator.Method) methodCombo.getSelectedItem();
			int methodi = methodCombo.getSelectedIndex();
			int threshA = (int) thresholdASpinner.getValue();
			int threshB = (int) thresholdBSpinner.getValue();
			double maxOffset = (double) offsetSpinner.getValue();
			int its = (int) itSpinner.getValue();
			boolean doScatter = scatterplotTick.isSelected();
			
			int C = imp.getNChannels();
			if(CA>C||CB>C){
				JOptionPane.showMessageDialog(this, "No Channel "+Math.max(CA,CB)+" in "+imp.getTitle(), "Invalid Channel", JOptionPane.ERROR);
				return;
			}
			if(CA==CB){
				int op = JOptionPane.showConfirmDialog(this, "Channels A and B are the same.\nCalculate autocorrelation?", "Same Channel", JOptionPane.YES_NO_OPTION);
				if(op!=JOptionPane.YES_OPTION){
					return;
				}
			}
			
			Prefs.set("ESCoP.CA", CA);
			Prefs.set("ESCoP.CB", CB);
			Prefs.set("ESCoP.methodi", methodi);
			Prefs.set("ESCoP.maxOffset", maxOffset);
			Prefs.set("ESCoP.its", its);
			Prefs.set("ESCoP.thresholdA", threshA);
			Prefs.set("ESCoP.thresholdB", threshB);
			Prefs.set("ESCoP.scatterPlot", doScatter);
			
			setEnabled(false);
			SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>(){
				public Object doInBackground(){
					try{
						escop.run(imp, CA, CB, method, threshA, threshB, maxOffset, its, doScatter);
						setEnabled(true);
					}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
					return null;
				}
			};
			worker.execute();
			
		}
	}

}
