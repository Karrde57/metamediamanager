/*Copyright 2014  M3Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package org.t3.metamediamanager.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JLabel;

public class AutoSizeLabel extends JLabel {
    public static final int MIN_FONT_SIZE=3;
    public static final int MAX_FONT_SIZE=240;
    Graphics g;
 
    public AutoSizeLabel(String text) {
        super(text);
        init();
    }
 
    protected void init() {
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                adaptLabelFont(AutoSizeLabel.this);
            }
        });
    }
 
    protected void adaptLabelFont(JLabel l) {
        if (g==null) {
            return;
        }
        Rectangle r=l.getBounds();
        int fontSize=MIN_FONT_SIZE;
        Font f=l.getFont();
 
        Rectangle r1=new Rectangle();
        Rectangle r2=new Rectangle();
        while (fontSize<MAX_FONT_SIZE) {
            r1.setSize(getTextSize(l, f.deriveFont(f.getStyle(), fontSize)));
            r2.setSize(getTextSize(l, f.deriveFont(f.getStyle(),fontSize+1)));
            if (r.contains(r1) && ! r.contains(r2)) {
                break;
            }
            fontSize++;
        }
 
        setFont(f.deriveFont(f.getStyle(),fontSize));
        repaint();
    }
 
    private Dimension getTextSize(JLabel l, Font f) {
        Dimension size=new Dimension();
        g.setFont(f);
        FontMetrics fm=g.getFontMetrics(f);
        size.width=fm.stringWidth(l.getText());
        size.height=fm.getHeight();
 
        return size;
    }
 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.g=g;
    }
}