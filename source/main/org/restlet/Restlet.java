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

package org.restlet;

import org.restlet.component.RestletContainer;

/**
 * Handler of calls to resources or sets of resources. Restlets live inside a parent container and are
 * selected by parsing the resource's URI. Restlets are attached to other restlets using maplets.
 * @see org.restlet.UniformInterface
 * @see org.restlet.Maplet
 */
public interface Restlet
{
   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException;

   /**
    * Returns the container.
    * @return The container.
    */
   public RestletContainer getContainer();

}
