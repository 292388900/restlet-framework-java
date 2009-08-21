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

package org.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.restlet.engine.Engine;
import org.restlet.engine.RestletHelper;
import org.restlet.engine.application.ApplicationHelper;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.resource.Finder;
import org.restlet.security.Role;
import org.restlet.service.ConnectorService;
import org.restlet.service.ConverterService;
import org.restlet.service.DecoderService;
import org.restlet.service.MetadataService;
import org.restlet.service.RangeService;
import org.restlet.service.Service;
import org.restlet.service.StatusService;
import org.restlet.service.TunnelService;

/**
 * Restlet managing a coherent set of Resources and Services. Applications are
 * guaranteed to receive calls with their base reference set relatively to the
 * VirtualHost that served them. This class is both a descriptor able to create
 * the root Restlet and the actual Restlet that can be attached to one or more
 * VirtualHost instances.<br>
 * <br>
 * Applications also have many useful services associated. They are all enabled
 * by default and are available as properties that can be eventually overridden:
 * <ul>
 * <li>"connectorService" to declare necessary client and server connectors.</li>
 * <li>"converterService" to convert between regular objects and
 * representations.</li>
 * <li>"decoderService" to automatically decode or uncompress request entities.</li>
 * <li>"metadataService" to provide access to metadata and their associated
 * extension names.</li>
 * <li>"rangeService" to automatically exposes ranges of response entities.</li>
 * <li>"statusService" to provide common representations for exception status.</li>
 * <li>"taskService" to run tasks asynchronously.</li>
 * <li>"tunnelService" to tunnel method names or client preferences via query
 * parameters.</li>
 * </ul>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class Application extends Restlet {
    private static final ThreadLocal<Application> CURRENT = new ThreadLocal<Application>();

    /**
     * This variable is stored internally as a thread local variable and updated
     * each time a call enters an application.
     * 
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current application using methods such as
     * {@link org.restlet.resource.Resource#getApplication()}
     * 
     * @return The current context.
     */
    public static Application getCurrent() {
        return CURRENT.get();
    }

    /**
     * Sets the context to associated with the current thread.
     * 
     * @param application
     *            The thread's context.
     */
    public static void setCurrent(Application application) {
        CURRENT.set(application);
    }

    /** Finder class to instantiate. */
    private volatile Class<? extends Finder> finderClass;

    /** The helper provided by the implementation. */
    private volatile RestletHelper<Application> helper;

    /** The modifiable list of roles. */
    private final List<Role> roles;

    /** The root Restlet. */
    private volatile Restlet root;

    /** The list of services. */
    private final List<Service> services;

    /**
     * Constructor. Note this constructor is convenient because you don't have
     * to provide a context like for {@link #Application(Context)}. Therefore
     * the context will initially be null. It's only when you attach the
     * application to a virtual host via one of its attach*() methods that a
     * proper context will be set.
     */
    public Application() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use based on parent component context. This
     *            context should be created using the
     *            {@link Context#createChildContext()} method to ensure a proper
     *            isolation with the other applications.
     */
    public Application(Context context) {
        super(context);

        if (Engine.getInstance() != null) {
            this.helper = new ApplicationHelper(this);
        }

        this.roles = new CopyOnWriteArrayList<Role>();
        this.root = null;
        this.services = new CopyOnWriteArrayList<Service>();
        this.services.add(new TunnelService(true, true));
        this.services.add(new StatusService());
        this.services.add(new DecoderService());
        this.services.add(new RangeService());
        this.services.add(new ConnectorService());
        this.services.add(new ConverterService());
        this.services.add(new MetadataService());

        // [ifndef gae]
        this.services.add(new org.restlet.service.TaskService());
        // [enddef]
    }

    /**
     * Creates a root Restlet that will receive all incoming calls. In general,
     * instances of Router, Filter or Handler classes will be used as initial
     * application Restlet. The default implementation returns null by default.
     * This method is intended to be overridden by subclasses.
     * 
     * @return The root Restlet.
     */
    public Restlet createRoot() {
        return null;
    }

    /**
     * Finds the role associated to the given name.
     * 
     * @param name
     *            The name of the role to find.
     * @return The role matched or null.
     */
    public Role findRole(String name) {
        for (Role role : getRoles()) {
            if (role.getName().equals(name)) {
                return role;
            }
        }

        return null;
    }

    /**
     * Returns the connector service. The service is enabled by default.
     * 
     * @return The connector service.
     */
    public ConnectorService getConnectorService() {
        return getService(ConnectorService.class);
    }

    /**
     * Returns the converter service. The service is enabled by default.
     * 
     * @return The converter service.
     */
    public ConverterService getConverterService() {
        return getService(ConverterService.class);
    }

    /**
     * Returns the decoder service. The service is enabled by default.
     * 
     * @return The decoder service.
     */
    public DecoderService getDecoderService() {
        return getService(DecoderService.class);
    }

    /**
     * Returns the finder class to instantiate.
     * 
     * @return the finder class to instantiate.
     */
    public Class<? extends Finder> getFinderClass() {
        return finderClass;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private RestletHelper<Application> getHelper() {
        return this.helper;
    }

    /**
     * Returns the metadata service. The service is enabled by default.
     * 
     * @return The metadata service.
     */
    public MetadataService getMetadataService() {
        return getService(MetadataService.class);
    }

    /**
     * Returns the range service.
     * 
     * @return The range service.
     */
    public RangeService getRangeService() {
        return getService(RangeService.class);
    }

    /**
     * Returns the modifiable list of roles.
     * 
     * @return The modifiable list of roles.
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Returns the root Restlet. Invokes the createRoot() method if no root has
     * been set, and stores the Restlet created for future uses.
     * 
     * @return The root Restlet.
     */
    public synchronized Restlet getRoot() {
        if (this.root == null) {
            this.root = createRoot();
        }

        return this.root;
    }

    /**
     * Returns a service matching a given service class.
     * 
     * @param <T>
     *            The service type.
     * @param clazz
     *            The service class to match.
     * @return The matched service instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(Class<T> clazz) {
        for (Service service : getServices()) {
            if (clazz.isAssignableFrom(service.getClass())) {
                return (T) service;
            }
        }

        return null;
    }

    /**
     * Returns the modifiable list of services.
     * 
     * @return The modifiable list of services.
     */
    public List<Service> getServices() {
        return services;
    }

    /**
     * Returns the status service. The service is enabled by default.
     * 
     * @return The status service.
     */
    public StatusService getStatusService() {
        return getService(StatusService.class);
    }

    /**
     * Returns a task service to run concurrent tasks. The service is enabled by
     * default.
     * 
     * @return A task service.
     */
    // [ifndef gae] method
    public org.restlet.service.TaskService getTaskService() {
        return getService(org.restlet.service.TaskService.class);
    }

    /**
     * Returns the tunnel service. The service is enabled by default.
     * 
     * @return The tunnel service.
     */
    public TunnelService getTunnelService() {
        return getService(TunnelService.class);
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null) {
            getHelper().handle(request, response);
        }
    }

    /**
     * Sets the connector service.
     * 
     * @param connectorService
     *            The connector service.
     */
    public void setConnectorService(ConnectorService connectorService) {
        setService(connectorService);
    }

    /**
     * Sets the converter service.
     * 
     * @param converterService
     *            The converter service.
     */
    public void setConverterService(ConverterService converterService) {
        setService(converterService);
    }

    /**
     * Sets the decoder service.
     * 
     * @param decoderService
     *            The decoder service.
     */
    public void setDecoderService(DecoderService decoderService) {
        setService(decoderService);
    }

    /**
     * Sets the finder class to instantiate.
     * 
     * @param finderClass
     *            The finder class to instantiate.
     */
    public void setFinderClass(Class<? extends Finder> finderClass) {
        this.finderClass = finderClass;
    }

    /**
     * Sets the metadata service.
     * 
     * @param metadataService
     *            The metadata service.
     */
    public void setMetadataService(MetadataService metadataService) {
        setService(metadataService);
    }

    /**
     * Sets the range service.
     * 
     * @param rangeService
     *            The range service.
     */
    public void setRangeService(RangeService rangeService) {
        setService(rangeService);
    }

    /**
     * Sets the list of roles.
     * 
     * @param roles
     *            The list of roles.
     */
    public void setRoles(List<Role> roles) {
        this.roles.clear();

        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    /**
     * Sets the root Resource class.
     * 
     * @param rootClass
     *            The root Resource class.
     */
    public synchronized void setRoot(Class<?> rootClass) {
        this.root = Finder.createFinder(rootClass, getFinderClass(),
                getContext(), getLogger());
    }

    /**
     * Sets the root Restlet.
     * 
     * @param root
     *            The root Restlet.
     */
    public synchronized void setRoot(Restlet root) {
        this.root = root;
    }

    /**
     * Replaces or adds a service. The replacement is based on the service
     * class.
     * 
     * @param newService
     *            The new service to set.
     */
    protected synchronized void setService(Service newService) {
        List<Service> services = new ArrayList<Service>();
        Service service;
        boolean replaced = false;

        for (int i = 0; (i < this.services.size()); i++) {
            service = this.services.get(i);

            if (service != null) {
                if (service.getClass().isAssignableFrom(newService.getClass())) {
                    try {
                        service.stop();
                    } catch (Exception e) {
                        getLogger().log(Level.WARNING,
                                "Unable to stop service replaced", e);
                    }

                    services.add(newService);
                    replaced = true;
                } else {
                    services.add(service);
                }
            }
        }

        if (!replaced) {
            services.add(newService);
        }

        setServices(services);
    }

    /**
     * Sets the list of services.
     * 
     * @param services
     *            The list of services.
     */
    public synchronized void setServices(List<Service> services) {
        this.services.clear();

        if (services != null) {
            this.services.addAll(services);
        }
    }

    /**
     * Sets the status service.
     * 
     * @param statusService
     *            The status service.
     */
    public void setStatusService(StatusService statusService) {
        setService(statusService);
    }

    /**
     * Sets the task service.
     * 
     * @param taskService
     *            The task service.
     */
    // [ifndef gae] method
    public void setTaskService(org.restlet.service.TaskService taskService) {
        setService(taskService);
    }

    /**
     * Sets the tunnel service.
     * 
     * @param tunnelService
     *            The tunnel service.
     */
    public void setTunnelService(TunnelService tunnelService) {
        setService(tunnelService);
    }

    /**
     * Starts the application then all the enabled associated services.
     */
    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            super.start();

            if (getHelper() != null) {
                getHelper().start();
            }

            for (Service service : getServices()) {
                service.start();
            }
        }
    }

    /**
     * Stops all the enabled associated services the the application itself.
     */
    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            for (Service service : getServices()) {
                service.stop();
            }

            if (getHelper() != null) {
                getHelper().stop();
            }

            // Clear the annotations cache
            AnnotationUtils.clearCache();

            super.stop();
        }
    }

}
