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
package org.goko.tools.dro;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.goko.core.common.exception.GkException;
import org.goko.core.common.service.AbstractGokoService;
import org.goko.core.controller.IControllerService;
import org.goko.core.controller.bean.MachineValueDefinition;
import org.goko.core.log.GkLog;
import org.goko.tools.dro.preferences.DROPreferences;

public class DROServiceImpl extends AbstractGokoService implements IDROService, IPropertyChangeListener{
	private static final GkLog LOG = GkLog.getLogger(DROServiceImpl.class);
	public static final String SERVICE_ID = "org.goko.tools.dro.service";	
	private IControllerService controllerService;
	private List<MachineValueDefinition> lstDefinition;
	
	/** (inheritDoc)
	 * @see org.goko.core.common.service.IGokoService#getServiceId()
	 */
	@Override
	public String getServiceId() throws GkException {
		return SERVICE_ID;
	}

	/** (inheritDoc)
	 * @see org.goko.core.common.service.IGokoService#start()
	 */
	@Override
	public void startService() throws GkException {
		lstDefinition = new ArrayList<MachineValueDefinition>();		
		DROPreferences.getInstance().addPropertyChangeListener(this);
		updateValues();
	}

	/** (inheritDoc)
	 * @see org.goko.core.common.service.IGokoService#stop()
	 */
	@Override
	public void stopService() throws GkException {
		
	}

	/** (inheritDoc)
	 * @see org.goko.tools.dro.IDROService#getDisplayedMachineValueDefinition()
	 */
	@Override
	public List<MachineValueDefinition> getDisplayedMachineValueDefinition() throws GkException{
		return lstDefinition;
	}

	/** (inheritDoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {		
		updateValues();
	}

	private void updateValues() {
		String[] token = StringUtils.split(DROPreferences.getInstance().getString(DROPreferences.KEY_DISPLAYED_VALUES), DROPreferences.DISPLAYED_VALUES_SEPARATOR);
		String updatedValue = StringUtils.EMPTY;
		lstDefinition.clear();
		if(token != null && token.length > 0){
			for (String string : token) {
				try {					
					MachineValueDefinition val = controllerService.findMachineValueDefinition(string);
					if(val != null){
						lstDefinition.add(val);
						// Confirm existence
						updatedValue += string+DROPreferences.DISPLAYED_VALUES_SEPARATOR;
					}
				} catch (GkException e) {
					LOG.error(e);					
				}
			}
			// Memorize only existing machine values. We use the put method to prevent firing an update event
			DROPreferences.getInstance().putValue(DROPreferences.KEY_DISPLAYED_VALUES, updatedValue);
		}
	}

	/**
	 * @return the controllerService
	 */
	public IControllerService getControllerService() {
		return controllerService;
	}

	/**
	 * @param controllerService the controllerService to set
	 */
	public void setControllerService(IControllerService controllerService) {
		this.controllerService = controllerService;
	}
	
	

}
