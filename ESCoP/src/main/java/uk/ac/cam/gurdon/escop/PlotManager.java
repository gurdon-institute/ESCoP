package uk.ac.cam.gurdon.escop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import uk.ac.cam.gurdon.kgui.KButton;
import uk.ac.cam.gurdon.kgui.KPanel;


public class PlotManager extends JFrame implements ActionListener{

	private static final long serialVersionUID = 2330418802595468638L;
	private static final Dimension DIM = new Dimension(800, 800);
	
	ArrayList<PlotHolder> plotHolders;
	private LayoutPanel layoutPanel;
	private KPanel controlPanel;
	
	private KButton moveTool, resizeTool;
	boolean doMove, doResize;
	
	private KButton autoButton, captureButton;
	
	
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
			
			return dim;
		}
		
	}
	
	public PlotManager(){
		super("Plot Manager");
		setIconImage( Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo_icon.gif")) );
		setLayout(new BorderLayout());
		
		layoutPanel = new LayoutPanel();
		layoutPanel.setBackground(Color.WHITE);

		JPanel main = new JPanel();
		main.add(layoutPanel);
		main.add(Box.createGlue());
		JScrollPane scroll = new JScrollPane(main, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll, BorderLayout.CENTER);
		
		moveTool = new KButton("Move", this);
		moveTool.setBackground(Color.GRAY);
		resizeTool = new KButton("Resize", this);
		resizeTool.setBackground(Color.GRAY);
		
		autoButton = new KButton("Auto", this);
		captureButton = new KButton("Capture", this);
		
		controlPanel = new KPanel(moveTool, resizeTool, 40, autoButton, 10, captureButton);
		add(controlPanel, BorderLayout.SOUTH);
		
		plotHolders = new ArrayList<PlotHolder>();
	}
	
	@Override
	public void actionPerformed(ActionEvent ae){
		KButton src = (KButton) ae.getSource();
		
		if(src==moveTool||src==resizeTool){
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
			
			for(PlotHolder ph:plotHolders){
				ph.cp.setDomainZoomable(!doMove&&!doResize);
				ph.cp.setRangeZoomable(!doMove&&!doResize);
			}
		}
		else if(src==autoButton){
			autoLayout();
		}
		else if(src==captureButton){
			fitPanel();
			BufferedImage bi = new BufferedImage(layoutPanel.getWidth(), layoutPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.getGraphics();
			layoutPanel.paint(g);
			ColorProcessor ip = new ColorProcessor(bi);
			new ImagePlus("ESCoP Plots", ip).show();
		}
		
	}
	
	public void autoLayout(){
		setSharedSize(PlotHolder.DEFAULT_SIZE, PlotHolder.DEFAULT_SIZE);
		Rectangle bounds = getBounds();
		int x = 0;
		int y = 0;
		for(PlotHolder ph:plotHolders){
			int w = ph.getWidth();
			int h = ph.getHeight();
			if(x>0&&x+w>bounds.width){
				x = 0;
				y += h;
			}
			ph.setLocation(x,y);
			x += w;
		}
		fitPanel();
	}
	
	void fitPanel(){
		Dimension dim = layoutPanel.getPreferredSize();
		layoutPanel.setSize(dim);
		validate();
		repaint();
	}
	
	public void arrange(){
		for(PlotHolder ph:plotHolders){
			int x = ph.getX();
			int y = ph.getY();
			int w = ph.getWidth();
			int h = ph.getHeight();
			
			if(w==0) w = PlotHolder.DEFAULT_SIZE;
			if(h==0) h = PlotHolder.DEFAULT_SIZE;
			
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
						ok = false;
					}
				}
			}while(!ok);

			ph.setLocation(x,y);
			
		}
		validate();
	}
	
	public Dimension getPreferredSize(){

		if(plotHolders==null||plotHolders.size()==0) return DIM;
		
		int n = (int) Math.round(Math.sqrt(plotHolders.size()));
		int w = plotHolders.get(0).getWidth();
		int h = plotHolders.get(0).getHeight();
		Dimension dim = new Dimension(n*w+50, n*h+90);
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if(getX()+dim.width>screen.width) dim.width = screen.width-getX();
		if(getY()+dim.height>screen.height) dim.height = screen.height-getY();
		return dim;
	}
	
	public void setSharedSize(int w, int h){
		if(w>=100&&h>=100){
			for(PlotHolder ph:plotHolders){
				ph.setSize(w, h);
				ph.cp.setSize(w-(2*ph.edge), h-(2*ph.edge)-ph.nameLabel.getHeight());
				
			//ph.topPanel.setSize(w,topPanel.getHeight());	//FIXME: increase height to display text in nameLabel when shrunk
			ph.nameLabel.setSize(w-ph.closeButton.getWidth(),ph.topPanel.getHeight());
				
				ph.dim = new Dimension(w,h);
			}
		}
		fitPanel();
	}
	
	public void addPlot(JFreeChart plot, String name){
		try{
			PlotHolder pt = new PlotHolder(plot, name, this);
			plotHolders.add(pt);
			layoutPanel.add(pt);
			
			
			setSharedSize(plotHolders.get(0).getWidth(), plotHolders.get(0).getHeight());
			
			autoLayout();
			pack();
			display();
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
	public void removePlot(PlotHolder ph){
		try{
			plotHolders.remove(ph);
			layoutPanel.remove(ph);
			autoLayout();
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
	public void display(){
		try{

			layoutPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));
			pack();

			Dimension dim = getPreferredSize();
			if(dim.width<PlotHolder.DEFAULT_SIZE){
				dim.width = PlotHolder.DEFAULT_SIZE;
			}
			if(dim.height<PlotHolder.DEFAULT_SIZE){
				dim.height = PlotHolder.DEFAULT_SIZE;
			}
			setSize(dim);
			
			if(!isVisible()){ 
				setLocationRelativeTo(null);
			}
			layoutPanel.setLayout(null);
			
			setVisible(true);	//validates when already visible
			
			autoLayout();
			
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}
	
}
