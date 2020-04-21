package uk.ac.cam.gurdon.escop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import uk.ac.cam.gurdon.kgui.KButton;
import uk.ac.cam.gurdon.kgui.KPanel;

public class PlotHolder extends JPanel implements MouseListener, MouseMotionListener, Comparable<PlotHolder>{
	
	private static final long serialVersionUID = 1542426845993960902L;
	public static final int DEFAULT_SIZE = 400;
	
	private PlotManager manager;
	private JFreeChart plot;
	private String name;
	private Dimension dim;
	
	private JPanel topPanel;
	private JTextArea nameLabel;
	private KButton closeButton;
	ChartPanel cp;

	private boolean resizing = false;
	private boolean moving = false;
    private Point dragStart  = new Point();
	int edge = 8;
		

	public PlotHolder(JFreeChart plot, String name, PlotManager manager){
		this.plot = plot;
		this.name = name;
		this.manager = manager;

		setLayout(new BorderLayout());
		
		closeButton = new KButton("x", new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(JOptionPane.showConfirmDialog(null, "Remove "+name+"?", "Remove Plot?", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
					manager.removePlot(PlotHolder.this);
				}
			}
		});
		
		nameLabel = new JTextArea(name);
		nameLabel.setBackground(Color.LIGHT_GRAY);
		nameLabel.setLineWrap(true);
		nameLabel.setEditable(false);
		
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.setBackground(Color.LIGHT_GRAY);
		topPanel.add(closeButton);
		topPanel.add(Box.createHorizontalStrut(5));
		topPanel.add(nameLabel);
		add(topPanel, BorderLayout.NORTH);
		
		cp = new ChartPanel(plot);
		add(cp, BorderLayout.CENTER);
		
		dim = new Dimension(DEFAULT_SIZE, DEFAULT_SIZE);
		
		//setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

		addMouseListener(this);
		addMouseMotionListener(this);
		
		cp.addMouseListener(this);
		cp.addMouseMotionListener(this);
	}
	
	public void setSharedSize(int w, int h){
		if(w>=100&&h>=100){
			for(PlotHolder ph:manager.plotHolders){
				ph.setSize(w, h);
				ph.cp.setSize(w-(2*edge), h-(2*edge)-nameLabel.getHeight());
			ph.topPanel.setSize(w,topPanel.getHeight());	//FIXME: increase height to display text in nameLabel when shrunk
				ph.dim = new Dimension(w,h);
			}
		}
	}
	
	public Dimension getPreferredSize(){
		return dim;
	}
	
	@Override
	public void mouseClicked(MouseEvent me){
		int n = me.getClickCount();
		if(SwingUtilities.isLeftMouseButton(me) && n>=2){
			ChartFrame frame = new ChartFrame(name, plot);
	        frame.pack();
			frame.setSize( new Dimension(800, 800) );
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
	}
	@Override
	public void mouseMoved(MouseEvent me){
		Point p = me.getPoint();
		
		if(manager.doResize){
			setCursor( Cursor.getPredefinedCursor( Cursor.SE_RESIZE_CURSOR) );
		}
		else if(manager.doMove){
			setCursor( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR) );
		}
		else{
			setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR) );
		}

	}
	@Override
    public void mousePressed(MouseEvent me) {
		if(manager.doResize){
	        resizing = true;
	        moving = false;
	        dragStart = me.getPoint();
		}
		else if(manager.doMove){
	        resizing = false;
	        moving = true;
	        dragStart = me.getPoint();
		}
    }
	 @Override
     public void mouseReleased(MouseEvent e) {
         resizing = false;
         moving = false;
         manager.arrange();
        // manager.pack();
     }
	public void mouseDragged(MouseEvent me) {
        if (manager.doResize&&resizing) {
        	Point p = me.getPoint();
        	Rectangle bounds = getBounds();
            setSharedSize((int)(bounds.width+(p.x-dragStart.x)), (int)(bounds.height+(p.y-dragStart.y)));
            dragStart = p;
            manager.fitPanel();
        }
        else if(manager.doMove&&moving){
        	Point p = me.getPoint();
        	Rectangle bounds = getBounds();
            setLocation(bounds.x+p.x-dragStart.x, bounds.y+p.y-dragStart.y);
            manager.fitPanel();
        }
    }

	@Override
	public void mouseEntered(MouseEvent me) {}

	@Override
	public void mouseExited(MouseEvent me) {
		setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) );
	}

	
	@Override
	public int compareTo(PlotHolder other) {
		if(getX()!=other.getX()) return new Integer(getX()).compareTo(other.getX());
		else if(getY()!=other.getY()) return new Integer(getY()).compareTo(other.getY());
		else return 0;
	}
	
}
