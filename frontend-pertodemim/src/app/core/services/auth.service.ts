import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { Role } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private browser = isPlatformBrowser(inject(PLATFORM_ID));
  private session = signal(this.read('token'));
  private role = signal(
    this.read('role')
      .replace(/^ROLE_/, '')
      .toUpperCase(),
  );
  private read(key: string): string {
    return this.browser ? (localStorage.getItem(key) ?? '') : '';
  }

  login(credentials: { email: string; password: string }) {
    return this.http.post<{ token: string; role: string }>('/api/auth/login', credentials).pipe(
      tap((response) => {
        this.store(response.token, response.role);
        if (this.browser) localStorage.setItem('userEmail', credentials.email);
      }),
    );
  }
  register(data: unknown) {
    return this.http.post('/api/auth/register', data, { responseType: 'text' });
  }
  store(token: string, role: string) {
    const normalized = role.replace(/^ROLE_/i, '').toUpperCase();
    if (this.browser) {
      localStorage.setItem('token', token);
      localStorage.setItem('role', normalized);
    }
    this.session.set(token);
    this.role.set(normalized);
  }
  getToken(): string | null {
    return this.session() || null;
  }
  getCurrentUserRole(): string {
    return this.role();
  }
  getCurrentUserEmail(): string {
    return this.read('userEmail');
  }
  getCurrentUserName(): string {
    return this.read('userName');
  }
  getCurrentUserId(): string {
    return this.read('userId');
  }
  hasRole(...roles: Role[]): boolean {
    return roles.includes(this.role() as Role);
  }
  isLoggedIn(): boolean {
    try {
      const token = this.session();
      const encoded = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(encoded)).exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
  home(): string {
    if (this.hasRole('ADMIN')) return '/admin/dashboard';
    if (this.hasRole('PROFESSIONAL')) return '/app/professional/services';
    return '/app/user/search';
  }
  logout(): void {
    if (this.browser)
      for (const key of ['token', 'role', 'userEmail', 'userName', 'userId'])
        localStorage.removeItem(key);
    this.session.set('');
    this.role.set('');
    void this.router.navigateByUrl('/login');
  }
}
