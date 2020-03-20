package uk.ac.cam.gurdon.escop;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;

public class PlotThumb extends JPanel{
	
	private static final long serialVersionUID = 1542426845993960902L;
	private static final int DRAW = 512;
	private static final int SIZE = 256;
	
	private JFreeChart plot;
	
	
	public PlotThumb(JFreeChart plot, String name){
		this.plot = plot;
		BufferedImage plotImage = (BufferedImage) plot.createBufferedImage(DRAW, DRAW);
		BufferedImage scaled = new BufferedImage(SIZE, SIZE, plotImage.getType());
		Graphics2D g = (Graphics2D) scaled.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(plotImage, 0,0, SIZE,SIZE, 0,0, DRAW,DRAW, null);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JLabel("<html><div style='width:200px;'>"+name+"</div></html>"));
		JLabel thumbLabel = new JLabel(new ImageIcon(scaled));
		add(thumbLabel);
		add(Box.createVerticalStrut(20));
		
		addMouseListener(new MouseAdapter(){
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
		});
	}
	
	
}
