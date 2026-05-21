import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService, DailyTransactionHistory, TransactionLog, Biller } from './transaction.service';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-transaction-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transaction-dashboard.component.html'
})
export class TransactionDashboardComponent implements OnInit {
  private transactionService = inject(TransactionService);
  private authService = inject(AuthService);

  billers: Biller[] = [];
  billerId = '';
  selectedDate = new Date().toISOString().split('T')[0];
  currentPage = 0;
  pageSize = 20;
  selectedGateway = 'ALL';

  historyData: DailyTransactionHistory | null = null;
  isLoading = false;
  errorMessage = '';

  showLoginModal = false;
  loginData = { username: '', password: '' };
  loginError = '';
  isLoggingIn = false;

  ngOnInit(): void {
    this.fetchBillers();
  }

  fetchBillers(): void {
    this.transactionService.getBillers().subscribe({
      next: (data) => {
        if (data.content && data.content.length > 0) {
          this.billers = data.content;
          // select the first one by default
          this.billerId = this.billers[0].id;
          this.fetchData();
        } else {
          this.errorMessage = 'No billers found.';
        }
      },
      error: (err) => {
        console.error('Error fetching billers:', err);
        if (err.status === 401 || err.status === 403) {
          this.showLoginModal = true;
        } else {
          this.errorMessage = 'Failed to load billers list.';
        }
      }
    });
  }

  onBillerChange(): void {
    this.currentPage = 0;
    this.fetchData();
  }

  fetchData(): void {
    if (!this.billerId) return;

    this.isLoading = true;
    this.errorMessage = '';
    this.transactionService.getDailyHistory(this.billerId, this.selectedDate, this.currentPage, this.pageSize)
      .subscribe({
        next: (data) => {
          this.historyData = data;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching transaction history:', err);
          if (err.status === 401 || err.status === 403) {
            this.showLoginModal = true;
          } else {
            this.errorMessage = 'Failed to load transaction history. Please try again later.';
          }
          this.isLoading = false;
        }
      });
  }

  onDateChange(): void {
    this.currentPage = 0; // Reset to the first page when the date changes
    this.fetchData();
  }

  onPageChange(newPage: number): void {
    if (this.historyData && newPage >= 0 && newPage < this.historyData.transactions.page.totalPages) {
      this.currentPage = newPage;
      this.fetchData();
    }
  }

  get filteredTransactions(): TransactionLog[] {
    if (!this.historyData || !this.historyData.transactions?.content) {
      return [];
    }
    if (this.selectedGateway === 'ALL') {
      return this.historyData.transactions.content;
    }
    return this.historyData.transactions.content.filter(
      (tx) => tx.gatewayName === this.selectedGateway
    );
  }

  submitLogin(): void {
    if (!this.loginData.username || !this.loginData.password) {
      this.loginError = 'Please enter both username and password';
      return;
    }
    this.isLoggingIn = true;
    this.loginError = '';

    this.authService.login(this.loginData).subscribe({
      next: (res) => {
        this.authService.setToken(res.token);
        this.showLoginModal = false;
        this.isLoggingIn = false;
        this.loginData = { username: '', password: '' }; // reset

        // Retry fetching billers if we don't have them yet, otherwise just fetch data
        if (this.billers.length === 0) {
          this.fetchBillers();
        } else {
          this.fetchData();
        }
      },
      error: (err) => {
        console.error('Login failed', err);
        this.loginError = 'Invalid credentials or server error.';
        this.isLoggingIn = false;
      }
    });
  }

  cancelLogin(): void {
    this.showLoginModal = false;
    this.errorMessage = 'Authentication required. Please login to view data.';
  }
}
