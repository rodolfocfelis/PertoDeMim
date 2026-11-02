import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { authInterceptor } from '../auth.interceptor';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
describe('Integração da API', () => {
  let api: ApiService;
  let http: HttpTestingController;
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'login', children: [] }]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    api = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {
    http.verify();
    localStorage.clear();
  });
  it('envia JWT automaticamente para a API', () => {
    TestBed.inject(AuthService).store('token-de-teste', 'ADMIN');
    api.get('admin/users').subscribe();
    const request = http.expectOne('/api/admin/users');
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-de-teste');
    request.flush([]);
  });
  it('encerra a sessão em 401', () => {
    const auth = TestBed.inject(AuthService);
    auth.store('token-de-teste', 'USER');
    api.get('me').subscribe({ error: () => {} });
    http.expectOne('/api/me').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.getToken()).toBe(null);
  });
});
