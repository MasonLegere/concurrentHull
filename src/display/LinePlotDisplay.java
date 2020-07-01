package display;

import java.awt.BorderLayout;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/** 
 *  Creates JFrame used for plotting the final results
 * */ 
public class LinePlotDisplay extends JFrame {

  private XYSeries data;

  public LinePlotDisplay(XYSeries data) {
    super("Concurrent Convex Hull");
    this.data = data;
    add(createChartPanel(), BorderLayout.CENTER);
    setSize(640, 480);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
  }

  private JPanel createChartPanel() {
    String title = "";
    String xAxisLabel = "Time (ms)";
    String yAxisLabel = "Number of Threads";

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(data);
    JFreeChart chart = ChartFactory.createXYLineChart(title, yAxisLabel, xAxisLabel, dataset);
    XYPlot plot = chart.getXYPlot();

    // Adds data points to the line graph to show granularity
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    plot.setRenderer(renderer);

    File imageFile = new File("MatrixMultiplication.png");
    int width = 640;
    int height = 480;

    // Saves the line plot in the local directory
    try {
      ChartUtils.saveChartAsPNG(imageFile, chart, width, height);
    } catch (IOException ex) {
      System.err.println(ex);
    }

    return new ChartPanel(chart);
  }

}
