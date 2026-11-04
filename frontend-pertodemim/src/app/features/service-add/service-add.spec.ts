import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ServiceAdd } from './service-add';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
describe('Gestão dos próprios serviços', () => {
  let component: ServiceAdd;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ServiceAdd],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    component = TestBed.createComponent(ServiceAdd).componentInstance;
    http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/categories').flush([]);
    http.expectOne('/api/professionals/mine').flush([]);
  });
  afterEach(() => http.verify());
  it('valida formulário antes de salvar', () => {
    component.submitForm();
    expect(component.error()).toContain('campos');
  });
  it('libera o botão quando a gravação falha', () => {
    component.form.patchValue({
      name: 'Teste',
      phone: '11999999999',
      categoryId: '1',
      cep: '',
      street: 'Rua Teste',
      addressNumber: '1',
      complement: '',
      city: 'Teste',
      state: 'SP',
    });
    component.submitForm();
    expect(component.saving()).toBe(true);
    http.expectOne('/api/professionals').flush({}, { status: 503, statusText: 'Unavailable' });
    expect(component.saving()).toBe(false);
    expect(component.error()).toContain('Não foi possível salvar');
  });
});
