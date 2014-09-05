/*
 * This file is part of LCMC written by Rasto Levrinc.
 *
 * Copyright (C) 2014, Rastislav Levrinc.
 *
 * The LCMC is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * The LCMC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LCMC; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package lcmc.gui.resources.vms;

import java.util.List;

import lcmc.model.AccessMode;
import lcmc.model.Application;
import lcmc.model.Host;
import lcmc.gui.ClusterBrowser;

import static org.junit.Assert.assertEquals;

import lcmc.utilities.EnablePredicate;
import lcmc.utilities.MenuAction;
import lcmc.utilities.MenuFactory;
import lcmc.utilities.MyMenu;
import lcmc.utilities.MyMenuItem;
import lcmc.utilities.Predicate;
import lcmc.utilities.UpdatableItem;
import lcmc.utilities.VisiblePredicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import org.mockito.runners.MockitoJUnitRunner;

import javax.swing.ImageIcon;

@RunWith(MockitoJUnitRunner.class)
public class DomainMenuTest {
    @Mock
    private DomainInfo domainInfoStub;
    @Mock
    private ClusterBrowser clusterBrowserStub;
    @Mock
    private Host hostStub;
    @Mock
    private Application applicationStub;
    @Mock
    private MyMenu menuStub;
    @Mock
    private MyMenuItem menuItemStub;
    @Mock
    private MenuFactory menuFactoryStub;
    @InjectMocks
    private DomainMenu domainMenu;

    @Before
    public void setUp() {
        when(applicationStub.isUseTightvnc()).thenReturn(true);
        when(applicationStub.isUseRealvnc()).thenReturn(true);
        when(applicationStub.isUseUltravnc()).thenReturn(true);
        when(domainInfoStub.getBrowser()).thenReturn(clusterBrowserStub);
        when(menuFactoryStub.createMenuItem(
                anyString(),
                (ImageIcon) anyObject(),
                anyString(),
                (AccessMode) anyObject(),
                (AccessMode) anyObject())).thenReturn(menuItemStub);
        when(menuFactoryStub.createMenu(
                anyString(),
                (AccessMode) anyObject(),
                (AccessMode) anyObject())).thenReturn(menuStub);
        when(menuFactoryStub.createMenuItem(anyString(),
                (ImageIcon) anyObject(),
                anyString(),

                anyString(),
                (ImageIcon) anyObject(),
                anyString(),

                (AccessMode) anyObject(),
                (AccessMode) anyObject())).thenReturn(menuItemStub);
        when(menuItemStub.enablePredicate((EnablePredicate) anyObject())).thenReturn(menuItemStub);
        when(menuItemStub.visiblePredicate((VisiblePredicate) anyObject())).thenReturn(menuItemStub);
        when(menuItemStub.predicate((Predicate) anyObject())).thenReturn(menuItemStub);
        when(menuItemStub.addAction((MenuAction) anyObject())).thenReturn(menuItemStub);
        when(menuStub.enablePredicate((EnablePredicate) anyObject())).thenReturn(menuStub);

        final Host[] hosts = new Host[]{hostStub};
        when(clusterBrowserStub.getClusterHosts()).thenReturn(hosts);
    }

    @Test
    public void menuShouldHaveItems() {
        final List<UpdatableItem> items = domainMenu.getPulldownMenu(domainInfoStub);

        verify(menuItemStub, times(1)).predicate((Predicate) anyObject());
        verify(menuItemStub, times(4)).visiblePredicate((VisiblePredicate) anyObject());
        verify(menuItemStub, times(11)).enablePredicate((EnablePredicate) anyObject());
        verify(menuItemStub, times(11)).addAction((MenuAction) anyObject());
        verify(menuStub, times(1)).enablePredicate((EnablePredicate) anyObject());
        verify(menuStub, times(1)).onUpdate((Runnable) anyObject());
        assertEquals(11, items.size());
    }
}
