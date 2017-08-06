package settings;

/**
 * @author Ritwik Banerjee
 * @author Po Yiu Ho
 */
public enum InitializationParameters {

    APP_PROPERTIES_XML("app-properties.xml"),
    WORKSPACE_PROPERTIES_XML("workspace-properties.xml"),
    PROPERTIES_SCHEMA_XSD("properties-schema.xsd"),
    ERROR_DIALOG_BUTTON_LABEL("I understand."),
    APP_WORKDIR_PATH("./BuzzWord/profiles/"),
    APP_IMAGEDIR_PATH("images");

    private String parameter;

    InitializationParameters(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}