import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionDashboardComponent } from './transaction-dashboard/transaction-dashboard.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, TransactionDashboardComponent],
  template: '<app-transaction-dashboard></app-transaction-dashboard>'
})
export class AppComponent {
  title = 'frontend';
}
