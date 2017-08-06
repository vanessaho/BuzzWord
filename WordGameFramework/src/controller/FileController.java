package controller;

import java.io.IOException;

/**
 * @author Po Yiu Ho
 */
public interface FileController {
    boolean handleLoginRequest(String s, String p) throws IOException;

    void handleCreateNewProfileRequest(String s, String p) throws IOException;

    void handleSaveProgressRequest() throws IOException;

    void handleHelpRequest();

}
