package org.example.controller;

import org.example.datastructure.CustomList;
import org.example.datastructure.CustomQueue;
import org.example.datastructure.CustomStack;
import org.example.datastructure.graph.DirectedGraph;
import org.example.datastructure.tree.AVLTree;
import org.example.datastructure.tree.BinarySearchTree;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/datastructures")
public class DataStructureController {
    // Instancias simples en memoria (para demo, no persistente)
    private final CustomStack<String> stack = new CustomStack<>();
    private final CustomQueue<String> queue = new CustomQueue<>();
    private final CustomList<String> list = new CustomList<>();
    private final BinarySearchTree<String> bst = new BinarySearchTree<>();
    private final AVLTree<String> avl = new AVLTree<>();
    private final DirectedGraph<String> graph = new DirectedGraph<>();

    // --- PILA ---
    @PostMapping("/stack/push")
    public ResponseEntity<?> pushStack(@RequestBody Map<String, String> body) {
        stack.push(body.get("value"));
        return ResponseEntity.ok(stack.toArray());
    }
    @PostMapping("/stack/pop")
    public ResponseEntity<?> popStack() {
        Object value = stack.isEmpty() ? null : stack.pop();
        return ResponseEntity.ok(value);
    }
    @GetMapping("/stack")
    public ResponseEntity<?> getStack() {
        return ResponseEntity.ok(stack.toArray());
    }

    // --- COLA ---
    @PostMapping("/queue/enqueue")
    public ResponseEntity<?> enqueueQueue(@RequestBody Map<String, String> body) {
        queue.enqueue(body.get("value"));
        return ResponseEntity.ok(queue.toArray());
    }
    @PostMapping("/queue/dequeue")
    public ResponseEntity<?> dequeueQueue() {
        Object value = queue.isEmpty() ? null : queue.dequeue();
        return ResponseEntity.ok(value);
    }
    @GetMapping("/queue")
    public ResponseEntity<?> getQueue() {
        return ResponseEntity.ok(queue.toArray());
    }

    // --- LISTA ---
    @PostMapping("/list/add")
    public ResponseEntity<?> addList(@RequestBody Map<String, String> body) {
        list.add(body.get("value"));
        return ResponseEntity.ok(list.toArray());
    }
    @PostMapping("/list/remove")
    public ResponseEntity<?> removeList(@RequestBody Map<String, String> body) {
        boolean removed = list.remove(body.get("value"));
        return ResponseEntity.ok(removed);
    }
    @GetMapping("/list")
    public ResponseEntity<?> getList() {
        return ResponseEntity.ok(list.toArray());
    }

    // --- ÁRBOL BINARIO ---
    @PostMapping("/bst/insert")
    public ResponseEntity<?> insertBST(@RequestBody Map<String, String> body) {
        bst.insert(body.get("value"));
        return ResponseEntity.ok(bst.toSortedList());
    }
    @PostMapping("/bst/remove")
    public ResponseEntity<?> removeBST(@RequestBody Map<String, String> body) {
        boolean removed = bst.remove(body.get("value"));
        return ResponseEntity.ok(removed);
    }
    @GetMapping("/bst")
    public ResponseEntity<?> getBST() {
        return ResponseEntity.ok(bst.toSortedList());
    }

    // --- ÁRBOL AVL ---
    @PostMapping("/avl/insert")
    public ResponseEntity<?> insertAVL(@RequestBody Map<String, String> body) {
        avl.insert(body.get("value"));
        return ResponseEntity.ok(avl.toSortedList());
    }
    @PostMapping("/avl/remove")
    public ResponseEntity<?> removeAVL(@RequestBody Map<String, String> body) {
        boolean removed = avl.remove(body.get("value"));
        return ResponseEntity.ok(removed);
    }
    @GetMapping("/avl")
    public ResponseEntity<?> getAVL() {
        return ResponseEntity.ok(avl.toSortedList());
    }

    // --- GRAFO DIRIGIDO ---
    @PostMapping("/graph/add-vertex")
    public ResponseEntity<?> addVertex(@RequestBody Map<String, String> body) {
        boolean added = graph.addVertex(body.get("vertex"));
        return ResponseEntity.ok(added);
    }
    @PostMapping("/graph/add-edge")
    public ResponseEntity<?> addEdge(@RequestBody Map<String, String> body) {
        boolean added = graph.addEdge(body.get("source"), body.get("destination"), Double.parseDouble(body.get("weight")));
        return ResponseEntity.ok(added);
    }
    @GetMapping("/graph/vertices")
    public ResponseEntity<?> getVertices() {
        return ResponseEntity.ok(graph.getVertices());
    }
    @GetMapping("/graph/edges")
    public ResponseEntity<?> getEdges() {
        return ResponseEntity.ok(graph.getAllEdges());
    }
    @GetMapping("/graph/bfs")
    public ResponseEntity<?> bfs(@RequestParam String start) {
        return ResponseEntity.ok(graph.breadthFirstTraversal(start));
    }
    @GetMapping("/graph/dfs")
    public ResponseEntity<?> dfs(@RequestParam String start) {
        return ResponseEntity.ok(graph.depthFirstTraversal(start));
    }
} 