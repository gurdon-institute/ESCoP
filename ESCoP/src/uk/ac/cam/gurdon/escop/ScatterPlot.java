package uk.ac.cam.gurdon.escop;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.cam.gurdon.kgui.KLabel;
import uk.ac.cam.gurdon.kgui.KPanel;


public class ScatterPlot extends JPanel{
	private static final long serialVersionUID = -7807316698010260885L;
	private static final BasicStroke STROKE = new BasicStroke(0.5f);
	private static final int DEFAULT_NBINS = 256;
	
	private float[][] data;
	private float minX,minY,maxX,maxY;
	private JFrame frame;
	private KLabel nBinsLabel, coordLabel;
	private JButton resetButton;
	private JComboBox<Display> displayCombo;
	private Dimension dim;
	
	private int nBins;
	private int[][] counts;
	private int maxCount;
	
	private Rectangle drawRect, zoomRect;
	
	
	private enum Display{
		Channel(),Frequency(),Binary();
	}
	
	
	public ScatterPlot(float[][] data, String xLabel, String yLabel){
		this.data = data;
		
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		for(int i=0;i<data[0].length;i++){
			minX = Math.min( minX, data[0][i] );
			minY = Math.min( minY, data[1][i] );
			maxX = Math.max( maxX, data[0][i] );
			maxY = Math.max( maxY, data[1][i] );
		}
		
		
		dim = new Dimension(800,800);
		
		setN(DEFAULT_NBINS);
		
		MouseAdapter mouse = new MouseAdapter(){
			
			Point start;
			
			@Override
			public void mouseMoved(MouseEvent me){
				Point p = me.getPoint();
				Rectangle rect = getBounds();

				int xi = (int) ((p.x/(float)rect.width) * nBins);
				int yi = (int) (((p.y/(float)rect.height) * nBins));
				
				if(zoomRect!=null){
					xi = (int) (((xi/(float)nBins) * zoomRect.width) + zoomRect.x);
					yi = (int) (((yi/(float)nBins) * zoomRect.height) + zoomRect.y);
				}
				
				if(xi<0||xi>counts.length-1||yi<0||yi>counts[0].length-1){
					return;
				}
				
				int count = counts[xi][yi];
				
				int xValue = (int) (xi/(float)nBins * maxX);
				int yValue = (int) ((nBins-yi)/(float)nBins * maxY);
				coordLabel.setText(xValue+" , "+yValue+" n = "+count);
			}
			
			@Override
			public void mousePressed(MouseEvent me){
				if(SwingUtilities.isLeftMouseButton(me)){
					if(zoomRect!=null) return;	//ignore if the view is already zoomed
					start = me.getPoint();
					Rectangle bounds = getBounds();
					start.x = (int) ((start.x/(float)bounds.width) * (nBins));
					start.y = (int) ((start.y/(float)bounds.height) * (nBins));
				}
				else if(SwingUtilities.isRightMouseButton(me)){	//reset zoom
					drawRect = null;
					zoomRect = null;
					repaint();
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent me){
				if(start==null) return;
				Point p = me.getPoint();
				Rectangle bounds = getBounds();
				p.x = (int) ((p.x/(float)bounds.width) * nBins);
				p.y = (int) ((p.y/(float)bounds.height) * nBins);

				int x = Math.min(start.x, p.x);
				int y = Math.min(start.y, p.y);
				int w = Math.abs(start.x-p.x);
				int h = Math.abs(start.y-p.y);

				w = Math.max(w,h);
				h = w;
				
				if(x+w>nBins) w = nBins-x;
				if(y+h>nBins) h = nBins-y;
				
				drawRect = new Rectangle(x,y, w,h);
				repaint();
			}
			
			@Override
			public void mouseReleased(MouseEvent me){
				if(drawRect==null) return;
				
				zoomRect = (Rectangle) drawRect.clone();
				start = null;
				drawRect = null;
				
				repaint();
			}
			
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		//addMouseWheelListener(mouse);
		

		JSlider nSlider = new JSlider(16, 1024, nBins);
		nSlider.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent ce) {
				//if(nSlider.getValueIsAdjusting()) return;
				drawRect = null;
				zoomRect = null;
				int value = nSlider.getValue();
				setN(value);
			}
			
		});
		
		nBinsLabel = new KLabel(""+DEFAULT_NBINS,60);
		coordLabel = new KLabel();
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				drawRect = null;
				zoomRect = null;
				repaint();
			}
		});
		
		displayCombo = new JComboBox<Display>(Display.values());
		displayCombo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				repaint();	//paintComponent gets selected item
			}
		});
		
		frame = new JFrame("Scatter Plot");
		frame.setIconImage( Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo_icon.gif")) );
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		
		frame.add(new KLabel(yLabel,20), BorderLayout.WEST);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add( new KPanel(new KLabel(xLabel,20)) );
		bottomPanel.add( new KPanel(resetButton, 40, "Precision", nSlider, nBinsLabel, "Display", displayCombo, 40, coordLabel) );
		frame.add( bottomPanel, BorderLayout.SOUTH );

	}
	
	
	
	private int[][] getBinnedCounts(float[][] values, int nXbins, int nYbins) {		//TODO: add regression fit? subpopulation extraction?
		int[][] binned = new int[nXbins][nYbins];
		for(int i=0;i<values[0].length;i++){
			int x = (int) ((values[0][i]/maxX) * (nXbins-1));
			int y = nYbins - 1 - (int) ((values[1][i]/maxY) * (nYbins-1));
			binned[x][y]++;
		}
		return binned;
	}

	@Override
	public void paintComponent(Graphics g1d){
		super.paintComponent(g1d);
		Graphics2D g = (Graphics2D) g1d;

		Rectangle bounds = getBounds();
		
		if(nBins>bounds.width||nBins>bounds.height){	//anti-alias only when subpixel resolution is needed
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		float sx = bounds.width/(float)(nBins);
		float sy = bounds.height/(float)(nBins);

		AffineTransform trans = AffineTransform.getScaleInstance(sx, sy);
		
		if(zoomRect!=null&&zoomRect.width>1&&zoomRect.height>1){	//apply zoom, concatenate to binning scale
			trans.scale((nBins)/zoomRect.width, (nBins)/zoomRect.height);
			trans.translate(-zoomRect.x,-zoomRect.y);
		}

		g.transform(trans);
		
		g.setColor(Color.BLACK);
		g.fillRect(0,0,nBins,nBins);
		
		Display display = (Display) displayCombo.getSelectedItem();
		for(int x=0;x<nBins;x++){
			for(int y=0;y<nBins;y++){
				if(counts[x][y]==0) continue;
				
				float red, green, blue;
				float gamma = 0.1f;
				switch(display){
					case Channel:
						red = x/(float)nBins + 0.1f;
						green = (nBins-y)/(float)nBins + 0.1f;
						blue = 0f;
						break;
					case Frequency:
						red = (float) Math.pow(counts[x][y]/(float)maxCount, gamma);
						green = 1f-red;
						blue = 1f-red;
						break;
					case Binary:
						red = 1f;
						green = 1f;
						blue = 1f;
						break;
					default:
						red = (x/(float)nBins);
						green = red;
						blue = red;
				}

				red = Math.max(0f, Math.min(red, 1f));
				green = Math.max(0f, Math.min(green, 1f));
				blue = Math.max(0f, Math.min(blue, 1f));
				Color colour = new Color(red,green,blue);
				g.setColor(colour);
				g.fillRect(x,y,1,1);
			}
		}
		
		if(drawRect!=null){
			g.setColor(Color.CYAN);
			g.setStroke(STROKE);
			g.drawRect(drawRect.x,drawRect.y, drawRect.width,drawRect.height);
		}
	}

	@Override
	public Dimension getPreferredSize(){
		return dim;
	}
	
	private void setN(int n){
		if(nBins==n||n<2) return;
		this.nBins = n;
		if(nBinsLabel!=null) nBinsLabel.setText(""+nBins);
		counts = getBinnedCounts(data, nBins, nBins);
		maxCount = Stream.of(counts).flatMapToInt(row->Arrays.stream(row)).summaryStatistics().getMax();
		repaint();
	}
	
	public void display(){
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
}
