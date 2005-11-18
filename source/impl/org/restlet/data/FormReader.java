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

package org.restlet.data;

import java.io.IOException;
import java.util.Map;

/**
 * Submission form reader.
 */
public interface FormReader
{
   /**
    * Reads the parameters with the given name.
    * If multiple values are found, a list is returned created.
    * @param name The parameter name to match.
    * @return 		The parameter value or list of values.
    */
   public Object readParameter(String name) throws IOException;

   /**
    * Reads the parameters whose name is a key in the given map.
    * If a matching parameter is found, its value is put in the map.
    * If multiple values are found, a list is created and set in the map.
    * @param parameters The parameters map controlling the reading.
    */
   public void readParameters(Map<String, Object> parameters) throws IOException;

   /**
    * Reads the next parameter available or null.
    * @return The next parameter available or null.
    */
   public Parameter readParameter() throws IOException;

   /** Closes the reader. */
   public void close() throws IOException;
}



