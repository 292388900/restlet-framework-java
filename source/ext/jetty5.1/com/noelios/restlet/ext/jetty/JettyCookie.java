/*
 * Copyright 2005 J�r�me LOUVEL
 * 
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * http://www.opensource.org/licenses/cddl1.txt 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL 
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information: 
 * Portions Copyright [yyyy] [name of copyright owner]
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
