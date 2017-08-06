package buzzword;

import components.AppComponentsBuilder;
import components.AppDataComponent;
import components.AppFileComponent;
import components.AppWorkspaceComponent;
import data.GameData;
import data.GameFile;
import gui.Workspace;
import wordapp.WordApp;

/**
 * @author Po Yiu Ho
 */
public class BuzzWord extends WordApp {

    public static void main(String[] args) {launch(args);}

    public String getFileControllerClass() {return "BuzzController";}

    @Override
    public AppComponentsBuilder makeAppBuilderHook() {
        return new AppComponentsBuilder() {
            @Override
            public AppDataComponent buildDataComponent() throws Exception {
                return new GameData(BuzzWord.this);
            }

            @Override
            public AppFileComponent buildFileComponent() throws Exception {
                return new GameFile();
            }

            @Override
            public AppWorkspaceComponent buildWorkspaceComponent() throws Exception {
                return new Workspace(BuzzWord.this);
            }
        };
    }

}
