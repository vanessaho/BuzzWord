package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import components.AppDataComponent;
import components.AppFileComponent;
import propertymanager.PropertyManager;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import ui.AppMessageDialogSingleton;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import static com.sun.javafx.font.FontResource.SALT;
import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_WORKDIR_PATH;

/**
 * @author Po Yiu Ho
 */
public class GameFile implements AppFileComponent {
    public static final String PROFILE = "PROFILE";
    public static final String PROFILE_USERNAME = "PROFILE_USERNAME";
    public static final String ENCRYPTED_PASSWORD = "ENCRYPTED_PASSWORD";
    public static final String ENGLISH_DICTIONARY_LEVEL = "ENGLISH_DICTIONARY_LEVEL";
    public static final String ANIMALS_LEVEL = "ANIMALS_LEVEL";
    public static final String FIRST_NAMES_LEVEL = "FIRST_NAMES_LEVEL";
    public static final String ANIMALS = "Animals";
    public static final String ENGLISH_DICTIONARY = "English Dictionary";
    public static final String FIRST_NAMES = "First Names";
    private static final char[] PASSWORD = "poyiuhopass".toCharArray();
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };

    @Override
    public void saveData(AppDataComponent d, Path filePath) throws IOException {
        try {
            GameData data = (GameData) d;
            JsonObjectBuilder profileObj = Json.createObjectBuilder()
                    .add(PROFILE_USERNAME, data.getUsername());
            String encrypted = encrypt(data.getPassword());
            profileObj.add(ENCRYPTED_PASSWORD, encrypted);
            profileObj.add(ENGLISH_DICTIONARY_LEVEL, data.getLevel(ENGLISH_DICTIONARY));
            profileObj.add(ANIMALS_LEVEL, data.getLevel(ANIMALS));
            profileObj.add(FIRST_NAMES_LEVEL, data.getLevel(FIRST_NAMES));
            JsonObject profile = profileObj.build();

            JsonObject profObj = Json.createObjectBuilder().add(PROFILE, profile).build();
            ObjectMapper mapper = new ObjectMapper();
            Object js = mapper.readValue(profObj.toString(), Object.class);
            String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(js);
            FileWriter file = new FileWriter(filePath.toString());
            file.write(str);
            file.flush();
            file.close();
        } catch(Exception ex) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(SAVE_ERROR_TITLE), props.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
    }

    @Override
    public void loadData(AppDataComponent data, Path filePath) throws IOException {
        GameData gamedata = (GameData) data;
        try {
            gamedata.reset();
            File loginFile = new File(filePath.toString());
            InputStream is = new FileInputStream(loginFile);

            JsonReader jsonReader = Json.createReader(is);
            JsonObject profObj = jsonReader.readObject();
            JsonObject profile = profObj.getJsonObject(PROFILE);
            String encrypted = profile.getString(ENCRYPTED_PASSWORD);
            String decrypted = decrypt(encrypted);

            String username = profile.getString(PROFILE_USERNAME);
            int dicLevel = profile.getInt(ENGLISH_DICTIONARY_LEVEL);
            int aniLevel = profile.getInt(ANIMALS_LEVEL);
            int nameLevel = profile.getInt(FIRST_NAMES_LEVEL);
            gamedata.setUsername(username);
            gamedata.setPassword(decrypted);
            gamedata.setModeToLevel(ENGLISH_DICTIONARY, dicLevel);
            gamedata.setModeToLevel(ANIMALS, aniLevel);
            gamedata.setModeToLevel(FIRST_NAMES, nameLevel);

        } catch(FileNotFoundException ex) {
            System.out.println("file not found");
        } catch(Exception ex) {
            System.out.println("decryption gone wrong");
        }
    }


    public void loadAllWords(GameData data) {
        ArrayList<String> modes = new ArrayList<>();
        modes.add("English Dictionary");
        modes.add("Animals");
        modes.add("First Names");

        for (String mode: modes) {
            if (!mode.equals("English Dictionary")) {
                String m = mode + "/words.txt";
                URL wordsResource = getClass().getClassLoader().getResource("words/" + m);
                String path = "words/" + m;

                try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {
                    BufferedReader reader = new BufferedReader(new FileReader("./BuzzWord/resources/" + path));
                    String s;
                    while ((s = reader.readLine()) != null) {
                        if (mode.equals("Animals")) {
                            if (s.length() >= 3) {
                                data.getAnimals().add(s.toUpperCase());
                                data.fillTree(mode);
                            }

                        } else if (mode.equals("First Names")) {
                            if (s.length() >= 3) {
                                data.getNames().add(s.toUpperCase());
                                data.fillTree(mode);
                            }

                        }
                    }
                    reader.close();
                } catch (IOException | URISyntaxException e) {
                    AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
                    PropertyManager propertyManager = PropertyManager.getManager();
                    messageDialog.show(propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE), propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE));
                    System.exit(1);
                }
            } else {
                for (int i = 65; i <= 90; i++) {
                    String m = mode + "/" + ((char) i) + " Words.txt";
                    URL wordsResource = getClass().getClassLoader().getResource("words/" + m);
                    String path = "words/" + m;

                    try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {
                        BufferedReader reader = new BufferedReader(new FileReader("./BuzzWord/resources/" + path));
                        String s;
                        while ((s = reader.readLine()) != null) {
                            if (s.length() >= 3) {
                                data.getDictionary().add(s.toUpperCase());
                                data.fillTree(mode);
                            }

                        }
                        reader.close();
                    } catch (IOException | URISyntaxException e) {
                        AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
                        PropertyManager propertyManager = PropertyManager.getManager();
                        messageDialog.show(propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_TITLE), propertyManager.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE));
                        System.exit(1);
                    }
                }

            }
        }

    }

    public String getDecryptedPass(Path loginPath) {
        try {
            File loginFile = new File(loginPath.toString());

            InputStream is = new FileInputStream(loginFile);
            JsonReader jsonReader = Json.createReader(is);
            JsonObject profObj = jsonReader.readObject();
            JsonObject profile = profObj.getJsonObject(PROFILE);
            String encrypted = profile.getString(ENCRYPTED_PASSWORD);
            String decrypted = decrypt(encrypted);

            return decrypted;
        } catch(FileNotFoundException ex) {
            System.out.println("not found");
            return null;
        } catch(Exception ex) {
            System.out.println("decryption gone wrong");
            return null;
        }
    }

    private String encrypt(String s) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(s.getBytes("UTF-8")));
    }

    private static String base64Encode(byte[] bytes) {
        // NB: This class is internal, and you probably should use another impl
        return new BASE64Encoder().encode(bytes);
    }

    private String decrypt(String encryptedPass) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(encryptedPass)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        // NB: This class is internal, and you probably should use another impl
        return new BASE64Decoder().decodeBuffer(property);
    }


    public void saveCredentials(String user, String pass) throws GeneralSecurityException {
        try {
            System.out.println("saving credentials");

            JsonObjectBuilder profileObj = Json.createObjectBuilder()
                    .add(PROFILE_USERNAME, user);
            String encrypted = encrypt(pass);
            profileObj.add(ENCRYPTED_PASSWORD, encrypted);
            profileObj.add(ENGLISH_DICTIONARY_LEVEL, 0);
            profileObj.add(ANIMALS_LEVEL, 0);
            profileObj.add(FIRST_NAMES_LEVEL, 0);
            JsonObject profile = profileObj.build();

            JsonObject profObj = Json.createObjectBuilder().add(PROFILE, profile).build();
            PropertyManager           props  = PropertyManager.getManager();
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();

            ObjectMapper mapper = new ObjectMapper();
            Object js = mapper.readValue(profObj.toString(), Object.class);
            String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(js);
            File f = new File(APP_WORKDIR_PATH.getParameter() + "\\" + user + ".json");
            if (f.exists()) {
                throw new IOException();
            } else {
                FileWriter file = new FileWriter(APP_WORKDIR_PATH.getParameter() + "\\" + user + ".json");
                file.write(str);
                file.flush();
                file.close();

                dialog.show(props.getPropertyValue(NEW_PROFILE_TITLE), props.getPropertyValue(NEW_PROFILE_MESSAGE));
            }

        } catch(IOException ex) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(CREATE_PROFILE_ERROR_TITLE), props.getPropertyValue(CREATE_PROFILE_ERROR_MESSAGE));
        }
    }

}
