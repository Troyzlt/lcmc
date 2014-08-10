/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2011-2012, Rastislav Levrinc.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */


package lcmc.gui.dialog.drbdConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import lcmc.gui.GUIData;
import lcmc.model.AccessMode;
import lcmc.model.Application;
import lcmc.model.Host;
import lcmc.model.StringValue;
import lcmc.model.Value;
import lcmc.gui.ClusterBrowser;
import lcmc.gui.SpringUtilities;
import lcmc.gui.dialog.WizardDialog;
import lcmc.gui.resources.drbd.BlockDevInfo;
import lcmc.gui.resources.drbd.VolumeInfo;
import lcmc.gui.widget.Widget;
import lcmc.gui.widget.WidgetFactory;
import lcmc.utilities.DRBD;
import lcmc.utilities.ExecCallback;
import lcmc.utilities.MyButton;
import lcmc.utilities.Tools;
import lcmc.utilities.WidgetListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of a dialog where drbd block devices are initialized.
 * information.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
final class CreateMD extends DrbdConfig {
    private static final int COMBOBOX_WIDTH = 250;
    private static final int CREATE_MD_FS_ALREADY_THERE_RC = 40;
    private Widget metadataWidget;
    private final MyButton makeMetaDataButton = new MyButton();
    @Autowired
    private GUIData guiData;
    @Autowired
    private CreateFS createFSDialog;

    private void createMetadataAndCheckResult(final boolean destroyData) {
        Tools.invokeLater(new Runnable() {
            @Override
            public void run() {
                makeMetaDataButton.setEnabled(false);
            }
        });
        final Thread[] thread = new Thread[2];
        final String[] answerStore = new String[2];
        final Integer[] returnCode = new Integer[2];
        final BlockDevInfo[] bdis = {getDrbdVolumeInfo().getFirstBlockDevInfo(),
                                     getDrbdVolumeInfo().getSecondBlockDevInfo()};
        for (int i = 0; i < 2; i++) {
            final int index = i;
            returnCode[index] = -1;
            thread[i] = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    final ExecCallback execCallback =
                        new ExecCallback() {
                            @Override
                            public void done(final String answer) {
                                Tools.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        makeMetaDataButton.setEnabled(false);
                                    }
                                });
                                answerStore[index] = answer;
                                returnCode[index] = 0;
                            }

                            @Override
                            public void doneError(final String answer, final int errorCode) {
                                answerStore[index] = answer;
                                returnCode[index] = errorCode;
                            }

                        };
                    String drbdMetaDisk = getDrbdVolumeInfo().getMetaDiskForHost(bdis[index].getHost());
                    if ("internal".equals(drbdMetaDisk)) {
                        drbdMetaDisk = bdis[index].getName();
                    }
                    final Application.RunMode runMode = Application.RunMode.LIVE;
                    if (destroyData) {
                        DRBD.createMDDestroyData(bdis[index].getHost(),
                                                 getDrbdVolumeInfo().getDrbdResourceInfo().getName(),
                                                 getDrbdVolumeInfo().getName(),
                                                 drbdMetaDisk,
                                                 execCallback,
                                                 runMode);
                    } else {
                        DRBD.createMD(bdis[index].getHost(),
                                      getDrbdVolumeInfo().getDrbdResourceInfo().getName(),
                                      getDrbdVolumeInfo().getName(),
                                      drbdMetaDisk,
                                      execCallback,
                                      runMode);
                    }
                }
            });
            thread[i].start();
        }
        boolean error = false;
        for (int i = 0; i < 2; i++) {
            try {
                thread[i].join(0);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (returnCode[i] == CREATE_MD_FS_ALREADY_THERE_RC) {
                answerStore[i] = Tools.getString("Dialog.DrbdConfig.CreateMD.CreateMD.Failed.40");
                error = true;
            } else if (returnCode[i] > 0) {
                answerStore[i] = Tools.getString("Dialog.DrbdConfig.CreateMD.CreateMD.Failed") + answerStore[i];
                error = true;
            } else {
                answerStore[i] = Tools.getString("Dialog.DrbdConfig.CreateMD.CreateMD.Done");
            }
            answerStore[i] = answerStore[i].replaceAll("@HOST@", Matcher.quoteReplacement(bdis[i].getHost().getName()));
        }
        if (error) {
            answerPaneSetTextError(Tools.join("\n", answerStore));
        } else {
            Tools.invokeLater(new Runnable() {
                @Override
                public void run() {
                    makeMetaDataButton.setEnabled(false);
                    buttonClass(nextButton()).setEnabled(true);
                    if (Tools.getApplication().getAutoOptionGlobal("autodrbd") != null) {
                        pressNextButton();
                    }
                }
            });
            answerPaneSetText(Tools.join("\n", answerStore));
        }
    }

    /**
     * Returns next dialog plus it calls drbd up command for both devices and
     * returns the drbd config create fs dialog.
     */
    @Override
    public WizardDialog nextDialog() {
        final BlockDevInfo bdi1 = getDrbdVolumeInfo().getFirstBlockDevInfo();
        final BlockDevInfo bdi2 = getDrbdVolumeInfo().getSecondBlockDevInfo();
        final String clusterName = bdi1.getHost().getCluster().getName();
        guiData.startProgressIndicator(clusterName, "scanning block devices...");
        final Application.RunMode runMode = Application.RunMode.LIVE;
        if (getDrbdVolumeInfo().getDrbdResourceInfo().isProxy(bdi1.getHost())) {
            DRBD.proxyUp(bdi1.getHost(), getDrbdVolumeInfo().getDrbdResourceInfo().getName(), null, runMode);
        }
        if (getDrbdVolumeInfo().getDrbdResourceInfo().isProxy(bdi2.getHost())) {
            DRBD.proxyUp(bdi2.getHost(), getDrbdVolumeInfo().getDrbdResourceInfo().getName(), null, runMode);
        }
        DRBD.adjustApply(bdi1.getHost(),
                         getDrbdVolumeInfo().getDrbdResourceInfo().getName(),
                         getDrbdVolumeInfo().getName(),
                         runMode);
        DRBD.adjustApply(bdi2.getHost(),
                         getDrbdVolumeInfo().getDrbdResourceInfo().getName(),
                         getDrbdVolumeInfo().getName(),
                         runMode);
        final String device = getDrbdVolumeInfo().getDevice();
        final ClusterBrowser browser = getDrbdVolumeInfo().getDrbdResourceInfo().getBrowser();
        browser.updateHWInfo(bdi1.getHost(), !Host.UPDATE_LVM);
        browser.updateHWInfo(bdi2.getHost(), !Host.UPDATE_LVM);
        bdi1.getBlockDevice().setDrbdBlockDevice(bdi1.getHost().getDrbdBlockDevice(device));
        bdi2.getBlockDevice().setDrbdBlockDevice(bdi2.getHost().getDrbdBlockDevice(device));
        guiData.stopProgressIndicator(clusterName, "scanning block devices...");
        createFSDialog.init(this, getDrbdVolumeInfo());
        return createFSDialog;
    }

    @Override
    protected String getDialogTitle() {
        return Tools.getString("Dialog.DrbdConfig.CreateMD.Title");
    }

    @Override
    protected String getDescription() {
        return Tools.getString("Dialog.DrbdConfig.CreateMD.Description");
    }

    @Override
    protected void initDialogBeforeVisible() {
        super.initDialogBeforeVisible();
        makeMetaDataButton.setBackgroundColor(Tools.getDefaultColor("ConfigDialog.Button"));
        if (getDrbdVolumeInfo().getDrbdResourceInfo().isHaveToCreateMD()) {
            enableComponentsLater(new JComponent[]{});
        } else {
            enableComponentsLater(new JComponent[]{buttonClass(nextButton())});
        }
    }

    /** Inits the dialog after it becomes visible. */
    @Override
    protected void initDialogAfterVisible() {
        enableComponents();
        if (Tools.getApplication().getAutoOptionGlobal("autodrbd") != null) {
            Tools.invokeLater(new Runnable() {
                @Override
                public void run() {
                    makeMetaDataButton.pressButton();
                }
            });
        }
    }

    /** Returns input pane with choices what to do with meta-data. */
    @Override
    protected JComponent getInputPane() {
        final JPanel pane = new JPanel(new SpringLayout());
        final JPanel inputPane = new JPanel(new SpringLayout());

        /* Meta-Data */
        final JLabel metadataLabel = new JLabel(Tools.getString("Dialog.DrbdConfig.CreateMD.Metadata"));
        final String useExistingMetadata = Tools.getString("Dialog.DrbdConfig.CreateMD.UseExistingMetadata");
        final String createNewMetadata = Tools.getString("Dialog.DrbdConfig.CreateMD.CreateNewMetadata");
        final String createNewMetadataDestroyData =
                                        Tools.getString("Dialog.DrbdConfig.CreateMD.CreateNewMetadataDestroyData");
        if (getDrbdVolumeInfo().getDrbdResourceInfo().isHaveToCreateMD()) {
            final Value[] choices = {new StringValue(createNewMetadata), new StringValue(createNewMetadataDestroyData)};
            makeMetaDataButton.setEnabled(true);
            makeMetaDataButton.setText(Tools.getString("Dialog.DrbdConfig.CreateMD.CreateMDButton"));
            metadataWidget = WidgetFactory.createInstance(
                                        Widget.Type.COMBOBOX,
                                        new StringValue(createNewMetadata),
                                        choices,
                                        Widget.NO_REGEXP,
                                        COMBOBOX_WIDTH,
                                        Widget.NO_ABBRV,
                                        new AccessMode(Application.AccessType.RO, !AccessMode.ADVANCED),
                                        Widget.NO_BUTTON);
        } else {
            final Value[] choices = {new StringValue(useExistingMetadata),
                                     new StringValue(createNewMetadata),
                                     new StringValue(createNewMetadataDestroyData)};
            makeMetaDataButton.setEnabled(false);
            makeMetaDataButton.setText(Tools.getString("Dialog.DrbdConfig.CreateMD.OverwriteMDButton"));
            String metadataDefault = useExistingMetadata;
            if (Tools.getApplication().getAutoOptionGlobal("autodrbd") != null) {
                metadataDefault = createNewMetadata;
                makeMetaDataButton.setEnabled(true);
            }
            metadataWidget = WidgetFactory.createInstance(
                                        Widget.Type.COMBOBOX,
                                        new StringValue(metadataDefault),
                                        choices,
                                        Widget.NO_REGEXP,
                                        COMBOBOX_WIDTH,
                                        Widget.NO_ABBRV,
                                        new AccessMode(Application.AccessType.RO, !AccessMode.ADVANCED),
                                        Widget.NO_BUTTON);
        }

        inputPane.add(metadataLabel);
        inputPane.add(metadataWidget.getComponent());
        metadataWidget.addListeners(
                new WidgetListener() {
                    @Override
                    public void check(final Value value) {
                        if (metadataWidget.getStringValue().equals(useExistingMetadata)) {
                            makeMetaDataButton.setEnabled(false);
                            buttonClass(nextButton()).setEnabled(true);
                        } else {
                            buttonClass(nextButton()).setEnabled(false);
                            makeMetaDataButton.setEnabled(true);
                        }
                    }
                });

        makeMetaDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getProgressBar().start(10000);
                        if (metadataWidget.getStringValue().equals(createNewMetadataDestroyData)) {
                            createMetadataAndCheckResult(true);
                        } else {
                            createMetadataAndCheckResult(false);
                        }
                        progressBarDone();
                    }
                });
                thread.start();
            }
        });
        inputPane.add(makeMetaDataButton);


        SpringUtilities.makeCompactGrid(inputPane, 1, 3,  // rows, cols
                                                   1, 1,  // initX, initY
                                                   1, 1); // xPad, yPad

        pane.add(inputPane);
        pane.add(getProgressBarPane(null));
        pane.add(getAnswerPane(""));
        SpringUtilities.makeCompactGrid(pane, 3, 1,  // rows, cols
                                              0, 0,  // initX, initY
                                              0, 0); // xPad, yPad

        return pane;
    }
}
