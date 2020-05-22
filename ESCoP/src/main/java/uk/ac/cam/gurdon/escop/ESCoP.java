package uk.ac.cam.gurdon.escop;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.HyperStackConverter;
import ij.process.LUT;
import uk.ac.cam.gurdon.escop.CorrelationCalculator.Axis;


@Plugin(type = Command.class, menuPath = "Plugins>ESCoP")
public class ESCoP extends ContextCommand{

	static LUT HEAT = new LUT(
			new byte[]{70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 69, 69, 68, 67, 66, 64, 63, 61, 59, 57, 55, 53, 51, 48, 45, 43, 40, 37, 34, 31, 28, 26, 23, 20, 17, 14, 12, 9, 6, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 4, 7, 11, 16, 21, 27, 32, 39, 45, 52, 60, 68, 76, 84, 92, 101, 109, 117, 126, -122, -113, -104, -96, -88, -80, -72, -64, -57, -50, -43, -37, -31, -25, -20, -16, -12, -9, -6, -4, -3, -2, -2, -3, -4, -5, -7, -10, -13, -16, -19, -23, -28, -33, -37, -42, -48, -53, -60, -66, -72, -78, -85, -91, -98, -105, -112, -118, -127, 120, 112, 103, 95, 87, 79, 71, 64, 57, 51, 45, 39, 35, 30, 27, 24, 21, 20, 19, 19, 21, 23, 27, 32, 37, 44, 51, 59, 68, 77, 86, 97, 107, 118, 125, -125, -119, -112, -106, -100, -94, -88, -82, -76, -71, -65, -59, -54, -49, -44, -39, -34, -29, -25, -21, -18, -14, -11, -8, -5, -3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 5, 9, 13, 17, 22, 27, 32, 38, 44, 50, 57, 63, 70, 77, 84, 91, 98, 106, 113, 121, -128, -120, -112, -104, -96, -88, -80, -73, -65, -58, -51, -44, -38, -32, -26, -21, -16, -11, -7, -3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -2, -4, -4, -6, -7, -8, -10, -11, -13, -15, -17, -19, -21, -23, -27, -30, -34, -38, -43, -48, -53, -58, -64, -69, -75, -81, -87, -94, -100, -107, -113, -120, -127, 122, 116, 109, 102, 95, 89, 82, 76, 70, 63, 57, 51, 45, 40, 35, 29, 25, 20, 16, 12, 8, 5, 2, 2},
			new byte[]{115, 115, 116, 118, 120, 122, 124, 126, -128, -125, -123, -120, -117, -115, -112, -109, -105, -102, -99, -96, -92, -89, -86, -82, -79, -75, -72, -68, -62, -56, -50, -45, -39, -34, -29, -24, -20, -16, -12, -8, -5, -3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, -3, -4, -5, -6, -8, -9, -10, -11, -13, -14, -15, -16, -17, -18, -19, -20, -21, -21, -21, -22, -22, -22, -22, -22, -21, -20, -19, -18, -17, -16, -15, -13, -12, -10, -9, -7, -6, -4, -3, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -4, -7, -10, -13, -17, -20, -25, -29, -33, -38, -43, -48, -53, -58, -64, -69, -75, -81, -87, -93, -99, -105, -111, -118, -124, 126, 118, 110, 102, 94, 87, 79, 72, 65, 58, 51, 45, 38, 32, 27, 22, 17, 13, 8, 5, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 5, 8, 10, 12, 14, 16, 17, 19, 21, 23, 25, 26, 28, 30, 31, 33, 34, 36, 37, 38, 39, 40, 41, 42, 43, 43, 43, 44, 44, 44, 44, 43, 43, 42, 42, 41, 40, 40, 39, 38, 37, 36, 34, 33, 32, 31, 30, 28, 27, 25, 24, 23, 21, 20, 19, 17, 16, 15, 13, 12, 11, 10, 9, 7, 6, 6, 5, 4, 3, 3, 2, 2}
		);
	
	private ESCoPGUI gui;
	private PlotManager plotManager;
	private ArrayList<CorrelationCalculator> calculators;
	private boolean cancelled;
	
	
	public void run(){
		gui = new ESCoPGUI(this);
		gui.display();
	}
	
	public void run(ImagePlus imp, int cA, int cB, CorrelationCalculator.Method method, double threshA, double threshB, double maxOffsetCal, boolean doX, boolean doY, boolean doZ, int shuffleIts, boolean doScatter, boolean doTable) {
		try{
			int frame = imp.getFrame();	//TODO: handle time series?

			final Data3D A = new Data3D(imp, cA, frame);
			final Data3D B = new Data3D(imp, cB, frame);
			
			Calibration cal = imp.getCalibration();
			Roi roi = imp.getRoi();
			if(roi!=null&&!roi.isArea()){
				roi = null;
			}
			
			int maxShiftX = (int) (maxOffsetCal/cal.pixelWidth);
			int maxShiftY = (int) (maxOffsetCal/cal.pixelHeight);
			int maxShiftZ = (int) (maxOffsetCal/cal.pixelDepth);
			
			AtomicInteger count = new AtomicInteger(0);

			int nThreads = Runtime.getRuntime().availableProcessors()-1;
			ExecutorService executor = Executors.newFixedThreadPool(nThreads);
			calculators = new ArrayList<CorrelationCalculator>();
			
			Timer timer = new Timer();
			TimerTask countUpdate = new TimerTask(){
				public void run(){
					if(gui!=null) gui.setStatus("Cross Correlation "+IJ.d2s(((count.get()+1)/(float)calculators.size())*100, 1)+"%");
				}
			};
			timer.scheduleAtFixedRate(countUpdate, 200L, 200L);
			
			if(doX){
				for(int shift=-maxShiftX;shift<=maxShiftX;shift++){
					CorrelationCalculator calc = new CorrelationCalculator( A,B, roi, threshA,threshB, shift,CorrelationCalculator.Axis.X, count );
					calculators.add( calc );
					executor.execute( calc );
				}
			}
			if(doY){
				for(int shift=-maxShiftY;shift<=maxShiftY;shift++){
					CorrelationCalculator calc = new CorrelationCalculator( A,B, roi, threshA,threshB,  shift,CorrelationCalculator.Axis.Y, count );
					calculators.add( calc );
					executor.execute( calc );
				}
			}
			if(doZ){
				for(int shift=-maxShiftZ;shift<=maxShiftZ;shift++){
					CorrelationCalculator calc = new CorrelationCalculator( A,B, roi, threshA,threshB,  shift,CorrelationCalculator.Axis.Z, count );
					calculators.add( calc );
					executor.execute( calc );
				}
			}
			executor.shutdown();
			try {
				executor.awaitTermination(7L, TimeUnit.DAYS);
			} catch (InterruptedException ie) {
				System.out.println(ie.toString());
			}
			timer.cancel();
			
			if(cancelled) return;

			double[][] resultX = doX?new double[2][2*maxShiftX+1]:null;
			double[][] resultY = doY?new double[2][2*maxShiftY+1]:null;
			double[][] resultZ = doZ?new double[2][2*maxShiftZ+1]:null;
			for(CorrelationCalculator calc:calculators){
				if(calc.stop) return;
				Axis axis = calc.getAxis();
				int shift = calc.getOffset();
				double cc = calc.getCorrelationCoefficient(method);
				if(axis==Axis.X){
					int i = shift+maxShiftX;
					resultX[0][i] = shift * cal.pixelWidth;
					resultX[1][i] = cc;
				}
				else if(axis==Axis.Y){
					int i = shift+maxShiftY;
					resultY[0][i] = shift * cal.pixelHeight;
					resultY[1][i] = cc;
				}
				if(axis==Axis.Z){
					int i = shift+maxShiftZ;
					resultZ[0][i] = shift * cal.pixelDepth;
					resultZ[1][i] = cc;
				}
			}

			double fwhmX = getFWHM(resultX);
			int fwhmXpx = (int) (fwhmX / cal.pixelWidth);
			int minW = (int) Math.sqrt(imp.getWidth());
			int maxW = (int) (imp.getWidth()/2f);
			int cubeW = (int) Math.max(minW, Math.min(fwhmXpx, maxW));
			
			double fwhmY = getFWHM(resultY);
			int fwhmYpx = (int) (fwhmY / cal.pixelHeight);
			int minH = (int) Math.sqrt(imp.getHeight());
			int maxH = (int) (imp.getHeight()/2f);
			int cubeH = (int) Math.max(minH, Math.min(fwhmYpx, maxH));
			
			double fwhmZ = getFWHM(resultZ);
			int fwhmZpx = (int) (fwhmZ / cal.pixelDepth);
			int minD = (int) Math.ceil(Math.sqrt(imp.getNSlices()));
			int maxD = (int) (imp.getNSlices()/2f);
			int cubeD = (int) Math.max(minD, Math.min(fwhmZpx, maxD));
			
			final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(nThreads);
			ThreadPoolExecutor shuffleExecutor = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,	queue);
			
			shuffleExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					//System.out.println("Runnable Rejected : "+ r);
					try {
						Thread.sleep(1000);	//wait for a second
					} catch (InterruptedException ie) {
						System.out.print(ie.toString()+"\n~~~~~\n"+Arrays.toString(ie.getStackTrace()).replace(",","\n"));
					}
					executor.execute(r);	//try again
				}
			});
			
			
			ArrayList<CorrelationCalculator> shuffles = new ArrayList<CorrelationCalculator>();
			
			for(int s=0;s<shuffleIts;s++){
				gui.setStatus("Costes Randomisation: "+(s+1)+"/"+shuffleIts);

				Data3D Ashuffle = A.getShuffled(cubeW,cubeH,cubeD);
				CorrelationCalculator calc = new CorrelationCalculator( Ashuffle,B, roi, threshA,threshB );
				
				shuffles.add( calc );
				shuffleExecutor.execute( calc );
			}
			shuffleExecutor.shutdown();
			try {
				shuffleExecutor.awaitTermination(7L, TimeUnit.DAYS);
			} catch (InterruptedException ie) {
				System.out.println(ie.toString());
			}
			gui.setStatus("Outputting results...");
			
			double[] shuffleResults = new double[shuffles.size()];
			for(int s=0;s<shuffles.size();s++){
				shuffleResults[s] = shuffles.get(s).getCorrelationCoefficient(method);
			}
			
			double[] confidenceInterval = getConfidenceInterval(shuffleResults);
			
			String dims = (doX?"X":"")+(doY?"Y":"")+(doZ?"Z":"");
			String plotName = imp.getTitle()+" "+method+" C"+cA+" vs C"+cB+" "+dims+" "+maxOffsetCal+" "+cal.getUnit();
			//String plotName = "this is an unreasonably long image title to test the behaviour of the PlotHolder name bar when it has a very very very long title to display";		//TEST
			
			JFreeChart plot = plotCCF(plotName, resultX, resultY, resultZ, confidenceInterval, cal.getUnit());

			if(plotManager==null) plotManager = new PlotManager();
			plotManager.addPlot(plot, plotName);
			
			if(doScatter){
				int n = (int) A.size();
				ArrayList<Float> listA = new ArrayList<Float>();
				ArrayList<Float> listB = new ArrayList<Float>();
				for(int i=0;i<n;i++){
					int x = (int) (i%A.W);
					int y = (int) ((i-x)/A.W);
					if(roi!=null&&!roi.contains(x,y)){
						continue;
					}
					listA.add(A.get(i));
					listB.add(B.get(i));
				}

				float[][] scatterData = new float[2][listA.size()];
				for(int a=0;a<listA.size();a++){
					scatterData[0][a] = listA.get(a);
					scatterData[1][a] = listB.get(a);
				}
				
				new ScatterPlot(scatterData, "C"+cA, "C"+cB).display();
			}
			
			if(doTable){
				ResultsTable rt = new ResultsTable();
				String unit = " ("+cal.getUnit()+")";
				if(resultX!=null){
					for(int i=0;i<resultX[0].length;i++){
						rt.setValue("X Offset"+unit, i, resultX[0][i]);
						rt.setValue("X "+method, i, resultX[1][i]);
					}
				}
				if(resultY!=null){
					for(int i=0;i<resultY[0].length;i++){
						rt.setValue("Y Offset"+unit, i, resultY[0][i]);
						rt.setValue("Y "+method, i, resultY[1][i]);
					}
				}
				if(resultZ!=null){
					for(int i=0;i<resultZ[0].length;i++){
						rt.setValue("Z Offset"+unit, i, resultZ[0][i]);
						rt.setValue("Z "+method, i, resultZ[1][i]);
					}
				}
				rt.show("ESCoP-"+imp.getTitle());
			}
			
			//IJ.log("ESCoP: "+imp.getTitle());
			//IJ.log("Estimated object size = "+fwhmX+" * "+fwhmY+" * "+fwhmZ+" "+cal.getUnit());
			
			gui.setStatus("");
			
		}catch(Exception e){System.out.print(e.toString()+"\n~~~~~\n"+Arrays.toString(e.getStackTrace()).replace(",","\n"));}
	}

	private double[] getConfidenceInterval(double[] values) {
		double mean = IntStream.range(0,values.length).mapToDouble( i -> values[i] ).sum() / (float)values.length;
		double sd = Math.sqrt(IntStream.range(0,values.length).mapToDouble( i -> (values[i]-mean)*(values[i]-mean) ).sum() / (float)(values.length-1));
	    double confidenceLevel = 1.96;	//critical Z for 0.95 confidence
	    double interval = confidenceLevel * sd / Math.sqrt(values.length);
	    return new double[]{mean - interval, mean + interval};
	}
	
	private JFreeChart plotCCF(String title, double[][] resultX, double[][] resultY, double[][] resultZ, double[] confidenceInterval, String unit){
		DefaultXYDataset dataset = new DefaultXYDataset();
		if(resultX!=null) dataset.addSeries( "X", resultX );
		if(resultY!=null) dataset.addSeries( "Y", resultY );
		if(resultZ!=null) dataset.addSeries( "Z", resultZ );
		
		JFreeChart chart = ChartFactory.createXYLineChart("", "Offset ("+unit+")", "Correlation", dataset, PlotOrientation.VERTICAL, true, true, false);
		
		XYPlot plot = chart.getXYPlot();
		
		plot.setBackgroundPaint(Color.WHITE);
    	plot.setDomainGridlinePaint(Color.GRAY);
    	plot.setRangeGridlinePaint(Color.GRAY);
    	
		XYItemRenderer render = plot.getRenderer();
        BasicStroke stroke = new BasicStroke(0.5f);
        LegendItemCollection legend = new LegendItemCollection();
        for(int s=0;s<dataset.getSeriesCount();s++){
        	String key = (String) dataset.getSeriesKey(s);
        	Color colour = Color.BLACK;
        	if(key.equals("X")){
        		colour = Color.RED;
        	}
        	else if(key.equals("Y")){
        		colour = Color.GREEN;
        	}
        	else if(key.equals("Z")){
        		colour = Color.BLUE;
        	}
        	render.setSeriesPaint(s, colour);
        	render.setSeriesStroke(s, stroke);
        	legend.add( new LegendItem((String) dataset.getSeriesKey(s), colour) );
        }
        
        if(confidenceInterval[1]-confidenceInterval[0]>1E-9){
	        Color confidenceColour = new Color(255,128,64, 64);
	        double minD = 0;
	        double maxD = 0;
	        if(resultX!=null){
	        	minD = Math.min(minD, resultX[0][0]);
	        	maxD = Math.max(maxD, resultX[0][resultX[0].length-1]);
	        }
	        if(resultY!=null){
	        	minD = Math.min(minD, resultY[0][0]);
	        	maxD = Math.max(maxD, resultY[0][resultY[0].length-1]);
	        }
	        if(resultZ!=null){
	        	minD = Math.min(minD, resultZ[0][0]);
	        	maxD = Math.max(maxD, resultZ[0][resultZ[0].length-1]);
	        }
	        XYBoxAnnotation confidenceBox = new XYBoxAnnotation(minD, confidenceInterval[0], maxD, confidenceInterval[1], null, null, confidenceColour);
			render.addAnnotation(confidenceBox);
			legend.add( new LegendItem("Costes Randomised Confidence Interval (P=0.95)", confidenceColour) );
        }
        
		plot.setFixedLegendItems(legend);
        
        /*ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
		frame.setSize( new Dimension(800, 800) );
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);*/
		
		return chart;
	}
	
	private double getFWHM(double[][] data){
		if(data==null) return 0;
		CurveFitter fitter = new CurveFitter(data[0], data[1]);
		fitter.doFit(CurveFitter.GAUSSIAN);
		double sd = fitter.getParams()[3];	//y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))
		double fwhm = (2 * Math.sqrt(2 * Math.log(2))) * sd;	
		return fwhm;
	}
	
	public void cancel(){
		super.cancel("Cancelled");
		cancelled = true;
		if(calculators!=null){
			for(CorrelationCalculator calc:calculators){
				calc.stop = true;
			}
		}
		gui.dispose();
		if(plotManager!=null) plotManager.dispose();
	}
	
	public void showManager(){
		if(cancelled) return;
		if(plotManager==null) plotManager = new PlotManager();
		plotManager.display();
	}
	
	public static void main(String[] arg){
		
		ImageJ.main(arg);

		//ImagePlus img = new ImagePlus("E:\\test data\\coloc\\3D4C.tif");
		//ImagePlus img = new ImagePlus("E:\\test data\\coloc\\12-bit_2C.tif");
		//ImagePlus img = new ImagePlus("C:\\Users\\USER\\work\\data\\2020_02_05_Khayam\\2020_02_05_DNAcomp_20x_controls.lif - Co_inj_Cy3Cy5_Position002b.tif");
		//ImagePlus img = new ImagePlus("C:\\Users\\USER\\work\\data\\2020_02_05_Khayam\\smallTest.tif");
		ImagePlus img = new ImagePlus("C:\\Users\\USER\\work\\data\\2020_02_05_Khayam\\2020_02_05_DNAcomp_20x_controls.lif - Co_injected_Cy3Cy5_MarkandFind Co_inj_Cy3Cy5_Position001.tif");
		
		final ImagePlus image = HyperStackConverter.toHyperStack(img, img.getNChannels(), img.getNSlices(), img.getNFrames());
		image.setDisplayMode(IJ.GRAYSCALE);
		image.setPosition(1, (int)(img.getNSlices()/2f), 1);
		image.show();
		
		ESCoP escop = new ESCoP();
		escop.run();
		
		boolean test = false;
		if(test){
			escop.run(image, 1, 3, CorrelationCalculator.Method.Pearson, 0.0, 0.0, 20.0, true, false, false, 20, false, false);
			escop.run(image, 1, 3, CorrelationCalculator.Method.Pearson, 0.0, 0.0, 20.0, false, true, false, 20, false, false);
			escop.run(image, 1, 3, CorrelationCalculator.Method.Pearson, 0.0, 0.0, 20.0, false, false, true, 20, false, false);
			escop.showManager();
		}

	}

}
