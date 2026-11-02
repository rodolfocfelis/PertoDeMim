import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AdminUsers } from './admin-users';
import { describe, it, expect } from 'vitest';
describe('Administração paginada', () => {
  it('consulta API paginada e envia filtros de nome e status', () => {
    TestBed.configureTestingModule({
      imports: [AdminUsers],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const component = TestBed.createComponent(AdminUsers).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    http
      .expectOne('/api/admin/users?page=0&size=10')
      .flush({ content: [], number: 0, totalPages: 0, totalElements: 0 });
    component.name = 'Teste';
    component.status = 'false';
    component.load();
    http
      .expectOne('/api/admin/users?page=0&size=10&name=Teste&active=false')
      .flush({ content: [], number: 0, totalPages: 0, totalElements: 0 });
    expect(component.loading()).toBe(false);
    http.verify();
  });
});
