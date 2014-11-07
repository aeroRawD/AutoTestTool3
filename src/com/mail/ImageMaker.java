package com.mail;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

public class ImageMaker {

    public static void setup() {
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        // 设置标题字体
        standardChartTheme.setExtraLargeFont(new Font("隶书", Font.BOLD, 20));
        // 设置图例的字体
        standardChartTheme.setRegularFont(new Font("宋书", Font.PLAIN, 15));
        // 设置轴向的字体
        standardChartTheme.setLargeFont(new Font("宋书", Font.PLAIN, 15));
        // 应用主题样式
        ChartFactory.setChartTheme(standardChartTheme);
    }

    public static void makeImage() {
        Map<String, Integer> ht = new Hashtable<String, Integer>();
        ht.put("美国", 139800);
        ht.put("日本", 52900);
        ht.put("德国", 32800);
        ht.put("中国", 30100);
        ht.put("英国", 25700);
        ht.put("法国", 25200);
        ht.put("意大利", 20900);
        ht.put("西班牙", 14100);
        ht.put("加拿大", 13600);
        ht.put("俄罗斯", 11400);

        float sum = 0;
        for (Integer i : ht.values()) {
            sum += i;
        }

        // 设定数据源
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // 向数据源中插值，第一个参数为名称，第二个参数是double数
        int i = 0;
        for (String nation : ht.keySet()) {
            i++;
            dataset.addValue((float) ht.get(nation), String.valueOf(i), nation);

        }

        // 使用ChartFactory来创建JFreeChart
        JFreeChart chart = ChartFactory.createBarChart("收支统计图", "国家", "数额",
                dataset, PlotOrientation.VERTICAL, false, false, false);

        // 设定图片标题
        chart.setTitle(new TextTitle("2007世界总GDP排名", new Font("隶书",
                Font.ITALIC, 15)));

        // 设定背景
        chart.setBackgroundPaint(Color.white);
        CategoryPlot categoryplot = chart.getCategoryPlot(); // 获得
        // plot：CategoryPlot！！
        categoryplot.setBackgroundPaint(Color.lightGray); // 设定图表数据显示部分背景色
        categoryplot.setDomainGridlinePaint(Color.white); // 横坐标网格线白色
        categoryplot.setDomainGridlinesVisible(true); // 可见
        categoryplot.setRangeGridlinePaint(Color.white); // 纵坐标网格线白色
        // 下面两行使纵坐标的最小单位格为整数
        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer(); // 获得renderer
        barrenderer.setMaximumBarWidth(0.2);
        // 注意这里是下嗍造型到BarRenderer！！
        barrenderer.setDrawBarOutline(false); // Bar的外轮廓线不画
        GradientPaint gradientpaint = new GradientPaint(0.0F, 0.0F, Color.blue,
                0.0F, 0.0F, new Color(0, 0, 64)); // 设定特定颜色,三种:蓝色,绿色,红色
        GradientPaint gradientpaint1 = new GradientPaint(0.0F, 0.0F,
                Color.green, 0.0F, 0.0F, new Color(0, 64, 0));
        GradientPaint gradientpaint2 = new GradientPaint(0.0F, 0.0F, Color.red,
                0.0F, 0.0F, new Color(64, 0, 0));

        // 把颜色加上去
        barrenderer.setSeriesPaint(0, gradientpaint); // 给series1 Bar设定上面定义的颜色
        barrenderer.setSeriesPaint(1, gradientpaint1); // 给series2 Bar
        // 设定上面定义的颜色
        barrenderer.setSeriesPaint(2, gradientpaint2); // 给series3 Bar
        // 设定上面定义的颜色
        
        categoryplot.setRenderer(barrenderer);

        CategoryAxis categoryaxis = categoryplot.getDomainAxis(); // 横轴上的
        // Lable
        // 45度倾斜,可以改成其他
        categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        categoryaxis.setTickLabelFont(new Font("SansSerif", 10, 12));// 设定字体、类型、字号
        try {
            ChartUtilities.saveChartAsPNG(new File("data/myp1.png"), chart,
                    1000, 500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        setup();
        makeImage();

    }

}
