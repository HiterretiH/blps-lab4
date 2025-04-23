package org.lab3.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.sheets.v4.Sheets;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;   

public class GoogleManagedConnection implements ManagedConnection {
    private static final Logger log = Logger.getLogger(GoogleManagedConnection.class.getName());

    private final Drive driveService;
    private final Sheets sheetsService;
    private final Forms formsService;
    private ConnectionEventListener listener;

    public GoogleManagedConnection(String jsonCredentials) throws IOException, GeneralSecurityException {
        this(new ByteArrayInputStream(jsonCredentials.getBytes()));
    }

    public GoogleManagedConnection(InputStream credentialsStream) throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        List<String> scopes = Arrays.asList(
                "https://www.googleapis.com/auth/drive",
                "https://www.googleapis.com/auth/spreadsheets",
                "https://www.googleapis.com/auth/forms"
        );

        GoogleCredential credential = GoogleCredential.fromStream(credentialsStream)
                .createScoped(scopes);

        this.driveService = new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Google JCA Adapter")
                .build();

        this.sheetsService = new Sheets.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Google JCA Adapter")
                .build();

        this.formsService = new Forms.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Google JCA Adapter")
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
    public Object getConnection(Subject subject, ConnectionRequestInfo info) throws ResourceException {
        return new GoogleConnectionImpl(this);
    }

    @Override
    public void destroy() {
        log.info("Google connection destroyed");
    }

    @Override
    public void cleanup() {
        // Cleanup resources if needed
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        // Implementation if needed
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        if (this.listener == listener) {
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
                return "Google API";
            }

            @Override
            public String getEISProductVersion() {
                return "1.0";
            }

            @Override
            public int getMaxConnections() {
                return 10;
            }

            @Override
            public String getUserName() {
                return "google-user";
            }
        };
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        // Implementation if needed
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }
}