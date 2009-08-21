/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch8;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.ChallengeScheme;
import org.restlet.example.book.restlet.ch8.objects.ObjectsFacade;
import org.restlet.example.book.restlet.ch8.objects.User;
import org.restlet.security.Guard;

/**
 * Guard access to the RMEP application.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 */
public class RmepGuard extends Guard {

    /** Data facade object. */
    protected ObjectsFacade dataFacade;

    /** Storage key in request's context. */
    public final static String CURRENT_USER = "CURRENT_USER";

    public RmepGuard(Context context, ChallengeScheme scheme, String realm,
            ObjectsFacade dataFacade) {
        super(context, scheme, realm);
        this.dataFacade = dataFacade;
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        final User user = this.dataFacade.getUserByLoginPwd(identifier, secret);
        if (user != null) {
            request.getAttributes().put(CURRENT_USER, user);
            return true;
        }

        return false;
    }

}
