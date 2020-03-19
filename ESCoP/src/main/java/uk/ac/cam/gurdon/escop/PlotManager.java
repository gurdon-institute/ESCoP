package uk.ac.cam.gurdon.escop;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import uk.ac.cam.gurdon.kgui.KPanel;

public class PlotManager extends JFrame{

	private static final long serialVersionUID = 2330418802595468638L;
	private static final Dimension DIM = new Dimension(800, 800);
	
	private ArrayList<JFreeChart> plots;
	private Vector<String> list;
	private JPanel cardPanel;
	private JTextPane listPane;
	private CardLayout card;
	private KPanel controlPanel;
	
	
	public PlotManager(){
		super("Plot Manager");
		setLayout(new BorderLayout());
		
		list = new Vector<String>();
		listPane = new JTextPane();
		add(listPane, BorderLayout.WEST);
		
		card = new CardLayout();
		cardPanel = new JPanel();
		cardPanel.setLayout(card);
		add(cardPanel, BorderLayout.CENTER);
		
		controlPanel = new KPanel("controls go here");
		add(controlPanel, BorderLayout.SOUTH);
		
		plots = new ArrayList<JFreeChart>();
	}
	
	private void updateList(){
		String txt = String.join("\n", list);
		listPane.setText(txt);
	}
	
	@Override
	public Dimension getPreferredSize(){
		return DIM;
	}
	
	public void add(JFreeChart plot){
		plots.add(plot);
		String name = "plot "+plots.size();
		list.add(name);
		updateList();
		
		ChartPanel cp = new ChartPanel(plot);
		cardPanel.add(cp);
		card.addLayoutComponent( cp, name );
		card.show(cardPanel, name);
	}
	
	public void display(){
		try{
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
}
