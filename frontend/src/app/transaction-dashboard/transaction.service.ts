import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TransactionLog {
  transactionId: string;
  amount: number;
  commission: number;
  gatewayName: string;
  status: string;
  timestamp: string;
}

export interface GatewaySummary {
  gatewayName: string;
  transactionCount: number;
  totalAmount: number;
  totalCommission: number;
}

export interface PagedTransactions {
  content: TransactionLog[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface DailyTransactionHistory {
  date: string;
  totalAmountProcessed: number;
  totalCommissionCharged: number;
  gatewayBreakdown: GatewaySummary[];
  transactions: PagedTransactions;
}

export interface Biller {
  id: string;
  name: string;
}

export interface PagedBillers {
  content: Biller[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  constructor(private http: HttpClient) {}

  getBillers(page: number = 0, size: number = 100): Observable<PagedBillers> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedBillers>('/api/billers', { params });
  }

  getDailyHistory(billerId: string, date: string, page: number, size: number): Observable<DailyTransactionHistory> {
    const params = new HttpParams()
      .set('date', date)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<DailyTransactionHistory>(`/api/billers/${billerId}/transactions`, { params });
  }
}
