package com.mail;

import java.awt.Color;
import java.awt.Paint;

public class CustomRenderer extends
        org.jfree.chart.renderer.category.BarRenderer {

    /** 
     *  
     */
    private static final long serialVersionUID = 784630226449158436L;
    private Paint[] colors;
    private Paint normalPaint, goodPaint, badPaint, lastPaint;
    // 初始化柱子颜色
    private String[] colorValues = { "#AFD8F8", "#F6BD0F", "#8BBA00",
            "#FF8E46", "#008E8E", "#D64646" };

    public CustomRenderer() {
        colors = new Paint[colorValues.length];
        for (int i = 0; i < colorValues.length; i++) {
            colors[i] = Color.decode(colorValues[i]);
        }
        normalPaint = Color.decode("#AFD8F8");
        goodPaint = Color.decode("#8BBA00");
        badPaint = Color.decode("#D64646");
        lastPaint = normalPaint;
    }

    public void setLastGood(boolean good) {
        if (good) {
            lastPaint = goodPaint;
        } else {
            lastPaint = badPaint;
        }
    }

    // 每根柱子以初始化的颜色不断轮循
    public Paint getItemPaint(int i, int j) {
        if(i==1){
            return badPaint;
        }
        if (getColumnCount() - 1 == j) {
            return lastPaint;
        }
        return normalPaint;
        // return colors[j % colors.length];
    }
}