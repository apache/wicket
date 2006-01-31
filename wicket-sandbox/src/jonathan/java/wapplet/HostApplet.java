package wapplet;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JApplet;
import javax.swing.JButton;

/**
 * The applet implementation used to host the user's JPanel.
 * 
 * @author Jonathan Locke
 */
public class HostApplet extends JApplet
{
	public void init()
	{
	    Container content = getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout()); 
	    content.add(new JButton("Button 1"));
	    content.add(new JButton("Button 2"));
	    content.add(new JButton("Button 3"));		
	}
}
