/*
 * This file is part of LCMC
 *
 * Copyright (C) 2012, Rastislav Levrinc.
 *
 * LCMC is free software; you can redistribute it and/or
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
package lcmc.gui.widget;

import java.awt.Color;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import lcmc.model.AccessMode;
import lcmc.model.Application;
import lcmc.model.StringValue;
import lcmc.model.Value;
import lcmc.utilities.MyButton;
import lcmc.utilities.PatternDocument;
import lcmc.utilities.WidgetListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An implementation of a field where user can enter new value. The
 * field can be Textfield or combo box, depending if there are values
 * too choose from.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class Textfield extends GenericWidget<JComponent> {
    @Autowired
    private Application application;

    public void init(final Value selectedValue,
                     final String regexp,
                     final int width,
                     final Map<String, String> abbreviations,
                     final AccessMode enableAccessMode,
                     final MyButton fieldButton) {
        super.init(regexp, enableAccessMode, fieldButton);
        addComponent(getTextField(selectedValue, regexp, abbreviations), width);
    }

    private JComponent getTextField(final Value value, final String regexp, final Map<String, String> abbreviations) {
        final String valueS;
        if (value == null) {
            valueS = null;
        } else {
            valueS = value.getValueForConfig();
        }

        final MTextField tf;
        if (regexp == null) {
            tf = new MTextField(valueS);
        } else {
            tf = new MTextField(new PatternDocument(regexp, abbreviations), valueS, 0);
        }
        return tf;
    }

    /**
     * Returns string value. If object value is null, returns empty string (not null).
     */
    @Override
    public String getStringValue() {
        final Value v = getValue();
        if (v == null) {
            return "";
        }
        return v.getValueForConfig();
    }

    /** Return value, that user have chosen in the field or typed in. */
    @Override
    protected Value getValueInternal() {
        final Value value = new StringValue(((JTextComponent) getInternalComponent()).getText());
        if (value.isNothingSelected()) {
            return null;
        }
        return value;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    protected void setValueAndWait0(final Value item) {
        if (item == null) {
            ((JTextComponent) getInternalComponent()).setText(null);
        } else {
            ((JTextComponent) getInternalComponent()).setText(item.getValueForConfig());
        }
    }

    @Override
    public Document getDocument() {
        return ((JTextComponent) getInternalComponent()).getDocument();
    }

    @Override
    public void addListeners(final WidgetListener widgetListener) {
        getWidgetListeners().add(widgetListener);
        addDocumentListener(getDocument(), widgetListener);
    }

    @Override
    protected void setComponentBackground(final Color backgroundColor, final Color compColor) {
        getInternalComponent().setBackground(compColor);
    }

    @Override
    public void requestFocus() {
        getInternalComponent().requestFocus();
    }

    @Override
    public void selectAll() {
        ((JTextComponent) getInternalComponent()).selectAll();
    }

    @Override
    public void setBackgroundColor(final Color bg) {
        application.invokeLater(!Application.CHECK_SWING_THREAD, new Runnable() {
            @Override
            public void run() {
                setBackground(bg);
                getInternalComponent().setBackground(bg);
            }
        });
    }

    /** Cleanup whatever would cause a leak. */
    @Override
    public void cleanup() {
        getWidgetListeners().clear();
        final AbstractDocument d = (AbstractDocument) getDocument();
        for (final DocumentListener dl : d.getDocumentListeners()) {
            d.removeDocumentListener(dl);
        }
    }
}
