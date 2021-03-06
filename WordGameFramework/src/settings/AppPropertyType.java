package settings;

/**
 * This enum provides properties that are to be loaded via
 * XML files to be used for setting up the application.
 *
 * @author Richard McKenna, Ritwik Banerjee
 * @author Po Yiu Ho
 * @version 1.0
 */
@SuppressWarnings("unused")
public enum AppPropertyType {

    // from app-properties.xml
    APP_WINDOW_WIDTH,
    APP_WINDOW_HEIGHT,
    APP_TITLE,
    APP_LOGO,
    APP_PIC,
    APP_CSS,
    APP_PATH_CSS,

    // APPLICATION ICONS
    LOGIN_ICON,
    CREATE_PROFILE_ICON,
    HELP_ICON,
    PLAY_ICON,
    PAUSE_ICON,
    REPLAY_ICON,
    SAVE_ICON,
    DICTIONARY_ICON,
    ANIMAL_ICON,
    NAME_ICON,

    // APPLICATION TOOLTIPS FOR BUTTONS
    LOGIN_TOOLTIP,
    CREATE_PROFILE_TOOLTIP,
    HELP_TOOLTIP,
    SAVE_TOOLTIP,
    PLAY_TOOLTIP,
    PAUSE_TOOLTIP,
    REPLAY_TOOLTIP,

    // WORDS FOR BUTTONS
    LOGIN_WORDS_ICON,
    CREATE_PROFILE_WORDS_ICON,
    HELP_WORDS_ICON,

    // ERROR MESSAGES
    CREATE_PROFILE_ERROR_MESSAGE,
    SAVE_ERROR_MESSAGE,
    LOGIN_ERROR_MESSAGE,
    PROPERTIES_LOAD_ERROR_MESSAGE,
    LOAD_IMAGE_ERROR_MESSAGE,
    INITIALIZATION_ERROR_MESSAGE,

    // ERROR TITLES
    CREATE_PROFILE_ERROR_TITLE,
    SAVE_ERROR_TITLE,
    LOGIN_ERROR_TITLE,
    PROPERTIES_LOAD_ERROR_TITLE,
    LOAD_IMAGE_ERROR_TITLE,
    INITIALIZATION_ERROR_TITLE,

    // AND VERIFICATION MESSAGES AND TITLES
    NEW_PROFILE_MESSAGE,
    NEW_PROFILE_TITLE,
    SAVE_COMPLETED_MESSAGE,
    SAVE_COMPLETED_TITLE,
    SAVE_UNSAVED_WORK_TITLE,
    SAVE_UNSAVED_WORK_MESSAGE,
    LOAD_COMPLETED_TITLE,
    LOAD_COMPLETED_MESSAGE,

    // MESSAGES
    PERSONAL_BEST_MESSAGE,
    PERSONAL_BEST_TITLE,

    SAVE_WORK_TITLE,
    LOAD_WORK_TITLE,
    WORK_FILE_EXT,
    WORK_FILE_EXT_DESC,
    PROPERTIES_
}
