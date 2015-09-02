package com.clementsclan.rogue;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Rogue {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Start: Rogue");

		//SWT Screen Stuff
		Display display  = new Display();
		Shell shell = new Shell(display);;
		FormLayout layout = new FormLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		shell.setLayout(layout);
		shell.setText("Maze Runner");

		World myWorld = new World(shell);

		//Open the window and spin waiting for the UI to close
		shell.open();
		while (!shell.isDisposed()){
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
				


	}

}
