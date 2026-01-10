export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterCredentials extends LoginCredentials {
  email: string;
}

export interface Token {
  token: string;
  expirationDate: number;
  role: string;
  userId: number;
  username: string;
}
