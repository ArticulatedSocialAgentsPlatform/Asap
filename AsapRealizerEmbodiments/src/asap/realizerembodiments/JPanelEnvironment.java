/*******************************************************************************
 * 
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package asap.realizerembodiments;

import javax.swing.JComponent;
import javax.swing.JPanel;

import lombok.Getter;
import lombok.Setter;

/**
*/
public class JPanelEnvironment implements JComponentEnvironment
{

    private JPanel thePanel = null;
    protected volatile boolean shutdown = false;

    public void setPanel(JPanel newPanel)
    {
        thePanel = newPanel;
    }

    @Override
    public void addJComponent(JComponent jc)
    {
        thePanel.add(jc);
        thePanel.revalidate();
        thePanel.repaint();
    }

    @Override
    public void removeJComponent(JComponent jc)
    {
        thePanel.remove(jc);
        thePanel.revalidate();
        thePanel.repaint();
    }

    @Getter
    @Setter
    private String id = "jpanelenvironment";

    @Override
    public void requestShutdown()
    {
        // nothing
        shutdown = true;

    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }
}
