export enum UserRank {
  BRONZE = 'BRONZE',
  SILVER = 'SILVER',
  GOLD = 'GOLD',
  PLATINUM = 'PLATINUM',
  DIAMOND = 'DIAMOND'
}

export interface PointsTransaction {
  id: number;
  userId: number;
  amount: number;
  description: string;
  transactionType: 'EARNED' | 'REDEEMED' | 'ADJUSTED';
  createdAt: string;
  relatedEntityId?: number;
  relatedEntityType?: string;
}

export interface UserPoints {
  userId: number;
  totalPoints: number;
  rank: UserRank;
  pointsToNextRank: number;
  transactions: PointsTransaction[];
}

export interface PointsReward {
  id: number;
  name: string;
  description: string;
  pointsCost: number;
  isAvailable: boolean;
  imageUrl?: string;
  requiredRank?: UserRank;
}

// Tipos adicionales para el slice de puntos
export interface PointsAccount {
  id: number;
  userId: number;
  totalPoints: number;
  availablePoints: number;
  redeemedPoints: number;
  currentRank: UserRank;
  pointsToNextRank: number;
  createdAt: string;
  updatedAt: string;
}

export interface RedeemPointsRequest {
  rewardId: number;
  userId: number;
  pointsAmount: number;
}

export interface PointsRedemptionRequest {
  points: number;
  benefitCode: string;
}

export interface Benefit {
  id: number;
  title: string;
  description: string;
  rank: UserRank;
  iconName?: string;
}

export interface AvailableBenefit {
  code: string;
  name: string;
  description: string;
  points: number;
}

// Constantes para los rangos (coinciden con el backend)
export const RANK_THRESHOLDS = {
  [UserRank.BRONZE]: 0,
  [UserRank.SILVER]: 500,
  [UserRank.GOLD]: 1000,
  [UserRank.PLATINUM]: 5000,
  [UserRank.DIAMOND]: 50000
};

export const RANK_COLORS = {
  [UserRank.BRONZE]: '#CD7F32',
  [UserRank.SILVER]: '#C0C0C0',
  [UserRank.GOLD]: '#FFD700',
  [UserRank.PLATINUM]: '#E5E4E2',
  [UserRank.DIAMOND]: '#B9F2FF'
};

export const RANK_BENEFITS = {
  [UserRank.BRONZE]: [
    { title: 'Acumulación básica', description: 'Gana 1 punto por cada $10 gastados' },
    { title: 'Notificaciones', description: 'Recibe notificaciones sobre ofertas especiales' }
  ],
  [UserRank.SILVER]: [
    { title: 'Acumulación mejorada', description: 'Gana 1.5 puntos por cada $10 gastados' },
    { title: 'Descuentos exclusivos', description: 'Accede a descuentos exclusivos en servicios seleccionados' },
    { title: 'Soporte prioritario', description: 'Obtén respuestas más rápidas del equipo de soporte' }
  ],
  [UserRank.GOLD]: [
    { title: 'Acumulación premium', description: 'Gana 2 puntos por cada $10 gastados' },
    { title: 'Recompensas exclusivas', description: 'Accede a recompensas exclusivas para miembros Gold' },
    { title: 'Transferencias sin comisión', description: 'Realiza transferencias sin comisiones adicionales' },
    { title: 'Soporte VIP', description: 'Línea de soporte dedicada 24/7' }
  ],
  [UserRank.PLATINUM]: [
    { title: 'Acumulación élite', description: 'Gana 3 puntos por cada $10 gastados' },
    { title: 'Recompensas premium', description: 'Accede a las mejores recompensas disponibles' },
    { title: 'Asesor financiero', description: 'Consultas gratuitas con un asesor financiero' },
    { title: 'Eventos exclusivos', description: 'Invitaciones a eventos exclusivos' },
    { title: 'Monederos ilimitados', description: 'Crea monederos ilimitados sin costo adicional' }
  ],
  [UserRank.DIAMOND]: [
    { title: 'Acumulación suprema', description: 'Gana 5 puntos por cada $10 gastados' },
    { title: 'Concierge personal', description: 'Servicio de concierge personal 24/7' },
    { title: 'Beneficios personalizados', description: 'Beneficios adaptados a tus necesidades específicas' },
    { title: 'Acceso anticipado', description: 'Acceso anticipado a nuevas funcionalidades' },
    { title: 'Estatus de por vida', description: 'Mantén tu estatus Diamond de por vida' }
  ]
};
