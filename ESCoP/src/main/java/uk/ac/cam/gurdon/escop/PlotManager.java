package uk.ac.cam.gurdon.escop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;

import uk.ac.cam.gurdon.kgui.KButton;
import uk.ac.cam.gurdon.kgui.KPanel;


public class PlotManager extends JFrame{

	private static final long serialVersionUID = 2330418802595468638L;
	private static final Dimension DIM = new Dimension(800, 800);
	
	ArrayList<PlotHolder> plotHolders;
	private LayoutPanel layoutPanel;
	private KPanel controlPanel;
	private KButton moveTool, resizeTool;
	
	boolean doMove, doResize;
	
	
	private class LayoutPanel extends JPanel{
		private static final long serialVersionUID = 8825763395757344373L;

		private LayoutPanel(){
			super();
		}
		
		public Dimension getPreferredSize(){
			Dimension dim = new Dimension(400,400);
			for(PlotHolder ph:plotHolders){
				dim.width = Math.max(dim.width, ph.getX()+ph.getWidth());
				dim.height = Math.max(dim.height, ph.getY()+ph.getHeight());
			}

		//	dim.width += 50;
		//	dim.height += 50;
			
			return dim;
		}
		
	}
	
	public PlotManager(){
		super("Plot Manager");
		setLayout(new BorderLayout());
		
		layoutPanel = new LayoutPanel();

		JScrollPane scroll = new JScrollPane(layoutPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll, BorderLayout.CENTER);
		
		ActionListener toolButtonListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				KButton src = (KButton) ae.getSource();
				if(src==moveTool){
					doMove = !doMove;
					doResize = false;
				}
				else if(src==resizeTool){
					doResize = !doResize;
					doMove = false;
				}
				
				moveTool.setBackground(doMove?Color.GREEN:Color.GRAY);
				resizeTool.setBackground(doResize?Color.GREEN:Color.GRAY);
			}
		};
		
		moveTool = new KButton("Move", toolButtonListener);
		moveTool.setBackground(Color.GRAY);
		resizeTool = new KButton("Resize", toolButtonListener);
		resizeTool.setBackground(Color.GRAY);
		controlPanel = new KPanel(moveTool, 20, resizeTool);
		add(controlPanel, BorderLayout.SOUTH);
		
		plotHolders = new ArrayList<PlotHolder>();
		
		/*addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent ce){
				arrange();
			}
		});*/
	}
	
	public void arrange(){
		
		//Collections.sort(plotHolders);
		
		for(PlotHolder ph:plotHolders){
			int x = ph.getX();
			int y = ph.getY();
			int w = ph.getWidth();
			int h = ph.getHeight();

			int modX = x%w;
			int modY = y%h;
			x -= modX;
			y -= modY;
			if(modX>w/2f) x += w;
			if(modY>h/2f) y += h;
			
			boolean ok;
			do{
				ok = true;
				for(PlotHolder check:plotHolders){
					if(check==ph) continue;
					if( check.getX()==x && check.getY()==y ){
						x+=w;
						//y+=h;
						ok = false;
					}
				}
			}while(!ok);

			ph.setLocation(x,y);
			
		}
		repaint();
	}
	
	public Dimension getPreferredSize(){

		if(plotHolders==null||plotHolders.size()==0) return DIM;
		
		int n = (int) Math.round(Math.sqrt(plotHolders.size()));
		int w = plotHolders.get(0).getWidth();
		int h = plotHolders.get(0).getHeight();
		Dimension dim = new Dimension(n*w+40, n*h+80);
		
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
			layoutPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
			layoutPanel.setLayout(null);
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
}
