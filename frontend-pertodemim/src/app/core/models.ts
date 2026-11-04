export type Role = 'ADMIN' | 'USER' | 'PROFESSIONAL';
export interface Category {
  id: number;
  name: string;
  description: string;
}
export interface Account {
  id: number;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  phone: string;
  zipCode: string;
  street: string;
  addressNumber: string;
  complement: string;
  neighborhood: string;
  city: string;
  state: string;
  bio: string;
  latitude: number | null;
  longitude: number | null;
}
export interface Professional {
  description?: string | null;
  startingPrice?: number | null;
  acceptingRequests?: boolean;
  id: number;
  name: string;
  phone: string;
  category: Category | null;
  city: string;
  street: string;
  addressNumber: string;
  complement: string;
  state: string;
  zipCode: string;
  latitude: number;
  longitude: number;
  active: boolean;
}
export interface Page<T> {
  content: T[];
  number: number;
  totalPages: number;
  totalElements: number;
}
