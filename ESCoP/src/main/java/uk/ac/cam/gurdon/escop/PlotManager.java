package uk.ac.cam.gurdon.escop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;

import uk.ac.cam.gurdon.kgui.KPanel;


public class PlotManager extends JFrame{

	private static final long serialVersionUID = 2330418802595468638L;
	private static final Dimension DIM = new Dimension(800, 800);
	
	ArrayList<PlotHolder> plotHolders;
	private LayoutPanel layoutPanel;
	private KPanel controlPanel;
	
	
	private class LayoutPanel extends JPanel{
		private static final long serialVersionUID = 8825763395757344373L;

		private LayoutPanel(){
			super();
			setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));
		}
		
		
		
	}
	
	public PlotManager(){
		super("Plot Manager");
		setLayout(new BorderLayout());
		
		layoutPanel = new LayoutPanel();

		JScrollPane scroll = new JScrollPane(layoutPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll, BorderLayout.CENTER);
				
		controlPanel = new KPanel("controls go here");
		add(controlPanel, BorderLayout.SOUTH);
		
		plotHolders = new ArrayList<PlotHolder>();
	}
	
	public Dimension getPreferredSize(){
		Dimension dim = new Dimension(400,400);
		for(PlotHolder ph:plotHolders){
			dim.width = Math.max(dim.width, ph.getX()+ph.getWidth()+200);
			dim.height = Math.max(dim.height, ph.getY()+ph.getHeight()+200);
		}
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if(getX()+dim.width>screen.width) dim.width = screen.width-getX();
		if(getY()+dim.height>screen.height) dim.height = screen.height-getY();
		return dim;
	}
	
	public void addPlot(JFreeChart plot, String name){
		try{
			PlotHolder pt = new PlotHolder(plot, name, this);
			plotHolders.add(pt);
			layoutPanel.add(pt);
			pack();
			repaint();
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
	public void display(){
		try{
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
}
