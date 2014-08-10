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

package lcmc.gui.resources.drbd;

import java.util.ArrayList;
import java.util.List;
import lcmc.EditClusterDialog;
import lcmc.ProxyHostWizard;
import lcmc.model.AccessMode;
import lcmc.model.Application;
import lcmc.model.Host;
import lcmc.gui.dialog.cluster.DrbdLogs;
import lcmc.model.HostFactory;
import lcmc.utilities.MyMenuItem;
import lcmc.utilities.Tools;
import lcmc.utilities.UpdatableItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GlobalMenu {
    @Autowired
    private Provider<EditClusterDialog> editClusterDialogProvider;
    @Autowired
    private HostFactory hostFactory;
    @Autowired
    private Provider<ProxyHostWizard> proxyHostWizardProvider;

    public List<UpdatableItem> getPulldownMenu(final GlobalInfo globalInfo) {
        final List<UpdatableItem> items = new ArrayList<UpdatableItem>();

        /** Add proxy host */
        final UpdatableItem addProxyHostMenu = new MyMenuItem(
                Tools.getString("GlobalInfo.AddProxyHost"), null,
                Tools.getString("GlobalInfo.AddProxyHost"), new AccessMode(
                        Application.AccessType.OP, false), new AccessMode(
                        Application.AccessType.OP, false)) {

            private static final long serialVersionUID = 1L;

            @Override
            public void action() {
                addProxyHostWizard(globalInfo);
            }
        };
        items.add(addProxyHostMenu);

        /* cluster wizard */
        final UpdatableItem clusterWizardItem = new MyMenuItem(
                Tools.getString("ClusterBrowser.Hb.ClusterWizard"),
                GlobalInfo.CLUSTER_ICON, null, new AccessMode(
                        Application.AccessType.ADMIN, AccessMode.ADVANCED),
                new AccessMode(Application.AccessType.ADMIN,
                        !AccessMode.ADVANCED)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void action() {
                final EditClusterDialog editClusterDialog = editClusterDialogProvider.get();
                editClusterDialog.showDialogs(globalInfo.getBrowser().getCluster());
            }
        };
        items.add(clusterWizardItem);

        /* Rescan LVM */
        final UpdatableItem rescanLvmItem = new MyMenuItem(
                Tools.getString("GlobalInfo.RescanLvm"), null, /* icon */
                null, new AccessMode(Application.AccessType.OP,
                        !AccessMode.ADVANCED), new AccessMode(
                        Application.AccessType.OP, AccessMode.ADVANCED)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void action() {
                globalInfo.getBrowser().updateHWInfo(
                        Host.UPDATE_LVM);
            }
        };
        items.add(rescanLvmItem);

        /* view log */
        final UpdatableItem viewLogMenu = new MyMenuItem(
                Tools.getString("ClusterBrowser.Drbd.ViewLogs"),
                GlobalInfo.LOGFILE_ICON, null, new AccessMode(
                        Application.AccessType.RO, false), new AccessMode(
                        Application.AccessType.RO, false)) {

            private static final long serialVersionUID = 1L;

            @Override
            public void action() {
                globalInfo.hidePopup();
                final DrbdLogs l = new DrbdLogs(
                        globalInfo.getCluster(),
                        GlobalInfo.ALL_LOGS_PATTERN);
                l.showDialog();
            }
        };
        items.add(viewLogMenu);
        return items;
    }

    private void addProxyHostWizard(final GlobalInfo globalInfo) {
        final Host proxyHost = hostFactory.createInstance();
        proxyHost.init();
        proxyHost.setCluster(globalInfo.getCluster());
        final ProxyHostWizard proxyHostWizard = proxyHostWizardProvider.get();
        proxyHostWizard.init(proxyHost, null);
        proxyHostWizard.showDialogs();
    }
}