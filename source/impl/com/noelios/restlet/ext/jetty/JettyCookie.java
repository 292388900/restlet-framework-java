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

package com.noelios.restlet.ext.jetty;

import javax.servlet.http.Cookie;

import org.restlet.data.CookieSetting;

/**
 * Jetty cookie that can convert a Restlet cookie setting.
 */
public class JettyCookie extends Cookie
{
   /**
    * Constructor.
    * @param cookieSetting The Restlet cookie setting to convert.
    */
   public JettyCookie(CookieSetting cookieSetting)
   {
      super(cookieSetting.getName(), cookieSetting.getValue());

      if(cookieSetting.getComment() != null) setComment(cookieSetting.getComment());

      if(cookieSetting.getDomain() != null) setDomain(cookieSetting.getDomain());

      setMaxAge(cookieSetting.getMaxAge());

      if(cookieSetting.getPath() != null) setPath(cookieSetting.getPath());

      setSecure(cookieSetting.isSecure());
      setVersion(cookieSetting.getVersion());
   }

}
