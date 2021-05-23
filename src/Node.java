import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Node {
    private int identifier;
    private HashMap<Character, List<Integer>> paths;


    public Node(int identifier, HashMap<Character, List<Integer>> paths) {
        this.identifier = identifier;
        this.paths = paths;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public HashMap<Character, List<Integer>> getPaths() {
        return paths;
    }

    public void setPaths(HashMap<Character, List<Integer>> paths) {
        this.paths = paths;
    }

    @Override
    public String toString(){
        Character[] ar = paths.keySet().toArray(new Character[0]);
        String keysValues = "";
        for(Character c : ar){
            for(Integer i : paths.get(c)){
                keysValues += c + " -> " + i + "\n";
            }
        }

        return "identifier: " + identifier + "\n" + keysValues;
    }

}
