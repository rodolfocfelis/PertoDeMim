import { ServiceCard } from '../../shared/service-card';
import { RequestDialog, ReviewsDialog } from '../../shared/market-dialogs';
import { ServiceCardData, Summary } from '../../core/market.models';
import { finalize } from 'rxjs';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { Category, Professional, Account } from '../../core/models';
@Component({
  standalone: true,
  imports: [FormsModule, RouterLink, ServiceCard, RequestDialog, ReviewsDialog],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss',
})
export class SearchComponent {
  private api = inject(ApiService);
  auth = inject(AuthService);
  query = '';
  maxPrice: number | null = null;
  minRating = 0;
  sort = 'distance';
  onlyAvailable = false;
  summaries = signal<Record<number, Summary>>({});
  favoriteIds = signal<Set<number>>(new Set());
  favoriteBusy = signal<number | null>(null);
  requesting = signal<ServiceCardData | null>(null);
  reviewing = signal<ServiceCardData | null>(null);
  success = signal('');
  notice = signal('');
  visible() {
    const normalized = (text: string) =>
      text
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase();
    const query = normalized(this.query.trim());
    return this.professionals()
      .filter(
        (service) =>
          (!query ||
            normalized(service.name + ' ' + (service.description ?? '')).includes(query)) &&
          (this.maxPrice === null ||
            (service.startingPrice != null && service.startingPrice <= this.maxPrice)) &&
          (this.summaries()[service.id]?.average ?? 0) >= this.minRating &&
          (!this.onlyAvailable || service.acceptingRequests !== false),
      )
      .sort((a, b) => {
        if (this.sort === 'rating')
          return (
            (this.summaries()[b.id]?.average ?? 0) - (this.summaries()[a.id]?.average ?? 0) ||
            a.distance - b.distance
          );
        if (this.sort === 'price')
          return (
            (a.startingPrice ?? Infinity) - (b.startingPrice ?? Infinity) || a.distance - b.distance
          );
        return a.distance - b.distance;
      });
  }
  toggleFavorite(service: ServiceCardData) {
    if (this.favoriteBusy() !== null) return;
    const saved = this.favoriteIds().has(service.id);
    this.favoriteBusy.set(service.id);
    this.error.set('');
    const request = saved
      ? this.api.delete('market/favorites/' + service.id)
      : this.api.put('market/favorites/' + service.id, {});
    request.pipe(finalize(() => this.favoriteBusy.set(null))).subscribe({
      next: () =>
        this.favoriteIds.update((ids) => {
          const copy = new Set(ids);
          if (saved) copy.delete(service.id);
          else copy.add(service.id);
          return copy;
        }),
      error: () => this.error.set('Não foi possível atualizar seus favoritos.'),
    });
  }
  requested() {
    this.requesting.set(null);
    this.success.set('Solicitação enviada. Acompanhe em Minhas solicitações.');
  }
  private loadSummaries() {
    const ids = this.professionals().map((service) => service.id);
    this.summaries.set({});
    for (let start = 0; start < ids.length; start += 100) {
      this.api
        .get<Summary[]>('market/summaries?ids=' + ids.slice(start, start + 100).join(','))
        .subscribe({
          next: (data) =>
            this.summaries.update((current) => ({
              ...current,
              ...Object.fromEntries(data.map((row) => [row.serviceId, row])),
            })),
          error: () => this.notice.set('As avaliações estão temporariamente indisponíveis.'),
        });
    }
  }
  profileLink = inject(AuthService)
    .home()
    .replace(/(users|services|search|dashboard)$/, 'profile');
  categories = signal<Category[]>([]);
  professionals = signal<(Professional & { distance: number })[]>([]);
  loading = signal(false);
  error = signal('');
  searched = signal(false);
  categoryId = '';
  radius = 15;
  constructor() {
    if (this.auth.hasRole('USER'))
      this.api
        .get<ServiceCardData[]>('market/favorites')
        .subscribe({
          next: (data) => this.favoriteIds.set(new Set(data.map((item) => item.id))),
          error: () => this.notice.set('Não foi possível carregar seus favoritos.'),
        });
    this.api.get<Category[]>('categories').subscribe({
      next: (data) => this.categories.set(data),
      error: () =>
        this.error.set('Não foi possível carregar as categorias. Tente atualizar a página.'),
    });
  }
  private async location(): Promise<{ lat: number; lon: number }> {
    try {
      return await new Promise<{ lat: number; lon: number }>((resolve, reject) => {
        if (!navigator.geolocation) {
          reject(new Error('GPS indisponível'));
          return;
        }
        const timer = setTimeout(() => reject(new Error('GPS sem resposta')), 10000);
        navigator.geolocation.getCurrentPosition(
          (position) => {
            clearTimeout(timer);
            const lat = position.coords.latitude,
              lon = position.coords.longitude;
            if (
              position.coords.accuracy > 2000 ||
              !Number.isFinite(lat) ||
              !Number.isFinite(lon) ||
              Math.abs(lat) > 90 ||
              Math.abs(lon) > 180
            ) {
              reject(new Error('GPS inválido'));
              return;
            }
            resolve({ lat, lon });
          },
          () => {
            clearTimeout(timer);
            reject(new Error('GPS indisponível'));
          },
          { timeout: 10000, maximumAge: 60000 },
        );
      });
    } catch {
      const account = await firstValueFrom(this.api.get<Account>('me'));
      if (
        account.latitude === null ||
        account.longitude === null ||
        !Number.isFinite(account.latitude) ||
        !Number.isFinite(account.longitude)
      )
        throw new Error(
          'Não foi possível obter o GPS. Complete seu endereço em Meu perfil para buscar profissionais.',
        );
      return { lat: account.latitude, lon: account.longitude };
    }
  }
  async search(): Promise<void> {
    if (this.loading()) return;
    this.error.set('');
    this.professionals.set([]);
    this.searched.set(false);
    if (!Number.isFinite(this.radius) || this.radius <= 0 || this.radius > 500) {
      this.error.set('Informe um raio maior que zero e de até 500 km.');
      return;
    }
    this.loading.set(true);
    try {
      const { lat, lon } = await this.location();
      const params = new URLSearchParams({
        lat: String(lat),
        lon: String(lon),
        radius: String(this.radius),
      });
      if (this.categoryId) params.set('categoryId', this.categoryId);
      const data = await firstValueFrom(
        this.api.get<Professional[]>(`professionals/search?${params}`),
      );
      this.professionals.set(
        data
          .map((prof) => ({
            ...prof,
            distance: this.distance(lat, lon, Number(prof.latitude), Number(prof.longitude)),
          }))
          .sort((a, b) => a.distance - b.distance),
      );
      this.searched.set(true);
      this.loadSummaries();
    } catch (error: unknown) {
      this.error.set(
        error instanceof Error
          ? error.message
          : 'Não foi possível buscar profissionais. Tente novamente.',
      );
    } finally {
      this.loading.set(false);
    }
  }
  distance(lat: number, lon: number, lat2: number, lon2: number): number {
    const rad = Math.PI / 180;
    const a =
      Math.sin(((lat2 - lat) * rad) / 2) ** 2 +
      Math.cos(lat * rad) * Math.cos(lat2 * rad) * Math.sin(((lon2 - lon) * rad) / 2) ** 2;
    return 6371 * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, a))));
  }
  whatsapp(phone: string): string {
    return 'https://wa.me/55' + phone.replace(/\D/g, '');
  }
}
