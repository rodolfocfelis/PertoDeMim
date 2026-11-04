import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { Account, Page } from '../../core/models';
import { ConfirmDialog } from '../../shared/confirm-dialog';
@Component({
  standalone: true,
  imports: [FormsModule, RouterLink, ConfirmDialog],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss',
})
export class AdminUsers {
  private api = inject(ApiService);
  users = signal<Account[]>([]);
  loading = signal(false);
  busy = signal(false);
  error = signal('');
  success = signal('');
  selected = signal<Account | null>(null);
  page = signal(0);
  pages = signal(0);
  total = signal(0);
  name = '';
  status = '';
  role = '';
  constructor() {
    this.load();
  }
  load(page = 0) {
    this.loading.set(true);
    this.error.set('');
    const params = new URLSearchParams({ page: String(page), size: '10' });
    if (this.name.trim()) params.set('name', this.name.trim());
    if (this.status) params.set('active', this.status);
    if (this.role) params.set('role', this.role);
    this.api
      .get<Page<Account>>(`admin/users?${params}`)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (result) => {
          this.users.set(result.content);
          this.page.set(result.number);
          this.pages.set(result.totalPages);
          this.total.set(result.totalElements);
        },
        error: () => this.error.set('Não foi possível carregar os usuários. Tente novamente.'),
      });
  }
  changeStatus() {
    const user = this.selected();
    if (!user) return;
    this.busy.set(true);
    this.error.set('');
    this.success.set('');
    this.api
      .patch(`admin/users/${user.id}/status?active=${!user.active}`, {})
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => {
          this.selected.set(null);
          this.success.set('Status atualizado com sucesso.');
          this.load(this.page());
        },
        error: () => {
          this.selected.set(null);
          this.error.set('Não foi possível alterar o status.');
        },
      });
  }
}
