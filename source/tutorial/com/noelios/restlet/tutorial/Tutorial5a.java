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

package com.noelios.restlet.tutorial;

import org.restlet.AbstractRestlet;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.DefaultRestletContainer;
import org.restlet.component.RestletContainer;
import org.restlet.data.MediaTypes;

import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.ext.jetty.JettyServer;

public class Tutorial5a
{
   public static void main(String[] args)
   {
      try
      {
         // Registering the Restlet API implementation
         com.noelios.restlet.Engine.register();
      
         // Create a new restlet container
         RestletContainer myContainer = new DefaultRestletContainer("My container");

         // Create the HTTP server connector, then add it as a server connector 
         // to the restlet container. Note that the container is the call handler.
         JettyServer httpServer = new JettyServer("My connector", 8182, myContainer);
         myContainer.addServer(httpServer);
         
         // Create a new restlet that will display some path information. 
         // Note that restlets are call handlers similar to servlets.
         Restlet myRestlet = new AbstractRestlet(myContainer)
            {
               public void handle(RestletCall call) throws RestletException
               {
                  // Print the requested URI path
                  String output = "Resource path = " + call.getPath(0, false) + '\n' + 
                                  "Restlet  path = " + call.getPath(1, false);
                  
                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };

         // Then attach it to the container. Note that the restlet will be called 
         // only if the beginning of the requested URI matches the given pattern.
         myContainer.attach("http://localhost:8182/tutorial", myRestlet);
            
         // Now, let's start the container! Note that the HTTP server connector is
         // also automatically started.
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
