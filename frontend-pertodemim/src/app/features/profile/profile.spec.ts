import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Profile } from './profile';
import { describe, it, expect } from 'vitest';
describe('Perfil autenticado', () => {
  it('usa endpoint próprio em vez de procurar o usuário na lista administrativa', () => {
    TestBed.configureTestingModule({
      imports: [Profile],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const component = TestBed.createComponent(Profile).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    http
      .expectOne('/api/me')
      .flush({ id: 1, name: 'Teste', email: 'teste@example.test', role: 'USER', active: true });
    expect(component.form.controls.name.value).toBe('Teste');
    http.verify();
  });
});
