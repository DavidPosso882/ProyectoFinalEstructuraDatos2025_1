import React, { useState } from 'react';
import { DataStructureService } from '../services/datastructure.service';

const DataStructuresDemo: React.FC = () => {
  // Estados para cada estructura
  const [stack, setStack] = useState<any[]>([]);
  const [queue, setQueue] = useState<any[]>([]);
  const [list, setList] = useState<any[]>([]);
  const [bst, setBST] = useState<any[]>([]);
  const [avl, setAVL] = useState<any[]>([]);
  const [vertices, setVertices] = useState<any[]>([]);
  const [edges, setEdges] = useState<any[]>([]);
  const [bfs, setBFS] = useState<any[]>([]);
  const [dfs, setDFS] = useState<any[]>([]);

  // Inputs
  const [input, setInput] = useState('');
  const [graphSource, setGraphSource] = useState('');
  const [graphDest, setGraphDest] = useState('');
  const [graphWeight, setGraphWeight] = useState('1');
  const [graphStart, setGraphStart] = useState('');

  // Stack
  const loadStack = () => DataStructureService.getStack().then(setStack);
  const handlePushStack = () => DataStructureService.pushStack(input).then(loadStack);
  const handlePopStack = () => DataStructureService.popStack().then(loadStack);

  // Queue
  const loadQueue = () => DataStructureService.getQueue().then(setQueue);
  const handleEnqueue = () => DataStructureService.enqueueQueue(input).then(loadQueue);
  const handleDequeue = () => DataStructureService.dequeueQueue().then(loadQueue);

  // List
  const loadList = () => DataStructureService.getList().then(setList);
  const handleAddList = () => DataStructureService.addList(input).then(loadList);
  const handleRemoveList = () => DataStructureService.removeList(input).then(loadList);

  // BST
  const loadBST = () => DataStructureService.getBST().then(setBST);
  const handleInsertBST = () => DataStructureService.insertBST(input).then(loadBST);
  const handleRemoveBST = () => DataStructureService.removeBST(input).then(loadBST);

  // AVL
  const loadAVL = () => DataStructureService.getAVL().then(setAVL);
  const handleInsertAVL = () => DataStructureService.insertAVL(input).then(loadAVL);
  const handleRemoveAVL = () => DataStructureService.removeAVL(input).then(loadAVL);

  // Graph
  const loadGraph = () => {
    DataStructureService.getVertices().then(setVertices);
    DataStructureService.getEdges().then(setEdges);
  };
  const handleAddVertex = () => DataStructureService.addVertex(input).then(loadGraph);
  const handleAddEdge = () => DataStructureService.addEdge(graphSource, graphDest, Number(graphWeight)).then(loadGraph);
  const handleBFS = () => DataStructureService.bfs(graphStart).then(setBFS);
  const handleDFS = () => DataStructureService.dfs(graphStart).then(setDFS);

  return (
    <div style={{ padding: 24 }}>
      <h2>Demo de Estructuras de Datos</h2>
      <div style={{ display: 'flex', gap: 32, flexWrap: 'wrap' }}>
        {/* Stack */}
        <div>
          <h3>Pila (Stack)</h3>
          <input value={input} onChange={e => setInput(e.target.value)} placeholder="Valor" />
          <button onClick={handlePushStack}>Push</button>
          <button onClick={handlePopStack}>Pop</button>
          <button onClick={loadStack}>Refrescar</button>
          <div>Contenido: {JSON.stringify(stack)}</div>
        </div>
        {/* Queue */}
        <div>
          <h3>Cola (Queue)</h3>
          <input value={input} onChange={e => setInput(e.target.value)} placeholder="Valor" />
          <button onClick={handleEnqueue}>Enqueue</button>
          <button onClick={handleDequeue}>Dequeue</button>
          <button onClick={loadQueue}>Refrescar</button>
          <div>Contenido: {JSON.stringify(queue)}</div>
        </div>
        {/* List */}
        <div>
          <h3>Lista</h3>
          <input value={input} onChange={e => setInput(e.target.value)} placeholder="Valor" />
          <button onClick={handleAddList}>Agregar</button>
          <button onClick={handleRemoveList}>Eliminar</button>
          <button onClick={loadList}>Refrescar</button>
          <div>Contenido: {JSON.stringify(list)}</div>
        </div>
        {/* BST */}
        <div>
          <h3>Árbol Binario</h3>
          <input value={input} onChange={e => setInput(e.target.value)} placeholder="Valor" />
          <button onClick={handleInsertBST}>Insertar</button>
          <button onClick={handleRemoveBST}>Eliminar</button>
          <button onClick={loadBST}>Refrescar</button>
          <div>Contenido: {JSON.stringify(bst)}</div>
        </div>
        {/* AVL */}
        <div>
          <h3>Árbol AVL</h3>
          <input value={input} onChange={e => setInput(e.target.value)} placeholder="Valor" />
          <button onClick={handleInsertAVL}>Insertar</button>
          <button onClick={handleRemoveAVL}>Eliminar</button>
          <button onClick={loadAVL}>Refrescar</button>
          <div>Contenido: {JSON.stringify(avl)}</div>
        </div>
        {/* Graph */}
        <div>
          <h3>Grafo Dirigido</h3>
          <input value={input} onChange={e => setInput(e.target.value)} placeholder="Vértice" />
          <button onClick={handleAddVertex}>Agregar Vértice</button>
          <div>
            <input value={graphSource} onChange={e => setGraphSource(e.target.value)} placeholder="Origen" />
            <input value={graphDest} onChange={e => setGraphDest(e.target.value)} placeholder="Destino" />
            <input value={graphWeight} onChange={e => setGraphWeight(e.target.value)} placeholder="Peso" type="number" />
            <button onClick={handleAddEdge}>Agregar Arista</button>
          </div>
          <button onClick={loadGraph}>Refrescar</button>
          <div>Vértices: {JSON.stringify(vertices)}</div>
          <div>Aristas: {JSON.stringify(edges)}</div>
          <div>
            <input value={graphStart} onChange={e => setGraphStart(e.target.value)} placeholder="Inicio BFS/DFS" />
            <button onClick={handleBFS}>BFS</button>
            <button onClick={handleDFS}>DFS</button>
            <div>BFS: {JSON.stringify(bfs)}</div>
            <div>DFS: {JSON.stringify(dfs)}</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DataStructuresDemo; 