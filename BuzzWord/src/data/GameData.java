package data;

import components.AppDataComponent;
import sun.reflect.generics.tree.Tree;
import wordapp.WordApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * @author Po Yiu Ho
 */
public class GameData implements AppDataComponent {
    public WordApp app;
    private String username;
    private String password;
    private static final int MAX_LEVEL = 8;
    private TreeSet<String> possible;
    private HashMap<String, ArrayList<String>> modeToWords;
    private HashMap<String, Integer> modeToLevel;
    private TreeSet<String> dictionary;
    private TreeSet<String> animals;
    private TreeSet<String> names;
    private boolean gotDic;
    private boolean gotAni;
    private boolean gotNa;

    public GameData(WordApp wordapp) {
        this.app = wordapp;
        this.modeToLevel = new HashMap<>();
        this.modeToWords = new HashMap<>();
        this.modeToWords.put("English Dictionary", new ArrayList<>());
        this.modeToWords.put("Animals", new ArrayList<>());
        this.modeToWords.put("First Names", new ArrayList<>());
        this.dictionary = new TreeSet<>();
        this.animals = new TreeSet<>();
        this.names = new TreeSet<>();
        this.possible = new TreeSet<>();
        this.gotDic = false;
        this.gotAni = false;
        this.gotNa = false;
    }

    public String getUsername() {return this.username;}
    public String getPassword() {return this.password;}

    public void setUsername(String s) {this.username = s;}
    public void setPassword(String p) {this.password = p;}

    public void addWord(String mode, String word) {
        ArrayList<String> list = this.modeToWords.get(mode);

        list.add(word);
    }

    public ArrayList<String> getList(String mode) {
        return this.modeToWords.get(mode);
    }

    public void increaseLevel(String mode) {
        int i = this.modeToLevel.get(mode)+ 1;
        if (i <= MAX_LEVEL) {
            this.modeToLevel.put(mode, i);
        }
    }
    public void setModeToLevel(String s, int level) {
        this.modeToLevel.put(s, level);
    }

    public int getLevel(String mode) {
        return this.modeToLevel.get(mode);
    }

    public void resetWords() {
        this.modeToWords.put("English Dictionary", new ArrayList<>());
        this.modeToWords.put("Animals", new ArrayList<>());
        this.modeToWords.put("First Names", new ArrayList<>());
    }

    public void addPossibleWord(String s) {
        this.possible.add(s);
    }
    public void resetPossibleWords() {this.possible.clear();}
    public TreeSet<String> getPossible() {return this.possible;}

    public void fillTree(String mode) {
        if (mode.equals("English Dictionary")) {
            this.gotDic = true;
        } else if (mode.equals("Animals")) {
            this.gotAni = true;
        } else {
            this.gotNa = true;
        }
    }

    public boolean treeFilled(String mode) {
        if (mode.equals("English Dictionary")) {
            return this.gotDic;
        } else if (mode.equals("Animals")) {
            return this.gotAni;
        } else {
            return this.gotNa;
        }
    }

    public TreeSet<String> getDictionary() {return this.dictionary;}
    public TreeSet<String> getAnimals() {return this.animals;}
    public TreeSet<String> getNames() {return this.names;}

    @Override
    public void reset() {
        this.username = "";
        this.password = "";
        this.modeToLevel = new HashMap<>();
        this.modeToWords = new HashMap<>();
        this.modeToWords.put("English Dictionary", new ArrayList<>());
        this.modeToWords.put("Animals", new ArrayList<>());
        this.modeToWords.put("First Names", new ArrayList<>());
        this.possible = new TreeSet<>();
    }
}
