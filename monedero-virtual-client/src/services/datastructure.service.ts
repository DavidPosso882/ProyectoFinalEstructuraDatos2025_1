import ApiService from './api.service';

export const DataStructureService = {
  // Stack
  getStack: () => ApiService.get<any[]>('/api/datastructures/stack'),
  pushStack: (value: string) => ApiService.post<any[]>('/api/datastructures/stack/push', { value }),
  popStack: () => ApiService.post<any>('/api/datastructures/stack/pop'),

  // Queue
  getQueue: () => ApiService.get<any[]>('/api/datastructures/queue'),
  enqueueQueue: (value: string) => ApiService.post<any[]>('/api/datastructures/queue/enqueue', { value }),
  dequeueQueue: () => ApiService.post<any>('/api/datastructures/queue/dequeue'),

  // List
  getList: () => ApiService.get<any[]>('/api/datastructures/list'),
  addList: (value: string) => ApiService.post<any[]>('/api/datastructures/list/add', { value }),
  removeList: (value: string) => ApiService.post<boolean>('/api/datastructures/list/remove', { value }),

  // Binary Search Tree
  getBST: () => ApiService.get<any[]>('/api/datastructures/bst'),
  insertBST: (value: string) => ApiService.post<any[]>('/api/datastructures/bst/insert', { value }),
  removeBST: (value: string) => ApiService.post<boolean>('/api/datastructures/bst/remove', { value }),

  // AVL Tree
  getAVL: () => ApiService.get<any[]>('/api/datastructures/avl'),
  insertAVL: (value: string) => ApiService.post<any[]>('/api/datastructures/avl/insert', { value }),
  removeAVL: (value: string) => ApiService.post<boolean>('/api/datastructures/avl/remove', { value }),

  // Directed Graph
  getVertices: () => ApiService.get<any[]>('/api/datastructures/graph/vertices'),
  getEdges: () => ApiService.get<any[]>('/api/datastructures/graph/edges'),
  addVertex: (vertex: string) => ApiService.post<boolean>('/api/datastructures/graph/add-vertex', { vertex }),
  addEdge: (source: string, destination: string, weight: number) => ApiService.post<boolean>('/api/datastructures/graph/add-edge', { source, destination, weight }),
  bfs: (start: string) => ApiService.get<any[]>(`/api/datastructures/graph/bfs?start=${encodeURIComponent(start)}`),
  dfs: (start: string) => ApiService.get<any[]>(`/api/datastructures/graph/dfs?start=${encodeURIComponent(start)}`),
}; 