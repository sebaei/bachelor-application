package com.vogella.maven.quickstart;

import javax.swing.*;

import org.jxmapviewer.JXMapKit;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
 
public class Tr extends JLabel {
    Dimension minSize = new Dimension(150, 100);
 
    public Tr(Color color) {
        setBackground(color);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.black));
    }
 
    public Dimension getMinimumSize() {
        return minSize;
    }
 
    public Dimension getPreferredSize() {
        return minSize;
    }

	public void setBackground(JXMapKit jxmapkit) {
		// TODO Auto-generated method stub
		
	}

	public void setForeground(JXMapKit jxmapkit) {
		// TODO Auto-generated method stub
		
	}
}
