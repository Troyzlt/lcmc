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


package lcmc.gui.dialog.vm;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import lcmc.model.vm.VmsXml;
import lcmc.gui.dialog.WizardDialog;
import lcmc.gui.resources.vms.DomainInfo;
import lcmc.gui.widget.Widget;
import lcmc.utilities.Tools;

/**
 * An implementation of a dialog where user can enter a new domain.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public final class Domain extends VMConfig {
    /** Configuration options of the new domain. */
    private static final String[] PARAMS = {VmsXml.VM_PARAM_DOMAIN_TYPE,
                                            VmsXml.VM_PARAM_NAME,
                                            VmsXml.VM_PARAM_VIRSH_OPTIONS,
                                            VmsXml.VM_PARAM_EMULATOR,
                                            VmsXml.VM_PARAM_VCPU,
                                            VmsXml.VM_PARAM_CURRENTMEMORY,
                                            VmsXml.VM_PARAM_BOOT,
                                            VmsXml.VM_PARAM_BOOT_2,
                                            VmsXml.VM_PARAM_LOADER,
                                            VmsXml.VM_PARAM_TYPE,
                                            VmsXml.VM_PARAM_INIT,
                                            VmsXml.VM_PARAM_TYPE_ARCH,
                                            VmsXml.VM_PARAM_TYPE_MACHINE};
    /** Input pane cache for back button. */
    private JComponent inputPane = null;
    private Widget domainNameWi;
    /** Next dialog object. */
    private WizardDialog nextDialogObject = null;

    /** Prepares a new {@code Domain} object. */
    public Domain(final WizardDialog previousDialog,
                  final DomainInfo vmsVirtualDomainInfo) {
        super(previousDialog, vmsVirtualDomainInfo);
    }

    /** Next dialog. */
    @Override
    public WizardDialog nextDialog() {
        if (nextDialogObject == null) {
            if (getVMSVirtualDomainInfo().needFilesystem()) {
                nextDialogObject =
                        new Filesystem(this, getVMSVirtualDomainInfo());
            } else {
                nextDialogObject =
                        new InstallationDisk(this, getVMSVirtualDomainInfo());
            }
        }
        return nextDialogObject;
    }

    /**
     * Returns the title of the dialog. It is defined as
     * Dialog.vm.Domain.Title in TextResources.
     */
    @Override
    protected String getDialogTitle() {
        return Tools.getString("Dialog.vm.Domain.Title");
    }

    /**
     * Returns the description of the dialog. It is defined as
     * Dialog.vm.Domain.Description in TextResources.
     */
    @Override
    protected String getDescription() {
        return Tools.getString("Dialog.vm.Domain.Description");
    }

    /** Inits dialog. */
    @Override
    protected void initDialogBeforeVisible() {
        super.initDialogBeforeVisible();
        enableComponentsLater(new JComponent[]{buttonClass(nextButton())});
    }

    /** Inits the dialog. */
    @Override
    protected void initDialogAfterVisible() {
        super.initDialogAfterVisible();
        final DomainInfo vdi = getVMSVirtualDomainInfo();
        final boolean cor = vdi.checkResourceFields(null, PARAMS).isCorrect();
        if (cor || nextDialogObject != null) {
            enableComponents();
        } else {
            /* don't enable */
            enableComponents(new JComponent[]{buttonClass(nextButton())});
        }
        Tools.invokeLater(new Runnable() {
            @Override
            public void run() {
                makeDefaultButton(buttonClass(nextButton()));
            }
        });
        Tools.invokeLater(new Runnable() {
            @Override
            public void run() {
                domainNameWi.requestFocus();
            }
        });
    }

    /** Returns input pane where user can configure a vm. */
    @Override
    protected JComponent getInputPane() {
        final DomainInfo vdi = getVMSVirtualDomainInfo();
        vdi.waitForInfoPanel();
        if (inputPane != null) {
            return inputPane;
        }
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        final JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
        optionsPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        vdi.getResource().setValue(VmsXml.VM_PARAM_BOOT,
                                   DomainInfo.BOOT_CDROM);
        vdi.savePreferredValues();
        vdi.addWizardParams(
                      optionsPanel,
                      PARAMS,
                      buttonClass(nextButton()),
                      Tools.getDefaultSize("Dialog.vm.Resource.LabelWidth"),
                      Tools.getDefaultSize("Dialog.vm.Resource.FieldWidth"),
                      null);
        domainNameWi = vdi.getWidget(VmsXml.VM_PARAM_NAME,
                                     Widget.WIZARD_PREFIX);
        panel.add(optionsPanel);

        final JScrollPane sp = new JScrollPane(panel);
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
        sp.setPreferredSize(new Dimension(Short.MAX_VALUE, 200));
        inputPane = sp;
        return sp;
    }
}
