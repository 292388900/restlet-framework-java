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

package org.restlet.resource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.resource.AnnotationInfo;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.ConverterService;
import org.restlet.util.Series;

/**
 * Client-side resource. Acts like a proxy of a target resource.<br>
 * <br>
 * Concurrency note: instances of the class are not designed to be shared among
 * several threads. If thread-safety is necessary, consider using the
 * lower-level {@link Client} class instead.
 * 
 * @author Jerome Louvel
 */
public class ClientResource extends UniformResource {

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(Context context, Reference reference,
            Class<? extends T> resourceInterface) {
        ClientResource clientResource = new ClientResource(context, reference);
        return clientResource.wrap(resourceInterface);
    }

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(Reference reference,
            Class<? extends T> resourceInterface) {
        return create(null, reference, resourceInterface);
    }

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param uri
     *            The target URI.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(String uri, Class<? extends T> resourceInterface) {
        return create(null, new Reference(uri), resourceInterface);
    }

    /** Indicates if idempotent requests should be retried on error. */
    private volatile boolean retryOnError;

    /** Delay in milliseconds between two retry attempts. */
    private volatile long retryDelay;

    /** Number of retry attempts before reporting an error. */
    private volatile int retryAttempts;

    /** Indicates if redirections are followed. */
    private volatile boolean followRedirects;

    /** The next Restlet. */
    private volatile Uniform next;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Context context, Method method, Reference reference) {
        Request request = new Request(method, reference);
        Response response = new Response(request);

        if (context == null) {
            context = Context.getCurrent();
        }

        if (context != null) {
            this.next = context.getClientDispatcher();
        }

        this.followRedirects = true;
        this.retryOnError = true;
        this.retryDelay = 1000L;
        this.retryAttempts = 2;
        init(context, request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, Method method, String uri) {
        this(context, method, new Reference(uri));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, Method method, URI uri) {
        this(context, method, new Reference(uri));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Context context, Reference reference) {
        this(context, Method.GET, reference);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The current context.
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public ClientResource(Context context, Request request, Response response) {
        this.followRedirects = true;
        init(context, request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, String uri) {
        this(context, Method.GET, uri);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, URI uri) {
        this(context, Method.GET, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Method method, Reference reference) {
        this(Context.getCurrent(), method, reference);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Method method, String uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Method method, URI uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The target reference.
     */
    public ClientResource(Reference reference) {
        this(Context.getCurrent(), null, reference);
    }

    /**
     * Constructor.
     * 
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public ClientResource(Request request, Response response) {
        this(Context.getCurrent(), request, response);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(String uri) {
        this(Context.getCurrent(), null, uri);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(URI uri) {
        this(Context.getCurrent(), null, uri);
    }

    /**
     * Deletes the target resource and all its representations.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The optional response entity.
     */
    public Representation delete() throws ResourceException {
        setMethod(Method.DELETE);
        return handle();
    }

    /**
     * Deletes the target resource and all its representations.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param <T>
     *            The expected type for the response entity.
     * @param resultClass
     *            The expected class for the response entity object.
     * @return The response entity object.
     */
    public <T> T delete(Class<T> resultClass) throws ResourceException {
        T result = null;

        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        try {
            updateClientInfo(resultClass);
            result = toObject(delete(), resultClass);
        } finally {
            // Restore the current client info
            setClientInfo(currentClientInfo);
        }

        return result;
    }

    /**
     * Calls the {@link #release()}.
     */
    @Override
    protected void finalize() throws Throwable {
        release();
    }

    /**
     * Represents the resource using content negotiation to select the best
     * variant based on the client preferences.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public Representation get() throws ResourceException {
        setMethod(Method.GET);
        return handle();
    }

    /**
     * Represents the resource in the given object class.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param <T>
     *            The expected type for the response entity.
     * @param resultClass
     *            The expected class for the response entity object.
     * @return The response entity object.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public <T> T get(Class<T> resultClass) throws ResourceException {
        T result = null;

        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        try {
            updateClientInfo(resultClass);
            result = toObject(get(), resultClass);
        } finally {
            // Restore the current client info
            setClientInfo(currentClientInfo);
        }

        return result;
    }

    /**
     * Represents the resource using a given media type.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The representation matching the given media type.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public Representation get(MediaType mediaType) throws ResourceException {
        Representation result = null;

        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        ClientInfo newClientInfo = new ClientInfo();
        newClientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(mediaType));
        setClientInfo(newClientInfo);

        try {
            result = get();
        } finally {
            // Restore the current client info
            setClientInfo(currentClientInfo);
        }

        return result;
    }

    /**
     * Returns the converter service. Creates one if necessary.
     * 
     * @return The converter service.
     */
    private ConverterService getConverterService() {
        ConverterService cs = null;

        if (getApplication() != null) {
            cs = getApplication().getConverterService();
        } else {
            cs = new ConverterService();
        }

        return cs;
    }

    /**
     * Returns the next Restlet. By default, it is the client dispatcher if a
     * context is available.
     * 
     * @return The next Restlet or null.
     */
    public Uniform getNext() {
        return this.next;
    }

    /**
     * Returns the parent resource. The parent resource is defined in the sense
     * of hierarchical URIs. If the resource URI is not hierarchical, then an
     * exception is thrown.
     * 
     * @return The parent resource.
     */
    public ClientResource getParent() throws ResourceException {
        ClientResource result = null;

        if (getReference().isHierarchical()) {
            Reference parentRef = getReference().getParentRef();
            result = new ClientResource(parentRef);
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "The resource URI is not hierarchical.");
        }

        return result;
    }

    /**
     * Returns the number of retry attempts before reporting an error. Default
     * value is 2.
     * 
     * @return The number of retry attempts before reporting an error.
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Returns the delay in milliseconds between two retry attempts. Default
     * value is 1 second.
     * 
     * @return The delay in milliseconds between two retry attempts.
     */
    public long getRetryDelay() {
        return retryDelay;
    }

    /**
     * Handles the call by invoking the next handler.
     * 
     * @return The optional response entity.
     * @see #getNext()
     */
    @Override
    public Representation handle() {
        Representation result = null;

        if (!hasNext()) {
            Protocol protocol = (getReference() == null) ? null
                    : getReference().getSchemeProtocol();

            if (protocol != null) {
                setNext(new Client(protocol));
            }
        }

        if (hasNext()) {
            handle(getRequest(), getResponse(), null, 0);
            result = getResponse().getEntity();
        } else {
            getLogger()
                    .warning(
                            "Unable to process the call for a client resource. No next Restlet has been provided.");
        }

        return result;
    }

    /**
     * Handle the call and follow redirection for safe methods.
     * 
     * @param request
     *            The request to send.
     * @param response
     *            The response to update.
     * @param references
     *            The references that caused a redirection to prevent infinite
     *            loops.
     */
    private void handle(Request request, Response response,
            List<Reference> references, int retryAttempt) {
        // Actually handle the call
        getNext().handle(request, response);

        // Check for redirections
        if (request.getMethod().isSafe()
                && response.getStatus().isRedirection()
                && response.getLocationRef() != null) {
            Reference newTargetRef = response.getLocationRef();

            if ((references != null) && references.contains(newTargetRef)) {
                getLogger().warning(
                        "Infinite redirection loop detected with URI: "
                                + newTargetRef);
            } else if (request.getEntity() != null
                    && !request.isEntityAvailable()) {
                getLogger()
                        .warning(
                                "Unable to follow the redirection because the request entity isn't available anymore.");
            } else {
                if (references == null) {
                    references = new ArrayList<Reference>();
                }

                // Add to the list of redirection reference
                // to prevent infinite loops
                references.add(request.getResourceRef());
                request.setResourceRef(newTargetRef);
                handle(request, response, references, 0);
            }
        } else if (request.getMethod().isIdempotent()
                && response.getStatus().isRecoverableError()) {
            if (isRetryOnError() && (retryAttempt < getRetryAttempts())) {
                if ((getRequestEntity() == null)
                        || getRequestEntity().isAvailable()) {
                    getLogger().log(
                            Level.INFO,
                            "A recoverable error was detected, attempting again in "
                                    + getRetryDelay() + " ms.");

                    // Wait before attempting again
                    if (getRetryDelay() > 0) {
                        try {
                            Thread.sleep(getRetryDelay());
                        } catch (InterruptedException e) {
                            getLogger().log(Level.FINE,
                                    "Retry delay sleep was interrupted", e);
                        }
                    }

                    // Retry the call
                    handle(request, response, references, ++retryAttempt);
                }
            }
        }
    }

    /**
     * Indicates if there is a next Restlet.
     * 
     * @return True if there is a next Restlet.
     */
    public boolean hasNext() {
        return getNext() != null;
    }

    /**
     * Represents the resource using content negotiation to select the best
     * variant based on the client preferences. This method is identical to
     * {@link #get()} but doesn't return the actual content of the
     * representation, only its metadata.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
     */
    public Representation head() throws ResourceException {
        setMethod(Method.HEAD);
        return handle();
    }

    /**
     * Represents the resource using a given media type. This method is
     * identical to {@link #get(MediaType)} but doesn't return the actual
     * content of the representation, only its metadata.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The representation matching the given media type.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
     */
    public Representation head(MediaType mediaType) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        ClientInfo newClientInfo = new ClientInfo();
        newClientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(mediaType));
        setClientInfo(newClientInfo);
        Representation result = head();

        // Restore the current client info
        setClientInfo(currentClientInfo);
        return result;
    }

    /**
     * Indicates if redirections are followed.
     * 
     * @return True if redirections are followed.
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * Indicates if idempotent requests should be retried on error. Default
     * value is true.
     * 
     * @return True if idempotent requests should be retried on error.
     */
    public boolean isRetryOnError() {
        return retryOnError;
    }

    /**
     * Describes the resource using content negotiation to select the best
     * variant based on the client preferences.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best description.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public Representation options() throws ResourceException {
        setMethod(Method.OPTIONS);
        return handle();
    }

    /**
     * Describes the resource using a given media type.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The matched description or null.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public Representation options(MediaType mediaType) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        ClientInfo newClientInfo = new ClientInfo();
        newClientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(mediaType));
        setClientInfo(newClientInfo);
        Representation result = options();

        // Restore the current client info
        setClientInfo(currentClientInfo);
        return result;
    }

    /**
     * Posts an object entity. Automatically serializes the object using the
     * {@link ConverterService}.
     * 
     * @param entity
     *            The object entity to post.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public Representation post(Object entity) throws ResourceException {
        return post(toRepresentation(entity));
    }

    /**
     * Posts an object entity. Automatically serializes the object using the
     * {@link ConverterService}.
     * 
     * @param entity
     *            The object entity to post.
     * @param resultClass
     *            The class of the response entity.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public <T> T post(Object entity, Class<T> resultClass)
            throws ResourceException {
        T result = null;

        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        try {
            updateClientInfo(resultClass);
            result = toObject(post(toRepresentation(entity)), resultClass);
        } finally {
            // Restore the current client info
            setClientInfo(currentClientInfo);
        }

        return result;
    }

    /**
     * Posts a representation. If a success status is not returned, then a
     * resource exception is thrown.
     * 
     * @param entity
     *            The posted entity.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public Representation post(Representation entity) throws ResourceException {
        setMethod(Method.POST);
        getRequest().setEntity(entity);
        return handle();
    }

    /**
     * Puts an object entity. Automatically serializes the object using the
     * {@link ConverterService}.
     * 
     * @param entity
     *            The object entity to put.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public Representation put(Object entity) throws ResourceException {
        return put(toRepresentation(entity));
    }

    /**
     * Puts an object entity. Automatically serializes the object using the
     * {@link ConverterService}.
     * 
     * @param entity
     *            The object entity to put.
     * @param resultClass
     *            The class of the response entity.
     * @return The response object entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public <T> T put(Object entity, Class<T> resultClass)
            throws ResourceException {
        T result = null;

        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        try {
            updateClientInfo(resultClass);
            result = toObject(put(toRepresentation(entity)), resultClass);
        } finally {
            // Restore the current client info
            setClientInfo(currentClientInfo);
        }

        return result;
    }

    /**
     * Creates or updates a resource with the given representation as new state
     * to be stored.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param representation
     *            The representation to store.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public Representation put(Representation representation)
            throws ResourceException {
        setMethod(Method.PUT);
        getRequest().setEntity(representation);
        return handle();
    }

    /**
     * Sets the authentication response sent by a client to an origin server.
     * 
     * @param challengeResponse
     *            The authentication response sent by a client to an origin
     *            server.
     * @see Request#setChallengeResponse(ChallengeResponse)
     */
    public void setChallengeResponse(ChallengeResponse challengeResponse) {
        getRequest().setChallengeResponse(challengeResponse);
    }

    /**
     * Sets the authentication response sent by a client to an origin server
     * given a scheme, identifier and secret.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param identifier
     *            The user identifier, such as a login name or an access key.
     * @param secret
     *            The user secret, such as a password or a secret key.
     */
    public void setChallengeResponse(ChallengeScheme scheme,
            final String identifier, String secret) {
        setChallengeResponse(new ChallengeResponse(scheme, identifier, secret));
    }

    /**
     * Sets the client-specific information.
     * 
     * @param clientInfo
     *            The client-specific information.
     * @see Request#setClientInfo(ClientInfo)
     */
    public void setClientInfo(ClientInfo clientInfo) {
        getRequest().setClientInfo(clientInfo);
    }

    /**
     * Sets the conditions applying to this request.
     * 
     * @param conditions
     *            The conditions applying to this request.
     * @see Request#setConditions(Conditions)
     */
    public void setConditions(Conditions conditions) {
        getRequest().setConditions(conditions);
    }

    /**
     * Sets the cookies provided by the client.
     * 
     * @param cookies
     *            The cookies provided by the client.
     * @see Request#setCookies(Series)
     */
    public void setCookies(Series<Cookie> cookies) {
        getRequest().setCookies(cookies);
    }

    /**
     * Indicates if redirections are followed.
     * 
     * @param followRedirects
     *            True if redirections are followed.
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * Sets the host reference.
     * 
     * @param hostRef
     *            The host reference.
     * @see Request#setHostRef(Reference)
     */
    public void setHostRef(Reference hostRef) {
        getRequest().setHostRef(hostRef);
    }

    /**
     * Sets the host reference using an URI string.
     * 
     * @param hostUri
     *            The host URI.
     * @see Request#setHostRef(String)
     */
    public void setHostRef(String hostUri) {
        getRequest().setHostRef(hostUri);
    }

    /**
     * Sets the method called.
     * 
     * @param method
     *            The method called.
     * @see Request#setMethod(Method)
     */
    public void setMethod(Method method) {
        getRequest().setMethod(method);
    }

    /**
     * Sets the next handler such as a Restlet or a Filter.
     * 
     * In addition, this method will set the context of the next Restlet if it
     * is null by passing a reference to its own context.
     * 
     * @param next
     *            The next handler.
     */
    public void setNext(org.restlet.Uniform next) {
        if (next instanceof Restlet) {
            Restlet nextRestlet = (Restlet) next;

            if (nextRestlet.getContext() == null) {
                nextRestlet.setContext(getContext());
            }
        }

        this.next = next;
    }

    /**
     * Sets the original reference requested by the client.
     * 
     * @param originalRef
     *            The original reference.
     * @see Request#setOriginalRef(Reference)
     */
    public void setOriginalRef(Reference originalRef) {
        getRequest().setOriginalRef(originalRef);
    }

    /**
     * Sets the ranges to return from the target resource's representation.
     * 
     * @param ranges
     *            The ranges.
     * @see Request#setRanges(List)
     */
    public void setRanges(List<Range> ranges) {
        getRequest().setRanges(ranges);
    }

    /**
     * Sets the target resource reference. If the reference is relative, it will
     * be resolved as an absolute reference. Also, the context's base reference
     * will be reset. Finally, the reference will be normalized to ensure a
     * consistent handling of the call.
     * 
     * @param resourceRef
     *            The resource reference.
     * @see Request#setResourceRef(Reference)
     */
    public void setReference(Reference resourceRef) {
        getRequest().setResourceRef(resourceRef);
    }

    /**
     * Sets the referrer reference if available.
     * 
     * @param referrerRef
     *            The referrer reference.
     * @see Request#setReferrerRef(Reference)
     */
    public void setReferrerRef(Reference referrerRef) {
        getRequest().setReferrerRef(referrerRef);
    }

    /**
     * Sets the referrer reference if available using an URI string.
     * 
     * @param referrerUri
     *            The referrer URI.
     * @see Request#setReferrerRef(String)
     */
    public void setReferrerRef(String referrerUri) {
        getRequest().setReferrerRef(referrerUri);
    }

    /**
     * Sets the target resource reference using a Reference. Note that the
     * Reference can be either absolute or relative to the context's base
     * reference.
     * 
     * @param resourceRef
     *            The resource Reference.
     * @see Request#setResourceRef(Reference)
     */
    public void setResourceRef(Reference resourceRef) {
        getRequest().setResourceRef(resourceRef);
    }

    /**
     * Sets the target resource reference using an URI string. Note that the URI
     * can be either absolute or relative to the context's base reference.
     * 
     * @param resourceUri
     *            The resource URI.
     * @see Request#setResourceRef(String)
     */
    public void setResourceRef(String resourceUri) {
        getRequest().setResourceRef(resourceUri);
    }

    /**
     * Sets the number of retry attempts before reporting an error.
     * 
     * @param retryAttempts
     *            The number of retry attempts before reporting an error.
     */
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    /**
     * Sets the delay in milliseconds between two retry attempts.
     * 
     * @param retryDelay
     *            The delay in milliseconds between two retry attempts.
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Indicates if idempotent requests should be retried on error.
     * 
     * @param retryOnError
     *            True if idempotent requests should be retried on error.
     */
    public void setRetryOnError(boolean retryOnError) {
        this.retryOnError = retryOnError;
    }

    /**
     * Converts a representation into a Java object. Leverages the
     * {@link ConverterService}.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param source
     *            The source representation to convert.
     * @param target
     *            The target class of the Java object.
     * @return The converted Java object.
     * @throws ResourceException
     */
    protected <T> T toObject(Representation source, Class<T> target)
            throws ResourceException {
        T result = null;

        if (source != null) {
            try {
                ConverterService cs = getConverterService();
                result = cs.toObject(source, target, this);
            } catch (Exception e) {
                throw new ResourceException(e);
            }
        }

        return result;
    }

    /**
     * Converts an object into a representation based on client preferences.
     * 
     * @param source
     *            The object to convert.
     * @return The wrapper representation.
     */
    protected Representation toRepresentation(Object source) {
        Representation result = null;

        if (source != null) {
            ConverterService cs = getConverterService();
            result = cs.toRepresentation(source);
        }

        return result;
    }

    /**
     * Update the client preferences to match the given response class.
     * 
     * @param resultClass
     *            The result class to match.
     */
    private <T> void updateClientInfo(Class<T> resultClass) {
        ConverterService cs = getConverterService();
        updateClientInfo(cs.getVariants(resultClass, null));
    }

    /**
     * Update the client preferences to match the given response class.
     * 
     * @param resultClass
     *            The result class to match.
     */
    private <T> void updateClientInfo(List<? extends Variant> variants) {
        // Create a fresh one for this request
        ClientInfo newClientInfo = new ClientInfo();

        if (variants != null) {
            for (Variant variant : variants) {
                newClientInfo.getAcceptedMediaTypes().add(
                        new Preference<MediaType>(variant.getMediaType()));
            }
        }

        setClientInfo(newClientInfo);
    }

    /**
     * Wraps the client resource to proxy calls to the given Java interface into
     * Restlet method calls.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    @SuppressWarnings("unchecked")
    public <T> T wrap(Class<? extends T> resourceInterface) {
        // Introspect the interface for Restlet annotations
        final List<AnnotationInfo> annotations = AnnotationUtils
                .getAnnotationDescriptors(resourceInterface);

        // Create the client resource proxy
        InvocationHandler h = new InvocationHandler() {

            public Object invoke(Object proxy,
                    java.lang.reflect.Method javaMethod, Object[] args)
                    throws Throwable {
                Object result = null;
                AnnotationInfo annotation = AnnotationUtils.getAnnotation(
                        annotations, javaMethod);

                if (annotation != null) {
                    // Save the current client info
                    ClientInfo currentClientInfo = getClientInfo();

                    try {
                        List<Variant> responseVariants = annotation
                                .getResponseVariants(getApplication()
                                        .getMetadataService());

                        if (responseVariants != null) {
                            updateClientInfo(responseVariants);
                        } else if (annotation.getJavaReturnType() != null) {
                            updateClientInfo(annotation.getJavaReturnType());
                        }

                        // The Java method was annotated
                        setMethod(annotation.getRestletMethod());

                        if ((args != null) && args.length > 0) {
                            Representation entity = toRepresentation(args[0]);
                            getRequest().setEntity(entity);
                        }

                        handle();

                        if (annotation.getJavaReturnType() != null) {
                            result = toObject(getResponseEntity(), annotation
                                    .getJavaReturnType());
                        }
                    } finally {
                        // Restore the current client info
                        setClientInfo(currentClientInfo);
                    }
                }

                return result;
            }

        };

        // Instantiate our dynamic proxy
        return (T) Proxy.newProxyInstance(Engine.getClassLoader(),
                new Class<?>[] { resourceInterface }, h);
    }
}
