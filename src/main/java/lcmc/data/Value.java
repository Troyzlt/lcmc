/*
 * This file is part of LCMC written by Rasto Levrinc.
 *
 * Copyright (C) 2013, Rastislav Levrinc.
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


package lcmc.data;

import lcmc.utilities.Unit;
import lcmc.utilities.Tools;

/**
 */
public interface Value {
    static final String NOTHING_SELECTED =
                                     Tools.getString("Widget.NothingSelected");

    public String getValueForGui();
    public String getValueForConfig();
    public boolean isNothingSelected();
    public Unit getUnit();
    public String getValueForConfigWithUnit();
    public String getNothingSelected();
}