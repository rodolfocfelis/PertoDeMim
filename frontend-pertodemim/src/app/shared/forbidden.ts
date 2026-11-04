import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
@Component({
  standalone: true,
  imports: [RouterLink],
  template: ` <section class="page-container">
    <header class="section-header">
      <span class="eyebrow">Permissões</span>
      <h1>Acesso restrito</h1>
      <p>Seu perfil não tem permissão para acessar esta página.</p>
    </header>
    <a class="btn-secondary" [routerLink]="auth.home()">Voltar à minha área</a>
  </section>`,
})
export class Forbidden {
  auth = inject(AuthService);
}
