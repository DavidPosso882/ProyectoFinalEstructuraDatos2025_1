package org.example.datastructure;

/**
 * Clase para probar las estructuras de datos personalizadas.
 */
public class DataStructureTest {
    
    /**
     * Método principal para probar las estructuras de datos.
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        testCustomList();
        System.out.println("------------------------");
        testCustomStack();
        System.out.println("------------------------");
        testCustomQueue();
    }
    
    /**
     * Prueba la implementación de CustomList.
     */
    private static void testCustomList() {
        System.out.println("Probando CustomList:");
        
        CustomList<String> list = new CustomList<>();
        
        // Prueba de add
        list.add("Uno");
        list.add("Dos");
        list.add("Tres");
        System.out.println("Lista después de añadir elementos: " + list);
        
        // Prueba de addFirst
        list.addFirst("Cero");
        System.out.println("Lista después de añadir al principio: " + list);
        
        // Prueba de add en posición específica
        list.add(2, "Uno y medio");
        System.out.println("Lista después de añadir en posición 2: " + list);
        
        // Prueba de get
        System.out.println("Elemento en posición 0: " + list.get(0));
        System.out.println("Elemento en posición 2: " + list.get(2));
        
        // Prueba de set
        list.set(1, "UNO");
        System.out.println("Lista después de modificar posición 1: " + list);
        
        // Prueba de remove por índice
        String removed = list.remove(0);
        System.out.println("Elemento eliminado de posición 0: " + removed);
        System.out.println("Lista después de eliminar posición 0: " + list);
        
        // Prueba de remove por elemento
        boolean wasRemoved = list.remove("Tres");
        System.out.println("¿Se eliminó 'Tres'?: " + wasRemoved);
        System.out.println("Lista después de eliminar 'Tres': " + list);
        
        // Prueba de contains
        System.out.println("¿Contiene 'UNO'?: " + list.contains("UNO"));
        System.out.println("¿Contiene 'Tres'?: " + list.contains("Tres"));
        
        // Prueba de indexOf
        System.out.println("Índice de 'Dos': " + list.indexOf("Dos"));
        System.out.println("Índice de 'Tres': " + list.indexOf("Tres"));
        
        // Prueba de size e isEmpty
        System.out.println("Tamaño de la lista: " + list.size());
        System.out.println("¿Está vacía?: " + list.isEmpty());
        
        // Prueba de clear
        list.clear();
        System.out.println("Lista después de clear: " + list);
        System.out.println("Tamaño después de clear: " + list.size());
        System.out.println("¿Está vacía después de clear?: " + list.isEmpty());
        
        // Prueba de iterador
        list.add("A");
        list.add("B");
        list.add("C");
        System.out.println("Iterando sobre la lista:");
        for (String item : list) {
            System.out.println("  - " + item);
        }
    }
    
    /**
     * Prueba la implementación de CustomStack.
     */
    private static void testCustomStack() {
        System.out.println("Probando CustomStack:");
        
        CustomStack<Integer> stack = new CustomStack<>();
        
        // Prueba de push
        stack.push(10);
        stack.push(20);
        stack.push(30);
        System.out.println("Pila después de push: " + stack);
        
        // Prueba de peek
        System.out.println("Peek: " + stack.peek());
        System.out.println("Pila después de peek: " + stack);
        
        // Prueba de pop
        System.out.println("Pop: " + stack.pop());
        System.out.println("Pila después de pop: " + stack);
        
        // Prueba de search
        System.out.println("Posición de 20: " + stack.search(20));
        System.out.println("Posición de 50: " + stack.search(50));
        
        // Prueba de contains
        System.out.println("¿Contiene 10?: " + stack.contains(10));
        System.out.println("¿Contiene 30?: " + stack.contains(30));
        
        // Prueba de size e isEmpty
        System.out.println("Tamaño de la pila: " + stack.size());
        System.out.println("¿Está vacía?: " + stack.isEmpty());
        
        // Prueba de clear
        stack.clear();
        System.out.println("Pila después de clear: " + stack);
        System.out.println("¿Está vacía después de clear?: " + stack.isEmpty());
        
        // Prueba de iterador
        stack.push(100);
        stack.push(200);
        stack.push(300);
        System.out.println("Iterando sobre la pila:");
        for (Integer item : stack) {
            System.out.println("  - " + item);
        }
    }
    
    /**
     * Prueba la implementación de CustomQueue.
     */
    private static void testCustomQueue() {
        System.out.println("Probando CustomQueue:");
        
        CustomQueue<Double> queue = new CustomQueue<>();
        
        // Prueba de enqueue
        queue.enqueue(1.1);
        queue.enqueue(2.2);
        queue.enqueue(3.3);
        System.out.println("Cola después de enqueue: " + queue);
        
        // Prueba de peek
        System.out.println("Peek: " + queue.peek());
        System.out.println("Cola después de peek: " + queue);
        
        // Prueba de dequeue
        System.out.println("Dequeue: " + queue.dequeue());
        System.out.println("Cola después de dequeue: " + queue);
        
        // Prueba de contains
        System.out.println("¿Contiene 2.2?: " + queue.contains(2.2));
        System.out.println("¿Contiene 1.1?: " + queue.contains(1.1));
        
        // Prueba de remove
        boolean wasRemoved = queue.remove(3.3);
        System.out.println("¿Se eliminó 3.3?: " + wasRemoved);
        System.out.println("Cola después de eliminar 3.3: " + queue);
        
        // Prueba de size e isEmpty
        System.out.println("Tamaño de la cola: " + queue.size());
        System.out.println("¿Está vacía?: " + queue.isEmpty());
        
        // Prueba de clear
        queue.clear();
        System.out.println("Cola después de clear: " + queue);
        System.out.println("¿Está vacía después de clear?: " + queue.isEmpty());
        
        // Prueba de iterador
        queue.enqueue(10.5);
        queue.enqueue(20.5);
        queue.enqueue(30.5);
        System.out.println("Iterando sobre la cola:");
        for (Double item : queue) {
            System.out.println("  - " + item);
        }
    }
}
