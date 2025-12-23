package org.lab3.google.resource;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.sheets.v4.Sheets;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import org.lab3.google.service.GoogleConnectionImpl;

public final class GoogleManagedConnection implements ManagedConnection {
  private static final int MAX_CONNECTIONS = 10;
  private static final String APPLICATION_NAME = "Google JCA Adapter";
  private static final String EIS_PRODUCT_NAME = "Google API";
  private static final String EIS_PRODUCT_VERSION = "1.0";
  private static final String USER_NAME = "google-user";
  private static final String DRIVE_SCOPE =
      "https://www.googleapis.com/auth/drive";
  private static final String SHEETS_SCOPE =
      "https://www.googleapis.com/auth/spreadsheets";
  private static final String FORMS_SCOPE =
      "https://www.googleapis.com/auth/forms";

  private static final Logger LOG =
      Logger.getLogger(GoogleManagedConnection.class.getName());

  private final Drive driveService;
  private final Sheets sheetsService;
  private final Forms formsService;
  private ConnectionEventListener listener;

  public GoogleManagedConnection(final String jsonCredentials)
      throws IOException, GeneralSecurityException {
    this(new ByteArrayInputStream(jsonCredentials.getBytes()));
  }

  public GoogleManagedConnection(final InputStream credentialsStream)
      throws IOException, GeneralSecurityException {
    NetHttpTransport httpTransport = GoogleNetHttpTransport
        .newTrustedTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    List<String> scopes = Arrays
        .asList(DRIVE_SCOPE, SHEETS_SCOPE, FORMS_SCOPE);

    GoogleCredential credential = GoogleCredential
        .fromStream(credentialsStream).createScoped(scopes);

    this.driveService = new Drive
        .Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();

    this.sheetsService = new Sheets
        .Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();

    this.formsService = new Forms
        .Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  public Drive getDriveService() {
    return driveService;
  }

  public Sheets getSheetsService() {
    return sheetsService;
  }

  public Forms getFormsService() {
    return formsService;
  }

  @Override
  public Object getConnection(final Subject subject,
                              final ConnectionRequestInfo info) {
    return new GoogleConnectionImpl(this);
  }

  @Override
  public void destroy() {
    LOG.info("Google connection destroyed");
  }

  @Override
  public void cleanup() {
  }

  @Override
  public void associateConnection(final Object connection) {
  }

  @Override
  public void addConnectionEventListener(
      final ConnectionEventListener connectionEventListener) {
    this.listener = connectionEventListener;
  }

  @Override
  public void removeConnectionEventListener(
      final ConnectionEventListener connectionEventListener) {
    if (this.listener == connectionEventListener) {
      this.listener = null;
    }
  }

  @Override
  public XAResource getXAResource() throws ResourceException {
    return null;
  }

  @Override
  public LocalTransaction getLocalTransaction() throws ResourceException {
    return null;
  }

  @Override
  public ManagedConnectionMetaData getMetaData() throws ResourceException {
    return new ManagedConnectionMetaData() {
      @Override
      public String getEISProductName() {
        return EIS_PRODUCT_NAME;
      }

      @Override
      public String getEISProductVersion() {
        return EIS_PRODUCT_VERSION;
      }

      @Override
      public int getMaxConnections() {
        return MAX_CONNECTIONS;
      }

      @Override
      public String getUserName() {
        return USER_NAME;
      }
    };
  }

  @Override
  public void setLogWriter(final PrintWriter out) throws ResourceException {
  }

  @Override
  public PrintWriter getLogWriter() throws ResourceException {
    return null;
  }
}
