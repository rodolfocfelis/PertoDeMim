import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
describe('Sessão JWT', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {
    http.verify();
    localStorage.clear();
  });
  it('normaliza roles e redireciona profissionais', () => {
    const token =
      'header.' + btoa(JSON.stringify({ exp: Date.now() / 1000 + 3600 })) + '.signature';
    auth.login({ email: 'pro@example.test', password: 'test' }).subscribe();
    http.expectOne('/api/auth/login').flush({ token, role: 'professional' });
    expect(auth.isLoggedIn()).toBe(true);
    expect(auth.hasRole('PROFESSIONAL')).toBe(true);
    expect(auth.home()).toBe('/app/professional/services');
  });
  it('rejeita token expirado ou malformado', () => {
    auth.store('header.' + btoa(JSON.stringify({ exp: 1 })) + '.signature', 'USER');
    expect(auth.isLoggedIn()).toBe(false);
    auth.store('inválido', 'USER');
    expect(auth.isLoggedIn()).toBe(false);
  });
});
