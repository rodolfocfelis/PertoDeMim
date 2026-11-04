import { Category } from './models';
export interface ServiceCardData {
  id: number;
  name: string;
  phone: string;
  category: Category | string | null;
  city: string;
  state: string;
  description?: string | null;
  startingPrice?: number | null;
  acceptingRequests?: boolean;
  distance?: number;
}
export interface Summary {
  serviceId: number;
  average: number;
  count: number;
}
export type RequestStatus =
  | 'REQUESTED'
  | 'ACCEPTED'
  | 'DECLINED'
  | 'AWAITING_CONFIRMATION'
  | 'COMPLETED'
  | 'CANCELLED';
export interface ServiceRequest {
  id: number;
  serviceId: number;
  serviceName: string;
  customerName: string;
  customerPhone: string;
  professionalPhone: string;
  description: string;
  preferredDate: string | null;
  status: RequestStatus;
  createdAt: string;
  updatedAt: string;
  reviewRating: number | null;
}
export interface ServiceReview {
  id: number;
  serviceId: number;
  serviceName: string;
  customerName: string;
  rating: number;
  comment: string;
  createdAt: string;
  visible: boolean;
}
export const STATUS_LABELS: Record<RequestStatus, string> = {
  REQUESTED: 'Aguardando resposta',
  ACCEPTED: 'Aceita',
  DECLINED: 'Recusada',
  AWAITING_CONFIRMATION: 'Aguardando confirmação do cliente',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
};
export function money(value: number | null | undefined): string {
  return value == null
    ? 'Preço sob consulta'
    : 'A partir de ' +
        new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
}
