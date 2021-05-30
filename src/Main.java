import java.io.*;
import java.util.*;

/**
 * Programa de validación de cadenas de caracteres
 * por medio de un NFA especificado en un archivo move
 * y otro archivo donde esten los terminales.
 *
 * @author Diego Gerardo Navarro González A01338941
 * @author Alan Rodrigo Mendoza Peréz A01112918
 */

public class Main {
    public static Stack<Integer> oldStates = new Stack<>(); //pila de antiguos estados ya analizados
    public static Stack<Integer> newStates = new Stack<>(); //pila de nuevos estados a analizar
    public static boolean[] alreadyOn; //arreglo de booleanos para ver sí el estado lo agregamos o no
    public static HashMap<Character, Integer> alphabet = new HashMap<>(); //alphabeto del NFA
    public static ArrayList<String[]> move  = new ArrayList<>(); // Lista de Strings con las transiciónes del NFA
    public static HashSet<Integer> terminals = new HashSet(); //Hashset de los terminales

    /**
     * metodo main para ejecutar el programa, aquí se ejecutara la lógica del programa
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String pathToCsv = "src/move3.csv"; //ubicación del archivo move
        String pathTerminals = "src/terminals.csv";//ubicación del archivo terminales

        String row = ""; // string para almacenar las lineas del archivo csv
        //BufferReader para leeer el arvhico con la tabla move del NFA
        BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));

        /*
         * Iteramos el archivo para conseguir cada una de las lineas del archivo move, se separan por las comas
         * y almacenamos el arreglo en la variable move que va a contener todas las transiciones de cada estado
         * de nuestro NFA al igual que el alfabeto del NFA. En la primera posición se almacena el alfabeto y
         * en las siguiente se almacenan todas las transiciones de cada nodo.
         * */
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            move.add(data);
        }
        /*
         * se agrega un ultimo arreglo vacio para que funcione como ultimo nodo del NFA
         * este nodo no tiene transiciones es por eso que es un arreglo vacio
         * */
        move.add(new String[0]);

        row = ""; //reiniciamos la variable row a un string vacio
        /*
         * leeemos el segundo archivo que contine los terminales
         * los almacenamos en un arreglo de String temporalmente llamaddo data
         * posteriormente se agrega el primer elemento de data a el hashset de terminales
         * el archivo de terminales tiene que tener enter entre cada terminal para que lo pueda incluir el programa.
         * */
        csvReader = new BufferedReader(new FileReader(pathTerminals));
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            terminals.add(Integer.parseInt(data[0]));
        }

        csvReader.close(); // cerramos el lector de csvReader
        int i = 0; //inicializamos una variable que va a funcionar como indice.
        /*
         * iteramos el el arreglo en la primera posición de nuestra Lista move
         * en esta posición se encuentra el alphabeto del NFA
         * también almacenamos el indice de donde se encuentra la letra para que
         * podamos acceder a ella con la tabla move en las transiciones del nodo
         * Esto lo almacenamos en el hashset alphabet.
         */
        for(String c : move.get(0))
            alphabet.put(c.charAt(0), i++);

        alreadyOn = new boolean[move.size() - 1]; //inicializamos el arreglo de booleanos con el tamaño de la lista de nodos.

        Scanner scan = new Scanner(System.in);  // se crea un escanos para poder leer el input del usuario.
        System.out.println("Agrega cadena a analizar por el NFA con terminación $: "); // se imprime las instrucciones al usuario en consola
        String cadena = scan.nextLine(); // almacenamos el input del usuario en la variable cadena

        /*
         * Revisamos si el usuario ingreso correctamente la cadena con el terminal correcto,
         * de caso contrario imprimimos que por favor ingrese la cadena con el terminal
         * */
        if(cadena.charAt(cadena.length() - 1) == '$'){
            /*
             * El programa atrapa excepciones en caso de que en la cadena del usuario se encuentre un caracter
             * que el nfa no tenga en su alfabeto, en este caso se imprime un false para decirle al usuario
             * que su cadena no fue aceptada por el NFA
             * */
            try{
                //Se hace la transicion epsilon por el estado 0 antes de inciiar la lectura del
                e_closure(0);
                while (!newStates.isEmpty()) {
                    int state = newStates.pop();
                    oldStates.push(state);
                    alreadyOn[state] = false;
                }
                /*
                 * mientras la cadena no sea igual a el caracter final seguimos procesando el input del usuario
                 * leyendo las transiciones del NFA y comprobando si pertenece o no
                 * */
                while (!cadena.equals("$")){
                    transition(cadena.charAt(0));
                    cadena = cadena.substring(1);
                }
                System.out.println(checkChain());
            }catch (Exception ex){
                System.out.println(false);
            }
        }else {
            System.out.println("ingrese la cadena con '$' al final para poder analizarlo correctamente");
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
                            e_closure(Integer.parseInt(s));
                        }else {
                            if(!alreadyOn[Integer.parseInt(s)]){
                                e_closure(Integer.parseInt(s));
                            }
                        }
                    }
                }
            }
        }

        while (!newStates.isEmpty()) {
            int state = newStates.pop();
            oldStates.push(state);
            alreadyOn[state] = false;
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
    }

    private static boolean checkChain(){
        while (!oldStates.isEmpty())
            if(terminals.contains(oldStates.pop()))
                return true;

        return false;
    }
}
