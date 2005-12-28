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

import java.util.ArrayList;
import java.util.List;

import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.restlet.data.CharacterSets;
import org.restlet.data.Cookies;
import org.restlet.data.Languages;
import org.restlet.data.MediaTypes;
import org.restlet.data.Method;
import org.restlet.data.Methods;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Representation;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.MediaTypeImpl;
import com.noelios.restlet.data.MethodImpl;
import com.noelios.restlet.data.PreferenceImpl;
import com.noelios.restlet.data.PreferenceReaderImpl;
import com.noelios.restlet.data.ReferenceImpl;

/**
 * Call that is used by the Jetty HTTP server connector.
 */
public class JettyCall extends UniformCallImpl
{
   /**
    * Constructor.
    * @param request The Jetty HTTP request.
    * @param response The Jetty HTTP response.
    */
   public JettyCall(HttpRequest request, HttpResponse response)
   {
      super(getReferrer(request), request.getField("User-Agent"), getMediaPrefs(request),
            getCharacterSetPrefs(request), getLanguagePrefs(request), getMethod(request),
            getResource(request), getCookies(request), getInput(request));

      setClientAddress(request.getRemoteAddr());
   }

   /**
    * Extracts the call's referrer from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's referrer.
    */
   private static Reference getReferrer(HttpRequest request)
   {
      String referrer = request.getField("Referer");

      if(referrer != null)
      {
         return new ReferenceImpl(referrer);
      }
      else
      {
         return null;
      }
   }

   /**
    * Extracts the call's resource from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's resource.
    */
   private static Reference getResource(HttpRequest request)
   {
      String resource = request.getRootURL() + request.getURI().toString();

      if(resource != null)
      {
         return new ReferenceImpl(resource);
      }
      else
      {
         return null;
      }
   }

   /**
    * Extracts the call's method from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's method.
    */
   private static Method getMethod(HttpRequest request)
   {
      String method = request.getMethod();
      if(method.equals(HttpRequest.__DELETE)) return Methods.DELETE;
      else if(method.equals(HttpRequest.__GET)) return Methods.GET;
      else if(method.equals(HttpRequest.__POST)) return Methods.POST;
      else if(method.equals(HttpRequest.__PUT)) return Methods.PUT;
      else return new MethodImpl(method);
   }

   /**
    * Extracts the call's input representation from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's input representation.
    */
   private static Representation getInput(HttpRequest request)
   {
      return new InputRepresentation(request.getInputStream(), new MediaTypeImpl(request.getContentType()));
   }

   /**
    * Extracts the call's media preferences from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's media preferences.
    */
   private static List<Preference> getMediaPrefs(HttpRequest request)
   {
      List<Preference> result = null;
      String accept = request.getField(HttpFields.__Accept);

      if(accept != null)
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_MEDIA_TYPE, accept);
         result = pr.readPreferences();
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(MediaTypes.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's character set preferences from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's character set preferences.
    */
   private static List<Preference> getCharacterSetPrefs(HttpRequest request)
   {
      // Implementation according to
      // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
      List<Preference> result = null;
      String acceptCharset = request.getField(HttpFields.__AcceptCharset);

      if(acceptCharset != null)
      {
         if(acceptCharset.length() == 0)
         {
            result = new ArrayList<Preference>();
            result.add(new PreferenceImpl(CharacterSets.ISO_8859_1));
         }
         else
         {
            PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_CHARACTER_SET,
                  acceptCharset);
            result = pr.readPreferences();
         }
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(CharacterSets.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's language preferences from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's language preferences.
    */
   private static List<Preference> getLanguagePrefs(HttpRequest request)
   {
      List<Preference> result = null;
      String acceptLanguage = request.getField(HttpFields.__AcceptLanguage);

      if(acceptLanguage != null)
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_LANGUAGE,
               acceptLanguage);
         result = pr.readPreferences();
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(Languages.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's cookies from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return The call's cookies.
    */
   private static Cookies getCookies(HttpRequest request)
   {
      Cookies result = null;

      if(request.getField(HttpFields.__Cookie) != null)
      {
         result = new com.noelios.restlet.data.CookiesImpl(request.getField(HttpFields.__Cookie));
      }

      return result;
   }

}
