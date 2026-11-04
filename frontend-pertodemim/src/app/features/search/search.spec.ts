import { AuthService } from '../../core/services/auth.service';
import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SearchComponent } from './search.component';
import { describe, beforeEach, afterEach, it, expect, vi } from 'vitest';
describe('Localização automática', () => {
  let component: SearchComponent;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SearchComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: { home: () => '/app/user/search', hasRole: () => false },
        },
      ],
    });
    component = TestBed.createComponent(SearchComponent).componentInstance;
    http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/categories').flush([]);
  });
  afterEach(() => {
    http
      .match((request) => request.url.startsWith('/api/market/summaries'))
      .forEach((request) => request.flush([]));
    http.verify();
    vi.unstubAllGlobals();
    vi.useRealTimers();
  });
  it('valida o raio antes de acessar GPS ou perfil', async () => {
    component.radius = -1;
    await component.search();
    expect(component.error()).toContain('raio');
    http.expectNone('/api/me');
  });
  it('prioriza GPS e ordena resultados', async () => {
    vi.stubGlobal('navigator', {
      geolocation: {
        getCurrentPosition: (success: Function) =>
          success({ coords: { latitude: 0, longitude: 0, accuracy: 10 } }),
      },
    });
    const pending = component.search();
    await Promise.resolve();
    await Promise.resolve();
    http.expectOne('/api/professionals/search?lat=0&lon=0&radius=15').flush([
      { id: 2, latitude: 0.01, longitude: 0 },
      { id: 1, latitude: 0, longitude: 0 },
    ]);
    await pending;
    expect(component.professionals().map((p) => p.id)).toEqual([1, 2]);
    http.expectNone('/api/me');
  });
  it('usa endereço cadastrado quando GPS falha', async () => {
    vi.stubGlobal('navigator', {
      geolocation: { getCurrentPosition: (_: Function, error: Function) => error() },
    });
    const pending = component.search();
    await Promise.resolve();
    http.expectOne('/api/me').flush({ latitude: -23.55, longitude: -46.63 });
    await Promise.resolve();
    await Promise.resolve();
    http.expectOne('/api/professionals/search?lat=-23.55&lon=-46.63&radius=15').flush([]);
    await pending;
    expect(component.searched()).toBe(true);
  });
  it('solicita completar cadastro se GPS e endereço faltarem', async () => {
    vi.stubGlobal('navigator', {});
    const pending = component.search();
    await Promise.resolve();
    http.expectOne('/api/me').flush({ latitude: null, longitude: null });
    await pending;
    expect(component.error()).toContain('Complete seu endereço');
    expect(component.loading()).toBe(false);
  });
  it('não fica aguardando GPS indefinidamente', async () => {
    vi.useFakeTimers();
    vi.stubGlobal('navigator', { geolocation: { getCurrentPosition: () => {} } });
    const pending = component.search();
    await vi.advanceTimersByTimeAsync(10001);
    http.expectOne('/api/me').flush({ latitude: null, longitude: null });
    await pending;
    expect(component.loading()).toBe(false);
  });
});
