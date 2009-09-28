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

package org.restlet.test.gwt.client;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.Client;
import org.restlet.data.Protocol;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 */
public class TestClient implements EntryPoint {

    public void onModuleLoad() {
        GWT.log("Restlet module loaded.", null);
        final Button button = new Button("Restlet, Fetch!");
        final Label label = new Label();

        /*
         * button.addClickListener(new ClickListener() { public void
         * onClick(Widget sender) { new Client(Protocol.HTTP).put(
         * "http://localhost:8888/demo/hello.txt", "entity", new Uniform() {
         * 
         * public void handle(Request request, Response response, Uniform
         * callback) { try { label.setText(response.getEntity() .getText()); }
         * catch (Exception ioException) { GWT.log("Restlet I/O failed",
         * ioException); } }
         * 
         * }); } });
         */

        RootPanel.get("slot1").add(button);
        RootPanel.get("slot2").add(label);
    }
}
