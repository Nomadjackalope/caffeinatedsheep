package com.nomadjackalope.caffeinatedsheep;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class SaveHelper {

    public static class JsonScores {
        public TreeMap<String, ArrayList<String>> scoresD = new TreeMap<String, ArrayList<String>>();
    }

    public static class JsonUsers {

        ArrayList<User> users = new ArrayList<User>();
    }


    public static void saveHighScores(TreeMap<Integer, ArrayList<String>> gameScores) {
        JsonScores jScores = new JsonScores();

        for (Map.Entry<Integer, ArrayList<String>> entry : gameScores.entrySet()) {

            String key = entry.getKey().toString();
            jScores.scoresD.put(key, new ArrayList<String>(entry.getValue().size()));

            for(String name : entry.getValue()) {
                jScores.scoresD.get(key).add(name);
            }
        }

        Json json = new Json();
        writeFile("game.txt", json.prettyPrint(jScores));
    }

    public static TreeMap<Integer, ArrayList<String>> loadScores() {
        String save = readFile("game.txt");
        if (!save.isEmpty()) {

            Json json = new Json();
            JsonScores jScores = json.fromJson(JsonScores.class, save);

            TreeMap<Integer, ArrayList<String>> returnable = new TreeMap<Integer, ArrayList<String>>();

            for (Map.Entry<String, ArrayList<String>> entry : jScores.scoresD.entrySet()) {

                Integer key = Integer.parseInt(entry.getKey());
                returnable.put(key, new ArrayList<String>(entry.getValue().size()));

                for(String name : entry.getValue()) {
                    returnable.get(key).add(name);
                }
            }

            return returnable;
        }
        return null;
    }

    public static void saveUsers(ArrayList<User> users) {
        //System.out.println("SH| " + users);
        Json json = new Json();
        writeFile("users.txt", json.prettyPrint(users));
    }

    public static ArrayList<User> loadUsers() {
        String save = readFile("users.txt");
        if (!save.isEmpty()) {
            Json json = new Json();
            return json.fromJson(ArrayList.class, User.class, save);
        }
        return null;
    }

    public static void writeFile(String fileName, String s) {
        FileHandle file = Gdx.files.local(fileName);
        //file.writeString(com.badlogic.gdx.utils.Base64Coder.encodeString(s), false);
        file.writeString(s, false);
    }

    public static String readFile(String fileName) {
        FileHandle file = Gdx.files.local(fileName);
        if (file != null && file.exists()) {
            String s = file.readString();
            if (!s.isEmpty()) {
                //return com.badlogic.gdx.utils.Base64Coder.decodeString(s);
                return s;
            }
        }
        return "";
    }
}
