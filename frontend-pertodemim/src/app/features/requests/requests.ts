import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { Page } from '../../core/models';
import { ServiceRequest, RequestStatus, STATUS_LABELS } from '../../core/market.models';
import { ConfirmDialog } from '../../shared/confirm-dialog';
import { ReviewDialog } from '../../shared/market-dialogs';
@Component({
  standalone: true,
  imports: [FormsModule, DatePipe, ConfirmDialog, ReviewDialog],
  templateUrl: './requests.html',
})
export class Requests {
  private api = inject(ApiService);
  auth = inject(AuthService);
  labels = STATUS_LABELS;
  statuses = Object.entries(STATUS_LABELS);
  status = '';
  items = signal<ServiceRequest[]>([]);
  loading = signal(false);
  busy = signal(false);
  error = signal('');
  success = signal('');
  page = signal(0);
  pages = signal(0);
  selected = signal<{ request: ServiceRequest; status: RequestStatus } | null>(null);
  reviewing = signal<ServiceRequest | null>(null);
  constructor() {
    this.load();
  }
  load(page = 0) {
    this.loading.set(true);
    this.error.set('');
    const params = new URLSearchParams({ page: String(page), size: '10' });
    if (this.status) params.set('status', this.status);
    this.api
      .get<Page<ServiceRequest>>(`market/requests?${params}`)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => {
          this.items.set(data.content);
          this.page.set(data.number);
          this.pages.set(data.totalPages);
        },
        error: () => this.error.set('Não foi possível carregar as solicitações.'),
      });
  }
  ask(request: ServiceRequest, status: RequestStatus) {
    this.selected.set({ request, status });
  }
  change() {
    const selected = this.selected();
    if (!selected) return;
    this.busy.set(true);
    this.success.set('');
    this.error.set('');
    this.api
      .patch(`market/requests/${selected.request.id}/status`, { status: selected.status })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => {
          this.selected.set(null);
          this.success.set('Solicitação atualizada.');
          this.load(this.page());
        },
        error: () => {
          this.selected.set(null);
          this.error.set('Esta solicitação mudou ou não permite mais esta ação. Atualize a lista.');
        },
      });
  }
  reviewed() {
    this.reviewing.set(null);
    this.success.set('Avaliação publicada. Obrigado!');
    this.load(this.page());
  }
  whatsapp(phone: string) {
    return 'https://wa.me/55' + phone.replace(/\D/g, '');
  }
}
