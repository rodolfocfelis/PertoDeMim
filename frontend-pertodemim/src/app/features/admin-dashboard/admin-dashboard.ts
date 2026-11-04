import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { Page } from '../../core/models';
import { ServiceReview } from '../../core/market.models';
import { ConfirmDialog } from '../../shared/confirm-dialog';
@Component({
  standalone: true,
  imports: [DatePipe, RouterLink, ConfirmDialog],
  templateUrl: './admin-dashboard.html',
})
export class AdminDashboard {
  private api = inject(ApiService);
  metrics = signal<Partial<Record<string, number>>>({});
  reviews = signal<ServiceReview[]>([]);
  loading = signal(false);
  busy = signal(false);
  error = signal('');
  page = signal(0);
  pages = signal(0);
  selected = signal<ServiceReview | null>(null);
  cards = [
    ['users', 'Contas cadastradas'],
    ['activeServices', 'Serviços ativos'],
    ['categories', 'Categorias'],
    ['requests', 'Solicitações'],
    ['pending', 'Aguardando resposta'],
    ['completed', 'Atendimentos concluídos'],
    ['reviews', 'Avaliações'],
    ['hiddenReviews', 'Avaliações ocultas'],
  ];
  constructor() {
    this.load();
  }
  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .get<Partial<Record<string, number>>>('market/admin/dashboard')
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => this.metrics.set(data),
        error: () => this.error.set('Não foi possível carregar os indicadores.'),
      });
    this.loadReviews();
  }
  loadReviews(page = 0) {
    this.api.get<Page<ServiceReview>>('market/admin/reviews?page=' + page).subscribe({
      next: (data) => {
        this.reviews.set(data.content);
        this.page.set(data.number);
        this.pages.set(data.totalPages);
      },
      error: () => this.error.set('Não foi possível carregar a moderação.'),
    });
  }
  moderate() {
    const review = this.selected();
    if (!review) return;
    this.busy.set(true);
    this.api
      .patch(`market/admin/reviews/${review.id}?visible=${!review.visible}`, {})
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => {
          this.selected.set(null);
          this.load();
        },
        error: () => {
          this.selected.set(null);
          this.error.set('Não foi possível atualizar a avaliação.');
        },
      });
  }
}
