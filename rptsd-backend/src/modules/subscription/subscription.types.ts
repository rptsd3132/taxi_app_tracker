export interface SubscriptionStatusResponse {
  status: string;
  endDate: Date | null;
  daysRemaining: number;
  isActive: boolean;
}

export interface CreatePaymentRequest {
  amount: number;
  paymentMethod: string;
}

export interface ConfirmPaymentRequest {
  transactionId: string;
}
