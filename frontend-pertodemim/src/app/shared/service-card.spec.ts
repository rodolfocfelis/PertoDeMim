import { TestBed } from '@angular/core/testing';
import { describe, it, expect } from 'vitest';
import { ServiceCard } from './service-card';
import { AuthService } from '../core/services/auth.service';
import { brazilianPhone, whatsappLink } from '../core/contact';

describe('Contato direto com o fornecedor', () => {
  it('normaliza DDD e evita duplicar o código do Brasil', () => {
    expect(brazilianPhone('(19) 98765-4321')).toBe('5519987654321');
    expect(brazilianPhone('+55 (19) 98765-4321')).toBe('5519987654321');
    expect(brazilianPhone('(19) 3456-7890')).toBe('551934567890');
  });
  it('não produz um contato para número ausente ou inválido', () => {
    for (const phone of [null, '', 'abc', '00000000000', '1234']) {
      expect(whatsappLink(phone, 'Serviço')).toBeNull();
    }
  });
  it('codifica o nome do serviço na mensagem sem permitir parâmetros adicionais', () => {
    const url = new URL(whatsappLink('19987654321', 'Pintura & reparos #1')!);
    expect(url.origin).toBe('https://wa.me');
    expect(url.pathname).toBe('/5519987654321');
    expect(url.searchParams.get('text')).toContain('Pintura & reparos #1');
    expect([...url.searchParams.keys()]).toEqual(['text']);
  });
  function render(phone: string) {
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: { hasRole: () => false } }],
    });
    const fixture = TestBed.createComponent(ServiceCard);
    fixture.componentRef.setInput('service', {
      id: 1,
      name: 'Pintura',
      description: 'Pintura de paredes e tetos',
      phone,
      category: 'Pintura',
      city: 'Sumaré',
      state: 'SP',
      startingPrice: 150,
      acceptingRequests: false,
    });
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }
  it('exibe informações e link direto inclusive para administrador e agenda indisponível', () => {
    const card = render('19987654321');
    expect(card.textContent).toContain('Pintura de paredes e tetos');
    expect(card.textContent).toContain('Sumaré');
    const link = card.querySelector('a')!;
    expect(link.textContent).toContain('Conversar no WhatsApp');
    expect(link.getAttribute('href')).toContain('https://wa.me/5519987654321?text=');
    expect(link.getAttribute('rel')).toContain('noopener');
    expect(card.textContent).toContain('(19) 98765-4321');
  });
  it('explica a ausência do WhatsApp em vez de criar link inválido', () => {
    const card = render('');
    expect(card.textContent).toContain('WhatsApp não informado');
    expect(card.querySelector('a')).toBeNull();
  });
});
