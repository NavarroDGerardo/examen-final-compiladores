import java.io.*;
import java.util.*;

/**
 * Programa de validación de cadenas de caracteres
 * por medio de un NFA especificado en un archivo move
 * y en otro archivo donde se especifican los estados terminales.
 *
 * @version 1, 5/29/2021
 * @author Diego Gerardo Navarro González A01338941
 * @author Alan Rodrigo Mendoza Aguilar A01339625
 */

public class Main {
    public static Stack<Integer> oldStates = new Stack<>(); //pila de antiguos estados ya analizados
    public static Stack<Integer> newStates = new Stack<>(); //pila de nuevos estados a analizar
    public static boolean[] alreadyOn; //arreglo de booleanos para ver sí el estado lo agregamos o no
    public static HashMap<Character, Integer> alphabet = new HashMap<>(); //alfabeto del NFA
    public static ArrayList<String[]> move  = new ArrayList<>(); // Lista de Strings con las transiciones del NFA
    public static HashSet<Integer> terminals = new HashSet(); //Hashset de los terminales

    /**
     * método main para ejecutar el programa, aquí se ejecutara la lógica del programa
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //move2 ->  axs(a|b)*(a|b)+bbszp
        //move3  -> (a|b)*abb
        //move4 ->  al(b|a*)*(b|a*)+asxl
        //move5 ->  alx(a|b*)*(b|a*)(a|b*)

        String pathToCsv = "src/move5.csv"; //ubicación del archivo move
        String pathTerminals = "src/terminals5.csv";//ubicación del archivo terminales

        String row = ""; // string para almacenar las líneas del archivo csv
        //BufferReader para leer el archivo con la tabla move del NFA
        BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));

        /*
         * Iteramos el archivo para conseguir cada una de las lineas del archivo move, se separan por las comas
         * y almacenamos el arreglo en la variable move que va a contener todas las transiciones de cada estado
         * de nuestro NFA al igual que el alfabeto del NFA. En la primera posición se almacena el alfabeto y
         * en las siguientes se almacenan todas las transiciones de cada nodo.
         * */
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            move.add(data);
        }
        /*
         * Se agrega un último arreglo vacío para que funcione como último nodo del NFA
         * este nodo no tiene transiciones, es por eso que es un arreglo vacio
         * */
        move.add(new String[0]);

        row = ""; //reiniciamos la variable row a un string vacio
        /*
         * Leemos el segundo archivo que contiene los terminales
         * los almacenamos en un arreglo de String temporalmente llamado data
         * posteriormente se agrega el primer elemento de data a el hashset de terminales
         * el archivo de terminales tiene que tener un enter entre cada terminal para que lo pueda incluir el programa.
         * */
        csvReader = new BufferedReader(new FileReader(pathTerminals));
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            terminals.add(Integer.parseInt(data[0]));
        }

        csvReader.close(); // cerramos el lector de csvReader
        int i = 0; //inicializamos una variable que va a funcionar como índice.
        /*
         * Iteramos el arreglo en la primera posición de nuestra Lista move
         * en esta posición se encuentra el alfabeto del NFA
         * también almacenamos el indice de donde se encuentra la letra para que
         * podamos acceder a ella con la tabla move en las transiciones del nodo
         * Esto lo almacenamos en el hashset alphabet.
         */
        for(String c : move.get(0))
            alphabet.put(c.charAt(0), i++);

        alreadyOn = new boolean[move.size() - 1]; //inicializamos el arreglo de booleanos con el tamaño de la lista de nodos.

        Scanner scan = new Scanner(System.in);  // se crea un escaner para poder leer el input del usuario.
        System.out.println("Agrega cadena a analizar por el NFA con terminación $: "); // se imprimen las instrucciones al usuario en consola
        String cadena = scan.nextLine(); // almacenamos el input del usuario en la variable cadena

        /*
         * Revisamos si el usuario ingresó correctamente la cadena con el terminal correcto,
         * de caso contrario imprimimos que por favor ingrese la cadena con el terminal
         * */
        if(cadena.charAt(cadena.length() - 1) == '$'){
            /*
             * El programa atrapa excepciones en caso de que en la cadena del usuario se encuentre un caracter
             * que el nfa no tenga en su alfabeto, en este caso se imprime un false para decirle al usuario
             * que su cadena no fue aceptada por el NFA
             * */
            try{
                //Se hace la transición epsilon por el estado 0 antes de inciar la lectura del NFA
                e_closure(0);
                /*
                 * La función e_closure pasa todos los estados a estados nuevos
                 * para poder hacer una correcta lectura tenemos que pasar todos los estados de la pila de nuevos
                 * a viejos para su correcto funcionamiento.
                 * */
                while (!newStates.isEmpty()) {
                    int state = newStates.pop(); // se saca el estado de la pila de newState
                    oldStates.push(state);//se agrega el estado a la pila de oldstate
                    alreadyOn[state] = false; //se pone falso en la posición del arreglo de alreadyOn
                }
                /*
                 * Mientras la cadena no sea igual a el caracter final seguimos procesando el input del usuario
                 * leyendo las transiciones del NFA y comprobando si pertenece o no
                 * */
                while (!cadena.equals("$")){
                    transition(cadena.charAt(0));//se procesa el primer caracter de la cadena
                    cadena = cadena.substring(1);//se recorta la cadena en una subcadena sin el primer caracter
                }
                /*
                 * Se imprime el resultado de sí la cadena es aceptada o no, para eso se llama a la función checkChain
                 */
                System.out.println(checkChain());
            }catch (Exception ex){
                //en caso de encontrar alguna excepción en el código se imprime false
                System.out.println(false);
            }
        }else {
            //en caso de no encontrar $ en el último caracter de la cadena se le pide al usuario que la agregue
            System.out.println("ingrese la cadena con '$' al final para poder analizarlo correctamente");
        }
    }

    /**
     * Transition valida el caracter que se le esta pasando. Es el primer
     * caracter del input del usuario y valida todos los estados en oldStates. Los estados en
     * oldstates ya pasaron por la cerradura epsilon y se van a evaluar con el caracter c.
     * @param c el caracter c es el caracter inicial de la cadena que se va a evaluar con los estados
     */
    private static void transition(char c){
        /*
         * Se hace el proceso de transición por todos los estados que se encuentran en oldStates
         * hasta que la pila este vacia.
         */
        while (!oldStates.isEmpty()){
            int state = oldStates.pop(); //estado al tope de la pila
            /*
             * Revisamos sí el estado tiene transiciones en el caracter que se le paso
             * para hacer esto revisamos sí en la tabla move el arreglo tiene una longuitud
             * mayor o que el índice horizontal en el que se encuentra la letra.
             */
            if(move.get(state + 1).length > alphabet.get(c)){
                /*
                 * Se vuelve a checar sí la longuitud del arreglo en la posición del caracter es mayor a 0,
                 * en caso contrario significaría que en ese estado no se encuentra ninguna transición procesando
                 * el caracter c.
                 */
                if(move.get(state + 1)[alphabet.get(c)].length() > 0){
                    //paths es el arreglo de Strings de los diferentes nodos que se puede llegar procesando c
                    String[] paths = move.get(state + 1)[alphabet.get(c)].split(" ");
                    /*
                     * Se itera sobre todos los Strings en paths para aplicar la cerradura epsilon en cada estado
                     */
                    for(String s : paths){
                        /*
                         * Este es un if de caso especial, en caso de llegar a un estado superior del que esta en la
                         * tabla, se aplica la cerradura epsilon con ese estado.
                         */
                        if(Integer.parseInt(s) > alreadyOn.length){
                            e_closure(Integer.parseInt(s));//se aplica cerradura epsilon en el estado s
                        }else {
                            //checamos si en la tabla de alreadyOn no estamos repitiendo estados.
                            if(!alreadyOn[Integer.parseInt(s)]){
                                e_closure(Integer.parseInt(s));//se aplica cerradura epsilon en el estado s
                            }
                        }
                    }
                }
            }
        }

        /*
         * Una vez terminando de procesar todos los estados de oldStates
         * estos pasaron a la pila de newStates. Iteramos sobre esta tabla
         * para pasar cada estado de newStates a oldStates y marcar en el arreglo
         * alreadyOn falso a todos los estados de oldstates.
         */
        while (!newStates.isEmpty()) {
            int state = newStates.pop(); //estado en el tope de la pila newState
            oldStates.push(state); //se pushea el estado a la pila oldstates
            alreadyOn[state] = false; // se pone false en la ubicación del estado en el arreglo
        }
    }

    /**
     * e_closure recibe el id de un estado para ser evaluado con la cerradura epsilon.
     * @param s se le pasa el estado a aplicar la cerradura epsilon
     */
    private static void e_closure(Integer s){
        newStates.push(s);//se pushea el estado a la nueva pila newStates
        alreadyOn[s] = true; //ponemos que el estado ya se ha visitado
        /*
         * checamos sí el estdo tiene transiciones epsilon, estas se encuentran en la última columna del archivo move
         */
        if (move.get(s + 1).length == move.get(0).length) {
            String aux = move.get(s + 1)[alphabet.get('ñ')];//obtenemos todas las transiciones epsilon del estado
            String[] moves = aux.split(" ");//separamos el string para analizar cada número
            for (String t : moves) { //iteramos sobre las transiciones epsilon del estado
                if (!alreadyOn[Integer.parseInt(t)]) { //se checa sí no hemos repetido el estado para no entrar en ciclos infinitos
                    e_closure(Integer.parseInt(t)); //se vuelve a aplicar la cerradura epsilon a los estados que llegamos con la transición epsilon
                }
            }
        }
    }

    /**
     * checkChain revisa si en la pila de oldstates se encuentra un estado terminal
     * en caso de encontrar un estado terminal en oldStates se puede decir que la
     * cadena fue aceptada, en caso contrario es rechazada.
     * @return boolean
     */
    private static boolean checkChain(){
        //mientras la pila no este vacía seguimos sacanso estados de ella
        while (!oldStates.isEmpty())
            if(terminals.contains(oldStates.pop())) //en caso de encontrar un estado que sea terminal se puede decir que el automata sí procesa la cadena de caracteres
                return true;
        //en caso de no encontrar ningún estado se regresa false diciendo que la cadena no puede ser procesada por el autómata.
        return false;
    }
}