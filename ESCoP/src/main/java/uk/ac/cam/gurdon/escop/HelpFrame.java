package uk.ac.cam.gurdon.escop;

import java.awt.Desktop;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HelpFrame {
	
	private static JFrame frame;
	private static String help =
	"<html>" 
	+ "<body style=\"font-family:arial,sans-serif;font-size:14pt;font-weight:normal;width:800px;background-color:white;padding:10px;\">" 
	+ "<h2>ESCoP</h2>"
	+ "<p>"
	+ "Easy Statistical Colocalisation Plot (ESCoP) uses a cross-correlation function based on Van Steensel's method <a href='http://www.ncbi.nlm.nih.gov/pubmed/8718670'>(Van Steensel et al. 1996)</a> "
	+ "to calculate intensity correlation between two channels and display the results in an intuitive, easily interpretable plot." 
	+ "Channel A is offset relative to channel B in increments of one voxel, and the intensity correlation is calculated at each position."
	+ "The principal additions to Van Steensel's method are offsets in 3 dimensions, a choice of correlation methods and the inclusion of Costes' randomisation to estimate a confidence interval"
	+ "for correlation due to chance in the same data."
	+ "</p>" 
	
	+ "<p><b>Channel A, B</b><br>"
	+ "The channels to cross-correlate. Setting the same channel for A and B allows autocorrelation to be calculated."
	+ "</p>"
	
	+ "<p><b>Threshold A, B</b><br>"
	+ "Only voxel positions with values in both channels greater than or equal to the corresponding threshold will be included."
	+ "</p>"
	
	+ "<p><b>Correlation Method</b><br>"
	+ "See <a href='https://pubmed.ncbi.nlm.nih.gov/17210054/'>Bolte and Cordelieres 2006</a> for a review of colocalisation analysis methods and JACoP, an implementation of the discussed methods. "
	+ "<br><b>Pearson's correlation coefficient - </b>the linear correlation between intensity values calculated as their covariance divided by their total variation. "
	+ "Values range from -1 (complete inverse correlation) to 1 (complete correlation)."
	+ "<br><b>Manders' overlap coefficient- </b>the ratio of the sum of channel A voxel intensities for which the intensity in channel B is greater than zero to the total intensity in channel A."
	+ "The coefficient used is M1, switching the assignments of channels A and B gives M2. <a href='https://pubmed.ncbi.nlm.nih.gov/20653013/'>Thresholds should be applied when using this method.</a>"
	+ " Values range from 0 (no co-occurance) to 1 (complete co-occurance)."
	+ "<br><b>Li's intensity correlation quotient - </b>the ratio of positive covariance values divided by the overall products minus 0.5. "
	+ "Values range from -0.5 (exclusion) to 0.5 (overlap)."
	+ "</p>"

	+ "<p><b>Maximum Offset</b><br>"
	+ "The maximum calibrated offset distance in X, Y and Z for cross-correlation. The should be at least the size of the largest expected object."
	+ "Select X, Y and Z to include offset axes on the same plot."
	+ "</p>"
		
	+ "<p><b>Costes Randomisation Iterations</b><br>"
	+ "The number of randomised tile or cube shuffling iterations to use to calculate the confidence interval for correlation due to chance (P >= 0.95)."
	+ "The block size is determined by taking the full width at half maximum of the CCF to calculate the estimated size of colocalised objects. "
	+ "Set to 0 to skip this step."
	+ "</p>"
	
	+ "<p><b>Scatter Plot</b><br>"
	+ "Displays voxel intensity values as a 2D binned scatter plot with adjustable precision. The plot can be colour coded by relative channel intensity or frequency."
	+ "</p>"
	
	+ "<p><b>Results Table</b><br>"
	+ "Displays a table of correlation coefficients at each offset, Full Width at Half Maximum (FWHM) for each dimension, and the Costes randomised confidence interval range."
	+ "</p>"
	
	+ "<p><b>Plot Manager</b><br>"
	+ "Plots are added to the plot manager after each run. They can be moved and resized for comparison, and the current layout can be captured."
	+ "Double-click a plot to open it in a separate window, click the close button in the top left to remove a plot from the plot manager."
	+ "</p>"

	+ "<br><i style='font-size:10pt;'>Copyright 2020, 2021 Richard Butler. ESCoP is released under the GNU General Public License v3.</i>"
	+ "</body></html>";

	
	public static void display(JFrame parent){
		if(frame==null){
			frame = new JFrame("ESCoP Help");
			frame.setIconImage( parent.getIconImage() );
			JEditorPane ed = new JEditorPane("text/html", help);
			ed.setEditable(false);
			ed.setCaretPosition(0);
			ed.addHyperlinkListener(new HyperlinkListener() {
			    public void hyperlinkUpdate(HyperlinkEvent hle) {
			        if(hle.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			        	try{
			        		Desktop.getDesktop().browse(hle.getURL().toURI());
			        	}catch(Exception e){System.out.println("Could not open link: "+hle.getURL());}
			        }
			    }
			});
			JScrollPane scroll = new JScrollPane(ed);
			frame.add(scroll);
		}
		frame.pack();
		frame.setLocationRelativeTo(parent);
		frame.setVisible(true);
	}
	
}
