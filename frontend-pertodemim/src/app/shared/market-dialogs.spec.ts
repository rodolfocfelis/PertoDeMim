import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { RequestDialog, ReviewDialog } from './market-dialogs';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';
describe('Solicitações e avaliações', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('mantém chave de idempotência após falha e libera o formulário', () => {
    const dialog = TestBed.runInInjectionContext(() => new RequestDialog());
    dialog.service = {
      id: 1,
      name: 'Teste',
      phone: '',
      category: null,
      city: 'Teste',
      state: 'SP',
    };
    dialog.description = 'Preciso de um atendimento de teste';
    dialog.submit();
    const first = http.expectOne('/api/market/requests');
    const key = first.request.body.requestKey;
    first.flush({}, { status: 503, statusText: 'Unavailable' });
    expect(dialog.busy()).toBe(false);
    dialog.submit();
    const retry = http.expectOne('/api/market/requests');
    expect(retry.request.body.requestKey).toBe(key);
    retry.flush({ id: 1 });
  });
  it('impede envio duplicado enquanto aguarda resposta', () => {
    const dialog = TestBed.runInInjectionContext(() => new RequestDialog());
    dialog.service = {
      id: 1,
      name: 'Teste',
      phone: '',
      category: null,
      city: 'Teste',
      state: 'SP',
    };
    dialog.description = 'Atendimento necessário para teste';
    dialog.submit();
    dialog.submit();
    http.expectOne('/api/market/requests').flush({ id: 1 });
  });
  it('valida nota e comentário antes de publicar', () => {
    const dialog = TestBed.runInInjectionContext(() => new ReviewDialog());
    dialog.requestId = 1;
    dialog.rating = 0;
    dialog.comment = 'Comentário de teste';
    dialog.submit();
    expect(dialog.error()).toContain('nota');
    http.expectNone('/api/market/requests/1/review');
  });
});
