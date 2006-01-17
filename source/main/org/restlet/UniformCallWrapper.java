/*
 * Copyright 2005-2006 J�r�me LOUVEL
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

package org.restlet;

import java.util.List;

import org.restlet.data.*;

/**
 * Uniform call wrapper. Useful for application developer who need to enrich the call with application related
 * things.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 */
public class UniformCallWrapper implements UniformCall
{
   /** Wrapped call. */
   protected UniformCall wrappedCall;

   /**
    * Constructor.
    * @param wrappedCall The wrapped call
    */
   public UniformCallWrapper(UniformCall wrappedCall)
   {
      this.wrappedCall = wrappedCall;
   }


   /**
    * Returns the list of restlets paths. The list is sorted according to the handlers hierarchy.
    * @return The list of restlets paths.
    */
   public List<String> getPaths()
   {
      return getWrappedCall().getPaths();
   }

   /**
    * Returns one of the paths in the list. The first path is the resource path relatively to the current
    * restlet. The second path is the current reslet path relatively to the parent restlet. All the hierarchy
    * of restlet paths is also available depending on the restlet tree.
    * @param index Index of the path in the list.
    * @param strip Indicates if leading and ending slashes should be stripped.
    * @return The path at the given index.
    */
   public String getPath(int index, boolean strip)
   {
      return getWrappedCall().getPath(index, strip);
   }

   /**
    * Returns the list of substring matched in the current restlet's path.
    * @return The list of substring matched.
    * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Matcher.html#group(int)">Matcher.group()</a>
    */
   public List<String> getMatches()
   {
      return getWrappedCall().getMatches();
   }
   
   
   /**
    * Returns the best variant representation for a given resource according the the client preferences.
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @return The best variant representation. 
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public RepresentationMetadata getBestVariant(Resource resource, Language fallbackLanguage)
   {
      return getWrappedCall().getBestVariant(resource, fallbackLanguage);
   }

   /**
    * Returns the character set preferences of the user agent.
    * @return The character set preferences of the user agent.
    */
   public List<Preference> getCharacterSetPrefs()
   {
      return getWrappedCall().getCharacterSetPrefs();
   }

   /**
    * Returns the client's IP address.
    * @return The client's IP address.
    */
   public String getClientAddress()
   {
      return getWrappedCall().getClientAddress();
   }

   /**
    * Returns the client's name (ex: user agent name).
    * @return The client's name.
    */
   public String getClientName()
   {
      return getWrappedCall().getClientName();
   }

   /**
    * Returns the conditions applying to this call if any.
    * @return The conditions applying to this call if any.
    */
   public Conditions getConditions()
   {
      return getWrappedCall().getConditions();
   }

   /**
    * Returns the cookies sent by the user agent.
    * @return The cookies sent by the user agent.
    */
   public Cookies getCookies()
   {
      return getWrappedCall().getCookies();
   }

   /**
    * Returns the list of cookies to be set in the user agent. Cookie settings can be browsed, added or
    * removed.
    * @return The list of cookies to be set in the user agent.
    */
   public List<CookieSetting> getCookieSettings()
   {
      return getWrappedCall().getCookieSettings();
   }

   /**
    * Returns the representation received from the user agent.
    * @return The representation received from the user agent.
    */
   public Representation getInput()
   {
      return getWrappedCall().getInput();
   }

   /**
    * Returns the language preferences of the user agent.
    * @return The language preferences of the user agent.
    */
   public List<Preference> getLanguagePrefs()
   {
      return getWrappedCall().getLanguagePrefs();
   }

   /**
    * Returns the media type preferences of the user agent.
    * @return The media type preferences of the user agent.
    */
   public List<Preference> getMediaTypePrefs()
   {
      return getWrappedCall().getMediaTypePrefs();
   }

   /**
    * Returns the method called.
    * @return The method called.
    */
   public Method getMethod()
   {
      return getWrappedCall().getMethod();
   }

   /**
    * Returns the representation to send to the user agent
    * @return The representation to send to the user agent
    */
   public Representation getOutput()
   {
      return getWrappedCall().getOutput();
   }

   /**
    * Returns the referrer reference if available.<br/>
    * This reference shouldn't be modified during the call handling.
    * @return The referrer reference.
    */
   public Reference getReferrerRef()
   {
      return getWrappedCall().getReferrerRef();
   }

   /**
    * Returns the resource's reference.<br/>
    * This reference shouldn't be modified during the call handling, exceptio for redirect rewritings.
    * @return The resource's reference.
    */
   public Reference getResourceRef()
   {
      return getWrappedCall().getResourceRef();
   }

   /**
    * Returns the security data related to this call.
    * @return The security data related to this call.
    */
   public Security getSecurity()
   {
      return getWrappedCall().getSecurity();
   }

   /**
    * Returns the result status.
    * @return The result status.
    */
   public Status getStatus()
   {
      return getWrappedCall().getStatus();
   }

   /**
    * Returns the wrapped call.
    * @return The wrapped call
    */
   protected UniformCall getWrappedCall()
   {
      return this.wrappedCall;
   }

   /**
    * Sets the best representation of a given resource according to the client preferences.<br/> 
    * If no representation is found, sets the status to "Not found".<br/>
    * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public void setBestOutput(Resource resource, Language fallbackLanguage)
   {
      getWrappedCall().setBestOutput(resource, fallbackLanguage);
   }

   /**
    * Sets the character set preferences of the user agent.
    * @param prefs The character set preferences of the user agent.
    */
   public void setCharacterSetPrefs(List<Preference> prefs)
   {
      getWrappedCall().setCharacterSetPrefs(prefs);
   }

   /**
    * Sets the client's IP address.
    * @param address The client's IP address.
    */
   public void setClientAddress(String address)
   {
      getWrappedCall().setClientAddress(address);
   }

   /**
    * Sets the client's name (ex: user agent name).
    * @param name The client's name.
    */
   public void setClientName(String name)
   {
      getWrappedCall().setClientName(name);
   }

   /**
    * Sets the conditions applying to this call if any.
    * @param conditions The conditions applying to this call if any.
    */
   public void setConditions(Conditions conditions)
   {
      getWrappedCall().setConditions(conditions);      
   }

   /**
    * Sets the cookies sent by the user agent.
    * @param cookies The cookies sent by the user agent.
    */
   public void setCookies(Cookies cookies)
   {
      getWrappedCall().setCookies(cookies);
   }

   /**
    * Sets the content received in the request. param input The content received in the request.
    */
   public void setInput(Representation input)
   {
      getWrappedCall().setInput(input);
   }

   /**
    * Sets the language preferences of the user agent.
    * @param prefs The language preferences of the user agent.
    */
   public void setLanguagePrefs(List<Preference> prefs)
   {
      getWrappedCall().setLanguagePrefs(prefs);
   }

   /**
    * Sets the media type preferences of the user agent.
    * @param prefs The media type preferences of the user agent.
    */
   public void setMediaTypePrefs(List<Preference> prefs)
   {
      getWrappedCall().setMediaTypePrefs(prefs);
   }

   /**
    * Sets the method called.
    * @param method The method called.
    */
   public void setMethod(Method method)
   {
      getWrappedCall().setMethod(method);
   }

   /**
    * Sets the representation to send to the user agent
    * @param output The representation to send to the user agent
    */
   public void setOutput(Representation output)
   {
      getWrappedCall().setOutput(output);
   }

   /**
    * Sets the referrer reference if available.<br/>
    * This reference shouldn't be modified during the call handling.
    * @param referrerRef The referrer reference.
    */
   public void setReferrerRef(Reference referrerRef)
   {
      getWrappedCall().setReferrerRef(referrerRef);
   }

   /**
    * Sets the resource's reference.<br/>
    * This reference shouldn't be modified during the call handling, except for redirection rewriting.
    * @param resourceRef The resource's reference.
    */
   public void setResourceRef(Reference resourceRef)
   {
      getWrappedCall().setResourceRef(resourceRef);
   }

   /**
    * Sets the security data related to this call.
    * @param security The security data related to this call.
    */
   public void setSecurity(Security security)
   {
      getWrappedCall().setSecurity(security);
   }

   /**
    * Sets the result status.
    * @param status The result status to set.
    */
   public void setStatus(Status status)
   {
      getWrappedCall().setStatus(status);
   }

   /**
    * Asks the user agent to redirect itself to the given URI.<br/>
    * Modifies the result output and status properties.
    * @param targetURI The target URI.
    * @param permanent Indicates if this is a permanent redirection.
    */
   public void setRedirect(String targetURI, boolean permanent)
   {
      getWrappedCall().setRedirect(targetURI, permanent);
   }

}
