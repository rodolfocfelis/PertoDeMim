import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ServiceCardData, Summary } from '../../core/market.models';
import { ServiceCard } from '../../shared/service-card';
import { RequestDialog, ReviewsDialog } from '../../shared/market-dialogs';
@Component({
  standalone: true,
  imports: [RouterLink, ServiceCard, RequestDialog, ReviewsDialog],
  template: ` <section class="page-container wide-page">
    <header class="section-header">
      <span class="eyebrow">Sua lista</span>
      <h1>Profissionais favoritos</h1>
      <p>Encontre novamente os serviços que você salvou.</p>
    </header>
    @if (error()) {
      <p class="feedback error" role="alert">{{ error() }}</p>
    }
    @if (success()) {
      <p class="feedback success" role="status">{{ success() }}</p>
    }
    @if (loading()) {
      <p class="state-box">Carregando favoritos...</p>
    } @else {
      <div class="market-grid">
        @for (service of items(); track service.id) {
          <app-service-card
            [service]="service"
            [summary]="summaries()[service.id]"
            [favorite]="true"
            [busy]="busy() === service.id"
            (toggle)="remove(service.id)"
            (request)="requesting.set(service)"
            (reviews)="reviewing.set(service)"
          />
        } @empty {
          <div class="state-box">
            Você ainda não tem favoritos.
            <a routerLink="/app/user/search">Encontrar profissionais</a>
          </div>
        }
      </div>
    }
    @if (requesting(); as service) {
      <app-request-dialog
        [service]="service"
        (cancel)="requesting.set(null)"
        (done)="requested()"
      />
    }
    @if (reviewing(); as service) {
      <app-reviews-dialog
        [serviceId]="service.id"
        [name]="service.name"
        (cancel)="reviewing.set(null)"
      />
    }
  </section>`,
})
export class Favorites {
  private api = inject(ApiService);
  items = signal<ServiceCardData[]>([]);
  summaries = signal<Record<number, Summary>>({});
  loading = signal(false);
  busy = signal<number | null>(null);
  error = signal('');
  success = signal('');
  requesting = signal<ServiceCardData | null>(null);
  reviewing = signal<ServiceCardData | null>(null);
  constructor() {
    this.load();
  }
  load() {
    this.loading.set(true);
    this.api
      .get<ServiceCardData[]>('market/favorites')
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => {
          this.items.set(data);
          const ids = data.slice(0, 100).map((p) => p.id);
          if (ids.length)
            this.api
              .get<Summary[]>('market/summaries?ids=' + ids.join(','))
              .subscribe({
                next: (rows) =>
                  this.summaries.set(Object.fromEntries(rows.map((row) => [row.serviceId, row]))),
                error: () => {},
              });
        },
        error: () => this.error.set('Não foi possível carregar seus favoritos.'),
      });
  }
  remove(id: number) {
    this.busy.set(id);
    this.error.set('');
    this.api
      .delete(`market/favorites/${id}`)
      .pipe(finalize(() => this.busy.set(null)))
      .subscribe({
        next: () => this.items.update((items) => items.filter((item) => item.id !== id)),
        error: () => this.error.set('Não foi possível remover o favorito.'),
      });
  }
  requested() {
    this.requesting.set(null);
    this.success.set('Solicitação enviada. Acompanhe em Minhas solicitações.');
  }
}
