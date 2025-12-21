export interface User {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'DEVELOPER' | 'PRIVACY_POLICY';
}

export interface Application {
  id: number;
  developerId: number;
  name: string;
  type: string; // В базе это строка, а не enum
  price: number;
  description: string;
  status: number; // В базе это число: 0, 1, 2
  createdAt?: string;
  updatedAt?: string;
}

export interface Developer {
  id: number;
  userId: number;
  name: string;
  description: string;
}

export interface MonetizedApplication {
  id: number;
  developerId: number;
  applicationId: number;
  currentBalance: number;
  revenue: number;
  downloadRevenue: number;
  adsRevenue: number;
  purchasesRevenue: number;
}

// Вспомогательные типы для преобразования
export interface ApiApplication {
  id: number;
  developerId: number;
  name: string;
  type: string;
  price: number;
  description: string;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

// Функции для преобразования статусов
export const ApplicationStatus = {
  PENDING: 0,
  ACCEPTED: 1,
  REJECTED: 2,
} as const;

export type ApplicationStatusType = (typeof ApplicationStatus)[keyof typeof ApplicationStatus];

export const ApplicationType = {
  GAME: 'GAME',
  MUSIC: 'MUSIC',
  HEALTH: 'HEALTH',
  SOCIAL: 'SOCIAL',
  EDUCATION: 'EDUCATION',
  FINANCE: 'FINANCE',
} as const;

export type ApplicationTypeType = (typeof ApplicationType)[keyof typeof ApplicationType];
