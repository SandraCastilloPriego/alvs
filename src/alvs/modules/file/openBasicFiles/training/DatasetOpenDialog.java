/*
 * Copyright 2010 - 2012 VTT Biotechnology
 * This file is part of ALVS.
 *
 * ALVS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * ALVS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ALVS; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package alvs.modules.file.openBasicFiles.training;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


import com.sun.java.ExampleFileFilter;
import alvs.desktop.impl.DesktopParameters;
import alvs.main.ALVSCore;
import alvs.util.dialogs.ExitCode;

/**
 * File open dialog
 */
public class DatasetOpenDialog extends JDialog implements ActionListener {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private JFileChooser fileChooser;
	private File datasetFile;
        private ExitCode exit = ExitCode.UNKNOWN;

	public DatasetOpenDialog(File lastpath) {

		super(ALVSCore.getDesktop().getMainFrame(),
				"Please select a dataset file to open...", true);

		logger.finest("Displaying dataset open dialog");

		fileChooser = new JFileChooser();		             
                if (lastpath != null) {
			fileChooser.setCurrentDirectory(lastpath);
		}
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.addActionListener(this);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
                ExampleFileFilter csv = new ExampleFileFilter();
                csv.addExtension("csv");
                csv.addExtension("xls");
		csv.setDescription("Excel and Comma Separated Files");
		fileChooser.addChoosableFileFilter(csv);                
		fileChooser.setFileFilter(csv);                
                
		add(fileChooser, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(ALVSCore.getDesktop().getMainFrame());
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		// check if user clicked "Open"

		if (command.equals("ApproveSelection")) {			
			try {
                                datasetFile = fileChooser.getSelectedFile();
                                DesktopParameters deskParameters = (DesktopParameters) ALVSCore
                                                    .getDesktop().getParameterSet();
                                deskParameters.setLastOpenProjectPath(datasetFile.getPath()); 
                                exit = ExitCode.OK;
			} catch (Throwable e) {
				JOptionPane.showMessageDialog(this,
						"Could not open dataset file", "Dataset opening error",
						JOptionPane.ERROR_MESSAGE);
				logger.fine("Could not open dataset file." + e.getMessage());
                                exit = ExitCode.CANCEL;
			}                        
		}else{
                    exit = ExitCode.CANCEL;
                }
		// discard this dialog
		dispose();
	}

	public String getCurrentDirectory() {
		return this.datasetFile.toString();
	}
        
        public ExitCode getExitCode(){
            return exit;
        }
}
