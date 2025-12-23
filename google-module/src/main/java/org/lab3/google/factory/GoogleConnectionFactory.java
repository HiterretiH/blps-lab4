package org.lab3.google.factory;

import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;
import java.io.Serializable;
import javax.naming.Reference;
import org.lab3.google.service.GoogleConnection;

public final class GoogleConnectionFactory
    implements Serializable, Referenceable {
  private final ConnectionManager manager;
  private final ManagedConnectionFactory mcf;
  private Reference reference;

  public GoogleConnectionFactory(
      final ManagedConnectionFactory managedConnectionFactory,
      final ConnectionManager connectionManager) {
    this.mcf = managedConnectionFactory;
    this.manager = connectionManager;
  }

  public GoogleConnection getConnection() throws ResourceException {
    return (GoogleConnection) manager.allocateConnection(mcf, null);
  }

  @Override
  public void setReference(final Reference ref) {
    this.reference = ref;
  }

  @Override
  public Reference getReference() {
    return reference;
  }
}
