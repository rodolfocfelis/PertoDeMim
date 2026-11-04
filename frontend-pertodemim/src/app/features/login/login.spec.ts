import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { describe, it, expect } from 'vitest';
describe('Formulário de login', () => {
  it('bloqueia envio de campos vazios', () => {
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.onSubmit();
    expect(component.isLoading).toBe(false);
    TestBed.inject(HttpTestingController).expectNone('/api/auth/login');
  });
});
