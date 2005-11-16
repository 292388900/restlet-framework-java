/*
 * Copyright � 2005 J�r�me LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet;

/**
 * Element of a software architecture.<br/><br/>
 * "A software architecture is defined by a configuration of architectural elements--components, connectors, and
 * data--constrained in their relationships in order to achieve a desired set of architectural properties." R.Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2">Source dissertation</a>
 */
public interface Element
{
   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription();
}



