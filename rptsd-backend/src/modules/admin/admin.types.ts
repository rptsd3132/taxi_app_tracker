export interface UserListQuery {
  page: number;
  limit: number;
  status?: string;
  search?: string;
}

export interface PaymentListQuery {
  page: number;
  limit: number;
  status?: string;
  search?: string;
}

export interface CommentListQuery {
  page: number;
  limit?: number;
  status?: string;
}

export interface PaginatedResult<T> {
  items: T[];
  total: number;
  page: number;
  totalPages: number;
}
