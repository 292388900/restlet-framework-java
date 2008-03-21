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

package org.restlet.example.book.restlet.ch9.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a mail.
 * 
 */
public class MailResource extends BaseResource {

    /** The mail represented by this resource. */
    private Mail mail;

    /** The parent mailbox. */
    private Mailbox mailbox;

    public MailResource(Context context, Request request, Response response) {
        super(context, request, response);
        String mailboxId = (String) request.getAttributes().get("mailboxId");
        mailbox = getDAOFactory().getMailboxDAO().getMailboxById(mailboxId);

        if (mailbox != null) {
            String mailId = (String) request.getAttributes().get("mailId");
            mail = getDAOFactory().getMailDAO().getMailById(mailId);

            if (mail != null) {
                getVariants().add(new Variant(MediaType.TEXT_HTML));
            }
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        getDAOFactory().getMailboxDAO().deleteMail(mailbox, mail);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", mailbox);

        // TODO ameliorer ceci.
        if (mail.getRecipients() != null) {
            StringBuilder builder = new StringBuilder();
            for (Iterator<Contact> iterator = mail.getRecipients().iterator(); iterator
                    .hasNext();) {
                Contact contact = iterator.next();
                builder.append(contact.getName());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            dataModel.put("recipients", builder.toString());
        }
        if (mail.getTags() != null) {
            StringBuilder builder = new StringBuilder();
            for (Iterator<String> iterator = mail.getTags().iterator(); iterator
                    .hasNext();) {
                String tag = iterator.next();
                builder.append(tag);
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            dataModel.put("tags", builder.toString());
        }
        dataModel.put("mail", mail);

        List<String> statuses = new ArrayList<String>();
        statuses.add(Mail.STATUS_SENDING);
        statuses.add(Mail.STATUS_DRAFT);
        statuses.add(Mail.STATUS_SENT);
        statuses.add(Mail.STATUS_RECEIVING);
        statuses.add(Mail.STATUS_RECEIVED);
        dataModel.put("statuses", statuses);

        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        TemplateRepresentation representation = new TemplateRepresentation(
                "mail.html", getFmcConfiguration(), dataModel, variant
                        .getMediaType());

        return representation;
    }

    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);
        mail.setSubject(form.getFirstValue("subject"));
        mail.setMessage(form.getFirstValue("message"));
        // TODO comment g�rer le statut? + la date d'envoi?
        mail.setStatus(form.getFirstValue("status"));

        String[] recipientsArray = form.getFirstValue("recipients").split(", ");
        List<Contact> recipients = new ArrayList<Contact>();
        for (int i = 0; i < recipientsArray.length; i++) {
            String string = recipientsArray[i];
            Contact contact = new Contact();
            contact.setName(string);
            recipients.add(contact);
        }
        mail.setRecipients(recipients);
        mail.setTags(Arrays.asList(form.getFirstValue("tags").split(" ")));

        getDAOFactory().getMailboxDAO().updateMail(mailbox, mail);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

}
