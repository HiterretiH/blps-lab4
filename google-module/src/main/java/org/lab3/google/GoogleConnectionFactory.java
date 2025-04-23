package org.lab3.google;

import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.io.Serializable;

public class GoogleConnectionFactory implements Serializable, Referenceable {
    private final ConnectionManager manager;
    private final ManagedConnectionFactory mcf;
    private Reference reference;

    public GoogleConnectionFactory(ManagedConnectionFactory mcf, ConnectionManager manager) {
        this.mcf = mcf;
        this.manager = manager;
    }

    public GoogleConnection getConnection() throws ResourceException {
        return (GoogleConnection) manager.allocateConnection(mcf, null);
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        return reference;
    }
}