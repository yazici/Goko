/*
 *
 *   Goko
 *   Copyright (C) 2013  PsyKo
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.goko.controller.grbl.v08.internal.handlers;

import javax.inject.Named;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.goko.controller.grbl.v08.GrblMachineState;
import org.goko.controller.grbl.v08.IGrblControllerService;
import org.goko.controller.grbl.v08.configuration.GrblConfigurationDialog;
import org.goko.core.common.exception.GkException;
import org.goko.core.connection.IConnectionService;
import org.goko.core.controller.IControllerService;


/**
 * Handler for displaying the Grbl configuration panel
 * @author PsyKo
 *
 */
public class GrblConfigurationOpenHandler {
	
	@CanExecute
	public boolean canExecute(IGrblControllerService controllerService, IConnectionService connectionService){		
		try {
			return !ObjectUtils.equals(GrblMachineState.UNDEFINED, controllerService.getState());
		} catch (GkException e) {			
			e.printStackTrace();
		}
		return false;		
	}
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, IControllerService service, IEclipseContext context) throws GkException {
		GrblConfigurationDialog cfgPanel = new GrblConfigurationDialog(shell, context);

		cfgPanel.open();
	}

}
