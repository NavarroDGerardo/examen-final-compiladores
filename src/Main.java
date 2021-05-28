import java.io.*;
import java.util.*;

public class Main {
    public static Stack<Integer> oldStates = new Stack<>();
    public static Stack<Integer> newStates = new Stack<>();
    public static Stack<Integer> intermediateStack = new Stack<>();
    public static boolean[] alreadyOn;
    public static HashMap<Character, Integer> alphabet = new HashMap<>();
    public static ArrayList<String[]> move  = new ArrayList<>();
    public static HashSet<Integer> terminals = new HashSet();
    public static boolean cadenaterminada = false;

    public static void main(String[] args) throws IOException {
        String pathToCsv = "src/move3.csv";
        String pathTerminals = "src/terminals.csv";

        String row = "";
        BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            move.add(data);
        }
        move.add(new String[0]);

        row = "";
        csvReader = new BufferedReader(new FileReader(pathTerminals));
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            terminals.add(Integer.parseInt(data[0]));
        }

        csvReader.close();
        int i = 0;
        for(String c : move.get(0))
            alphabet.put(c.charAt(0), i++);

        alreadyOn = new boolean[move.size() - 1];

        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Agrega cadena a analizar por el NFA: ");
        String cadena = myObj.nextLine();

        try{
            intermediateStack.push(0);
            while (!cadena.equals("$")){
                while (!intermediateStack.isEmpty()){
                    e_closure(intermediateStack.pop());
                }
                transition(cadena.charAt(0));
                cadena = cadena.substring(1);
            }
            while (!intermediateStack.isEmpty()){
                e_closure(intermediateStack.pop());
            }

            System.out.println(checkChain());
        }catch (Exception ex){
            System.out.println(false);
        }
    }

    private static void transition(char c){
        while (!oldStates.isEmpty()){
            int state = oldStates.pop();
            if(move.get(state + 1).length > alphabet.get(c)){
                if(move.get(state + 1)[alphabet.get(c)].length() > 0){
                    String[] paths = move.get(state + 1)[alphabet.get(c)].split(" ");
                    for(String s : paths){
                        if(Integer.parseInt(s) > alreadyOn.length){
                            intermediateStack.push(Integer.parseInt(s));
                        }else {
                            if(!alreadyOn[Integer.parseInt(s)]){
                                intermediateStack.push(Integer.parseInt(s));
                            }
                        }
                    }
                }
            }
        }
    }


    private static void e_closure(Integer s){
        newStates.push(s);
        alreadyOn[s] = true;
        if (move.get(s + 1).length == move.get(0).length) {
            String aux = move.get(s + 1)[alphabet.get('ñ')];
            String[] moves = aux.split(" ");
            for (String t : moves) {
                if (!alreadyOn[Integer.parseInt(t)]) {
                    e_closure(Integer.parseInt(t));
                }
            }
        }

        while (!newStates.isEmpty()) {
            int state = newStates.pop();
            oldStates.push(state);
            alreadyOn[state] = false;
        }
    }

    private static boolean checkChain(){
        while (!oldStates.isEmpty())
            if(terminals.contains(oldStates.pop()))
                return true;

            return false;
    }
}
