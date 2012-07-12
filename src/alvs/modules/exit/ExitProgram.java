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

package alvs.modules.exit;

import alvs.data.ParameterSet;
import alvs.desktop.Desktop;
import alvs.desktop.ALVSMenu;
import alvs.main.ALVSCore;
import alvs.main.ALVSModule;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 *
 * @author scsandra
 */
public class ExitProgram implements ALVSModule, ActionListener {
    private Desktop desktop;
    
        
    public void initModule() {

        this.desktop = ALVSCore.getDesktop();
        desktop.addMenuSeparator(ALVSMenu.FILE);
        desktop.addMenuItem(ALVSMenu.FILE, "Exit..",
                "Exit program", KeyEvent.VK_E, this, null, null);
    }

    public ParameterSet getParameterSet() {
        return null;
    }

    public void setParameters(ParameterSet parameterValues) {
        
    }

    public void actionPerformed(ActionEvent e) {      
        ALVSCore.exitALVS();
    }
    
}
