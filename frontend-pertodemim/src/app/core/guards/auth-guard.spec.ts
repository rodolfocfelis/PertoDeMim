import { TestBed } from '@angular/core/testing';
import {
  provideRouter,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth-guard';
import { describe, beforeEach, it, expect } from 'vitest';
describe('Rotas por perfil', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
  });
  function check(roles: string[]) {
    return TestBed.runInInjectionContext(() =>
      authGuard(
        { data: { roles } } as unknown as ActivatedRouteSnapshot,
        {} as RouterStateSnapshot,
      ),
    );
  }
  it('direciona visitante para login', () =>
    expect((check(['ADMIN']) as UrlTree).toString()).toBe('/login'));
  it('bloqueia usuário comum na administração', () => {
    TestBed.inject(AuthService).store(
      'a.' + btoa(JSON.stringify({ exp: Date.now() / 1000 + 600 })) + '.b',
      'USER',
    );
    expect((check(['ADMIN']) as UrlTree).toString()).toBe('/forbidden');
    expect(check(['USER'])).toBe(true);
  });
});
