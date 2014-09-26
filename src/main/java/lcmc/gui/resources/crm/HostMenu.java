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

package lcmc.gui.resources.crm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.swing.JColorChooser;

import lcmc.EditHostDialog;
import lcmc.gui.CallbackAction;
import lcmc.gui.GUIData;
import lcmc.model.AccessMode;
import lcmc.model.Application;
import lcmc.model.Host;
import lcmc.gui.ClusterBrowser;
import lcmc.gui.HostBrowser;
import lcmc.gui.dialog.HostLogs;
import lcmc.utilities.ButtonCallback;
import lcmc.utilities.CRM;
import lcmc.utilities.Corosync;
import lcmc.utilities.EnablePredicate;
import lcmc.utilities.Heartbeat;
import lcmc.utilities.MenuAction;
import lcmc.utilities.MenuFactory;
import lcmc.utilities.MyMenu;
import lcmc.utilities.MyMenuItem;
import lcmc.utilities.Openais;
import lcmc.utilities.Predicate;
import lcmc.utilities.Tools;
import lcmc.utilities.UpdatableItem;
import lcmc.utilities.VisiblePredicate;

@Named
public class HostMenu {
    private static final String NOT_IN_CLUSTER = "not in cluster";

    @Inject
    private EditHostDialog editHostDialog;
    @Inject
    private GUIData guiData;
    @Inject
    private MenuFactory menuFactory;
    @Inject
    private Application application;
    @Inject @Named("hostLogs")
    private Provider<HostLogs> hostLogsProvider;

    public List<UpdatableItem> getPulldownMenu(final HostInfo hostInfo) {
        final List<UpdatableItem> items = new ArrayList<UpdatableItem>();
        /* host wizard */
        final MyMenuItem hostWizardItem =
                menuFactory.createMenuItem(Tools.getString("HostBrowser.HostWizard"),
                        HostBrowser.HOST_ICON_LARGE,
                        "",
                        new AccessMode(Application.AccessType.RO, false),
                        new AccessMode(Application.AccessType.RO, false))
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                editHostDialog.showDialogs(hostInfo.getHost());
                            }
                        });
        items.add(hostWizardItem);
        guiData.registerAddHostButton(hostWizardItem);
        /* cluster manager standby on/off */
        final Application.RunMode runMode = Application.RunMode.LIVE;
        final MyMenuItem standbyItem =
                menuFactory.createMenuItem(Tools.getString("HostBrowser.CRM.StandByOn"),
                        HostInfo.HOST_STANDBY_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        Tools.getString("HostBrowser.CRM.StandByOff"),
                        HostInfo.HOST_STANDBY_OFF_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,
                        new AccessMode(Application.AccessType.OP, false),
                        new AccessMode(Application.AccessType.OP, false))
                        .predicate(new Predicate() {
                            @Override
                            public boolean check() {
                                return !hostInfo.isStandby(runMode);
                            }
                        })
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                if (!hostInfo.getHost().isCrmStatusOk()) {
                                    return HostInfo.NO_PCMK_STATUS_STRING;
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                final Host dcHost = hostInfo.getBrowser().getClusterBrowser().getDCHost();
                                if (hostInfo.isStandby(runMode)) {
                                    CRM.standByOff(dcHost, hostInfo.getHost(), runMode);
                                } else {
                                    CRM.standByOn(dcHost, hostInfo.getHost(), runMode);
                                }
                            }
                        });
        final ClusterBrowser cb = hostInfo.getBrowser().getClusterBrowser();
        if (cb != null) {
            final ButtonCallback standbyItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            if (hostInfo.isStandby(Application.RunMode.LIVE)) {
                                CRM.standByOff(dcHost, hostInfo.getHost(), Application.RunMode.TEST);
                            } else {
                                CRM.standByOn(dcHost, hostInfo.getHost(), Application.RunMode.TEST);
                            }
                        }
                    });
            hostInfo.addMouseOverListener(standbyItem, standbyItemCallback);
        }
        items.add(standbyItem);

        /* Migrate all services from this host. */
        final MyMenuItem allMigrateFromItem =
                menuFactory.createMenuItem(Tools.getString("HostInfo.CRM.AllMigrateFrom"),
                        HostInfo.HOST_STANDBY_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,
                        new AccessMode(Application.AccessType.OP, false),
                        new AccessMode(Application.AccessType.OP, false))
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                if (!hostInfo.getHost().isCrmStatusOk()) {
                                    return HostInfo.NO_PCMK_STATUS_STRING;
                                }
                                if (hostInfo.getBrowser().getClusterBrowser().getExistingServiceList(null).isEmpty()) {
                                    return "there are no services to migrate";
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                for (final ServiceInfo si : cb.getExistingServiceList(null)) {
                                    if (!si.isConstraintPlaceholder() && si.getGroupInfo() == null && si.getCloneInfo() == null) {
                                        final List<String> runningOnNodes = si.getRunningOnNodes(Application.RunMode.LIVE);
                                        if (runningOnNodes != null && runningOnNodes.contains(hostInfo.getHost().getName())) {
                                            final Host dcHost = hostInfo.getHost();
                                            si.migrateFromResource(dcHost, hostInfo.getHost().getName(), Application.RunMode.LIVE);
                                        }
                                    }
                                }
                            }
                        });
        if (cb != null) {
            final ButtonCallback allMigrateFromItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            for (final ServiceInfo si : cb.getExistingServiceList(null)) {
                                if (!si.isConstraintPlaceholder() && si.getGroupInfo() == null) {
                                    final List<String> runningOnNodes = si.getRunningOnNodes(Application.RunMode.LIVE);
                                    if (runningOnNodes != null && runningOnNodes.contains(hostInfo.getHost().getName())) {
                                        si.migrateFromResource(dcHost, hostInfo.getHost().getName(), Application.RunMode.TEST);
                                    }
                                }
                            }
                        }
                    });
            hostInfo.addMouseOverListener(allMigrateFromItem, allMigrateFromItemCallback);
        }
        items.add(allMigrateFromItem);
        /* Stop corosync/openais. */
        final MyMenuItem stopCorosyncItem =
                menuFactory.createMenuItem(Tools.getString("HostInfo.StopCorosync"),
                        HostInfo.HOST_STOP_COMM_LAYER_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        Tools.getString("HostInfo.StopOpenais"),
                        HostInfo.HOST_STOP_COMM_LAYER_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        new AccessMode(Application.AccessType.ADMIN, true),
                        new AccessMode(Application.AccessType.ADMIN, false))
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                final Host h = hostInfo.getHost();
                                if (!h.isInCluster()) {
                                    return NOT_IN_CLUSTER;
                                }
                                return null;
                            }
                        })
                        .predicate(new Predicate() {
                            @Override
                            public boolean check() {
                    /* when both are running it's openais. */
                                return hostInfo.getHost().isCorosyncRunning() && !hostInfo.getHost().isOpenaisRunning();
                            }
                        })
                        .visiblePredicate(new VisiblePredicate() {
                            @Override
                            public boolean check() {
                                return hostInfo.getHost().isCorosyncRunning() || hostInfo.getHost().isOpenaisRunning();
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                if (application.confirmDialog(Tools.getString("HostInfo.confirmCorosyncStop.Title"),
                                        Tools.getString("HostInfo.confirmCorosyncStop.Desc"),
                                        Tools.getString("HostInfo.confirmCorosyncStop.Yes"),
                                        Tools.getString("HostInfo.confirmCorosyncStop.No"))) {
                                    final Host thisHost = hostInfo.getHost();
                                    thisHost.setCommLayerStopping(true);
                                    if (!thisHost.isPcmkStartedByCorosync()
                                            && thisHost.hasPacemakerInitScript()
                                            && thisHost.isPacemakerRunning()) {
                                        if (hostInfo.getHost().isCorosyncRunning() && !hostInfo.getHost().isOpenaisRunning()) {
                                            Corosync.stopCorosyncWithPcmk(thisHost);
                                        } else {
                                            Openais.stopOpenaisWithPcmk(thisHost);
                                        }
                                    } else {
                                        if (hostInfo.getHost().isCorosyncRunning() && !hostInfo.getHost().isOpenaisRunning()) {
                                            Corosync.stopCorosync(thisHost);
                                        } else {
                                            Openais.stopOpenais(thisHost);
                                        }
                                    }
                                    updateClusterView(thisHost, hostInfo);
                                }
                            }
                        });
        if (cb != null) {
            final ButtonCallback stopCorosyncItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            if (!hostInfo.isStandby(Application.RunMode.LIVE)) {
                                CRM.standByOn(dcHost, hostInfo.getHost(), Application.RunMode.TEST);
                            }
                        }
                    });
            hostInfo.addMouseOverListener(stopCorosyncItem, stopCorosyncItemCallback);
        }
        items.add(stopCorosyncItem);
        /* Stop heartbeat. */
        final MyMenuItem stopHeartbeatItem =
                menuFactory.createMenuItem(Tools.getString("HostInfo.StopHeartbeat"),
                        HostInfo.HOST_STOP_COMM_LAYER_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        new AccessMode(Application.AccessType.ADMIN, true),
                        new AccessMode(Application.AccessType.ADMIN, false))
                        .visiblePredicate(new VisiblePredicate() {
                            @Override
                            public boolean check() {
                                return hostInfo.getHost().isHeartbeatRunning();
                            }
                        })
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                final Host h = hostInfo.getHost();
                                if (!h.isInCluster()) {
                                    return NOT_IN_CLUSTER;
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                if (application.confirmDialog(Tools.getString("HostInfo.confirmHeartbeatStop.Title"),
                                        Tools.getString("HostInfo.confirmHeartbeatStop.Desc"),
                                        Tools.getString("HostInfo.confirmHeartbeatStop.Yes"),
                                        Tools.getString("HostInfo.confirmHeartbeatStop.No"))) {
                                    hostInfo.getHost().setCommLayerStopping(true);
                                    Heartbeat.stopHeartbeat(hostInfo.getHost());
                                    updateClusterView(hostInfo.getHost(), hostInfo);
                                }
                            }
                        });
        if (cb != null) {
            final ButtonCallback stopHeartbeatItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            if (!hostInfo.isStandby(Application.RunMode.LIVE)) {
                                CRM.standByOn(dcHost, hostInfo.getHost(), Application.RunMode.TEST);
                            }
                        }
                    });
            hostInfo.addMouseOverListener(stopHeartbeatItem, stopHeartbeatItemCallback);
        }
        items.add(stopHeartbeatItem);
        /* Start corosync. */
        final MyMenuItem startCorosyncItem =
                menuFactory.createMenuItem(Tools.getString("HostInfo.StartCorosync"),
                        HostInfo.HOST_START_COMM_LAYER_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        new AccessMode(Application.AccessType.ADMIN, false),
                        new AccessMode(Application.AccessType.ADMIN, false))
                        .visiblePredicate(new VisiblePredicate() {
                            @Override
                            public boolean check() {
                                final Host h = hostInfo.getHost();
                                return h.isCorosyncInstalled()
                                        && h.hasCorosyncInitScript()
                                        && h.corosyncOrOpenaisConfigExists()
                                        && !h.isCorosyncRunning()
                                        && !h.isOpenaisRunning()
                                        && !h.isHeartbeatRunning()
                                        && !h.isHeartbeatInRc();
                            }
                        })
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                final Host h = hostInfo.getHost();
                                if (!h.isInCluster()) {
                                    return NOT_IN_CLUSTER;
                                }
                                if (h.isOpenaisInRc() && !h.isCorosyncInRc()) {
                                    return "Openais is in rc.d";
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                hostInfo.getHost().setCommLayerStarting(true);
                                if (hostInfo.getHost().isPacemakerInRc()) {
                                    Corosync.startCorosyncWithPcmk(hostInfo.getHost());
                                } else {
                                    Corosync.startCorosync(hostInfo.getHost());
                                }
                                updateClusterView(hostInfo.getHost(), hostInfo);
                            }
                        });
        if (cb != null) {
            final ButtonCallback startCorosyncItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            //TODO
                        }
                    });
            hostInfo.addMouseOverListener(startCorosyncItem, startCorosyncItemCallback);
        }
        items.add(startCorosyncItem);
        /* Start openais. */
        final MyMenuItem startOpenaisItem =
                menuFactory.createMenuItem(Tools.getString("HostInfo.StartOpenais"),
                        HostInfo.HOST_START_COMM_LAYER_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        new AccessMode(Application.AccessType.ADMIN, false),
                        new AccessMode(Application.AccessType.ADMIN, false))
                        .visiblePredicate(new VisiblePredicate() {
                            @Override
                            public boolean check() {
                                final Host h = hostInfo.getHost();
                                return h.hasOpenaisInitScript()
                                        && h.corosyncOrOpenaisConfigExists()
                                        && !h.isCorosyncRunning()
                                        && !h.isOpenaisRunning()
                                        && !h.isHeartbeatRunning()
                                        && !h.isHeartbeatInRc();
                            }
                        })
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                final Host h = hostInfo.getHost();
                                if (!h.isInCluster()) {
                                    return NOT_IN_CLUSTER;
                                }
                                if (h.isCorosyncInRc() && !h.isOpenaisInRc()) {
                                    return "Corosync is in rc.d";
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                hostInfo.getHost().setCommLayerStarting(true);
                                Openais.startOpenais(hostInfo.getHost());
                                updateClusterView(hostInfo.getHost(), hostInfo);
                            }
                        });
        if (cb != null) {
            final ButtonCallback startOpenaisItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            //TODO
                        }
                    });
            hostInfo.addMouseOverListener(startOpenaisItem, startOpenaisItemCallback);
        }
        items.add(startOpenaisItem);
        /* Start heartbeat. */
        final MyMenuItem startHeartbeatItem =
                menuFactory.createMenuItem(Tools.getString("HostInfo.StartHeartbeat"),
                        HostInfo.HOST_START_COMM_LAYER_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        new AccessMode(Application.AccessType.ADMIN, false),
                        new AccessMode(Application.AccessType.ADMIN, false))
                        .visiblePredicate(new VisiblePredicate() {
                            @Override
                            public boolean check() {
                                final Host h = hostInfo.getHost();
                                return h.hasHeartbeatInitScript()
                                        && h.heartbeatConfigExists()
                                        && !h.isCorosyncRunning()
                                        && !h.isOpenaisRunning()
                                        && !h.isHeartbeatRunning();
                                //&& !h.isAisRc()
                                //&& !h.isCsRc(); TODO should check /etc/defaults/
                            }
                        })
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                final Host h = hostInfo.getHost();
                                if (!h.isInCluster()) {
                                    return NOT_IN_CLUSTER;
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                hostInfo.getHost().setCommLayerStarting(true);
                                Heartbeat.startHeartbeat(hostInfo.getHost());
                                updateClusterView(hostInfo.getHost(), hostInfo);
                            }
                        });
        if (cb != null) {
            final ButtonCallback startHeartbeatItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            //TODO
                        }
                    });
            hostInfo.addMouseOverListener(startHeartbeatItem, startHeartbeatItemCallback);
        }
        items.add(startHeartbeatItem);

        /* Start pacemaker. */
        final MyMenuItem startPcmkItem =
                menuFactory.createMenuItem(Tools.getString("HostInfo.StartPacemaker"),
                        HostInfo.HOST_START_COMM_LAYER_ICON,
                        ClusterBrowser.STARTING_PTEST_TOOLTIP,

                        new AccessMode(Application.AccessType.ADMIN, false),
                        new AccessMode(Application.AccessType.ADMIN, false))
                        .visiblePredicate(new VisiblePredicate() {
                            @Override
                            public boolean check() {
                                final Host h = hostInfo.getHost();
                                return !h.isPcmkStartedByCorosync()
                                        && !h.isPacemakerRunning()
                                        && (h.isCorosyncRunning() || h.isOpenaisRunning())
                                        && !h.isHeartbeatRunning();
                            }
                        })
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                final Host h = hostInfo.getHost();
                                if (!h.isInCluster()) {
                                    return NOT_IN_CLUSTER;
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                hostInfo.getHost().setPacemakerStarting(true);
                                Corosync.startPacemaker(hostInfo.getHost());
                                updateClusterView(hostInfo.getHost(), hostInfo);
                            }
                        });
        if (cb != null) {
            final ButtonCallback startPcmkItemCallback = cb.new ClMenuItemCallback(hostInfo.getHost())
                    .addAction(new CallbackAction() {
                        @Override
                        public void run(final Host dcHost) {
                            //TODO
                        }
                    });
            hostInfo.addMouseOverListener(startPcmkItem, startPcmkItemCallback);
        }
        items.add(startPcmkItem);
        /* change host color */
        final UpdatableItem changeHostColorItem =
                menuFactory.createMenuItem(Tools.getString("HostBrowser.Drbd.ChangeHostColor"),
                        null,
                        "",
                        new AccessMode(Application.AccessType.RO, false),
                        new AccessMode(Application.AccessType.RO, false))
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                final Color newColor = JColorChooser.showDialog(
                                        guiData.getMainFrame(),
                                        "Choose " + hostInfo.getHost().getName() + " color",
                                        hostInfo.getHost().getPmColors()[0]);
                                if (newColor != null) {
                                    hostInfo.getHost().setSavedHostColorInGraphs(newColor);
                                }
                            }
                        });
        items.add(changeHostColorItem);

        /* view logs */
        final UpdatableItem viewLogsItem =
                menuFactory.createMenuItem(Tools.getString("HostBrowser.Drbd.ViewLogs"),
                        HostInfo.LOGFILE_ICON,
                        "",
                        new AccessMode(Application.AccessType.RO, false),
                        new AccessMode(Application.AccessType.RO, false))
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                if (!hostInfo.getHost().isConnected()) {
                                    return Host.NOT_CONNECTED_MENU_TOOLTIP_TEXT;
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                final HostLogs hostLogs = hostLogsProvider.get();
                                hostLogs.init(hostInfo.getHost());
                                hostLogs.showDialog();
                            }
                        });
        items.add(viewLogsItem);
        /* advacend options */
        final MyMenu hostAdvancedSubmenu = menuFactory.createMenu(Tools.getString("HostBrowser.AdvancedSubmenu"),
                new AccessMode(Application.AccessType.OP, false),
                new AccessMode(Application.AccessType.OP, false))
                .enablePredicate(new EnablePredicate() {
                    @Override
                    public String check() {
                        if (!hostInfo.getHost().isConnected()) {
                            return Host.NOT_CONNECTED_MENU_TOOLTIP_TEXT;
                        }
                        return null;
                    }
                });
        hostAdvancedSubmenu.onUpdate(new Runnable() {
            @Override
            public void run() {
                hostAdvancedSubmenu.updateMenuComponents();
                hostInfo.getBrowser().addAdvancedMenu(hostAdvancedSubmenu);
            }
        });
        items.add(hostAdvancedSubmenu);

        /* remove host from gui */
        final UpdatableItem removeHostItem =
                menuFactory.createMenuItem(Tools.getString("HostBrowser.RemoveHost"),
                        HostBrowser.HOST_REMOVE_ICON,
                        Tools.getString("HostBrowser.RemoveHost"),
                        new AccessMode(Application.AccessType.RO, false),
                        new AccessMode(Application.AccessType.RO, false))
                        .enablePredicate(new EnablePredicate() {
                            @Override
                            public String check() {
                                if (hostInfo.getHost().isInCluster()) {
                                    return "it is a member of a cluster";
                                }
                                return null;
                            }
                        })
                        .addAction(new MenuAction() {
                            @Override
                            public void run(final String text) {
                                hostInfo.getHost().disconnect();
                                final ClusterBrowser b = hostInfo.getBrowser().getClusterBrowser();
                                if (b != null) {
                                    guiData.unregisterAllHostsUpdate(b.getClusterViewPanel());
                                }
                                application.removeHostFromHosts(hostInfo.getHost());
                                guiData.allHostsUpdate();
                            }
                        });
        items.add(removeHostItem);
        return items;
    }

    private void updateClusterView(final Host host, final HostInfo hostInfo) {
        final ClusterBrowser cb = hostInfo.getBrowser().getClusterBrowser();
        if (cb == null) {
            host.setIsLoading();
            host.getHWInfo(!Host.UPDATE_LVM);
        } else {
            cb.updateHWInfo(host, !Host.UPDATE_LVM);
        }
    }
}
