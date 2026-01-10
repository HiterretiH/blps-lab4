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
  type: string;
  price: number;
  description: string;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Developer {
  id: number;
  name: string;
  description: string;
  user: {
    id: number;
    username: string;
    email: string;
    role: string;
    passwordHash: string;
  };
}

export interface MonetizedApplication {
  id: number;
  adsRevenue: number;
  currentBalance: number;
  downloadRevenue: number;
  purchasesRevenue: number;
  revenue: number;
  applicationId: number;
  developerId: number;
}

export interface InAppAdd {
  id: number;
  description: string;
  price: number;
  title: string;
  monetizedApplicationId: number;
  monetizedApplication?: MonetizedApplication;
}

export interface InAppAdd {
  id: number;
  description: string;
  price: number;
  title: string;
  monetizedApplicationId: number;
}

export interface InAppPurchase {
  id: number;
  description: string;
  price: number;
  title: string;
  monetizedApplicationId: number;
  monetizedApplication?: MonetizedApplication;
}

export interface ApplicationStats {
  id: number;
  downloads: number;
  rating: number;
  applicationId: number;
  application?: Application;
}

export interface PaymentRequest {
  id: number;
  amount: number;
  applicationId: number;
  isCardValid: boolean;
  requestTime: string;
}

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

export interface VerificationLog {
  id: number;
  adsCheckPassed: boolean;
  logMessage: string;
  policyCheckPassed: boolean;
  securityCheckPassed: boolean;
  applicationId: number;
}

export interface PayoutLog {
  id: number;
  payoutValue: number;
  timestamp: string;
  developerId: number;
  monetizedApplicationId: number;
}

export interface GoogleOperationResult {
  id: number;
  operation: string;
  result: string;
  error: boolean;
  targetValue: string;
  userId: number;
  createdAt: string;
}

export interface FormField {
  id: number;
  fieldName: string;
}

export interface Card {
  cardNumber: string;
  cardHolderName: string;
  expiryDate: string;
  cvv: string;
}

export interface MonetizationEvent {
  eventType: 'DOWNLOAD' | 'PURCHASE' | 'AD_VIEW';
  userId: number;
  applicationId: number;
  targetId: number;
  amount: number;
}

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
