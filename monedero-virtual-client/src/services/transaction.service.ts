import ApiService from './api.service';

const TransactionService = {
  undoLastTransaction: () => ApiService.post<string>('/api/transactions/undo-last'),
  processTransaction: (data: any) => ApiService.post('/api/transactions', data),
  // ... aquí puedes agregar otras funciones del servicio ...
};

export default TransactionService; 