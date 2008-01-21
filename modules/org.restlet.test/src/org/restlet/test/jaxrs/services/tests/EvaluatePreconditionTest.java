/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test.jaxrs.services.tests;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.restlet.Client;
import org.restlet.data.Conditions;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.test.jaxrs.services.EvaluatePreconditionService;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
 */
public class EvaluatePreconditionTest extends JaxRsTestCase {
    /**
     * After than 2008-01-08, 12h
     * 
     * @see EvaluatePreconditionService#getLastModificationDateFromDatastore()
     */
    @SuppressWarnings("deprecation")
    public static final Date AFTER = new Date(2008 - 1900, 0, 9); // 2008-01-09

    /**
     * Before 2008-01-08, 12h
     * 
     * @see EvaluatePreconditionService#getLastModificationDateFromDatastore()
     */
    @SuppressWarnings("deprecation")
    public static final Date BEFORE = new Date(2007 - 1900, 11, 31); // 2007-12-31

    /**
     * @param klasse
     * @param subPath
     * @param httpMethod
     * @param conditions
     * @return
     */
    private static Response accessServer(Class<?> klasse, String subPath,
            Method httpMethod, Conditions conditions) {
        Reference reference = createReference(klasse, subPath);
        Client client = new Client(PROTOCOL);
        Request request = new Request(httpMethod, reference);
        request.setConditions(conditions);
        Response response = client.handle(request);
        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection createRootResourceColl() {
        return Collections.singleton(EvaluatePreconditionService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetDateAndEntityTag() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        conditions.setMatch(Util.createList(getEntityTagFromDatastore()));
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.PUT, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        conditions.setMatch(Util.createList(getEntityTagFromDatastore()));
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.GET, conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());

        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.PUT, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        conditions.setMatch(Util.createList(new Tag("shkhsdk")));
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.GET, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());

        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.PUT, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        conditions.setMatch(Util.createList(new Tag("shkhsdk")));
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.GET, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());

        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.PUT, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());
    }

    @SuppressWarnings("deprecation")
    public void testGetDateNotModified() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
        assertEquals(0, response.getEntity().getText().length());
        // from RFC 2616, Section 10.3.5
        // The 304 response MUST include the following header fields:
        // - ETag and/or Content-Location, if the header would have been sent
        // in a 200 response to the same request
        // - Expires, Cache-Control, and/or Vary, if the field-value might
        // differ from that sent in any previous response for the same
        // variant
        // TODO JSR311: Wie das vorige einhalten?

        // wenn GET, dann 304, bei anderen Methoden andere Ergebnisse
        // (Precondition failed)
        // 304:
        // * muss das Datum der letzten �nderung enthalten
    }

    public void testGetEntityTagMatch() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setMatch(Util.createList(getEntityTagFromDatastore()));
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(EvaluatePreconditionService
                .getLastModificationDateFromDatastore(), response.getEntity()
                .getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setMatch(Util.createList(new Tag("affer")));
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.GET, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());
    }

    public void testGetEntityTagNoneMatch() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setNoneMatch(Util.createList(getEntityTagFromDatastore()));
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());

        conditions = new Conditions();
        conditions.setNoneMatch(Util.createList(new Tag("affer")));
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.GET, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /**
     * @see EvaluatePreconditionService#getLastModificationDateFromDatastore()
     * @throws Exception
     */
    public void testGetModifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(EvaluatePreconditionService
                .getLastModificationDateFromDatastore(), response.getEntity()
                .getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.GET, conditions);
        assertEquals(Status.REDIRECTION_NOT_MODIFIED, response.getStatus());
        assertEquals(EvaluatePreconditionService
                .getLastModificationDateFromDatastore(), response.getEntity()
                .getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertEquals(0, response.getEntity().getSize());

        // LATER test, what happens, because of Range-Header
        // see RFC2616, top of page 131
    }

    /**
     * @return
     */
    private Tag getEntityTagFromDatastore() {
        return Util.convertEntityTag(EvaluatePreconditionService
                .getEntityTagFromDatastore());
    }

    public void testGetUnmodifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setUnmodifiedSince(AFTER);
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.GET, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(EvaluatePreconditionService
                .getLastModificationDateFromDatastore(), response.getEntity()
                .getModificationDate());
        assertEquals(getEntityTagFromDatastore(), response.getEntity().getTag());
        assertNotNull(response.getEntity().getText());
        assertTrue(response.getEntity().getSize() > 0);

        conditions = new Conditions();
        conditions.setUnmodifiedSince(BEFORE);
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.GET, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());

        // LATER testen, was bei ung�ltigem Datum passiert:
        // If-Unmodified-Since-Header ignorieren.
    }

    /**
     * @see EvaluatePreconditionService#getLastModificationDateFromDatastore()
     * @throws Exception
     */
    public void testPutModifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setModifiedSince(BEFORE);
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.PUT, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setModifiedSince(AFTER);
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.PUT, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());
    }

    public void testPutUnmodifiedSince() throws Exception {
        Conditions conditions = new Conditions();
        conditions.setUnmodifiedSince(AFTER);
        Response response = accessServer(EvaluatePreconditionService.class,
                "date", Method.PUT, conditions);
        assertEquals(Status.SUCCESS_OK, response.getStatus());

        conditions = new Conditions();
        conditions.setUnmodifiedSince(BEFORE);
        response = accessServer(EvaluatePreconditionService.class, "date",
                Method.PUT, conditions);
        assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, response
                .getStatus());

        // LATER testen, was bei ung�ltigem Datum passiert:
        // If-Unmodified-Since-Header ignorieren.
    }

    public void testOptions() {
        Response response = accessServer(EvaluatePreconditionService.class,
                Method.OPTIONS);
        Set<Method> allowedMethods = response.getAllowedMethods();
        assertTrue("allowedOptions must contain ABC", allowedMethods
                .contains(Method.valueOf("ABC")));
        assertTrue("allowedOptions must contain DEF", allowedMethods
                .contains(Method.valueOf("DEF")));
        assertTrue("allowedOptions must contain GHI", allowedMethods
                .contains(Method.valueOf("GHI")));
        assertEquals(3, allowedMethods.size());
    }
}