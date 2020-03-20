package uk.ac.cam.gurdon.escop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.JFreeChart;

import uk.ac.cam.gurdon.kgui.KPanel;

public class PlotManager extends JFrame{

	private static final long serialVersionUID = 2330418802595468638L;
	private static final Dimension DIM = new Dimension(800, 800);
	
	private ArrayList<PlotThumb> plotThumbs;
	private JPanel thumbPanel;
	private KPanel controlPanel;
	
	
	public PlotManager(){
		super("Plot Manager");
		setLayout(new BorderLayout());
		
		JPanel layout = new JPanel();
		add(layout, BorderLayout.CENTER);
		
		thumbPanel = new JPanel();
		thumbPanel.setLayout(new BoxLayout(thumbPanel, BoxLayout.Y_AXIS));
		JScrollPane scroll = new JScrollPane(thumbPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll, BorderLayout.EAST);
				
		controlPanel = new KPanel("controls go here");
		add(controlPanel, BorderLayout.SOUTH);
		
		plotThumbs = new ArrayList<PlotThumb>();
	}
	
	@Override
	public Dimension getPreferredSize(){
		return DIM;
	}
	
	public void add(JFreeChart plot, String name){
		PlotThumb pt = new PlotThumb(plot, name);
		plotThumbs.add(pt);
		thumbPanel.add(pt);
		pack();
		repaint();
	}
	
	public void display(){
		try{
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
}
